package gui.editor;

import java.util.function.Predicate;
import java.util.regex.Matcher;

import database.Card;
import database.Deck;

/**
 * This class represents an action for editing a category in a deck.
 * 
 * @author Alec Roelke
 */
public class EditCategoryAction implements DeckAction
{
	/**
	 * Editor containing the deck that had the category that was edited.
	 */
	private EditorFrame editor;
	/**
	 * String representation of the category before editing.
	 */
	private Matcher formerRepr;
	/**
	 * Filter of the category before editing.
	 */
	private Predicate<Card> formerFilter;
	/**
	 * String representation of the category after editing.
	 */
	private Matcher newRepr;
	/**
	 * Filter of the category after editing.
	 */
	private Predicate<Card> newFilter;
	
	/**
	 * Create a new EditCategoryAction.
	 * 
	 * @param e Editor this action was performed on
	 * @param rf Former String representation of the category
	 * @param ff Former filter of the category
	 * @param ra New String representation of the category
	 * @param fa New filter of the category
	 */
	public EditCategoryAction(EditorFrame e, String rf, Predicate<Card> ff, String ra, Predicate<Card> fa)
	{
		editor = e;
		formerRepr = Deck.CATEGORY_PATTERN.matcher(rf);
		formerFilter = ff;
		newRepr = Deck.CATEGORY_PATTERN.matcher(ra);
		newFilter = fa;
		
		if (!formerRepr.matches())
			throw new IllegalArgumentException("Illegal former category string \"" + rf + "\"");
		if (!newRepr.matches())
			throw new IllegalArgumentException("Illegal new category string \"" + ra + "\"");
	}
	
	/**
	 * Undo the changes made to the category.
	 */
	@Override
	public void undo()
	{
		CategoryPanel editedCategory = editor.getCategory(newRepr.group(1).trim());
		if (editedCategory != null)
		{
			editedCategory.edit(formerRepr.group(1).trim(), formerRepr.group(4), formerFilter);
			editor.updateCategorySwitch();
			editor.revalidate();
			editor.repaint();
			editor.setUnsaved();
		}
		else
			throw new IllegalStateException("Deck does not contain a category named " + newRepr.group(1));
	}

	/**
	 * Restore the changes made to the category.
	 */
	@Override
	public void redo()
	{
		CategoryPanel editedCategory = editor.getCategory(formerRepr.group(1).trim());
		if (editedCategory != null)
		{
			editedCategory.edit(newRepr.group(1).trim(), newRepr.group(4), newFilter);
			editor.updateCategorySwitch();
			editor.revalidate();
			editor.repaint();
			editor.setUnsaved();
		}
		else
			throw new IllegalStateException("Deck does not contain a category named " + formerRepr.group(1).trim());
	}
}
