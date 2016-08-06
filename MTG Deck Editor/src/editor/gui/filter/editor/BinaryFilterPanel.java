package editor.gui.filter.editor;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents a panel that corresponds to a filter that
 * returns <code>true</code> or <code>false</code> for all cards.
 * There are no fields to edit.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class BinaryFilterPanel extends FilterEditorPanel<FilterLeaf<?>>
{
	/**
	 * String to display for letting all cards through the filter.
	 */
	private static final String ALL = "This clause will match every card.";
	/**
	 * String to display for letting no cards through the filter.
	 */
	private static final String NONE = "This clause will not match any card.";
	
	/**
	 * Whether or not to let cards through the filter.
	 */
	private boolean through;
	
	/**
	 * Create a new BinaryFilterPanel.
	 * 
	 * @param let Whether or not cards should pass through the filter
	 */
	public BinaryFilterPanel(boolean let)
	{
		super();
		setLayout(new GridLayout(1, 1));
		setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel label = new JLabel((through = let) ? ALL : NONE);
		add(label);
	}
	
	/**
	 * @return A Filter that either filters all cards, or no cards, depending
	 * on how this BinaryFilterPanel was created.
	 */
	@Override
	public Filter filter()
	{
		return FilterFactory.createFilter(through ? FilterType.ALL : FilterType.NONE);
	}

	/**
	 * There are no contents to set, so do nothing.
	 * 
	 * @param filter Filter to use
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{}
}
