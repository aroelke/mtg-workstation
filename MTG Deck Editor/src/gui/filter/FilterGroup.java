package gui.filter;

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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * TODO: Complete and comment this class
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class FilterGroup extends FilterPanel
{
	public static List<FilterPanel> getFilters(String s)
	{
		List<FilterPanel> panels = new ArrayList<FilterPanel>();
		return panels;
	}
	
	private FilterDialog parent;
	private JComboBox<Mode> modeBox;
	private JPanel filtersPanel;
	private List<FilterPanel> filters;

	public FilterGroup(FilterDialog p)
	{
		super(null);
		parent = p;
		init(new FilterTypePanel(this));
	}
	
	public FilterGroup(FilterGroup g)
	{
		super(g);
		parent = null;
		init(new FilterTypePanel(this));
	}
	
	public FilterGroup(FilterDialog p, FilterPanel panel)
	{
		super(null);
		parent = p;
		init(panel);
	}

	public FilterGroup(FilterGroup g, FilterPanel panel)
	{
		super(g);
		parent = null;
		init(panel);
	}
	
	private void init(FilterPanel panel)
	{
		setBorder(new CompoundBorder(new EmptyBorder(5, 0, 10, 5), new EtchedBorder()));
		setLayout(new BorderLayout());
		
		filters = new ArrayList<FilterPanel>();
		
		JPanel topPanel = new JPanel(new GridLayout(1, 2));
		add(topPanel, BorderLayout.NORTH);
		
		JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modeBox = new JComboBox<Mode>();
		modeBox.setModel(new DefaultComboBoxModel<Mode>(Mode.values()));
		modePanel.add(modeBox);
		topPanel.add(modePanel);
		
		JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton addButton = new JButton("+");
		addButton.addActionListener((e) -> addFilterPanel(new FilterTypePanel(this)));
		editPanel.add(addButton);
		JButton removeButton = new JButton("\u2013");
		removeButton.addActionListener((e) -> {
			if (parent != null)
				parent.reset();
			else
				getGroup().removeFilterPanel(this);
		});
		editPanel.add(removeButton);
		topPanel.add(editPanel);
		
		filtersPanel = new JPanel();
		filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
		addFilterPanel(panel);
		add(filtersPanel, BorderLayout.CENTER);
	}
	
	public void pack()
	{
		if (parent != null)
			parent.pack();
		else
			getGroup().pack();
	}
	
	public void addFilterPanel(FilterPanel panel)
	{
		filtersPanel.add(panel);
		filters.add(panel);
		panel.setGroup(this);
		revalidate();
		repaint();
		pack();
	}
	
	public void removeFilterPanel(FilterPanel panel)
	{
		if (filters.size() > 1 && filters.contains(panel))
		{
			filters.remove(panel);
			filtersPanel.remove(panel);
			revalidate();
			repaint();
			pack();
		}
	}
	
	public void groupFilterPanel(FilterPanel panel)
	{
		if (filters.contains(panel))
		{
			filters.remove(panel);
			filtersPanel.remove(panel);
			addFilterPanel(new FilterGroup(this, panel));
			revalidate();
			repaint();
			pack();
		}
	}
	
	@Override
	public CardFilter getFilter()
	{
		CardFilter f = filters.get(0).getFilter();
		switch (modeBox.getItemAt(modeBox.getSelectedIndex()))
		{
		case AND:
			for (int i = 1; i < filters.size(); i++)
				f = f.and(filters.get(i).getFilter());
			return f;
		case OR:
			for (int i = 1; i < filters.size(); i++)
				f = f.or(filters.get(i).getFilter());
			return f;
		default:
			// TODO: Throw/display an error (although this should never happen)
			return null;
		}
	}
	
	@Override
	public void setContents(String contents)
	{
		// TODO: Implement this
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	private enum Mode
	{
		AND("all of"),
		OR("any of");
		
		private final String mode;
		
		private Mode(String m)
		{
			mode = m;
		}
		
		@Override
		public String toString()
		{
			return mode;
		}
	}
}
