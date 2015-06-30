package gui.filter;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;
import java.util.function.Predicate;

import javax.swing.JDialog;
import javax.swing.JPanel;

import database.Card;

/**
 * This class represents a dialog for editing a filter.
 * 
 * TODO: Refactor code so that the dialog showing part is taken care of JOptionPane rather than a custom dialog
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class FilterDialog extends JDialog
{
	/**
	 * This FilterDialog's filter.
	 */
	private FilterGroupPanel filter;
	
	/**
	 * Create a new FilterDialog
	 * 
	 * @param window Parent window of the dialog.
	 * @param title Title of the dialog.
	 */
	public FilterDialog(Window frame, String title)
	{
		super(frame, title, Dialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		// The center of the dialog will always be the top-level filter group
		// Don't put anything in the center!
		setContentPane(new JPanel(new BorderLayout()));
		getContentPane().add(filter = new FilterGroupPanel(this), BorderLayout.CENTER);
	}
	
	/**
	 * Reset this FilterDialog back to its initial configuration.
	 */
	public void reset()
	{
		getContentPane().remove(filter);
		getContentPane().add(filter = new FilterGroupPanel(this), BorderLayout.CENTER);
		pack();
	}
	
	public void groupFilterPanel()
	{
		FilterGroupPanel newFilter = new FilterGroupPanel(this);
		newFilter.addFilterPanel(filter);
		getContentPane().remove(filter);
		getContentPane().add(filter = newFilter, BorderLayout.CENTER);
		pack();
	}
	
	/**
	 * Parse a String for filters and create the appropriate filter panels and set their values,
	 * but don't show the dialog.  Follow this call with a command to show the dialog to actually
	 * see the result.
	 * 
	 * The string should look like group enclosures with either AND or OR inside followed by filters which are
	 * also surrounded by group enclosures.  They may also start with AND or OR which are followed by filters.
	 * 
	 * @param s
	 * @see FilterGroupPanel#BEGIN_GROUP
	 * @see FilterGroupPanel#END_GROUP
	 * @see FilterGroupPanel#setContents(String)
	 */
	public void setContents(String s)
	{
		filter.setContents(s);
	}
	
	/**
	 * @return A <code>Predicate<Card></code> representing the filter built.
	 */
	public Predicate<Card> filter()
	{
		return filter.filter();
	}
	
	/**
	 * @return A String representation of this FilterDialog.
	 */
	@Override
	public String toString()
	{
		return filter.toString();
	}
}
