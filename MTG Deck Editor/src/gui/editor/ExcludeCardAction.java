package gui.editor;

import database.Card;

public class ExcludeCardAction implements DeckAction
{
	private CategoryPanel panel;
	private Card card;
	
	public ExcludeCardAction(CategoryPanel p, Card c)
	{
		panel = p;
		card = c;
	}
	
	@Override
	public void undo()
	{
		panel.include(card);
	}

	@Override
	public void redo()
	{
		panel.exclude(card);
	}
}
