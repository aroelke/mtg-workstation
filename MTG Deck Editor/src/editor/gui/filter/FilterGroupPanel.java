package editor.gui.filter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import editor.filter.Filter;
import editor.filter.FilterGroup;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents a group of filter panels that corresponds to a
 * group of filters.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class FilterGroupPanel extends FilterPanel<Filter>
{
	/**
	 * FilterPanels contained by this FilterGroupPanel.
	 */
	private List<FilterPanel<?>> children;
	/**
	 * Box showing the combination mode of the filter group.
	 */
	private JComboBox<FilterGroup.Mode> modeBox;
	/**
	 * Panel containing the children.
	 */
	private JPanel filtersPanel;
	
	/**
	 * Create a new FilterGroupPanel with one child.
	 */
	public FilterGroupPanel()
	{
		super();
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createEtchedBorder()));
		setLayout(new BorderLayout());
		
		children = new ArrayList<FilterPanel<?>>();
		
		// Panel containing the mode selector and edit buttons
		JPanel topPanel = new JPanel(new GridLayout(1, 2));
		add(topPanel, BorderLayout.NORTH);
		
		// Mode selection combo box
		JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modeBox = new JComboBox<FilterGroup.Mode>();
		modeBox.setModel(new DefaultComboBoxModel<FilterGroup.Mode>(FilterGroup.Mode.values()));
		modePanel.add(modeBox);
		topPanel.add(modePanel);
		
		// Add, remove, and group buttons
		JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton addButton = new JButton("+");
		addButton.addActionListener((e) -> {
			add(new FilterSelectorPanel());
			firePanelsChanged();
		});
		editPanel.add(addButton);
		JButton removeButton = new JButton("−");
		removeButton.addActionListener((e) -> {
			if (group == null)
			{
				clear();
				add(new FilterSelectorPanel());
				firePanelsChanged();
			}
			else
			{
				group.remove(this);
				group.firePanelsChanged();
			}
		});
		editPanel.add(removeButton);
		JButton groupButton = new JButton("…");
		groupButton.addActionListener((e) -> {
			if (group == null)
			{
				FilterGroupPanel newGroup = new FilterGroupPanel();
				newGroup.clear();
				newGroup.modeBox.setSelectedIndex(modeBox.getSelectedIndex());
				for (FilterPanel<?> child: children)
					newGroup.add(child);
				clear();
				add(newGroup);
			}
			else
				group.group(this);
			firePanelsChanged();
		});
		editPanel.add(groupButton);
		topPanel.add(editPanel);
		
		// Panel containing child filters
		filtersPanel = new JPanel();
		filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
		add(filtersPanel, BorderLayout.CENTER);
		
		add(new FilterSelectorPanel());
	}
	
	/**
	 * Add a new child filter.  This will extend the size of this
	 * FilterGroupPanel, but will not redo layout of the containing
	 * frame.
	 * 
	 * @param panel FilterPanel to add
	 */
	public void add(FilterPanel<?> panel)
	{
		children.add(panel);
		filtersPanel.add(panel);
		panel.group = this;
	}
	
	/**
	 * Removes the given child filter if it is in this FilterGroupPanel.
	 * This will shrink the size of this FilterGroupPanel, but will not
	 * redo layout of the containing frame.
	 * 
	 * @param panel FilterPanel to remove
	 */
	public void remove(FilterPanel<?> panel)
	{
		if (children.contains(panel))
		{
			if (panel instanceof FilterGroupPanel)
			{
				// Make this insert in place of the old group
				filtersPanel.remove(panel);
				children.remove(panel);
				for (FilterPanel<?> child: ((FilterGroupPanel)panel).children)
					add(child);
			}
			else if (children.size() > 1)
			{
				filtersPanel.remove(panel);
				children.remove(panel);
			}
		}
	}
	
	/**
	 * Clear all contents of this FilterGroupPanel.  A new filter is not
	 * replaced, and the layout of the container is not redone.
	 */
	public void clear()
	{
		children.clear();
		filtersPanel.removeAll();
		modeBox.setSelectedIndex(0);
	}
	
	/**
	 * If the given FilterPanel is a child of this FilterGroupPanel,
	 * create a new group for it and assign that group in its place.
	 * Otherwise, make it a child of this FilterGroupPanel first and
	 * do it anyway.  This does not redo layout of the container.
	 * 
	 * @param panel Panel to group.
	 */
	public void group(FilterPanel<?> panel)
	{
		if (panel.group != this)
			add(panel);
		
		int index = children.indexOf(panel);
		if (index >= 0)
		{
			filtersPanel.removeAll();
			FilterGroupPanel newGroup = new FilterGroupPanel();
			newGroup.clear();
			newGroup.add(panel);
			children.set(index, newGroup);
			newGroup.group = this;
			for (FilterPanel<?> child: children)
				filtersPanel.add(child);
		}
	}
	
	/**
	 * @return The filter represented by this FilterGroupPanel
	 * and its children.
	 */
	@Override
	public Filter filter()
	{
		FilterGroup group = new FilterGroup();
		group.mode = modeBox.getItemAt(modeBox.getSelectedIndex());
		for (FilterPanel<?> child: children)
			group.addChild(child.filter());
		return group;
	}

	/**
	 * Clear this FilterGroupPanel, then if the given Filter is
	 * a FilterGroup, fill it with its children.  Otherwise, create
	 * a FilterGroup for the given filter and then do it with that.
	 */
	@Override
	public void setContents(Filter filter)
	{
		FilterGroup group;
		clear();
		if (filter instanceof FilterGroup)
			group = (FilterGroup)filter;
		else
			group = new FilterGroup(filter);
		modeBox.setSelectedItem(group.mode);
		for (Filter child: group)
		{
			if (child instanceof FilterGroup)
			{
				FilterGroupPanel g = new FilterGroupPanel();
				g.setContents((FilterGroup)child);
				add(g);
			}
			else
			{
				FilterSelectorPanel panel = new FilterSelectorPanel();
				panel.setContents((FilterLeaf<?>)child);
				add(panel);
			}
		}
	}
}
