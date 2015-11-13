package editor.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import editor.database.CategorySpec;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryList extends JList<String>
{
	private static final String ADD_HINT = "<html><i>&lt;Double-click to add&gt;</i></html>";
	
	private boolean showAdd;
	private List<CategorySpec> categories;
	
	public CategoryList(boolean showHint, List<CategorySpec> c)
	{
		this(showHint);
		
		categories.addAll(c);
	}
	
	public CategoryList(boolean showHint, CategorySpec... c)
	{
		this(showHint, Arrays.asList(c));
	}
	
	public CategoryList(boolean showHint)
	{
		showAdd = showHint;
		categories = new ArrayList<CategorySpec>();
		setModel(new CategoryListModel());
	}
	
	@Override
	public int locationToIndex(Point p)
	{
		int index = super.locationToIndex(p);
		if (index < categories.size())
			return index;
		else
			return -1;
	}
	
	public void addCategory(CategorySpec c)
	{
		categories.add(c);
	}
	
	public void setCategoryAt(int index, CategorySpec c)
	{
		categories.set(index, c);
	}
	
	public void removeCategoryAt(int index)
	{
		categories.remove(index);
	}
	
	public int getCount()
	{
		return categories.size();
	}
	
	public CategorySpec getCategoryAt(int index)
	{
		return categories.get(index);
	}
	
	private class CategoryListModel extends DefaultListModel<String>
	{
		/**
		 * Create a new CategoryListModel.
		 */
		public CategoryListModel()
		{
			super();
		}
		
		/**
		 * @param Index into the list to look at.
		 * @return The name of the category at the index.
		 */
		@Override
		public String getElementAt(int index)
		{
			if (index < categories.size())
				return categories.get(index).name;
			else if (showAdd && index == categories.size())
				return ADD_HINT;
			else
				throw new IndexOutOfBoundsException("Illegal list index " + index);
		}
		
		@Override
		public int getSize()
		{
			return categories.size() + (showAdd ? 1 : 0);
		}
	}
}
