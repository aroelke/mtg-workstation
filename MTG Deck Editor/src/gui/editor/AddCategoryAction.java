package gui.editor;

import java.util.Set;

import database.Card;

/**
 * This class represents an action to add a category to the deck.
 * 
 * TODO: Figure out a way to make this work using only the category string.
 * 
 * @author Alec Roelke
 */
public class AddCategoryAction implements DeckAction
{
	/**
	 * Editor containing the deck the category was added to.
	 */
	private EditorFrame editor;
	/**
	 * Name of the category added.
	 */
	private String name;
	/**
	 * String representation of the category added.
	 */
	private String repr;
	private Set<Card> whitelist;
	private Set<Card> blacklist;
	
	/**
	 * Create a new AddCategoryAction
	 * 
	 * @param e Editor this action was performed on
	 * @param category Category added
	 */
	public AddCategoryAction(EditorFrame e, CategoryPanel category)
	{
		editor = e;
		name = category.name();
		repr = category.toString();
		whitelist = category.whitelist();
		blacklist = category.blacklist();
	}
	
	/**
	 * Undo the addition of a new category, or remove it.
	 */
	@Override
	public void undo()
	{
		if (editor.removeCategoryUnbuffered(name) == null)
			throw new IllegalStateException("Deck does not contain a category named " + name);
	}
	
	/**
	 * Redo the addition of the new category, or add it.
	 */
	@Override
	public void redo()
	{
		CategoryEditorPanel categoryEditor = new CategoryEditorPanel(repr);
		editor.addCategoryUnbuffered(new CategoryPanel(categoryEditor.name(), categoryEditor.repr(), whitelist, blacklist, categoryEditor.filter(), editor));
	}
}
