package editor.gui.filter.editor;

import java.awt.GridLayout;
import java.util.function.Predicate;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import editor.database.Card;
import editor.gui.filter.FilterType;

/**
 * This class represents a filter panel that provides a filter that
 * matches no cards.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class EmptyFilterPanel extends FilterEditorPanel
{
	/**
	 * Create a new EmptyFilterPanel.
	 */
	public EmptyFilterPanel()
	{
		super(FilterType.NONE);
		setLayout(new GridLayout(1, 1));
		setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel label = new JLabel("This clause will not match any cards.");
		add(label);
	}
	
	/**
	 * There are no contents to an EmptyFilterPanel, so no contents can be
	 * set.
	 * 
	 * @param content Unused
	 */
	@Override
	public void setContents(String content)
	{}

	/**
	 * @return A Predicate<Card> that filters out all cards (it returns false
	 * for every card).
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		return (c) -> false;
	}

	/**
	 * @return <code>false</code>, since this panel has no contents and
	 * hence cannot be empty.
	 */
	@Override
	public boolean isEmpty()
	{
		return false;
	}

	/**
	 * @return The String representation of this EmptyFilter's contents,
	 * which is an empty String.
	 */
	@Override
	protected String repr()
	{
		return "";
	}
}
