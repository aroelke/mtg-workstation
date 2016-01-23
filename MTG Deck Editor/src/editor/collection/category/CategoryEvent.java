package editor.collection.category;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class CategoryEvent
{
	private CategorySpec source;
	private boolean name;
	private boolean whitelist;
	private boolean blacklist;
	private boolean color;
	private boolean filter;
	
	public CategoryEvent(CategorySpec s,
			boolean n, boolean w, boolean b, boolean c, boolean f)
	{
		source = s;
		
		name = n;
		whitelist = w;
		blacklist = b;
		color = c;
		filter = f;
	}
	
	public CategorySpec getSource()
	{
		return source;
	}
	
	public boolean nameChanged()
	{
		return name;
	}
	
	public boolean whitelistChanged()
	{
		return whitelist;
	}
	
	public boolean blacklistChanged()
	{
		return blacklist;
	}
	
	public boolean colorChanged()
	{
		return color;
	}
	
	public boolean filterChanged()
	{
		return filter;
	}
}
