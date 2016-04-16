package editor.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import editor.collection.category.CategorySpec;
import editor.gui.editor.CategoryEditorPanel;

/**
 * This class represents an element that can display a list of CategorySpecs.
 * Optionally, it can show a hint for how to add new CategorySpecs to the list.
 * 
 * TODO: Make the hint be a parameter to the class and not static.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryList extends JList<String>
{
	/**
	 * String showing a hint for how to add a new category to the list.
	 */
	private static final String ADD_HINT = "<html><i>&lt;Double-click to add&gt;</i></html>";
	
	/**
	 * Whether or not to show the hint for adding categories (and enable adding
	 * them at the same time).
	 */
	private boolean showAdd;
	/**
	 * Categories to show.
	 */
	private List<CategorySpec> categories;
	/**
	 * Model for how to display categories.
	 */
	private CategoryListModel model;
	
	/**
	 * Create a new CategoryList with the specified List
	 * of CategorySpec.
	 * 
	 * @param showHint Whether or not to enable adding of categories
	 * and show a hint for how to do it
	 * @param c List of CategorySpecs to show
	 */
	public CategoryList(boolean showHint, List<CategorySpec> c)
	{
		this(showHint);
		
		categories.addAll(c);
	}
	
	/**
	 * Create a new CategoryList with the specified CategorySpecs.
	 * 
	 * @param showHint Whether or not to enable adding of categories
	 * and show a hint for how to do it
	 * @param c CategorySpecs to show
	 */
	public CategoryList(boolean showHint, CategorySpec... c)
	{
		this(showHint, Arrays.asList(c));
	}
	
	/**
	 * Create a new empty CategoryList.
	 * 
	 * @param showHint Whether or not to enable adding of categories
	 * and show a hint for how to do it
	 */
	public CategoryList(boolean showHint)
	{
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		showAdd = showHint;
		categories = new ArrayList<CategorySpec>();
		setModel(model = new CategoryListModel());
		
		if (showAdd)
		{
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent e)
				{
					int index = locationToIndex(e.getPoint());
					Rectangle rec = getCellBounds(index, index);
					if (rec == null || !rec.contains(e.getPoint()))
					{
						if (e.getClickCount() == 2)
						{
							clearSelection();
							CategorySpec spec = CategoryEditorPanel.showCategoryEditor();
							if (spec != null)
								addCategory(spec);
						}
					}
					else
					{
						if (e.getClickCount() == 2)
						{
							CategorySpec spec = CategoryEditorPanel.showCategoryEditor(getCategoryAt(index));
							if (spec != null)
								setCategoryAt(index, spec);
						}
					}
				}
			});
		}
	}
	
	/**
	 * Convert a point to an index into the displayed list.
	 * 
	 * @param p Point to convert
	 * @return The index of the specified Point, or -1 if
	 * there isn't one.
	 */
	@Override
	public int locationToIndex(Point p)
	{
		int index = super.locationToIndex(p);
		if (index < categories.size())
			return index;
		else
			return -1;
	}
	
	/**
	 * Add a new CategorySpec to the list.
	 * 
	 * @param c CategorySpec to display
	 */
	public void addCategory(CategorySpec c)
	{
		categories.add(c);
		model.addElement(c.getName());
	}
	
	/**
	 * Set the CategorySpec at a particular position in the list.
	 * 
	 * @param index Index to set
	 * @param c CategorySpec to display
	 */
	public void setCategoryAt(int index, CategorySpec c)
	{
		categories.set(index, c);
		model.setElementAt(c.getName(), index);
	}
	
	/**
	 * Remove the CategorySpec at a particular index.
	 * 
	 * @param index Index to remove the CategorySpec at
	 */
	public void removeCategoryAt(int index)
	{
		categories.remove(index);
		model.remove(index);
	}
	
	/**
	 * @return The CategorySpec list this CategoryList displays.
	 */
	public List<CategorySpec> getCategories()
	{
		return Collections.unmodifiableList(categories);
	}
	
	/**
	 * @return The number of CategorySpecs in this CategoryList.
	 */
	public int getCount()
	{
		return categories.size();
	}
	
	/**
	 * @param index Index into the list to search
	 * @return the CategorySpec at the given index.
	 */
	public CategorySpec getCategoryAt(int index)
	{
		return categories.get(index);
	}
	
	/**
	 * This class represnts a model for displaying a list of CategorySpecs.
	 * 
	 * @author Alec Roelke
	 */
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
				return categories.get(index).getName();
			else if (showAdd && index == categories.size())
				return ADD_HINT;
			else
				throw new IndexOutOfBoundsException("Illegal list index " + index);
		}
		
		/**
		 * @return The number of elements to show.  If the add categories
		 * hint is to be shown, this is the number of elements in the list
		 * plus one.
		 */
		@Override
		public int getSize()
		{
			return categories.size() + (showAdd ? 1 : 0);
		}
	}
}
