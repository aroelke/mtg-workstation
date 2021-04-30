package editor.gui.generic;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.text.ParseException;
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
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class SpinnerCellEditor extends DefaultCellEditor
{
    /**
     * Spinner that allows editing the value.
     */
    private JSpinner spinner;

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

    /**
     * {@inheritDoc}
     * Double-clicking activates editing.
     */
    @Override
    public boolean isCellEditable(EventObject eo)
    {
        return eo instanceof MouseEvent m && m.getClickCount() > 1;
    }

    @Override
    public boolean stopCellEditing()
    {
        try
        {
            spinner.commitEdit();
        }
        catch (ParseException e)
        {}
        return super.stopCellEditing();
    }
}
