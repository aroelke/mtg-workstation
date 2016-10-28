package editor.gui.filter;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.leaf.FilterLeaf;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.gui.generic.ComboBoxPanel;
import editor.util.UnicodeSymbols;

/**
 * This class represents a panel that presents a drop-down menu that allows the user
 * to select a filter and fill out its contents.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class FilterSelectorPanel extends FilterPanel<FilterLeaf<?>>
{
	/**
	 * Combo box displaying the types of filters available.
	 */
	private ComboBoxPanel<String> filterTypes;
	/**
	 * Map of filter type onto filter editor panel.  This will contain
	 * one copy of each filter editor and its respective type.
	 */
	private Map<String, FilterEditorPanel<?>> filterPanels;
	/**
	 * Panel containing the filters to flip through.
	 */
	private JPanel filtersPanel;

	/**
	 * Create a new FilterSelectorPanel which will display the first
	 * filter panel.
	 */
	public FilterSelectorPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		// Filter type selector
		filterTypes = new ComboBoxPanel<String>(FilterFactory.FILTER_TYPES.values().toArray(new String[0]));
		add(filterTypes);

		// Panel containing each editor panel
		filterPanels = new HashMap<String, FilterEditorPanel<?>>();
		filtersPanel = new JPanel(new CardLayout());
		add(filtersPanel);
		for (Map.Entry<String, String> e: FilterFactory.FILTER_TYPES.entrySet())
		{
			String code = e.getKey();
			String name = e.getValue();
			FilterEditorPanel<?> panel = FilterPanelFactory.createFilterPanel(code);
			filterPanels[name] = panel;
			filtersPanel.add(panel, name);
		}
		filterTypes.addItemListener((e) -> {
			CardLayout cards = (CardLayout)filtersPanel.getLayout();
			cards.show(filtersPanel, filterTypes.getSelectedItem());
		});

		// Button to remove this from the form
		JButton removeButton = new JButton(String.valueOf(UnicodeSymbols.MINUS));
		removeButton.addActionListener((e) -> {
			group.remove(this);
			firePanelsChanged();
		});
		add(removeButton);

		// Button to create a new group with this in it
		JButton groupButton = new JButton(String.valueOf(UnicodeSymbols.ELLIPSIS));
		groupButton.addActionListener((e) -> {
			group.group(this);
			firePanelsChanged();
		});
		add(groupButton);
	}

	/**
	 * @return The filter of the currently-active filter editor.
	 */
	@Override
	public Filter filter()
	{
		return filterPanels[filterTypes.getSelectedItem()].filter();
	}

	/**
	 * Set the contents of this FilterSelectorPanel to flip to the panel corresponding
	 * to the given filter and then filling out its contents.
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		filterTypes.setSelectedItem(FilterFactory.FILTER_TYPES[filter.type]);
		filterPanels[FilterFactory.FILTER_TYPES[filter.type]].setContents(filter);
		((CardLayout)filtersPanel.getLayout()).show(filtersPanel, filter.type.toString());
	}
}
