package editor.gui.filter.editor;

import java.util.function.Predicate;

import javax.swing.JPanel;

import editor.database.Card;
import editor.gui.filter.FilterType;

/**
 * This class represents a JPanel that can create a filter for a list of Cards.  It can
 * also set its filter based on a content String.  Typically each characteristic of a card
 * can be represented by a <code>Function<Card, ?></code>.
 * 
 * @author Alec Roelke
 * @see editor.database.Card
 */
@SuppressWarnings("serial")
public abstract class FilterEditorPanel extends JPanel
{
	/**
	 * The type of filter this FilterEditorPanel edits.
	 */
	private FilterType type;
	
	/**
	 * Create a new FilterPanel.  The appearance of a FilterPanel is filter-specific.
	 * 
	 * @param t The type of filter the new FilterEditorPanel edits.
	 */
	public FilterEditorPanel(FilterType t)
	{
		super();
		type = t;
	}
	
	/**
	 * Set the content of this FilterPanel.  This String has the same format as one
	 * returned by toString(), which is filter-specific.  It will always start with
	 * the filter's "code," which is one or more characters identifying the filter,
	 * followed by a string which the filter will read to determine how to set its
	 * content.  The actual string that is passed here is just the content string;
	 * the code is used to determine which kind of filter panel to pass the string
	 * to.
	 * 
	 * @param content String to parse content from
	 * @see editor.gui.filter.editor.FilterEditorPanel#toString()
	 */
	public abstract void setContents(String content);
	
	/**
	 * @return A Predicate representing the filter this FilterPanel has created.
	 * @see java.util.function.Predicate
	 */
	public abstract Predicate<Card> getFilter();
	
	/**
	 * @return <code>true</code> if this FilterPanel has no data entered in it, and
	 * <code>false</code> otherwise.  Some filter panels have valid values even when
	 * no changes have been made, so they will always return <code>true</code>.
	 */
	public abstract boolean isEmpty();
	
	/**
	 * @return A String representation of the contents of this FilterEditorPanel.
	 */
	protected abstract String repr();
	
	/**
	 * @return A String representation of this FilterEditorPanel, which is its code
	 * followed by a colon followed by its contents.
	 */
	@Override
	public String toString()
	{
		return type.code + ":" + repr();
	}
}
