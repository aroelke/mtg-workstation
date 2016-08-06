package editor.gui.filter.editor;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.FilterType;
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
@SuppressWarnings("serial")
public class NumberFilterPanel extends FilterEditorPanel<NumberFilter>
{
	/**
	 * Type of the filter this panel edits.
	 */
	private FilterType type;
	/**
	 * Comparison for the operand of the filter.
	 */
	private ComboBoxPanel<Comparison> comparison;
	/**
	 * Spinner for the operand of the filter.
	 */
	private JSpinner spinner;
	
	/**
	 * Create a new NumberFilterPanel.
	 */
	public NumberFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		// Combo box for choosing the type of comparison to make
		comparison = new ComboBoxPanel<Comparison>(Comparison.values());
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
	 * @param f Filter to use for initialization
	 */
	public NumberFilterPanel(NumberFilter f)
	{
		this();
		setContents(f);
	}
	
	/**
	 * @return The NumberFilter corresponding to the values of this
	 * NumberFilterPanel's fields.
	 */
	@Override
	public Filter filter()
	{
		NumberFilter filter = (NumberFilter)FilterFactory.createFilter(type);
		filter.compare = comparison.getSelectedItem();
		filter.operand = (double)spinner.getValue();
		return filter;
	}

	/**
	 * Set the contents of this NumberFilterPanel's fields according to
	 * the contents of the given NumberFilter.
	 * 
	 * @param filter Filter to use for setting fields
	 */
	@Override
	public void setContents(NumberFilter filter)
	{
		type = filter.type;
		comparison.setSelectedItem(filter.compare);
		spinner.setValue(filter.operand);
	}

	/**
	 * Set the contents of this NumberFilterPanel's fields according to
	 * the contents of the given FilterLeaf.
	 * 
	 * @param filter Filter to use for setting fields
	 * @throws IllegalArgumentException if the given filter is not a
	 * NumberFilter.
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof NumberFilter)
			setContents((NumberFilter)filter);
		else
			throw new IllegalArgumentException("Illegal number filter " + filter.type.name());
	}
}
