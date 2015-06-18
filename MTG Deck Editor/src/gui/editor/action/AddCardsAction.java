package gui.editor.action;

import gui.editor.EditorFrame;

import java.util.List;

import database.Card;

public class AddCardsAction implements DeckAction
{
	private EditorFrame editor;
	private List<Card> cardsAdded;
	private int count;
	
	public AddCardsAction(EditorFrame e, List<Card> added, int c)
	{
		editor = e;
		cardsAdded = added;
		count = c;
	}
	
	@Override
	public void undo()
	{
		editor.removeCardsUnbuffered(cardsAdded, count);
	}

	@Override
	public void redo()
	{
		editor.addCardsUnbuffered(cardsAdded, count);
	}
}
