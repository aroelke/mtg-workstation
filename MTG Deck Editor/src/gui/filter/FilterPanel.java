package gui.filter;

import java.util.function.Predicate;

import javax.swing.JPanel;

import database.Card;

/**
 * This class represents a panel that can create a filter for a list of cards.  The
 * filter can be comprised of a group of filters that are all ANDed or ORed together,
 * each of which can be a sub-group as well (recursing infinitely).  All FilterPanels
 * belong to a FilterGroup, so a dialog for creating a filter need only display a
 * single FilterGroup.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class FilterPanel extends JPanel
{
	/**
	 * The FilterGroup this FilterPanel belongs to.
	 */
	private FilterGroup group;
	
	/**
	 * Create a new FilterPanel.
	 * 
	 * @param g FilterGroup the new panel should belong to.
	 */
	public FilterPanel(FilterGroup g)
	{
		super();
		setGroup(g);
	}
	
	/**
	 * @return The FilterGroup this FilterPanel belongs to.
	 */
	public FilterGroup getGroup()
	{
		return group;
	}
	
	/**
	 * Reassign the FilterGroup this FiltePanel belongs to.
	 * 
	 * @param g The new FilterGroup
	 */
	public void setGroup(FilterGroup g)
	{
		group = g;
	}
	
	/**
	 * @return The CardFilter created from this FilterPanel's contents.
	 */
	public abstract Predicate<Card> getFilter();
	
	/**
	 * Set the contents of this FilterPanel according to the specified String.
	 * 
	 * @param contents String to parse to set the contents of this FilterPanel.
	 */
	public abstract void setContents(String contents);
	
	/**
	 * @return <code>true</code> if this FilterPanel has no contents, and
	 * <code>false</code> otherwise.
	 */
	public abstract boolean isEmpty();
}
