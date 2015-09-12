package gui;

import javax.swing.JPanel;

import database.Card;

@SuppressWarnings("serial")
public class CardImagePanel extends JPanel
{
	private Card card;
	
	public CardImagePanel(Card c)
	{
		setCard(c);
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
