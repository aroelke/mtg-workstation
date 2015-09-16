package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JLabel;
import javax.swing.JPanel;

import database.Card;

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
	 * Create a new CardImagePanel displaying the given Card.
	 * 
	 * @param c Card to display
	 */
	public CardImagePanel(Card c)
	{
		super(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		image = null;
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
			int height = 0;
			int width = 0;
			List<BufferedImage> images = new ArrayList<BufferedImage>();
			for (String name: card.imageNames())
			{
				try
				{
					// TODO: Extract this location into settings
					File imageFile = new File(SettingsDialog.CARD_SCANS + "/" + card.expansion().code + "/" + name + ".full.jpg");
					if (imageFile.exists())
					{
						BufferedImage img = ImageIO.read(imageFile);
						images.add(img);
						height = Math.max(height, img.getHeight());
						width += img.getWidth();
					}
					else
						images.add(null);
				}
				catch (IOException e)
				{
					images.add(null);
				}
			}
			if (height == 0)
				height = getHeight();
			width += (int)(height*ASPECT_RATIO*Collections.frequency(images, null));
			int x = 0;
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.createGraphics();
			for (int i = 0; i < images.size(); i++)
			{
				if (images.get(i) != null)
				{
					g.drawImage(images.get(i), x, (height - images.get(i).getHeight())/2, null);
					x += images.get(i).getWidth();
				}
				else
				{
					int w = (int)(height*ASPECT_RATIO);
					JLabel missingCardLabel = new JLabel("<html><body style='width:100%'>"
							+ "<font color='red'>Missing '" + card.imageNames()[images.size() > 1 ? i : 0] + ".full.jpg'<br></font>"
							+ (images.size() > 1 ? card.faceHTMLString(i) : card.toHTMLString())
							+ "</html>");
					missingCardLabel.setVerticalAlignment(JLabel.TOP);
					missingCardLabel.setSize(new Dimension(w - 4, height - 4));
					
					BufferedImage img = new BufferedImage(w, height, BufferedImage.TYPE_INT_ARGB);
					missingCardLabel.paint(img.getGraphics());
					g.drawImage(img, x + 2, 2, null);
					g.setColor(Color.BLACK);
					g.drawRect(x, 0, w - 1, height - 1);
					
					x += w;
				}
			}
		}
		revalidate();
		repaint();
	}
	
	/**
	 * Draw this CardImagePanel.  It will basically just be the image generated in {@link CardImagePanel#setCard(Card)}
	 * scaled to fit the container.
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (card != null && image != null)
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
