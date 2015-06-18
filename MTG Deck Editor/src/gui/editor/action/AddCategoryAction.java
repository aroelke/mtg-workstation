package gui.editor.action;

import gui.editor.CategoryPanel;
import gui.editor.EditorFrame;

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
		editor.removeCategory(addedCategory);
	}
	
	@Override
	public void redo()
	{
		editor.addCategory(addedCategory);
	}
}
