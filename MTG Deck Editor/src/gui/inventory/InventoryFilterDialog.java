package gui.inventory;

import gui.filter.FilterDialog;
import gui.filter.FilterGroup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
	 * Top-level group representing the entire filter.
	 */
	private FilterGroup filter;
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
		
		OK = false;
		
		// Initialize the content panel, which contains the filter panels
		getContentPane().add(filter = new FilterGroup(this), BorderLayout.CENTER);
		
		// Panel containing OK and cancel buttons
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(closePanel, BorderLayout.SOUTH);
		
		// OK button
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new OKListener());
		closePanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		// Cancel button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {setVisible(false); dispose();});
		closePanel.add(cancelButton);
		
		pack();
		
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
	 * Reset this InventoryFilterDialog to its initial state, with only one filter
	 * set to a name filter.
	 */
	@Override
	public void reset()
	{
		getContentPane().remove(filter);
		getContentPane().add(filter = new FilterGroup(this), BorderLayout.CENTER);
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
			System.out.println(filter.toString());
			return filter.getFilter();
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
			dispose();
		}
	}
}
