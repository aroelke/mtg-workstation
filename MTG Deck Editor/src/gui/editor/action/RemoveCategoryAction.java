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
		editor.addCategoryUnbuffered(removedCategory);
	}
	
	@Override
	public void redo()
	{
		editor.removeCategoryUnbuffered(removedCategory);
	}
}
