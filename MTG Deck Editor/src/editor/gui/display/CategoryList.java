package editor.gui.display;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import editor.collection.category.CategorySpec;
import editor.gui.editor.CategoryEditorPanel;
import editor.gui.generic.MouseListenerFactory;

/**
 * This class represents an element that can display a list of CategorySpecs.
 * Optionally, it can show an extra line that is not an element, which is useful
 * for a hint of how to perform an action on the list.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryList extends JList<String>
{
	/**
	 * Categories to show.
	 */
	private List<CategorySpec> categories;
	/**
	 * Model for how to display categories.
	 */
	private CategoryListModel model;
	/**
	 * Hint to show for activating the CategoryList (for example, to edit
	 * or add categories).  If it's the empty string, don't show it.
	 */
	private String hint;
	
	/**
	 * Create a new CategoryList with the specified List
	 * of CategorySpec.
	 * 
	 * @param showHint Whether or not to enable adding of categories
	 * and show a hint for how to do it
	 * @param c List of CategorySpecs to show
	 */
	public CategoryList(String h, List<CategorySpec> c)
	{
		this(h);
		
		categories.addAll(c);
	}
	
	/**
	 * Create a new CategoryList with the specified CategorySpecs.
	 * 
	 * @param showHint Whether or not to enable adding of categories
	 * and show a hint for how to do it
	 * @param c CategorySpecs to show
	 */
	public CategoryList(String h, CategorySpec... c)
	{
		this(h, Arrays.asList(c));
	}
	
	/**
	 * Create a new empty CategoryList.
	 * 
	 * @param showHint Whether or not to enable adding of categories
	 * and show a hint for how to do it
	 */
	public CategoryList(String h)
	{
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		hint = h;
		categories = new ArrayList<CategorySpec>();
		setModel(model = new CategoryListModel());
		
		if (!hint.isEmpty())
		{
			addMouseListener(MouseListenerFactory.createReleaseListener((e) -> {
				if (e.getClickCount() == 2)
				{
					int index = locationToIndex(e.getPoint());
					Rectangle rec = getCellBounds(index, index);
					CategorySpec spec = null;
					if (rec == null || !rec.contains(e.getPoint()))
					{
						clearSelection();
						spec = CategoryEditorPanel.showCategoryEditor(CategoryList.this);
					}
					else
						spec = CategoryEditorPanel.showCategoryEditor(CategoryList.this, getCategoryAt(index));
					if (spec != null)
						setCategoryAt(index, spec);
				}
			}));
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
		return index < categories.size() ? index : -1;
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
	 * This class represents a model for displaying a list of CategorySpecs.
	 * 
	 * @author Alec Roelke
	 */
	private class CategoryListModel extends DefaultListModel<String>
	{
		/**
		 * @param Index into the list to look at.
		 * @return The name of the category at the index.
		 */
		@Override
		public String getElementAt(int index)
		{
			if (index < categories.size())
				return categories.get(index).getName();
			else if (!hint.isEmpty() && index == categories.size())
				return hint;
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
			return categories.size() + (!hint.isEmpty() ? 1 : 0);
		}
	}
}
