package editor.gui.filter.editor.options;

import java.awt.BorderLayout;
import java.util.function.Predicate;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import editor.database.Card;
import editor.database.CategorySpec;
import editor.gui.SettingsDialog;
import editor.gui.filter.FilterGroupPanel;
import editor.gui.filter.FilterType;
import editor.gui.filter.editor.FilterEditorPanel;

/**
 * This class represents a filter panel that allows for the filtering of Cards based on
 * the list of predefined categories.
 * 
 * @author Alec
 */
@SuppressWarnings("serial")
public class DefaultsFilterPanel extends FilterEditorPanel
{
	/**
	 * List Model for displaying categories that can be used as filters.
	 */
	private CategoryListModel categoriesModel;
	/**
	 * List containing the categories to select from.
	 */
	private JList<String> categoriesList;

	/**
	 * Create a new DefaultsFilterPanel.
	 */
	public DefaultsFilterPanel()
	{
		super(FilterType.DEFAULTS);
		setLayout(new BorderLayout());
		
		categoriesModel = new CategoryListModel();
		if (!SettingsDialog.getSetting(SettingsDialog.EDITOR_PRESETS).isEmpty())
			for (String category: SettingsDialog.getSetting(SettingsDialog.EDITOR_PRESETS).split(SettingsDialog.CATEGORY_DELIMITER))
				categoriesModel.addElement(category);
		categoriesList = new JList<String>(categoriesModel);
		categoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		categoriesList.setVisibleRowCount(Math.min(OptionsFilterPanel.MAX_ROWS, 7));
		add(new JScrollPane(categoriesList), BorderLayout.CENTER);
	}

	/**
	 * This filter panel doesn't actually contain anything; it gets replaced with the contents
	 * of the selected category's filter when unloaded.  As a result, it has no contents to set.
	 */
	@Override
	public void setContents(String content)
	{}

	/**
	 * @return The Predicate<Card> representing the filter of the selected preset category.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		if (isEmpty())
			return (c) -> true;
		else
			return new CategorySpec(categoriesModel.getCategoryAt(categoriesList.getSelectedIndex())).filter;
	}

	/**
	 * @return <code>true</code> if there is no selected category, and <code>false</code>
	 * otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return categoriesList.getSelectedIndex() < 0;
	}

	/**
	 * @return The String representation of the filter of the selected category.
	 */
	@Override
	protected String repr()
	{
		return new CategorySpec(categoriesModel.getCategoryAt(categoriesList.getSelectedIndex())).filterString;
	}
	
	/**
	 * @return The String representation of the filter of the selected category, except without the enclosing
	 * characters.
	 */
	@Override
	public String toString()
	{
		String filterString = new CategorySpec(categoriesModel.getCategoryAt(categoriesList.getSelectedIndex())).filterString;
		return filterString.substring(1, filterString.length() - 1);
	}

	/**
	 * This class represents a model for displaying categories in a list by name, but allowing for
	 * the retrieval of their information.
	 * 
	 * @author Alec
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
			String category = super.getElementAt(index);
			return category.substring(category.indexOf(FilterGroupPanel.BEGIN_GROUP) + 1, category.indexOf(FilterGroupPanel.END_GROUP));
		}
		
		/**
		 * @param index Index into the list to look at.
		 * @return The String representation of the category at the index.
		 */
		public String getCategoryAt(int index)
		{
			return super.getElementAt(index);
		}
	}
}
