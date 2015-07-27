package gui.editor;

import java.awt.Color;
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
		whitelist = category.whitelist();
		blacklist = category.blacklist();
	}
	
	/**
	 * Undo the removal of the category, or add it back in.
	 */
	@Override
	public void undo()
	{
		CategoryEditorPanel categoryEditor = new CategoryEditorPanel(repr);
		editor.addCategoryUnbuffered(new CategoryPanel(categoryEditor.name(), categoryEditor.repr(), whitelist, blacklist, Color.BLACK, categoryEditor.filter(), editor));
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
