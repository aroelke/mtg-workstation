package gui.filter.editor;

import gui.SettingsDialog;
import gui.editor.CategoryEditorPanel;
import gui.filter.FilterGroupPanel;
import gui.filter.FilterType;

import java.awt.BorderLayout;
import java.util.function.Predicate;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import database.Card;

/**
 * TODO: Comment this
 * @author Alec
 *
 */
@SuppressWarnings("serial")
public class DefaultsFilterPanel extends FilterEditorPanel
{
	private CategoryListModel categoriesModel;
	private JList<String> categoriesList;

	public DefaultsFilterPanel()
	{
		super(FilterType.DEFAULTS);
		setLayout(new BorderLayout());
		
		categoriesModel = new CategoryListModel();
		if (!SettingsDialog.settings.getProperty(SettingsDialog.EDITOR_PRESETS).isEmpty())
			for (String category: SettingsDialog.settings.getProperty(SettingsDialog.EDITOR_PRESETS).split(SettingsDialog.CATEGORY_DELIMITER))
				categoriesModel.addElement(category);
		categoriesList = new JList<String>(categoriesModel);
		categoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(categoriesList), BorderLayout.CENTER);
	}

	@Override
	public void setContents(String content)
	{}

	@Override
	public Predicate<Card> getFilter()
	{
		if (isEmpty())
			return (c) -> true;
		else
			return new CategoryEditorPanel(categoriesModel.getCategoryAt(categoriesList.getSelectedIndex())).filter();
	}

	@Override
	public boolean isEmpty()
	{
		return categoriesList.getSelectedIndex() < 0;
	}

	@Override
	protected String repr()
	{
		return new CategoryEditorPanel(categoriesModel.getCategoryAt(categoriesList.getSelectedIndex())).repr();
	}
	
	@Override
	public String toString()
	{
		String filterString = new CategoryEditorPanel(categoriesModel.getCategoryAt(categoriesList.getSelectedIndex())).repr();
		return filterString.substring(1, filterString.length() - 1);
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
