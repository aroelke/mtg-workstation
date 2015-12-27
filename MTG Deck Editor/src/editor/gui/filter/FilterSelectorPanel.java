package editor.gui.filter;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.gui.filter.editor.FilterPanelType;

@SuppressWarnings("serial")
public class FilterSelectorPanel extends FilterPanel<FilterLeaf<?>>
{
	private ComboBoxPanel<FilterPanelType> filterTypes;
	private Map<FilterPanelType, FilterEditorPanel<?>> filterPanels;
	private JPanel filtersPanel;
	
	public FilterSelectorPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		filterTypes = new ComboBoxPanel<FilterPanelType>(FilterPanelType.values());
		add(filterTypes);
		
		filterPanels = new HashMap<FilterPanelType, FilterEditorPanel<?>>();
		filtersPanel = new JPanel(new CardLayout());
		add(filtersPanel);
		for (FilterPanelType type: FilterPanelType.values())
		{
			FilterEditorPanel<?> panel = type.createPanel();
			filterPanels.put(type, panel);
			filtersPanel.add(panel, type.toString());
		}
		filterTypes.addItemListener((e) -> {
			CardLayout cards = (CardLayout)filtersPanel.getLayout();
			cards.show(filtersPanel, filterTypes.getSelectedItem().toString());
		});
		
		JButton removeButton = new JButton("−");
		removeButton.addActionListener((e) -> {
			group.remove(this);
			SwingUtilities.getWindowAncestor(group).pack();
		});
		add(removeButton);
		
		JButton groupButton = new JButton("…");
		groupButton.addActionListener((e) -> {
			group.group(this);
			SwingUtilities.getWindowAncestor(group).pack();
		});
		add(groupButton);
	}
	
	@Override
	public Filter filter()
	{
		return filterPanels.get(filterTypes.getSelectedItem()).filter();
	}
	
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		filterPanels.get(filter.type).setContents(filter);
		filterTypes.setSelectedItem(filter.type);
		((CardLayout)filtersPanel.getLayout()).show(filtersPanel, filter.type.toString());
	}
}
