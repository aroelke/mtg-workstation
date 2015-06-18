package gui.editor.action;

import gui.editor.EditorFrame;

import java.util.List;

import database.Card;

public class RemoveCardsAction implements DeckAction
{
	private EditorFrame editor;
	private List<Card> cardsRemoved;
	private int count;
	
	public RemoveCardsAction(EditorFrame e, List<Card> removed, int c)
	{
		editor = e;
		cardsRemoved = removed;
		count = c;
	}
	
	@Override
	public void undo()
	{
		editor.addCardsUnbuffered(cardsRemoved, count);
	}

	@Override
	public void redo()
	{
		editor.removeCardsUnbuffered(cardsRemoved, count);
	}
}
