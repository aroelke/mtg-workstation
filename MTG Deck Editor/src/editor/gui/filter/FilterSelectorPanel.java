package editor.gui.filter;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.gui.generic.ComboBoxPanel;

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
	private ComboBoxPanel<FilterType> filterTypes;
	/**
	 * Map of filter type onto filter editor panel.  This will contain
	 * one copy of each filter editor and its respective type.
	 */
	private Map<FilterType, FilterEditorPanel<?>> filterPanels;
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
		filterTypes = new ComboBoxPanel<FilterType>(FilterType.values());
		add(filterTypes);
		
		// Panel containing each editor panel
		filterPanels = new HashMap<FilterType, FilterEditorPanel<?>>();
		filtersPanel = new JPanel(new CardLayout());
		add(filtersPanel);
		for (FilterType type: FilterType.values())
		{
			FilterEditorPanel<?> panel = FilterPanelFactory.createFilterPanel(type);
			filterPanels.put(type, panel);
			filtersPanel.add(panel, type.toString());
		}
		filterTypes.addItemListener((e) -> {
			CardLayout cards = (CardLayout)filtersPanel.getLayout();
			cards.show(filtersPanel, filterTypes.getSelectedItem().toString());
		});
		
		// Button to remove this from the form
		JButton removeButton = new JButton("−");
		removeButton.addActionListener((e) -> {
			group.remove(this);
			firePanelsChanged();
		});
		add(removeButton);
		
		// Button to create a new group with this in it
		JButton groupButton = new JButton("…");
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
		filterTypes.setSelectedItem(filter.type);
		filterPanels[filter.type].setContents(filter);
		((CardLayout)filtersPanel.getLayout()).show(filtersPanel, filter.type.toString());
	}
}
