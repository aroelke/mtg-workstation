package gui.editor;

import database.Card;

/**
 * TODO: Comment this class
 * 
 * @author Alec Roelke
 */
public class SetCardCountAction implements DeckAction
{
	private EditorFrame editor;
	private Card card;
	private int before;
	private int after;
	
	public SetCardCountAction(EditorFrame e, Card c, int b, int a)
	{
		editor = e;
		card = c;
		before = b;
		after = a;
	}
	
	@Override
	public void undo()
	{
		editor.deck.setCount(card, before);
		editor.revalidate();
		editor.repaint();
	}

	@Override
	public void redo()
	{
		editor.deck.setCount(card, after);
		editor.revalidate();
		editor.repaint();
	}
}
