package editor.gui.filter.editor;

import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import editor.database.CategorySpec;
import editor.filter.Filter;
import editor.filter.FilterGroup;
import editor.filter.leaf.FilterLeaf;
import editor.gui.SettingsDialog;
import editor.gui.filter.ComboBoxPanel;

@SuppressWarnings("serial")
public class DefaultsFilterPanel extends FilterEditorPanel<FilterLeaf<?>>
{
	public static DefaultsFilterPanel create()
	{
		return new DefaultsFilterPanel();
	}
	
	private ComboBoxPanel<String> defaults;
	private Map<String, String> categories;
	
	private DefaultsFilterPanel()
	{
		super();
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		categories = new HashMap<String, String>();
		defaults = new ComboBoxPanel<String>();
		add(defaults);
		for (String category: SettingsDialog.getPresetCategories())
		{
			String name = category.substring(0, category.indexOf(FilterGroup.BEGIN_GROUP)).trim();
			defaults.addItem(name);
			categories.put(name, category);
		}
	}
	
	@Override
	public Filter filter()
	{
		String category = categories.get(defaults.getSelectedItem());
		CategorySpec spec = new CategorySpec(category);
		return spec.filter;
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{}
}
