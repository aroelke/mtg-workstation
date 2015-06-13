package gui.inventory;

import gui.filter.FilterContainer;
import gui.filter.FilterDialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import database.Card;

/**
 * This class represents a dialog box that creates a filter for the Card inventory.
 * It is persistent across openings, so if a filter is set and the advanced filter is
 * opened again, its settings will remain.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InventoryFilterDialog extends FilterDialog
{
	/**
	 * Panel containing filter panels.
	 */
	private JPanel contentPanel;
	/**
	 * Panels that each add one term to the filter.
	 */
	private List<FilterContainer> filters;
	/**
	 * OK button.
	 */
	private boolean OK;
	
	/**
	 * Create a new InventoryFilterDialog.
	 * 
	 * @param owner Owner component of the dialog.
	 */
	public InventoryFilterDialog(JFrame owner)
	{
		super(owner, "Advanced Filter");
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		OK = false;
		
		// Initialize the content panel, which contains the filter panels
		contentPanel = new JPanel();
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		// Panel containing close buttons
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Panel containing the add new filter panel button
		JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(addPanel);
		
		// Add new filter panel button
		JButton addButton = new JButton("Add");
		addButton.addActionListener((e) -> addFilterPanel());
		addPanel.add(addButton);
		
		// Panel containing OK and cancel buttons
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(closePanel);
		
		// OK button
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new OKListener());
		closePanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		// Cancel button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {setVisible(false); dispose();});
		closePanel.add(cancelButton);
		
		// Filter panels
		filters = new ArrayList<FilterContainer>();
		reset();
		
		// When the window closes, rather than deleting it, reset it and make it invisible
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				reset();
				setVisible(false);
				dispose();
			}
		});
	}
	
	/**
	 * Add a new filter panel to the content panel, which starts with a name filter.
	 * 
	 * @return The filter panel that was added.
	 */
	@Override
	public FilterContainer addFilterPanel()
	{
		FilterContainer filter = new FilterContainer(this);
		filters.add(filter);
		contentPanel.add(filter);
		pack();
		return filter;
	}
	
	/**
	 * Remove the specified filter panel from the content panel.
	 * 
	 * @param panel Filter panel to remove
	 * @return <code>true</code> if the panel was successfully removed and <code>false</code>
	 * otherwise.
	 */
	@Override
	public boolean removeFilterPanel(FilterContainer panel)
	{
		if (filters.size() > 1 && filters.contains(panel))
		{
			filters.remove(panel);
			contentPanel.remove(panel);
			filters.get(0).alwaysAnd(true);
			pack();
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Reset this InventoryFilterDialog to its initial state, with only one filter
	 * set to a name filter.
	 */
	public void reset()
	{
		filters.clear();
		contentPanel.removeAll();
		addFilterPanel();
		filters.get(0).alwaysAnd(true);
	}
	
	/**
	 * Remove empty filters to prepare for when this InventoryFilterDialog is reopened.
	 */
	public void clean()
	{
		for (FilterContainer filter: new ArrayList<FilterContainer>(filters))
		{
			if (filter.isEmpty())
			{
				filters.remove(filter);
				contentPanel.remove(filter);
			}
		}
		if (filters.isEmpty())
			addFilterPanel();
		pack();
	}
	
	/**
	 * Show this InventoryFilterDialog, allow for editing it, and then return the composed
	 * filter that was created.
	 * 
	 * @return A <code>Predicate<Card></code> representing the filter composed from each
	 * filter panel.
	 */
	public Predicate<Card> getFilter()
	{
		OK = false;
		setVisible(true);
		if (!OK)
			return null;
		else
		{
			Predicate<Card> composedFilter = (c) -> true;
			for (FilterContainer filter: filters)
			{
				if (filter.isAnd())
					composedFilter = composedFilter.and(filter.getFilter());
				else
					composedFilter = composedFilter.or(filter.getFilter());
			}
			return composedFilter;
		}
	}
	
	/**
	 * This class represents the action that should be taken when the OK button is pressed,
	 * which is to close the frame and set it to return the filter that was created.
	 * 
	 * @author Alec Roelke
	 */
	private class OKListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			OK = true;
			setVisible(false);
			clean();
			dispose();
		}
	}
}
