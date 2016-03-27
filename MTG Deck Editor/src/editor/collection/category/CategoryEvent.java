package editor.collection.category;

import java.awt.Color;
import java.util.Set;

import editor.database.Card;
import editor.filter.Filter;

/**
 * This class represents an event that changes a CategorySpec.  It can
 * tell which CategorySpec changed, and which of its parameters changed
 * as a result of the event.  Use the *changed methods to tell if a
 * parameter changed.  If a parameter did not change and its old or
 * new value is requested from this CategoryEvent, an IllegalStateException
 * will be thrown.
 * 
 * @author Alec Roelke
 */
public class CategoryEvent
{
	/**
	 * The CategorySpec that generated this CategoryEvent.
	 */
	private CategorySpec source;
	/**
	 * The name of the CategorySpec before it was changed.
	 */
	private String oldName;
	/**
	 * The whitelist of the CategorySpec before it was changed.
	 */
	private Set<Card> oldWhitelist;
	/**
	 * The blacklist of the CategorySpec before it was changed.
	 */
	private Set<Card> oldBlacklist;
	/**
	 * The Color of the CategorySpec before it was changed.
	 */
	private Color oldColor;
	/**
	 * The Filter of the CategorySpec before it was changed.
	 */
	private Filter oldFilter;
	
	/**
	 * Create a new CategoryEvent.  Use <code>null</code> for any parameter
	 * that did not change during the event.
	 * 
	 * @param s CategorySpec source of the new CategoryEvent
	 * @param n Old name of the CategorySpec
	 * @param w Old whitelist of the CategorySpec
	 * @param b Old blacklist of the CategorySpec
	 * @param c Old Color of the CategorySpec
	 * @param f Old Filter of the CategorySpec
	 */
	public CategoryEvent(CategorySpec s,
			String n, Set<Card> w, Set<Card> b, Color c, Filter f)
	{
		source = s;
		
		oldName = n;
		oldWhitelist = w;
		oldBlacklist = b;
		oldColor = c;
		oldFilter = f;
	}
	
	/**
	 * @return The CategorySpec that generated this CategoryEvent.
	 */
	public CategorySpec getSource()
	{
		return source;
	}
	
	/**
	 * @return <code>true</code> if the CategorySpec's name changed,
	 * and <code>false</code> otherwise.
	 */
	public boolean nameChanged()
	{
		return oldName != null;
	}
	
	/**
	 * @return The old name of the CategorySpec before this CategoryEvent
	 * was generated.
	 * @throws IllegalStateException If the CategorySpec's name has not changed.
	 */
	public String oldName()
	{
		if (nameChanged())
			return oldName;
		else
			throw new IllegalStateException("Name of the category has not changed.");
	}
	
	/**
	 * @return The new name of the CategorySpec as a result of the event.
	 * @throws IllegalStateException If the CategorySpec's name has not changed.
	 */
	public String newName()
	{
		if (nameChanged())
			return getSource().getName();
		else
			throw new IllegalStateException("Name of the category has not changed.");
	}
	
	/**
	 * @return <code>true</code> if the CategorySpec's whitelist changed as a
	 * result of the event that generated this CategoryEvent.
	 */
	public boolean whitelistChanged()
	{
		return oldWhitelist != null;
	}
	
	/**
	 * @return The CategorySpec's whitelist before the event.
	 * @throws IllegalStateException if the CategorySpec's whitelist did not
	 * change.
	 */
	public Set<Card> oldWhitelist()
	{
		if (whitelistChanged())
			return oldWhitelist;
		else
			throw new IllegalStateException("Whitelist of the category has not changed.");
	}
	
	/**
	 * @return The CategorySpec's whitelist as a result of the event.
	 * @throws IllegalStateException if the CategorySpec's whitelist did not
	 * change.
	 */
	public Set<Card> newWhitelist()
	{
		if (whitelistChanged())
			return source.getWhitelist();
		else
			throw new IllegalStateException("Whitelist of the category has not changed.");
	}
	
	/**
	 * @return <code>true</code> if the event that generated this CategoryEvent
	 * changed the CategorySpec's blacklist.
	 */
	public boolean blacklistChanged()
	{
		return oldBlacklist != null;
	}
	
	/**
	 * @return The CategorySpec's blacklist as it was before the event.
	 * @throws IllegalStateException If the CategorySpec's blacklist did not
	 * change.
	 */
	public Set<Card> oldBlacklist()
	{
		if (blacklistChanged())
			return oldBlacklist;
		else
			throw new IllegalStateException("Blacklist of the category has not changed.");
	}
	
	/**
	 * @return The CategorySpec's blacklist after the event.
	 * @throws IllegalStateException if the CategorySpec's blacklist did not
	 * change.
	 */
	public Set<Card> newBlacklist()
	{
		if (blacklistChanged())
			return source.getBlacklist();
		else
			throw new IllegalStateException("Blacklist of the category has not changed.");
	}
	
	/**
	 * @return <code>true</code> if the event that generated this CategoryEvent
	 * changed the CategorySpec's Color, and <code>false</code> otherwise.
	 */
	public boolean colorChanged()
	{
		return oldColor != null;
	}
	
	/**
	 * @return The CategorySpec's Color as it was before the event.
	 * @throws IllegalStateException if the CategorySpec's color did not
	 * change.
	 */
	public Color oldColor()
	{
		if (colorChanged())
			return oldColor;
		else
			throw new IllegalStateException("Color of the category has not changed.");
	}
	
	/**
	 * @return The CategorySpec's color after the event.
	 * @throws IllegalStateException If the CategorySpec's Color did not
	 * change.
	 */
	public Color newColor()
	{
		if (colorChanged())
			return source.getColor();
		else
			throw new IllegalStateException("Color of the category has not changed.");
	}
	
	/**
	 * @return <code>true</code> if the CategorySpec's Filter changed as a result
	 * of the event that generated this CategoryEvent.
	 */
	public boolean filterChanged()
	{
		return oldFilter != null;
	}
	
	/**
	 * @return The CategorySpec's Filter as it was before the event.
	 * @throws IllegalStateException If the CategorySpec's Filter did not
	 * change.
	 */
	public Filter oldFilter()
	{
		if (filterChanged())
			return oldFilter;
		else
			throw new IllegalStateException("Filter of the category has not changed.");
	}
	
	/**
	 * @return The CategorySpec's Filter after the event.
	 * @throws IllegalStateException If the CategorySpec's Filter did not
	 * change.
	 */
	public Filter newFilter()
	{
		if (filterChanged())
			return source.getFilter();
		else
			throw new IllegalStateException("Filter of the category has not changed.");
	}
}
