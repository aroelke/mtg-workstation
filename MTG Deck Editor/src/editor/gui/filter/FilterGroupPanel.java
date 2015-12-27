package editor.gui.filter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import editor.filter.Filter;
import editor.filter.FilterGroup;
import editor.filter.leaf.FilterLeaf;

@SuppressWarnings("serial")
public class FilterGroupPanel extends FilterPanel<Filter>
{
	private List<FilterPanel<?>> children;
	private JComboBox<FilterGroup.Mode> modeBox;
	private JPanel filtersPanel;
	
	public FilterGroupPanel()
	{
		super();
		setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
		setLayout(new BorderLayout());
		
		children = new ArrayList<FilterPanel<?>>();
		
		JPanel topPanel = new JPanel(new GridLayout(1, 2));
		add(topPanel, BorderLayout.NORTH);
		
		JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modeBox = new JComboBox<FilterGroup.Mode>();
		modeBox.setModel(new DefaultComboBoxModel<FilterGroup.Mode>(FilterGroup.Mode.values()));
		modePanel.add(modeBox);
		topPanel.add(modePanel);
		
		JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton addButton = new JButton("+");
		addButton.addActionListener((e) -> {
			add(new FilterSelectorPanel());
			SwingUtilities.getWindowAncestor(this).pack();
		});
		editPanel.add(addButton);
		JButton removeButton = new JButton("−");
		removeButton.addActionListener((e) -> {
			if (group == null)
			{
				clear();
				add(new FilterSelectorPanel());
				SwingUtilities.getWindowAncestor(this).pack();
			}
			else
			{
				group.remove(this);
				SwingUtilities.getWindowAncestor(group).pack();
			}
		});
		editPanel.add(removeButton);
		JButton groupButton = new JButton("…");
		groupButton.addActionListener((e) -> {
			if (group == null)
			{
				FilterGroupPanel newGroup = new FilterGroupPanel();
				newGroup.modeBox.setSelectedIndex(modeBox.getSelectedIndex());
				for (FilterPanel<?> child: children)
					newGroup.add(child);
				clear();
				add(newGroup);
			}
			else
				group.group(this);
			SwingUtilities.getWindowAncestor(this).pack();
		});
		editPanel.add(groupButton);
		topPanel.add(editPanel);
		
		filtersPanel = new JPanel();
		filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
		add(filtersPanel, BorderLayout.CENTER);
		
		add(new FilterSelectorPanel());
	}
	
	public void add(FilterPanel<?> panel)
	{
		children.add(panel);
		filtersPanel.add(panel);
		panel.group = this;
	}
	
	public void remove(FilterPanel<?> panel)
	{
		if (children.contains(panel))
		{
			if (children.size() > 1)
			{
				filtersPanel.remove(panel);
				children.remove(panel);
			}
			else if (panel instanceof FilterGroupPanel)
			{
				filtersPanel.remove(panel);
				children.remove(panel);
				for (FilterPanel<?> child: ((FilterGroupPanel)panel).children)
					add(child);
			}
		}
	}
	
	public void clear()
	{
		children.clear();
		filtersPanel.removeAll();
		modeBox.setSelectedIndex(0);
	}
	
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
			for (FilterPanel<?> child: children)
				filtersPanel.add(child);
		}
	}
	
	@Override
	public Filter filter()
	{
		FilterGroup group = new FilterGroup();
		group.mode = modeBox.getItemAt(modeBox.getSelectedIndex());
		for (FilterPanel<?> child: children)
			group.addChild(child.filter());
		return group;
	}

	@Override
	public void setContents(Filter filter)
	{
		FilterGroup group;
		clear();
		if (filter instanceof FilterGroup)
			group = (FilterGroup)filter;
		else
		{
			group = new FilterGroup();
			group.addChild(filter);
		}
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
