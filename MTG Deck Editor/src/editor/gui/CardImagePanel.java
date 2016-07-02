package editor.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import editor.database.card.Card;

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
	 * Card this CardImagePanel should display.
	 */
	private Card card;
	/**
	 * Image of the card this CardImagePanel should display.
	 */
	private BufferedImage image;
	/**
	 * List of images to draw for the card.
	 */
	private List<BufferedImage> faceImages;
	
	/**
	 * Create a new CardImagePanel displaying the specified Card.
	 * 
	 * @param c Card to display
	 */
	public CardImagePanel(Card c)
	{
		super(null);
		image = null;
		faceImages = new ArrayList<BufferedImage>();
		setCard(c);
	}
	
	/**
	 * Create a new CardImagePanel displaying nothing.
	 */
	public CardImagePanel()
	{
		this(null);
	}
	
	/**
	 * Set the card to display.  If any of the images associated with the new Card are
	 * missing, they are replaced with card-shaped rectangles containing a warning and
	 * the oracle text of the associated face.
	 * 
	 * @param c Card to display
	 */
	public void setCard(Card c)
	{
		if ((card = c) != null)
		{
			faceImages.clear();
			for (String name: card.imageNames())
			{
				BufferedImage img = null;
				try
				{
					String[] codes = new String[] {card.expansion().code, card.expansion().magicCardsInfoCode, card.expansion().gathererCode, card.expansion().name};
					File imageFile;
					for (String code: codes)
					{
						imageFile = new File(SettingsDialog.getAsString(SettingsDialog.CARD_SCANS)
									+ "/" + code + "/" + name + ".full.jpg");
						if (imageFile.exists())
						{
							img = ImageIO.read(imageFile);
							break;
						}
					}
				}
				catch (IOException e)
				{}
				finally
				{
					faceImages.add(img);
				}
			}
		}
		if (getParent() != null)
		{
			getParent().validate();
			repaint();
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
			int h = 0;
			int w = 0;
			for (BufferedImage face: faceImages)
			{
				if (face != null)
				{
					h = Math.max(h, face.getHeight());
					w += face.getWidth();
				}
			}
			if (h == 0)
				h = height;
			w += (int)(h*ASPECT_RATIO*Collections.frequency(faceImages, null));
			image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.createGraphics();
			int l = 0;
			for (int i = 0; i < faceImages.size(); i++)
			{
				if (faceImages.get(i) != null)
				{
					g.drawImage(faceImages.get(i), l, (h - faceImages.get(i).getHeight())/2, null);
					l += faceImages.get(i).getWidth();
				}
				else
				{
					int faceWidth = (int)(h*ASPECT_RATIO);
					
					JTextPane missingCardPane = new JTextPane();
					StyledDocument document = (StyledDocument)missingCardPane.getDocument();
					Style textStyle = document.addStyle("text", null);
					StyleConstants.setFontFamily(textStyle, UIManager.getFont("Label.font").getFamily());
					StyleConstants.setFontSize(textStyle, MainFrame.TEXT_SIZE);
					Style reminderStyle = document.addStyle("reminder", textStyle);
					StyleConstants.setItalic(reminderStyle, true);
					card.formatDocument(document, i);
					missingCardPane.setSize(new Dimension(faceWidth - 4, h - 4));
					
					BufferedImage img = new BufferedImage(faceWidth, h, BufferedImage.TYPE_INT_ARGB);
					missingCardPane.paint(img.getGraphics());
					g.drawImage(img, l + 2, 2, null);
					g.setColor(Color.BLACK);
					g.drawRect(l, 0, faceWidth - 1, h - 1);
					
					l += faceWidth;
				}
			}
		}
	}
	
	/**
	 * @return The preferred size of this CardImagePanel, which is the largest rectangle that fits the image
	 * it is trying to draw that fits within the parent container.
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
	 * Draw this CardImagePanel.  It will basically just be the image generated in {@link CardImagePanel#setCard(Card)}
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
		}
	}
}
