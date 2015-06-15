package gui.filter;

import java.awt.Dialog;
import java.awt.Window;

import javax.swing.JDialog;

/**
 * This class represents a dialog for editing a filter.
 * 
 * TODO: Take more advantage of polymorphism
 * TODO: Create a class for filters that takes a predicate and a string representation
 * TOOD: Add functionality to create more complex expressions
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class FilterDialog extends JDialog
{
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
	}
	
	/**
	 * Reset this FilterDialog back to its initial configuration.
	 */
	public abstract void reset();
}
