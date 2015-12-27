package editor.gui.filter.editor;

import javax.swing.BoxLayout;
import javax.swing.JTextField;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.TypeLineFilter;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Containment;

@SuppressWarnings("serial")
public class TypeLineFilterPanel extends FilterEditorPanel<TypeLineFilter>
{
	public static TypeLineFilterPanel create(TypeLineFilter f)
	{
		TypeLineFilterPanel panel = new TypeLineFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	private ComboBoxPanel<Containment> contain;
	private JTextField line;
	
	private TypeLineFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		line = new JTextField();
		add(line);
	}
	
	@Override
	public Filter filter()
	{
		TypeLineFilter filter = (TypeLineFilter)FilterType.TYPE_LINE.createFilter();
		filter.contain = contain.getSelectedItem();
		filter.line = line.getText();
		return filter;
	}

	@Override
	public void setContents(TypeLineFilter filter)
	{
		contain.setSelectedItem(filter.contain);
		line.setText(filter.line);
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof TypeLineFilter)
			setContents((TypeLineFilter)filter);
		else
			throw new IllegalArgumentException("Illegal type line filter " + filter.type.name());
	}
}
