package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import database.Card;

/**
 * TODO: Comment this
 * @author Alec
 *
 */
@SuppressWarnings("serial")
public class CardImagePanel extends JPanel
{
	private Card card;
	private BufferedImage image;
	private JTextPane oracleTextPane;
	private JScrollPane oracleTextScrollPane;
	
	public CardImagePanel(Card c)
	{
		super(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		oracleTextPane = new JTextPane();
		oracleTextPane.setEditable(false);
		oracleTextPane.setContentType("text/html");
		oracleTextPane.setFont(UIManager.getFont("Label.font"));
		oracleTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		add(oracleTextScrollPane = new JScrollPane(oracleTextPane));
		
		image = null;
		setCard(c);
	}
	
	public CardImagePanel()
	{
		this(null);
	}
	
	public void setCard(Card c)
	{
		if ((card = c) != null)
		{
			int height = 0;
			int width = 0;
			List<BufferedImage> images = new ArrayList<BufferedImage>();
			for (String name: new LinkedHashSet<String>(Arrays.asList(card.imageNames())))
			{
				try
				{
					BufferedImage img = ImageIO.read(new File("images/cards/" + card.expansion().code + "/" + name + ".full.jpg"));
					images.add(img);
					height = Math.max(height, img.getHeight());
					width += img.getWidth();
				}
				catch (IOException e)
				{}
			}
			if (width == 0)
				image = null;
			else
			{
				int x = 0;
				image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics g = image.createGraphics();
				for (BufferedImage img: images)
				{
					g.drawImage(img, x, (height - img.getHeight())/2, null);
					x += img.getWidth();
				}
			}
		}
		
		oracleTextScrollPane.setVisible(card != null && image == null);
		if (card != null)
		{
			oracleTextPane.setText("<html>" + card.toHTMLString() + "</html>");
			oracleTextPane.setCaretPosition(0);
		}
		revalidate();
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g)
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
	
	@Override
	public void doLayout()
	{
		if (image == null)
		{
			int height = getHeight();
			oracleTextScrollPane.setPreferredSize(new Dimension((int)(height*63.0/88.0), height));
		}
		super.doLayout();
	}
}
