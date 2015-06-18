package gui.editor.action;

import gui.editor.CategoryPanel;
import gui.editor.EditorFrame;

/**
 * TODO: Make this only store the minimum amount of information necessary
 * (and possibly have to invoke the category creator to add a category back)
 * 
 * @author Alec Roelke
 */
public class AddCategoryAction implements DeckAction
{
	private EditorFrame editor;
	private CategoryPanel addedCategory;
	
	public AddCategoryAction(EditorFrame e, CategoryPanel category)
	{
		editor = e;
		addedCategory = category;
	}
	
	@Override
	public void undo()
	{
		/*
		 * XXX: This won't work because when a copy is created for re-adding,
		 * the old category won't be part of the deck anymore.  The best
		 * way to fix ths might be to change addedCategory to the new category
		 * that was created upon redo().
		 */
		editor.removeCategoryUnbuffered(addedCategory);
	}
	
	@Override
	public void redo()
	{
		/*
		 * XXX: This doesn't work because the deck needs to be properly updated
		 * for the re-added category.  The best way to do this might be to
		 * create a "new" category that looks exactly like the old one
		 */
		editor.addCategoryUnbuffered(addedCategory);
	}
}
