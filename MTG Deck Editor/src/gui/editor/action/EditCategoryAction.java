package gui.editor.action;

import java.util.function.Predicate;

import database.Card;
import database.Deck;

public class EditCategoryAction implements DeckAction
{
	private RemoveCategoryAction former;
	private AddCategoryAction after;
	
	public EditCategoryAction(Deck d, String nf, String rf, Predicate<Card> ff, String na, String ra, Predicate<Card> fa)
	{
		former = new RemoveCategoryAction(d, nf, rf, ff);
		after = new AddCategoryAction(d, na, ra, fa);
	}
	
	@Override
	public void undo()
	{
		after.undo();
		try
		{
			former.undo();
		}
		catch (Exception e)
		{
			after.redo();
			throw e;
		}
	}

	@Override
	public void redo()
	{
		former.redo();
		try
		{
			after.redo();
		}
		catch (Exception e)
		{
			former.undo();
			throw e;
		}
	}
}
