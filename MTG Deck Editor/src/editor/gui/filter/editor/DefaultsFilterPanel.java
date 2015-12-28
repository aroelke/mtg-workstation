package editor.gui.filter.editor;

import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import editor.database.CategorySpec;
import editor.filter.Filter;
import editor.filter.FilterGroup;
import editor.filter.leaf.FilterLeaf;
import editor.gui.SettingsDialog;
import editor.gui.filter.ComboBoxPanel;

/**
 * This class represents a panel that presents the user with a
 * single combo box showing the default categories in the
 * settings dialog and returns the filter for the selected one.
 * It is replaced with the contents of the selected filter in the
 * filter tree.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class DefaultsFilterPanel extends FilterEditorPanel<FilterLeaf<?>>
{
	
	/**
	 * Create a new DefaultsFilterPanel.
	 * 
	 * @return The created DefaultsFilterPanel.
	 */
	public static DefaultsFilterPanel create()
	{
		return new DefaultsFilterPanel();
	}
	
	/**
	 * Combo box showing the default categories.
	 */
	private ComboBoxPanel<String> defaults;
	/**
	 * Map of category names onto their String representations.
	 */
	private Map<String, String> categories;
	
	/**
	 * Create a new DefaultsFilterPanel.
	 */
	private DefaultsFilterPanel()
	{
		super();
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		categories = new HashMap<String, String>();
		
		String[] presets = SettingsDialog.getPresetCategories();
		String[] names = Arrays.stream(presets).map((s) -> s.substring(1, s.indexOf(FilterGroup.END_GROUP)).trim()).toArray(String[]::new);
		for (int i = 0; i < presets.length; i++)
			categories.put(names[i], presets[i]);
		
		defaults = new ComboBoxPanel<String>(names);
		add(defaults);
	}
	
	/**
	 * @return A FilterGroup containing the parsed contents of the selected
	 * default category. 
	 */
	@Override
	public Filter filter()
	{
		return new CategorySpec(categories.get(defaults.getSelectedItem())).filter;
	}

	/**
	 * Since this panel doesn't correspond to a filter, there are no contents
	 * to set.
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{}
}
