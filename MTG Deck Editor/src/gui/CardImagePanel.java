package gui;

import java.awt.Dimension;

import javax.swing.JPanel;

import database.Card;

@SuppressWarnings("serial")
public class CardImagePanel extends JPanel
{
	private Card card;
	
	public CardImagePanel(Card c)
	{
		setCard(c);
		setPreferredSize(new Dimension(0, 250));
	}
	
	public CardImagePanel()
	{
		this(null);
	}
	
	public void setCard(Card c)
	{
		card = c;
	}
}
