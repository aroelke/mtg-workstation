package util;

import java.awt.Color;
import java.awt.Component;
import java.text.ParseException;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;

/**
 * TODO: Comment this
 * 
 * @author 
 */
@SuppressWarnings("serial")
public class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private JSpinner spinner;
	
	public SpinnerCellEditor()
	{
		spinner = new JSpinner();
		spinner.setBorder(null);
	}
	
	@Override
	public Object getCellEditorValue()
	{
		return spinner.getValue();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		spinner.setValue(value);
		return spinner;
	}
	
	@Override
	public boolean isCellEditable(EventObject evt)
	{
		return true;
	}
	
	@Override
	public boolean stopCellEditing()
	{
		spinner.setBorder(null);
		try
		{
			spinner.commitEdit();
			return super.stopCellEditing();
		}
		catch (ParseException e)
		{
			spinner.setBorder(new LineBorder(Color.RED));
			spinner.repaint();
			return false;
		}
	}
}
