package gui.editor;

import java.util.Collection;

import database.Card;

/**
 * This class represents an action for excluding a card from a category.
 * 
 * @author Alec Roelke
 */
public class ExcludeCardsAction implements DeckAction
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
	private Collection<Card> cards;
	
	/**
	 * Create a new ExcludeCardAction.
	 * 
	 * @param e Editor for the action
	 * @param p Category for the action
	 * @param c Card for the action
	 */
	public ExcludeCardsAction(EditorFrame e, CategoryPanel p, Collection<Card> c)
	{
		editor = e;
		categoryName = p.name();
		cards = c;
	}
	
	/**
	 * Undo the exclusion of the card from the category, or include it in
	 * the category.
	 */
	@Override
	public void undo()
	{
		for (Card c: cards)
			editor.getCategory(categoryName).include(c);
	}

	/**
	 * Redo the exclusion of the card from the category.
	 */
	@Override
	public void redo()
	{
		for (Card c: cards)
			editor.getCategory(categoryName).exclude(c);
	}
}
