package editor.gui.filter;

import editor.filter.Filter;
import editor.filter.FilterAttribute;
import editor.filter.leaf.FilterLeaf;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.gui.generic.ComboBoxPanel;
import editor.util.UnicodeSymbols;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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
     * Map of filter type onto filter editor panel.  This will contain
     * one copy of each filter editor and its respective type.
     */
    private Map<FilterAttribute, FilterEditorPanel<?>> filterPanels;
    /**
     * Panel containing the filters to flip through.
     */
    private JPanel filtersPanel;
    /**
     * Combo box displaying the types of filters available.
     */
    private ComboBoxPanel<FilterAttribute> filterTypes;

    /**
     * Create a new FilterSelectorPanel which will display the first filter panel.
     */
    public FilterSelectorPanel()
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Filter type selector
        filterTypes = new ComboBoxPanel<>(FilterAttribute.filterableValues());
        add(filterTypes);

        // Panel containing each editor panel
        filterPanels = new HashMap<>();
        filtersPanel = new JPanel(new CardLayout());
        add(filtersPanel);
        for (FilterAttribute attribute: FilterAttribute.filterableValues())
        {
            FilterEditorPanel<?> panel = FilterPanelFactory.createFilterPanel(attribute);
            filterPanels.put(attribute, panel);
            filtersPanel.add(panel, String.valueOf(attribute));
        }
        filterTypes.addItemListener((e) -> {
            CardLayout cards = (CardLayout)filtersPanel.getLayout();
            cards.show(filtersPanel, String.valueOf(filterTypes.getSelectedItem()));
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
     * {@inheritDoc}
     *
     * @return The filter of the currently-active filter editor.
     */
    @Override
    public Filter filter()
    {
        return filterPanels.get(filterTypes.getSelectedItem()).filter();
    }

    /**
     * {@inheritDoc}
     * Switch to the panel corresponding to the given filter and then set that panel's
     * contents accordingly.
     */
    @Override
    public void setContents(FilterLeaf<?> filter)
    {
        filterTypes.setSelectedItem(filter.type());
        filterPanels.get(filter.type()).setContents(filter);
        ((CardLayout)filtersPanel.getLayout()).show(filtersPanel, String.valueOf(filter.type()));
    }
}
