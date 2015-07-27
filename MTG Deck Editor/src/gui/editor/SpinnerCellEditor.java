package gui.editor;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * This class represents an editor for a table cell containing integers.  It can
 * be edited using either the keyboard or the spinner arrows that appear on a double
 * click.
 * 
 * @author
 */
@SuppressWarnings("serial")
public class SpinnerCellEditor extends DefaultCellEditor
{
	/**
	 * Spinner that allows editing the value.
	 */
	JSpinner spinner;

	/**
	 * Create a new SpinnerCellEditor.
	 */
	public SpinnerCellEditor()
	{
		super(new JTextField());
		spinner = new JSpinner(new SpinnerNumberModel());
		// This line is necessary so the spinner goes away after the first press of the enter key
		((JSpinner.NumberEditor)spinner.getEditor()).getTextField().addActionListener((e) -> stopCellEditing());
	}

	/**
	 * Set the value of the editor component and return it.
	 * 
	 * @param table Table containing the cell to edit
	 * @param value Value to set the editor component to
	 * @param isSelected Whether or not the cell is selected
	 * @param row Row of the cell being edited
	 * @param column Column of the cell being edited
	 * @return The editor component.
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		spinner.setValue(value);
		return spinner;
	}

	/**
	 * @return <code>true</code>, if the user double-clicked on the cell, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isCellEditable(EventObject eo)
	{
		if (eo instanceof MouseEvent && ((MouseEvent)eo).getClickCount() > 1)
			return true;
		else
			return false;
	}

	/**
	 * @return The value of the spinner.
	 */
	@Override
	public Object getCellEditorValue()
	{
		return spinner.getValue();
	}

	/**
	 * Commit the value of the spinner and then stop editing.
	 * 
	 * @return <code>true</code>, since the cell is finished editing.
	 */
	@Override
	public boolean stopCellEditing()
	{
		try
		{
			spinner.commitEdit();
		}
		catch (java.text.ParseException e)
		{}
		return super.stopCellEditing();
	}
}
