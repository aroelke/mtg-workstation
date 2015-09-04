package gui.filter;

import gui.filter.editor.FilterEditorPanel;
import gui.filter.editor.options.OptionsFilterPanel;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import database.Card;

/**
 * This class represents a panel containing one of each FilterPanel.  The active one
 * can be changed using a combo box.  When the filter is retrieved, only the active
 * filter is used.  A FilterContainer can also be removed using its remove button.
 * 
 * TODO: Add a "defaults" filter type to add one of the default categories' filters to the filter
 * - this will be expanded into a filter group when committed to a category
 * TODO: Add an "empty" filter type that matches no cards
 * 
 * @author Alec
 */
@SuppressWarnings("serial")
public class FilterTypePanel extends FilterPanel
{
	/**
	 * Height that a FilterTypePanel should be, unless it is displaying an option filter
	 * panel, in which case it is five times this.
	 */
	public static final int ROW_HEIGHT = 23;
	/**
	 * Width that the FilterTypePanel should be, including all buttons and other elements.
	 */
	public static final int COL_WIDTH = 420;
	/**
	 * Combo box to choose what type of filter to display.
	 */
	private JComboBox<FilterType> filterTypeBox;
	/**
	 * Panel containing the filters.
	 */
	private JPanel filterPanel;
	/**
	 * Remove button.
	 */
	private JButton removeButton;
	/**
	 * Button to convert this FilterTypePanel into a FilterGroup containing it.
	 */
	private JButton groupButton;
	/**
	 * Map of filter type onto filter panel to know what panel to get the filter
	 * from.
	 */
	private Map<FilterType, FilterEditorPanel> filters;
	/**
	 * Currently selected FilterPanel.
	 */
	private FilterEditorPanel currentFilter;
	/**
	 * When selecting an option filter panel, the size of the frame has to change to fit
	 * it, which requires modifying the layout.
	 */
	private GridBagLayout layout;
	
	/**
	 * Create a new FilterTypePanel.  It will default to an empty NameFilterPanel.
	 * 
	 * @param g
	 */
	public FilterTypePanel(FilterGroupPanel g)
	{
		super(g);
		
		// Create the layout, which ensures correct sizing of this FilterContainer.
		layout = new GridBagLayout();
		layout.rowHeights = new int[] {0, 0, ROW_HEIGHT, 0, 0};
		layout.rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0};
		layout.columnWidths = new int[] {0, COL_WIDTH, 0, 0};
		layout.columnWeights = new double[] {0.0, 1.0, 0.0, 0.0};
		setLayout(layout);
		setBorder(new EmptyBorder(0, 0, 5, 0));
		
		// Combo box for choosing the filter type
		filterTypeBox = new JComboBox<FilterType>(FilterType.values());
		filterTypeBox.addItemListener(new FilterTypeListener());
		GridBagConstraints filterTypeConstraints = new GridBagConstraints();
		filterTypeConstraints.fill = GridBagConstraints.BOTH;
		filterTypeConstraints.gridx = 0;
		filterTypeConstraints.gridy = 2;
		add(filterTypeBox, filterTypeConstraints);
		
		// Panel containing the filters.  The CardLayout ensures that only one filter
		// will be shown at once, which is chosen using the above combo box
		filters = new HashMap<FilterType, FilterEditorPanel>();
		filterPanel = new JPanel(new CardLayout()
		{
			/*
			 * The panel's size should change dynamically according to the size
			 * of the filter panel in it.
			 * @see java.awt.CardLayout#preferredLayoutSize(java.awt.Container)
			 */
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
				Component current = null;
				for (Component comp: parent.getComponents())
					if (comp.isVisible())
						current = comp;
				if (current != null)
				{
					Insets insets = parent.getInsets();
					Dimension pref = current.getPreferredSize();
					pref.width += insets.left + insets.right;
					pref.height += insets.top + insets.bottom;
					return pref;
				}
				else
					return super.preferredLayoutSize(parent);
			}
		});
		for (FilterType filterType: FilterType.values())
		{
			try
			{
				filters.put(filterType, filterType.newInstance());
				filterPanel.add(filters.get(filterType), filterType.toString());
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				JOptionPane.showMessageDialog(null, "Error creating filter type " + filterType + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		currentFilter = filters.get(FilterType.NAME);
		GridBagConstraints filterConstraints = new GridBagConstraints();
		filterConstraints.fill = GridBagConstraints.BOTH;
		filterConstraints.gridx = 1;
		filterConstraints.gridy = 2;
		add(filterPanel, filterConstraints);
		
		// Remove button
		removeButton = new JButton("−");
		removeButton.addActionListener((e) -> {
			getGroup().removeFilterPanel(this);
			SwingUtilities.windowForComponent(getGroup()).pack();
		});
		GridBagConstraints removeConstraints = new GridBagConstraints();
		removeConstraints.fill = GridBagConstraints.HORIZONTAL;
		removeConstraints.gridx = 2;
		removeConstraints.gridy = 2;
		add(removeButton, removeConstraints);
		
		// Change to group button
		groupButton = new JButton("…");
		groupButton.addActionListener((e) -> {
			getGroup().groupFilterPanel(this);
			SwingUtilities.windowForComponent(this).pack();
		});
		GridBagConstraints groupConstraints = new GridBagConstraints();
		groupConstraints.fill = GridBagConstraints.HORIZONTAL;
		groupConstraints.gridx = 3;
		groupConstraints.gridy = 2;
		add(groupButton, groupConstraints);
	}
	
	/**
	 * @return The filter from the current FilterPanel.
	 */
	@Override
	public Predicate<Card> filter()
	{
		return currentFilter.getFilter();
	}
	
	/**
	 * Change to the filter type specified by the string and fill its contents with
	 * those specified by the string.  The string should be surrounded by group
	 * enclosures.
	 * 
	 * @param contents String to parse for contents
	 * @see FilterGroupPanel#setContents(String)
	 * @see FilterGroupPanel#BEGIN_GROUP
	 * @see FilterGroupPanel#END_GROUP
	 */
	@Override
	public void setContents(String contents)
	{
		String[] filter = contents.substring(1, contents.length() - 1).split(":", 2);
		filterTypeBox.setSelectedItem(FilterType.fromCode(filter[0]));
		currentFilter.setContents(filter[1]);
	}
	
	/**
	 * @return <code>true</code> if the current filter has valid data, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return currentFilter.isEmpty();
	}
	
	/**
	 * @return A String representation of this FilterTypePanel, which is its' current filter's
	 * string surrounded by group enclosures.
	 * @see FilterGroupPanel#BEGIN_GROUP
	 * @see FilterGroupPanel#END_GROUP
	 */
	@Override
	public String toString()
	{
		return FilterGroupPanel.BEGIN_GROUP + currentFilter.toString() + FilterGroupPanel.END_GROUP;
	}
	
	/**
	 * This class represents the action that should be taken when the combo box is changed.
	 * 
	 * @author Alec Roelke
	 */
	private class FilterTypeListener implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			// Get the current filter
			CardLayout cards = (CardLayout)filterPanel.getLayout();
			cards.show(filterPanel, String.valueOf(e.getItem()));
			currentFilter = filters.get(e.getItem());
			GridBagConstraints filterConstraints = new GridBagConstraints();
			filterConstraints.fill = GridBagConstraints.BOTH;
			filterConstraints.gridx = 1;
			// If the current filter is an option filter, resize the panel (or resize it if it was
			// an option filter and now isn't
			if (currentFilter instanceof OptionsFilterPanel)
			{
				layout.rowHeights = new int[] {ROW_HEIGHT, ROW_HEIGHT, ROW_HEIGHT, ROW_HEIGHT, ROW_HEIGHT};
				filterConstraints.gridy = 0;
				filterConstraints.gridheight = 5;
			}
			else
			{
				layout.rowHeights = new int[] {0, 0, ROW_HEIGHT, 0, 0};
				filterConstraints.gridy = 2;
				filterConstraints.gridheight = 1;
			}
			// Refresh the panel
			remove(filterPanel);
			add(filterPanel, filterConstraints);
			if (SwingUtilities.windowForComponent(FilterTypePanel.this) != null)
				SwingUtilities.windowForComponent(FilterTypePanel.this).pack();
		}
	}
}
