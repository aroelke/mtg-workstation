package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import database.Card;

@SuppressWarnings("serial")
public class CardImagePanel extends JPanel
{
	public static final double ASPECT_RATIO = 63.0/88.0;
	
	private Card card;
	private JTextPane oracleTextPane;
	private JScrollPane oracleTextScrollPane;
	
	public CardImagePanel(Card c)
	{
		super(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setCard(c);
		oracleTextPane = new JTextPane();
		oracleTextPane.setEditable(false);
		oracleTextPane.setContentType("text/html");
		oracleTextPane.setFont(UIManager.getFont("Label.font"));
		oracleTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		add(oracleTextScrollPane = new JScrollPane(oracleTextPane));
	}
	
	public CardImagePanel()
	{
		this(null);
	}
	
	public void setCard(Card c)
	{
		card = c;
		if (card != null)
			oracleTextPane.setText("<html>" + card.toHTMLString() + "</html>");
		repaint();
		revalidate();
	}
	
	@Override
	public void repaint()
	{
		if (oracleTextScrollPane != null)
		{
			int height = getHeight();
			oracleTextScrollPane.setPreferredSize(new Dimension((int)(height*ASPECT_RATIO), height));
		}
		super.repaint();
	}
}
