package gui.editor;

import database.Card;

public class IncludeCardAction implements DeckAction
{
	private CategoryPanel panel;
	private Card card;
	
	public IncludeCardAction(CategoryPanel p, Card c)
	{
		panel = p;
		card = c;
	}
	
	@Override
	public void undo()
	{
		panel.exclude(card);
	}

	@Override
	public void redo()
	{
		panel.include(card);
	}
}
