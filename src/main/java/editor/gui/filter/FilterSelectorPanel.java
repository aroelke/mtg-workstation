package editor.gui.filter;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import editor.database.attributes.CardAttribute;
import editor.filter.FacesFilter;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.gui.generic.ComboBoxPanel;
import editor.util.MouseListenerFactory;
import editor.util.UnicodeSymbols;

/**
 * This class represents a panel that presents a drop-down menu that allows the user
 * to select a filter and fill out its contents.
 *
 * @author Alec Roelke
 */
public class FilterSelectorPanel extends FilterPanel<FilterLeaf<?>>
{
    /**
     * Map of filter type onto filter editor panel.  This will contain
     * one copy of each filter editor and its respective type.
     */
    private Map<CardAttribute, FilterEditorPanel<?>> filterPanels;
    /**
     * Panel containing the filters to flip through.
     */
    private JPanel filtersPanel;
    /**
     * Combo box displaying the types of filters available.
     */
    private ComboBoxPanel<CardAttribute> filterTypes;
    private FacesFilter faces;
    private JLabel facesLabel;

    /**
     * Create a new FilterSelectorPanel which will display the first filter panel.
     */
    public FilterSelectorPanel()
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Filter type selector
        filterTypes = new ComboBoxPanel<>(CardAttribute.filterableValues());
        add(filterTypes);

        // Panel containing each editor panel
        filterPanels = new HashMap<>();
        filtersPanel = new JPanel(new CardLayout());
        add(filtersPanel);
        for (CardAttribute attribute: CardAttribute.filterableValues())
        {
            FilterEditorPanel<?> panel = FilterPanelFactory.createFilterPanel(attribute);
            filterPanels.put(attribute, panel);
            filtersPanel.add(panel, String.valueOf(attribute));
        }
        filterTypes.addItemListener((e) -> {
            CardLayout cards = (CardLayout)filtersPanel.getLayout();
            cards.show(filtersPanel, String.valueOf(filterTypes.getSelectedItem()));
        });

        // Small button to choose which faces to look at when filtering
        faces = FacesFilter.ANY;
        facesLabel = new JLabel();
        facesLabel.addMouseListener(MouseListenerFactory.createReleaseListener((e) -> {
            faces = switch (faces) {
                case ANY   -> FacesFilter.ALL;
                case ALL   -> FacesFilter.FRONT;
                case FRONT -> FacesFilter.BACK;
                case BACK  -> FacesFilter.ANY;
            };
            facesLabel.setIcon(faces.getIcon(getHeight()/2));
        }));
        add(facesLabel);

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

        facesLabel.setIcon(faces.getIcon(getPreferredSize().height/2));
    }

    /**
     * {@inheritDoc}
     *
     * @return The filter of the currently-active filter editor.
     */
    @Override
    public Filter filter()
    {
        Filter f = filterPanels.get(filterTypes.getSelectedItem()).filter();
        if (f instanceof FilterLeaf<?> l)
        {
            l.faces = faces;
            return l;
        }
        else
            return f;
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
        facesLabel.setIcon((faces = filter.faces).getIcon(getPreferredSize().height/2));
        ((CardLayout)filtersPanel.getLayout()).show(filtersPanel, String.valueOf(filter.type()));
    }
}
