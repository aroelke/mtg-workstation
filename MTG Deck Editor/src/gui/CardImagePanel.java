package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
	public static final double ASPECT_RATIO = 63.0/88.0;
	
	private Card card;
	private List<File> imageFiles;
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
		
		imageFiles = new ArrayList<File>();
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
			System.out.println("images/cards/" + card.expansion().code + "/" + card.imageNames()[0] + ".full.jpg");
			imageFiles = Arrays.stream(card.imageNames())
							   .map((name) -> new File("images/cards/" + card.expansion().code + "/" + name + ".full.jpg"))
							   .collect(Collectors.toList());
		}
		
		oracleTextScrollPane.setVisible(card != null && !imageExists());
		if (card != null)
			oracleTextPane.setText("<html>" + card.toHTMLString() + "</html>");
		revalidate();
		repaint();
	}
	
	@Override
	public void doLayout()
	{
		if (!imageExists())
		{
			int height = getHeight();
			oracleTextScrollPane.setPreferredSize(new Dimension((int)(height*ASPECT_RATIO), height));
		}
		super.doLayout();
	}
	
	private boolean imageExists()
	{
		return card != null && imageFiles.stream().allMatch(File::exists);
	}
}
