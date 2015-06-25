package gui.editor;

import database.Card;

/**
 * This class represents an action to include a card in a category.
 * 
 * @author Alec Roelke
 */
public class IncludeCardAction implements DeckAction
{
	/**
	 * EditorFrame the action was performed in.
	 */
	private EditorFrame editor;
	/**
	 * Name of the category the card was included in.
	 */
	private String categoryName;
	/**
	 * Card that was included in the category.
	 */
	private Card card;
	
	/**
	 * Create a new IncludeCardAction.
	 * 
	 * @param e Editor for the action
	 * @param p Category for the action
	 * @param c Card for the action
	 */
	public IncludeCardAction(EditorFrame e, CategoryPanel p, Card c)
	{
		editor = e;
		categoryName = p.name();
		card = c;
	}
	
	/**
	 * Undo the inclusion of the card in the category, or exclude it.
	 */
	@Override
	public void undo()
	{
		editor.getCategory(categoryName).exclude(card);
	}

	/**
	 * Redo the inclusion of the card in the category.
	 */
	@Override
	public void redo()
	{
		editor.getCategory(categoryName).include(card);
	}
}
