package editor.gui.filter.editor;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.TextFilter;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Containment;

@SuppressWarnings("serial")
public class TextFilterPanel extends FilterEditorPanel<TextFilter>
{
	public static TextFilterPanel create(TextFilter f)
	{
		TextFilterPanel panel = new TextFilterPanel();
		panel.setContents(f);
		return panel;
	}
	
	private FilterType type;
	private ComboBoxPanel<Containment> contain;
	private JTextField text;
	private JCheckBox regex;
	
	private TextFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);
		
		text = new JTextField();
		add(text);
		
		regex = new JCheckBox("regex");
		add(regex);
	}
	
	@Override
	public Filter filter()
	{
		TextFilter filter = (TextFilter)type.createFilter();
		filter.contain = contain.getSelectedItem();
		filter.text = text.getText();
		filter.regex = regex.isSelected();
		return filter;
	}

	@Override
	public void setContents(TextFilter filter)
	{
		type = filter.type;
		contain.setSelectedItem(filter.contain);
		text.setText(filter.text);
		regex.setSelected(filter.regex);
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof TextFilter)
			setContents((TextFilter)filter);
		else
			throw new IllegalArgumentException("Illegal text filter " + filter.type.name());
	}
}
