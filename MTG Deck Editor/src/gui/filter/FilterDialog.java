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
 * TODO: Try to make the OK button keep working with the enter key
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class FilterDialog extends JDialog
{
	/**
	 * This FilterDialog's filter.
	 */
	private FilterGroup filter;
	
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
		
		setContentPane(new JPanel(new BorderLayout()));
		getContentPane().add(filter = new FilterGroup(this), BorderLayout.CENTER);
	}
	
	/**
	 * Reset this FilterDialog back to its initial configuration.
	 */
	public void reset()
	{
		getContentPane().remove(filter);
		getContentPane().add(filter = new FilterGroup(this), BorderLayout.CENTER);
		pack();
	}
	
	/**
	 * Parse a String for filters and create the appropriate filter panels and set their values,
	 * but don't show the dialog.  Follow this call with a command to show the dialog to actually
	 * see the result.
	 * 
	 * The string should look like <> with either AND or OR inside followed by filters which are
	 * also surrounded by <>.  They may also start with AND or OR which are followed by filters.
	 * 
	 * @param s
	 */
	public void initializeFromString(String s)
	{
		filter.setContents(s);
	}
	
	public Predicate<Card> getFilter()
	{
		return filter.getFilter();
	}
	
	@Override
	public String toString()
	{
		return filter.toString();
	}
}
