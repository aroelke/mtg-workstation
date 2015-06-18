package gui.editor.action;

import gui.editor.CategoryPanel;
import gui.editor.EditorFrame;

public class RemoveCategoryAction implements DeckAction
{
	private EditorFrame editor;
	private CategoryPanel removedCategory;
	
	public RemoveCategoryAction(EditorFrame e, CategoryPanel category)
	{
		editor = e;
		removedCategory = category;
	}
	
	@Override
	public void undo()
	{
		/*
		 * XXX: This doesn't work because the deck needs to be properly updated
		 * for the re-added category.  The best way to do this might be to
		 * create a "new" category that looks exactly like the old one
		 */
		editor.addCategoryUnbuffered(removedCategory);
	}
	
	@Override
	public void redo()
	{
		/*
		 * XXX: This won't work because when a copy is created for re-adding,
		 * the old category won't be part of the deck anymore.  The best
		 * way to fix ths might be to change cardsRemoved to the new category
		 * that was created upon undo().
		 */
		editor.removeCategoryUnbuffered(removedCategory);
	}
}
