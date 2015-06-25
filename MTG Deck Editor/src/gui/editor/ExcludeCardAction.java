package gui.editor;

import database.Card;

/**
 * This class represents an action for excluding a card from a category.
 * 
 * @author Alec Roelke
 */
public class ExcludeCardAction implements DeckAction
{
	/**
	 * Editor the action was performed in.
	 */
	private EditorFrame editor;
	/**
	 * Name of the category that the card was excluded from.
	 */
	private String categoryName;
	/**
	 * Card that was excluded from the category.
	 */
	private Card card;
	
	/**
	 * Create a new ExcludeCardAction.
	 * 
	 * @param e Editor for the action
	 * @param p Category for the action
	 * @param c Card for the action
	 */
	public ExcludeCardAction(EditorFrame e, CategoryPanel p, Card c)
	{
		editor = e;
		categoryName = p.name();
		card = c;
	}
	
	/**
	 * Undo the exclusion of the card from the category, or include it in
	 * the category.
	 */
	@Override
	public void undo()
	{
		editor.getCategory(categoryName).include(card);
	}

	/**
	 * Redo the exclusion of the card from the category.
	 */
	@Override
	public void redo()
	{
		editor.getCategory(categoryName).exclude(card);
	}
}
