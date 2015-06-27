package gui.editor;



/**
 * This class represents an action to add a category to the deck.
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
		editor.categoryCreator.setContents(repr);
		editor.addCategoryUnbuffered(new CategoryPanel(editor.categoryCreator.name(), editor.categoryCreator.repr(), editor.categoryCreator.filter(), editor.deck));
		editor.categoryCreator.reset();
	}
}
