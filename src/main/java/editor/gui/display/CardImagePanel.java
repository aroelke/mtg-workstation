package editor.gui.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.database.symbol.FunctionalSymbol;
import editor.gui.generic.ComponentUtils;
import editor.gui.settings.SettingsDialog;

/**
 * This class represents a panel that shows the images associated with a card if they
 * can be found, or a card-shaped rectangle with its oracle text and a warning if
 * they cannot.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardImagePanel extends JPanel
{
    /**
     * Aspect ratio of a Magic: The Gathering card.
     */
    public static final double ASPECT_RATIO = 63.0/88.0;

    /**
     * This class represents a request by a CardImagePanel to download the image(s)
     * of a Card.
     */
    private static class DownloadRequest
    {
        /**
         * CardImagePanel that needs to download a card.
         */
        public final CardImagePanel source;
        /**
         * Card that needs to be downloaded.
         */
        public final Card card;

        /**
         * Create a new DownloadRequest.
         * 
         * @param panel CardImagePanel making the request
         * @param c Card whose images need to be downloaded
         */
        public DownloadRequest(CardImagePanel panel, Card c)
        {
            source = panel;
            card = c;
        }
    }

    /**
     * This class represents a worker that downloads a card image for its parent CardImagePanel
     * from Gatherer.
     */
    private static class ImageDownloadWorker extends SwingWorker<Void, DownloadRequest>
    {
        /**
         * Queue of cards whose images still need to be downloaded.
         */
        private BlockingQueue<DownloadRequest> toDownload;

        /**
         * Create a new ImageDownloadWorker.
         */
        public ImageDownloadWorker()
        {
            super();
            toDownload = new LinkedBlockingQueue<>();
        }

        /**
         * Create a request to download the image(s) of a card to display on the given
         * CardImagePanel.
         */
        public void downloadCard(CardImagePanel source, Card card)
        {
            try
            {
                toDownload.put(new DownloadRequest(source, card));
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground() throws Exception
        {
            while (true)
            {
                DownloadRequest req = toDownload.take();
                for (long multiverseid : req.card.multiverseid())
                {
                    File img = Paths.get(SettingsDialog.settings().inventory.scans, multiverseid + ".jpg").toFile();
                    if (!img.exists())
                    {
                        URL site = new URL(String.join("/", "https://gatherer.wizards.com", "Handlers", "Image.ashx?multiverseid=" + multiverseid + "&type=card"));

                        img.getParentFile().mkdirs();
                        try (BufferedInputStream in = new BufferedInputStream(site.openStream()))
                        {
                            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(img)))
                            {
                                byte[] data = new byte[1024];
                                int x;
                                while ((x = in.read(data)) > 0)
                                    out.write(data, 0, x);
                            }
                        }
                        catch (Exception e)
                        {
                            System.err.println("Error downloading " + multiverseid + ".jpg: " + e.getMessage());
                        }
                    }
                }
                publish(req);
            }
        }

        @Override
        protected void process(List<DownloadRequest> chunks)
        {
            for (DownloadRequest req: chunks)
                if (req.source.card == req.card)
                    req.source.loadImages();
        }
    }

    /**
     * Global ImageDownloadWorker to manage downloading cards images.
     */
    private static ImageDownloadWorker downloader = new ImageDownloadWorker();
    static
    {
        downloader.execute();
    }

    /**
     * This class represents a listener that listens for clicks on a CardImagePanel.
     */
    private class FaceListener extends MouseAdapter
    {
        /**
         * When the mouse is clicked, flip to the next face.
         */
        public void mousePressed(MouseEvent e)
        {
            if (SwingUtilities.isLeftMouseButton(e) && card != null)
            {
                face = switch (card.layout()) {
                    case SPLIT, AFTERMATH, ADVENTURE -> 0;
                    default -> (face + 1) % card.faces();
                };
                getParent().revalidate();
                repaint();
            }
        }
    }

    /**
     * Card this CardImagePanel should display.
     */
    private Card card;
    /**
     * List of images to draw for the card.
     */
    private List<BufferedImage> faceImages;
    /**
     * Image of the card this CardImagePanel should display.
     */
    private BufferedImage image;
    /**
     * Face of the card to display.
     */
    private int face;

    /**
     * Create a new CardImagePanel displaying nothing.
     */
    public CardImagePanel()
    {
        super(null);
        card = null;
        image = null;
        faceImages = new ArrayList<>();
        face = 0;
        addMouseListener(new FaceListener());
    }

    /**
     * Create a new CardImagePanel displaying the specified card.
     *
     * @param c card to display
     */
    public CardImagePanel(Card c)
    {
        this();
        setCard(c);
    }

    /**
     * {@inheritDoc}
     * The preferred size is the largest rectangle that fits the image this CardImagePanel is trying
     * to draw that fits within the parent container.
     */
    @Override
    public Dimension getPreferredSize()
    {
        if (getParent() == null)
            return super.getPreferredSize();
        else if (image == null)
            return super.getPreferredSize();
        else
        {
            double aspect = (double)image.getWidth()/(double)image.getHeight();
            return new Dimension((int)(getParent().getHeight()*aspect), getParent().getHeight());
        }
    }

    /**
     * Once the images have been downloaded, try to load them.  If they don't exist,
     * create a rectangle with Oracle text instead.
     */
    private synchronized void loadImages()
    {
        if (card != null)
        {
            faceImages.clear();
            for (long i : card.multiverseid())
            {
                BufferedImage img = null;
                try
                {
                    if (i > 0)
                    {
                        File imageFile = Paths.get(SettingsDialog.settings().inventory.scans, i + ".jpg").toFile();
                        if (imageFile.exists())
                            img = ImageIO.read(imageFile);
                    }
                }
                catch (IOException e)
                {}
                finally
                {
                    faceImages.add(img);
                }
            }
            if (getParent() != null)
            {
                SwingUtilities.invokeLater(() -> {
                    getParent().revalidate();
                    repaint();
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     * The panel will basically just be the image generated in {@link CardImagePanel#setCard(Card)}
     * scaled to fit the container.
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (image != null)
        {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            double aspectRatio = (double)image.getWidth()/(double)image.getHeight();
            int width = (int)(getHeight()*aspectRatio);
            int height = getHeight();
            if (width > getWidth())
            {
                width = getWidth();
                height = (int)(width/aspectRatio);
            }
            g2.drawImage(image, (getWidth() - width)/2, (getHeight() - height)/2, width, height, null);

            if (card.faces() > 1 && !List.of(CardLayout.SPLIT, CardLayout.AFTERMATH, CardLayout.ADVENTURE).contains(card.layout()))
            {
                final int SIZE = 15;
                final int BORDER = 3;
                FunctionalSymbol.SYMBOLS.get(face % 2 == 0 ? "T" : "Q").getIcon(SIZE).paintIcon(this, g2, getWidth() - SIZE - BORDER, getHeight() - SIZE - BORDER);
            }
        }
    }

    /**
     * Set the bounding box of this CardImagePanel.  This will cause it to refresh its image
     * to fit inside the new bounding box.
     */
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
        if (card == null || height == 0 || width == 0)
            image = null;
        else
        {
            int h = 0, w = 0;
            if (faceImages.size() <= face || faceImages.get(face) == null)
            {
                h = height;
                w = (int)(h*ASPECT_RATIO);
            }
            else
            {
                h = faceImages.get(face).getHeight();
                w = faceImages.get(face).getWidth();
            }
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.createGraphics();
            if (faceImages.size() <= face || faceImages.get(face) == null)
            {
                int faceWidth = (int)(h*ASPECT_RATIO);

                JTextPane missingCardPane = new JTextPane();
                StyledDocument document = (StyledDocument)missingCardPane.getDocument();
                Style textStyle = document.addStyle("text", null);
                StyleConstants.setFontFamily(textStyle, UIManager.getFont("Label.font").getFamily());
                StyleConstants.setFontSize(textStyle, ComponentUtils.TEXT_SIZE);
                Style reminderStyle = document.addStyle("reminder", textStyle);
                StyleConstants.setItalic(reminderStyle, true);
                card.formatDocument(document, false, face);
                missingCardPane.setSize(new Dimension(faceWidth - 4, h - 4));

                BufferedImage img = new BufferedImage(faceWidth, h, BufferedImage.TYPE_INT_ARGB);
                missingCardPane.paint(img.getGraphics());
                g.drawImage(img, 2, 2, null);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, faceWidth - 1, h - 1);
            }
            else
            {
                if (card.layout() == CardLayout.FLIP && face%2 == 1)
                    g.drawImage(faceImages.get(face), faceImages.get(0).getWidth(), faceImages.get(0).getHeight(), -faceImages.get(0).getWidth(), -faceImages.get(0).getHeight(), null);
                else
                    g.drawImage(faceImages.get(face), 0, 0, null);
            }
        }
    }

    /**
     * Set the card to display.  If its image is missing, try to download it.
     *
     * @param c card to display
     */
    public void setCard(Card c)
    {
        card = Objects.requireNonNull(c);
        face = 0;
        faceImages.clear();
        revalidate();
        repaint();
        downloader.downloadCard(this, card);
    }

    /**
     * Set the image panel to be blank and containing no card.
     */
    public void clearCard()
    {
        card = null;
        face = 0;
        faceImages.clear();
        revalidate();
        repaint();
    }
}
