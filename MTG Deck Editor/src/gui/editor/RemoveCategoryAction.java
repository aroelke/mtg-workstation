package gui.editor;

import java.util.Set;

import database.Card;

/**
 * This class represents an action to remove a category from a deck.
 * 
 * @author Alec Roelke
 */
public class RemoveCategoryAction implements DeckAction
{
	/**
	 * Editor containing the deck that the category was removed from.
	 */
	private EditorFrame editor;
	/**
	 * Name of the removed category.
	 */
	private String name;
	/**
	 * String representation of the removed category.
	 */
	private String repr;
	/**
	 * Whitelist of the removed category.
	 */
	private Set<Card> whitelist;
	/**
	 * Blacklist of the removed category.
	 */
	private Set<Card> blacklist;
	
	/**
	 * Create a new RemoveCategoryAction.
	 * 
	 * @param e Editor the action was performed on
	 * @param category Category that was removed
	 */
	public RemoveCategoryAction(EditorFrame e, CategoryPanel category)
	{
		editor = e;
		name = category.name();
		repr = category.toString();
	}
	
	/**
	 * Undo the removal of the category, or add it back in.
	 */
	@Override
	public void undo()
	{
		editor.categoryCreator.setContents(repr);
		editor.addCategoryUnbuffered(new CategoryPanel(editor.categoryCreator.name(), editor.categoryCreator.repr(), whitelist, blacklist, editor.categoryCreator.filter(), editor.deck));
		editor.categoryCreator.reset();
	}
	
	/**
	 * Redo the removal of the category, or remove it again.
	 */
	@Override
	public void redo()
	{
		if (editor.removeCategoryUnbuffered(name) == null)
			throw new IllegalStateException("Deck does not contain a category named " + name);
	}
}
