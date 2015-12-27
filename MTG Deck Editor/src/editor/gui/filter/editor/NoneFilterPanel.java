package editor.gui.filter.editor;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;

@SuppressWarnings("serial")
public class NoneFilterPanel extends FilterEditorPanel<FilterLeaf<?>>
{
	public static NoneFilterPanel create()
	{
		return new NoneFilterPanel();
	}
	
	private NoneFilterPanel()
	{
		super();
		setLayout(new GridLayout(1, 1));
		setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel label = new JLabel("This clause will not match any cards.");
		add(label);
	}
	
	@Override
	public Filter filter()
	{
		return FilterLeaf.NO_CARDS;
	}

	@Override
	public void setContents(FilterLeaf<?> filter)
	{}
}
