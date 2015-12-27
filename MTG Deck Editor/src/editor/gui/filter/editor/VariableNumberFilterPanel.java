package editor.gui.filter.editor;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.VariableNumberFilter;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Comparison;

@SuppressWarnings("serial")
public class VariableNumberFilterPanel extends FilterEditorPanel<VariableNumberFilter>
{
	public static VariableNumberFilterPanel create(VariableNumberFilter f)
	{
		VariableNumberFilterPanel panel = new VariableNumberFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	private FilterType type;
	private ComboBoxPanel<Comparison> comparison;
	private JSpinner spinner;
	private JCheckBox variable;
	
	public VariableNumberFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		comparison = new ComboBoxPanel<Comparison>(Comparison.values());
		add(comparison);
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0.0, 0.0, null, 1.0));
		spinner.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
		add(spinner);
		
		variable = new JCheckBox("Contains *");
		add(variable);
	}

	@Override
	public Filter filter()
	{
		VariableNumberFilter filter = (VariableNumberFilter)type.createFilter();
		filter.compare = comparison.getSelectedItem();
		filter.operand = (double)spinner.getValue();
		filter.varies = variable.isSelected();
		return filter;
	}

	@Override
	public void setContents(VariableNumberFilter filter)
	{
		type = filter.type;
		comparison.setSelectedItem(filter.compare);
		spinner.setValue(filter.operand);
		variable.setSelected(filter.varies);
		spinner.setEnabled(!filter.varies);
		comparison.setEnabled(!filter.varies);
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof VariableNumberFilter)
			setContents((VariableNumberFilter)filter);
		else
			throw new IllegalArgumentException("Illegal variable number filter " + filter.type.name());
	}
}
