package editor.gui.filter.editor;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.VariableNumberFilter;
import editor.gui.generic.ComboBoxPanel;
import editor.util.Comparison;

/**
 * This class represents a panel that corresponds to a filter that groups cards
 * by a numeric characteristic that can also be variable.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class VariableNumberFilterPanel extends FilterEditorPanel<VariableNumberFilter>
{
    /**
     * Combo box presenting comparison options.
     */
    private ComboBoxPanel<Comparison> comparison;
    /**
     * Spinner allowing the user to choose a value to compare with.
     */
    private JSpinner spinner;
    /**
     * Type of filter this VariableNumberFilterPanel edits.
     */
    private String type;
    /**
     * Check box specifying whether the characteristic should be variable
     * or not.
     */
    private JCheckBox variable;

    /**
     * Create a new VariableNumberFilterPanel.
     */
    public VariableNumberFilterPanel(String v)
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        comparison = new ComboBoxPanel<>(Comparison.values());
        add(comparison);

        spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(0.0, 0.0, null, 1.0));
        spinner.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
        add(spinner);

        variable = new JCheckBox("Contains " + v);
        variable.addActionListener((e) -> spinner.setEnabled(!variable.isSelected()));
        add(variable);
    }

    /**
     * Create a new VariableNumberFilterPanel, using the given
     * VariableNumberFilter to initialize its fields.
     *
     * @param f filter to use for initialization
     */
    public VariableNumberFilterPanel(VariableNumberFilter f)
    {
        this(f.type().equals(FilterFactory.LOYALTY) ? "X" : "*");
        setContents(f);
    }

    @Override
    public Filter filter()
    {
        VariableNumberFilter filter = (VariableNumberFilter)FilterFactory.createFilter(type);
        filter.operation = comparison.getSelectedItem();
        filter.operand = (double)spinner.getValue();
        filter.varies = variable.isSelected();
        return filter;
    }

    @Override
    public void setContents(FilterLeaf<?> filter) throws IllegalArgumentException
    {
        if (filter instanceof VariableNumberFilter)
            setContents((VariableNumberFilter)filter);
        else
            throw new IllegalArgumentException("Illegal variable number filter " + filter.type());
    }

    @Override
    public void setContents(VariableNumberFilter filter)
    {
        type = filter.type();
        comparison.setSelectedItem(filter.operation);
        spinner.setValue(filter.operand);
        variable.setSelected(filter.varies);
        spinner.setEnabled(!filter.varies);
        comparison.setEnabled(!filter.varies);
    }
}
