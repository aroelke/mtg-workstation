package gui.editor.action;

import gui.editor.CategoryPanel;
import gui.editor.EditorFrame;

import java.util.function.Predicate;

import database.Card;

public class EditCategoryAction implements DeckAction
{
	private EditorFrame editor;
	private CategoryPanel editedCategory;
	private String formerName;
	private String formerRepr;
	private Predicate<Card> formerFilter;
	private String newName;
	private String newRepr;
	private Predicate<Card> newFilter;
	
	public EditCategoryAction(EditorFrame e, CategoryPanel category, String nf, String rf, Predicate<Card> ff, String na, String ra, Predicate<Card> fa)
	{
		editor = e;
		editedCategory = category;
		formerName = nf;
		formerRepr = rf;
		formerFilter = ff;
		newName = na;
		newRepr = ra;
		newFilter = fa;
	}
	
	@Override
	public void undo()
	{
		editedCategory.edit(formerName, formerRepr, formerFilter);
		editor.revalidate();
		editor.repaint();
		editor.setUnsaved();
	}

	@Override
	public void redo()
	{
		editedCategory.edit(newName, newRepr, newFilter);
		editor.revalidate();
		editor.repaint();
		editor.setUnsaved();
	}
}
