package editor.gui.filter.editor;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.NumberFilter;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Comparison;

@SuppressWarnings("serial")
public class NumberFilterPanel extends FilterEditorPanel<NumberFilter>
{
	public static NumberFilterPanel create(NumberFilter f)
	{
		NumberFilterPanel panel = new NumberFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	private FilterType type;
	private ComboBoxPanel<Comparison> comparison;
	private JSpinner spinner;
	
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
	
	@Override
	public Filter filter()
	{
		NumberFilter filter = (NumberFilter)type.createFilter();
		filter.compare = comparison.getSelectedItem();
		filter.operand = (double)spinner.getValue();
		return filter;
	}

	@Override
	public void setContents(NumberFilter filter)
	{
		type = filter.type;
		comparison.setSelectedItem(filter.compare);
		spinner.setValue(filter.operand);
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof NumberFilter)
			setContents((NumberFilter)filter);
		else
			throw new IllegalArgumentException("Illegal number filter " + filter.type.name());
	}
}
