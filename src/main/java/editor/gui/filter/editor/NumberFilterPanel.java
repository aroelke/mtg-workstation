package editor.gui.filter.editor;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import editor.database.attributes.CardAttribute;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.NumberFilter;
import editor.gui.generic.ComboBoxPanel;
import editor.util.Comparison;

/**
 * This class represents a panel that corresponds to a filter that groups
 * cards by a numeric characteristic.
 *
 * @author Alec Roelke
 */
public class NumberFilterPanel extends FilterEditorPanel<NumberFilter>
{
    /**
     * Comparison for the operand of the filter.
     */
    private ComboBoxPanel<Comparison> comparison;
    /**
     * Spinner for the operand of the filter.
     */
    private JSpinner spinner;
    /**
     * Type of the filter this panel edits.
     */
    private CardAttribute type;

    /**
     * Create a new NumberFilterPanel.
     */
    public NumberFilterPanel()
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Combo box for choosing the type of comparison to make
        comparison = new ComboBoxPanel<>(Comparison.values());
        add(comparison);

        // Value to compare the characteristic against
        spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(0.0, 0.0, null, 1.0));
        spinner.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
        add(spinner);
    }

    /**
     * Create a new NumberFilterPanel and initialize its fields according
     * to the contents of the given NumberFilter.
     *
     * @param f filter to use for initialization
     */
    public NumberFilterPanel(NumberFilter f)
    {
        this();
        setContents(f);
    }

    @Override
    public Filter filter()
    {
        NumberFilter filter = (NumberFilter)CardAttribute.createFilter(type);
        filter.operation = comparison.getSelectedItem();
        filter.operand = (double)spinner.getValue();
        return filter;
    }

    @Override
    public void setContents(FilterLeaf<?> filter) throws IllegalArgumentException
    {
        if (filter instanceof NumberFilter f)
            setContents(f);
        else
            throw new IllegalArgumentException("Illegal number filter " + filter.type());
    }

    @Override
    public void setContents(NumberFilter filter)
    {
        type = filter.type();
        comparison.setSelectedItem(filter.operation);
        spinner.setValue(filter.operand);
    }
}
