package editor.gui.filter.editor;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents a panel that corresponds to a filter that matches
 * no cards.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class NoneFilterPanel extends FilterEditorPanel<FilterLeaf<?>>
{
	/**
	 * Create a new NoneFilterPanel.
	 */
	public NoneFilterPanel()
	{
		super();
		setLayout(new GridLayout(1, 1));
		setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel label = new JLabel("This clause will not match any cards.");
		add(label);
	}
	
	/**
	 * @return {@link editor.filter.leaf.FilterLeaf#NO_CARDS}
	 */
	@Override
	public Filter filter()
	{
		return FilterType.NONE.createFilter();
	}

	/**
	 * There are no contents to the corresponding filter.
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{}
}
