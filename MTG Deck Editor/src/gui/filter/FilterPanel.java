package gui.filter;

import javax.swing.JPanel;

/**
 * This class represents a JPanel that can create a filter for a list of Cards.  It can
 * also set its filter based on a content String.  Typically each characteristic of a card
 * can be represented by a <code>Function<Card, ?></code>.
 * 
 * @author Alec Roelke
 * @see database.Card
 */
@SuppressWarnings("serial")
public abstract class FilterPanel extends JPanel
{
	/**
	 * Create a new FilterPanel.  The appearance of a FilterPanel is filter-specific.
	 */
	public FilterPanel()
	{
		super();
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
	 * @see gui.filter.FilterPanel#toString()
	 */
	public abstract void setContent(String content);
	
	/**
	 * @return A Predicate representing the filter this FilterPanel has created.
	 * @see java.util.function.Predicate
	 */
	public abstract CardFilter getFilter();
	
	/**
	 * @return <code>true</code> if this FilterPanel has no data entered in it, and
	 * <code>false</code> otherwise.  Some filter panels have valid values even when
	 * no changes have been made, so they will always return <code>true</code>.
	 */
	public abstract boolean isEmpty();
	
	/**
	 * TODO: Comment this
	 * TODO: Store the filters' codes in this class in a protected variable
	 * @return
	 */
	public abstract String repr();
	
	/**
	 * TODO: Comment this
	 */
	@Override
	public String toString()
	{
		return "<" + repr() + ">";
	}
}
