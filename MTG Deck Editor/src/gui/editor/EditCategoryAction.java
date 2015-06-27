package gui.editor;

import java.util.function.Predicate;

import database.Card;

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
	 * Name of the category before editing.
	 */
	private String formerName;
	/**
	 * String representation of the category before editing.
	 */
	private String formerRepr;
	/**
	 * Filter of the category before editing.
	 */
	private Predicate<Card> formerFilter;
	/**
	 * Name of the category after editing.
	 */
	private String newName;
	/**
	 * String representation of the category after editing.
	 */
	private String newRepr;
	/**
	 * Filter of the category after editing.
	 */
	private Predicate<Card> newFilter;
	
	/**
	 * Create a new EditCategoryAction.
	 * 
	 * @param e Editor this action was performed on
	 * @param nf Former name of the category
	 * @param rf Former String representation of the category
	 * @param ff Former filter of the category
	 * @param na New name of the category
	 * @param ra New String representation of the category
	 * @param fa New filter of the category
	 */
	public EditCategoryAction(EditorFrame e, String nf, String rf, Predicate<Card> ff, String na, String ra, Predicate<Card> fa)
	{
		editor = e;
		formerName = nf;
		formerRepr = rf;
		formerFilter = ff;
		newName = na;
		newRepr = ra;
		newFilter = fa;
	}
	
	/**
	 * Undo the changes made to the category.
	 */
	@Override
	public void undo()
	{
		CategoryPanel editedCategory = editor.getCategory(newName);
		if (editedCategory != null)
		{
			editedCategory.edit(formerName, formerRepr, formerFilter);
			editor.updateCategorySwitch();
			editor.revalidate();
			editor.repaint();
			editor.setUnsaved();
		}
		else
			throw new IllegalStateException("Deck does not contain a category named " + newName);
	}

	/**
	 * Restore the changes made to the category.
	 */
	@Override
	public void redo()
	{
		CategoryPanel editedCategory = editor.getCategory(formerName);
		if (editedCategory != null)
		{
			editedCategory.edit(newName, newRepr, newFilter);
			editor.updateCategorySwitch();
			editor.revalidate();
			editor.repaint();
			editor.setUnsaved();
		}
		else
			throw new IllegalStateException("Deck does not contain a category named " + formerName);
	}
}
