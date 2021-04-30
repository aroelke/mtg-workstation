package editor.gui.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
public class CardImagePanel extends JPanel
{
    /**
     * Aspect ratio of a Magic: The Gathering card.
     */
    public static final double ASPECT_RATIO = 63.0/88.0;
    /**
     * String format for getting the URL of a Gatherer image.
     */
    public static final String GATHERER_FORMAT = "https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=%d&type=card%s";
    /**
     * String format for getting the URL of a Scryfall image.
     */
    public static final String SCRYFALL_FORMAT = "https://api.scryfall.com/cards/%s?format=image%s";

    private static final Collection<JProgressBar> progressBars = new HashSet<JProgressBar>();
    private static final Collection<JLabel> progressLabels = new HashSet<JLabel>();

    /**
     * This class represents a request by a CardImagePanel to download the image(s)
     * of a Card.
     */
    private static class DownloadRequest
    {
        /** CardImagePanel that needs to download a card. */
        public final CardImagePanel source;
        /** Card that needs to be downloaded. */
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
    private static class ImageDownloadWorker extends SwingWorker<Void, Integer>
    {
        /**
         * Queue of cards whose images still need to be downloaded.
         */
        private BlockingQueue<DownloadRequest> toDownload;
        private int size;

        /**
         * Create a new ImageDownloadWorker.
         */
        public ImageDownloadWorker()
        {
            super();
            toDownload = new LinkedBlockingQueue<>();
            size = 0;
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
                var files = getFiles(req.card);
                var urls = getURLs(req.card);
                for (int i = 0; i < urls.size(); i++)
                {
                    final int f = i;
                    if (!files.get(f).exists())
                    {
                        urls.get(f).ifPresent((site) -> {
                            files.get(f).getParentFile().mkdirs();
                            try
                            {
                                URLConnection connection = site.openConnection();
                                size = connection.getContentLength();
                                int downloaded = 0;
                                SwingUtilities.invokeLater(() -> {
                                    for (var bar : progressBars)
                                    {
                                        bar.setEnabled(true);
                                        bar.setMaximum(size);
                                    }
                                    for (var label : progressLabels)
                                        label.setText("Downloading image of " + req.card.unifiedName() + " ...");
                                });
                                try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream()))
                                {
                                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(files.get(f))))
                                    {
                                        byte[] data = new byte[1024];
                                        int x;
                                        while ((x = in.read(data)) > 0)
                                        {
                                            out.write(data, 0, x);
                                            publish(downloaded += x);
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                System.err.println("Error downloading " + files.get(f) + ": " + e.getMessage());
                            }
                        });
                    }
                }
                if (req.card.layout() == CardLayout.FLIP && files.get(0).exists())
                {
                    try
                    {
                        BufferedImage original = ImageIO.read(files.get(0));
                        BufferedImage flipped = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
                        AffineTransformOp op = new AffineTransformOp(AffineTransform.getRotateInstance(Math.PI, flipped.getWidth()/2, flipped.getHeight()/2), AffineTransformOp.TYPE_BILINEAR);
                        ImageIO.write(op.filter(original, flipped), "jpg", files.get(1));
                    }
                    catch (Exception e)
                    {
                        System.out.println(e);
                    }
                }
                if (SettingsDialog.settings().inventory.imageLimitEnable)
                {
                    int count = 0;
                    do
                    {
                        var images = Paths.get(SettingsDialog.settings().inventory.scans).toFile().listFiles();
                        count = images.length;
                        if (count > SettingsDialog.settings().inventory.imageLimit)
                            Arrays.stream(images).min(Comparator.comparingLong(File::lastModified)).ifPresent(File::delete);
                    } while (count > SettingsDialog.settings().inventory.imageLimit);
                }
                SwingUtilities.invokeLater(() -> {
                    if (req.source.card == req.card)
                        req.source.loadImages();
                });
                publish(0);
                if (toDownload.isEmpty())
                {
                    SwingUtilities.invokeLater(() -> {
                        for (var bar : progressBars)
                            bar.setEnabled(false);
                        for (var label : progressLabels)
                            label.setText("");
                    });
                }
            }
        }

        @Override
        protected void process(List<Integer> chunks)
        {
            for (var bar : progressBars)
                bar.setValue(chunks.get(chunks.size() - 1));
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
     * Determine the name(s) of file(s) a card's image(s) will be stored in.
     * 
     * @param c card to find image(s) for
     * @return A list of files pointing to the card's image(s).
     */
    private static List<File> getFiles(Card c)
    {
        switch (SettingsDialog.settings().inventory.imageSource)
        {
        case "Scryfall":
            return IntStream.range(0, c.imageNames().size()).mapToObj((i) -> Paths.get(SettingsDialog.settings().inventory.scans, c.scryfallid().get(i) + ";" + i + ".jpg").toFile()).collect(Collectors.toList());
        case "Gatherer":
            return IntStream.range(0, c.multiverseid().size()).mapToObj((i) -> Paths.get(SettingsDialog.settings().inventory.scans, c.multiverseid().get(i) + ";" + i + ".jpg").toFile()).collect(Collectors.toList());
        default:
            return Collections.emptyList();
        }
    }

    /**
     * Create a panel that contains a progress bar and a label to show updates and progress for cards
     * whose images are being downloaded.
     * 
     * @return A {@link JPanel} containing a {@link JProgressBar} and a {@link JLabel} arranged
     * horizontally.
     */
    public static JPanel createStatusBar()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBorder(BorderFactory.createEtchedBorder());
        JProgressBar bar = new JProgressBar();
        bar.setEnabled(false);
        panel.add(bar);
        panel.add(Box.createHorizontalStrut(5));
        JLabel label = new JLabel();
        panel.add(label);

        progressBars.add(bar);
        progressLabels.add(label);
        return panel;
    }

    /**
     * Determine the URL(s) to download a card's image(s) from.
     * 
     * @param c card to calculate URL(s) for
     * @return A list of URLs pointing to the card's image(s) online.  If the image is known beforehand not to exist
     * (e.g. because its multiverseid doesn't exist), then the corresponding list entry will be empty.
     * @throws MalformedURLException if any of the URLs are poorly formed.
     */
    private static List<Optional<URL>> getURLs(Card c) throws MalformedURLException
    {
        List<Optional<URL>> urls = new ArrayList<>();
        switch (SettingsDialog.settings().inventory.imageSource)
        {
        case "Scryfall":
            switch (c.layout())
            {
            case FLIP:
                urls.add(Optional.of(new URL(String.format(SCRYFALL_FORMAT, c.scryfallid().get(0), ""))));
                break;
            case MELD:
                for (int i = 0; i < c.imageNames().size(); i++)
                    urls.add(Optional.of(new URL(String.format(SCRYFALL_FORMAT, c.scryfallid().get(i), ""))));
                break;
            default:
                for (int i = 0; i < c.imageNames().size(); i++)
                    urls.add(Optional.of(new URL(String.format(SCRYFALL_FORMAT, c.scryfallid().get(i), (i > 0 && i == c.imageNames().size() - 1) ? "&face=back" : ""))));
                break;
            }
            break;
        case "Gatherer":
            switch (c.layout())
            {
            case FLIP:
                for (int i = 0; i < c.multiverseid().size(); i++)
                {
                    if (c.multiverseid().get(i) >= 0)
                        urls.add(Optional.of(new URL(String.format(GATHERER_FORMAT, c.multiverseid().get(i), i > 0 && i == c.multiverseid().size() - 1 ? "options=rotate180" : ""))));
                    else
                        urls.add(Optional.empty());
                }
                break;
            default:
                for (int id : c.multiverseid())
                {
                    if (id >= 0)
                        urls.add(Optional.of(new URL(String.format(GATHERER_FORMAT, id, ""))));
                    else
                        urls.add(Optional.empty());
                }
                break;
            }
            break;
        default:
            break;
        }
        return Collections.unmodifiableList(urls);
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
                face = (face + 1) % card.imageNames().size();
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
            for (File file : getFiles(card))
            {
                BufferedImage img = null;
                try
                {
                    if (file.exists())
                        img = ImageIO.read(file);
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

            if (card.imageNames().size() > 1)
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
                g.drawImage(faceImages.get(face), 0, 0, null);
        }
    }

    /**
     * Set the card to display.  If its image is missing, try to download it.
     *
     * @param c card to display
     */
    public void setCard(Card c)
    {
        if (c != card)
        {
            card = Objects.requireNonNull(c);
            face = 0;
            faceImages.clear();
            try
            {
                Files.createDirectories(Path.of(SettingsDialog.settings().inventory.scans));
                if (getFiles(card).stream().map(File::toPath).allMatch(Files::exists))
                    loadImages();
                else
                    downloader.downloadCard(this, card);
            }
            catch (IOException e)
            {}
            revalidate();
            repaint();
        }
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
