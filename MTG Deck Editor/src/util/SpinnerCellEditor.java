package util;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * TODO: Comment this
 * 
 * @author
 */
@SuppressWarnings("serial")
public class SpinnerCellEditor extends DefaultCellEditor
{
	JSpinner spinner;

	// Initializes the spinner.
	public SpinnerCellEditor()
	{
		super(new JTextField());
		spinner = new JSpinner(new SpinnerNumberModel());
		((JSpinner.NumberEditor)spinner.getEditor()).getTextField().addActionListener((e) -> stopCellEditing());
	}

	// Prepares the spinner component and returns it.
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		spinner.setValue(value);
		return spinner;
	}

	// TODO: Decide if this should activate on single or double click
	// (remove for double, uncomment for single)
//	@Override
//	public boolean isCellEditable(EventObject eo)
//	{
//		return true;
//	}

	// Returns the spinners current value.
	@Override
	public Object getCellEditorValue()
	{
		return spinner.getValue();
	}

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
