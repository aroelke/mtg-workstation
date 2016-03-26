package editor.collection.category;

import java.awt.Color;
import java.util.Set;

import editor.database.Card;
import editor.filter.Filter;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class CategoryEvent
{
	private CategorySpec source;
	private String oldName;
	private Set<Card> oldWhitelist;
	private Set<Card> oldBlacklist;
	private Color oldColor;
	private Filter oldFilter;
	
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
	
	public CategorySpec getSource()
	{
		return source;
	}
	
	public boolean nameChanged()
	{
		return oldName != null;
	}
	
	public String oldName()
	{
		if (nameChanged())
			return oldName;
		else
			throw new IllegalStateException("Name of the category has not changed.");
	}
	
	public String newName()
	{
		if (nameChanged())
			return getSource().getName();
		else
			throw new IllegalStateException("Name of the category has not changed.");
	}
	
	public boolean whitelistChanged()
	{
		return oldWhitelist != null;
	}
	
	public Set<Card> oldWhitelist()
	{
		if (whitelistChanged())
			return oldWhitelist;
		else
			throw new IllegalStateException("Whitelist of the category has not changed.");
	}
	
	public Set<Card> newWhitelist()
	{
		if (whitelistChanged())
			return source.getWhitelist();
		else
			throw new IllegalStateException("Whitelist of the category has not changed.");
	}
	
	public boolean blacklistChanged()
	{
		return oldBlacklist != null;
	}
	
	public Set<Card> oldBlacklist()
	{
		if (blacklistChanged())
			return oldBlacklist;
		else
			throw new IllegalStateException("Blacklist of the category has not changed.");
	}
	
	public Set<Card> newBlacklist()
	{
		if (blacklistChanged())
			return source.getBlacklist();
		else
			throw new IllegalStateException("Blacklist of the category has not changed.");
	}
	
	public boolean colorChanged()
	{
		return oldColor != null;
	}
	
	public Color oldColor()
	{
		if (colorChanged())
			return oldColor;
		else
			throw new IllegalStateException("Color of the category has not changed.");
	}
	
	public Color newColor()
	{
		if (colorChanged())
			return source.getColor();
		else
			throw new IllegalStateException("Color of the category has not changed.");
	}
	
	public boolean filterChanged()
	{
		return oldFilter != null;
	}
	
	public Filter oldFilter()
	{
		if (filterChanged())
			return oldFilter;
		else
			throw new IllegalStateException("Filter of the category has not changed.");
	}
	
	public Filter newFilter()
	{
		if (filterChanged())
			return source.getFilter();
		else
			throw new IllegalStateException("Filter of the category has not changed.");
	}
}
