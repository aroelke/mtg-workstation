package gui.filter;

import gui.filter.editor.DefaultsFilterPanel;
import gui.filter.editor.FilterEditorPanel;
import gui.filter.editor.options.OptionsFilterPanel;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.BoxLayout;
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
 * TODO: Align the option dropdown with the filter panel better
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class FilterTypePanel extends FilterPanel
{
	/**
	 * Height that a FilterTypePanel should be, unless it is displaying an option filter
	 * panel.
	 */
	public static final int ROW_HEIGHT = 27;
	/**
	 * Width that the FilterTypePanel should be, including all buttons and other elements.
	 */
	public static final int COL_WIDTH = 600;
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
	 * Create a new FilterTypePanel.  It will default to an empty NameFilterPanel.
	 * 
	 * @param g
	 */
	public FilterTypePanel(FilterGroupPanel g)
	{
		super(g);
		
		// Create the layout, which ensures correct sizing of this FilterContainer.
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(new EmptyBorder(0, 0, 5, 0));
		
		// Combo box for choosing the filter type
		filterTypeBox = new JComboBox<FilterType>(FilterType.values());
		filterTypeBox.addItemListener(new FilterTypeListener());
		filterTypeBox.setAlignmentY(CENTER_ALIGNMENT);
		add(filterTypeBox);
		
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
		filterPanel.setAlignmentY(CENTER_ALIGNMENT);
		add(filterPanel);
		
		// Remove button
		removeButton = new JButton("−");
		removeButton.addActionListener((e) -> {
			getGroup().removeFilterPanel(this);
			SwingUtilities.windowForComponent(getGroup()).pack();
		});
		removeButton.setAlignmentY(CENTER_ALIGNMENT);
		add(removeButton);
		
		// Change to group button
		groupButton = new JButton("…");
		groupButton.addActionListener((e) -> {
			getGroup().groupFilterPanel(this);
			SwingUtilities.windowForComponent(this).pack();
		});
		groupButton.setAlignmentY(CENTER_ALIGNMENT);
		add(groupButton);
		
		setPreferredSize(new Dimension(COL_WIDTH, ROW_HEIGHT));
		filterTypeBox.setPreferredSize(new Dimension(filterTypeBox.getPreferredSize().width, ROW_HEIGHT));
		filterTypeBox.setMaximumSize(new Dimension(filterTypeBox.getPreferredSize().width, ROW_HEIGHT));
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
			if (currentFilter instanceof OptionsFilterPanel || currentFilter instanceof DefaultsFilterPanel)
			{
				setPreferredSize(new Dimension(COL_WIDTH, 5*ROW_HEIGHT));
				filterTypeBox.setMaximumSize(new Dimension(filterTypeBox.getPreferredSize().width, ROW_HEIGHT - 5));
			}
			else
			{
				setPreferredSize(new Dimension(COL_WIDTH, ROW_HEIGHT));
				filterTypeBox.setMaximumSize(new Dimension(filterTypeBox.getPreferredSize().width, ROW_HEIGHT));
			}
			if (SwingUtilities.windowForComponent(FilterTypePanel.this) != null)
				SwingUtilities.windowForComponent(FilterTypePanel.this).pack();
		}
	}
}
