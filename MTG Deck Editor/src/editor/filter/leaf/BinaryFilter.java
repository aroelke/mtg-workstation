package editor.filter.leaf;

import editor.database.Card;
import editor.filter.FilterType;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class BinaryFilter extends FilterLeaf<Void>
{
	private boolean all;
	
	public BinaryFilter(boolean a)
	{
		super(a ? FilterType.ALL : FilterType.NONE, null);
		all = a;
	}
	
	@Override
	public boolean test(Card c)
	{
		return all;
	}

	@Override
	public String content()
	{
		return "";
	}

	@Override
	public void parse(String s)
	{}
}
