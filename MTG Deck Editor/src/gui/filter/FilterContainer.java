package gui.filter;

import gui.filter.options.OptionsFilterPanel;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import database.Card;

/**
 * This class represents a panel containing one of each FilterPanel.  The active one
 * can be changed using a combo box.  When the filter is retrieved, only the active
 * filter is used.  A FilterContainer can also be removed using its remove button.
 * 
 * @author Alec
 */
@SuppressWarnings("serial")
public class FilterContainer extends JPanel
{
	/**
	 * Height that a FilterPanel should be, unless it is displaying an option filter
	 * panel, in which case it is five times this.
	 */
	public static final int ROW_HEIGHT = 23;
	
	/**
	 * Parent dialog of this FilterContainer.
	 */
	private FilterDialog parent;
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
	private JButton remove;
	/**
	 * Map of filter type onto filter panel to know what panel to get the filter
	 * from.
	 */
	private Map<FilterType, FilterPanel> filters;
	/**
	 * Currently selected FilterPanel.
	 */
	private FilterPanel currentFilter;
	/**
	 * Check box for whether or not the filter produced by this panel should be
	 * ANDed or ORed with the previously generated filter.
	 */
	private JCheckBox andCheck;
	/**
	 * When selecting an option filter panel, the size of the frame has to change to fit
	 * it, which requires modifying the layout.
	 */
	private GridBagLayout layout;
	
	/**
	 * Create a new FilterContainer.  It will default to an empty NameFilterPanel.
	 * 
	 * @param p
	 */
	public FilterContainer(FilterDialog p)
	{
		super();
		
		parent = p;
		
		// Create the layout, which ensures correct sizing of this FilterContainer.
		layout = new GridBagLayout();
		layout.rowHeights = new int[] {0, 0, ROW_HEIGHT, 0, 0};
		layout.rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0};
		layout.columnWidths = new int[] {0, 0, 400, 0};
		layout.columnWeights = new double[] {0.0, 0.0, 1.0, 0.0};
		setLayout(layout);
		setBorder(new EmptyBorder(0, 0, 5, 0));
		
		// Check box for ANDing or ORing the filter
		andCheck = new JCheckBox("and");
		andCheck.setSelected(true);
		GridBagConstraints andConstraints = new GridBagConstraints();
		andConstraints.fill = GridBagConstraints.BOTH;
		andConstraints.gridx = 0;
		andConstraints.gridy = 2;
		add(andCheck, andConstraints);
		
		// Combo box for choosing the filter type
		filterTypeBox = new JComboBox<FilterType>(FilterType.values());
		filterTypeBox.addItemListener(new FilterTypeListener());
		GridBagConstraints filterTypeConstraints = new GridBagConstraints();
		filterTypeConstraints.fill = GridBagConstraints.BOTH;
		filterTypeConstraints.gridx = 1;
		filterTypeConstraints.gridy = 2;
		add(filterTypeBox, filterTypeConstraints);
		
		// Panel containing the filters.  The CardLayout ensures that only one filter
		// will be shown at once, which is chosen using the above combo box
		filters = new HashMap<FilterType, FilterPanel>();
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
		filterConstraints.gridx = 2;
		filterConstraints.gridy = 2;
		add(filterPanel, filterConstraints);
		
		// Remove button
		remove = new JButton("\u2013");
		remove.addActionListener((e) -> parent.removeFilterPanel(this));
		GridBagConstraints removeConstraints = new GridBagConstraints();
		removeConstraints.fill = GridBagConstraints.HORIZONTAL;
		removeConstraints.gridx = 3;
		removeConstraints.gridy = 2;
		add(remove, removeConstraints);
	}
	
	/**
	 * @return The filter from the current FilterPanel.
	 */
	public Predicate<Card> getFilter()
	{
		return currentFilter.getFilter();
	}
	
	/**
	 * Change to the specified type of filter and set its contents.
	 * 
	 * @param filterType Filter type to change to
	 * @param contents String to parse to set its contents
	 */
	public void setContents(FilterType filterType, String contents)
	{
		filterTypeBox.setSelectedItem(filterType);
		currentFilter.setContent(contents);
	}
	
	/**
	 * The first filter should always be ANDed, so set its AND box and
	 * disable it.
	 * 
	 * @param b Whether or not this FilterContainer should always be ANDed
	 */
	public void alwaysAnd(boolean b)
	{
		if (b)
			andCheck.setSelected(true);
		andCheck.setEnabled(!b);
	}
	
	/**
	 * @return <code>true</code> if the AND check box is selected, and
	 * <code>false</code> otherwise.
	 */
	public boolean isAnd()
	{
		return andCheck.isSelected();
	}
	
	/**
	 * Set the AND status of the filter.
	 * 
	 * @param and Whether or not the filter should be ANDed.
	 */
	public void setAnd(boolean and)
	{
		if (andCheck.isEnabled())
			andCheck.setSelected(and);
	}
	
	/**
	 * @return <code>true</code> if the current filter has valid data, and
	 * <code>false</code> otherwise.
	 */
	public boolean isEmpty()
	{
		return currentFilter.isEmpty();
	}
	
	/**
	 * @return A String representation of this FilterContainer, which is the String
	 * representation of its current filter.
	 */
	@Override
	public String toString()
	{
		return currentFilter.toString();
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
			currentFilter = filters.get((FilterType)e.getItem());
			GridBagConstraints filterConstraints = new GridBagConstraints();
			filterConstraints.fill = GridBagConstraints.BOTH;
			filterConstraints.gridx = 2;
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
			parent.pack();
		}
	}
}
