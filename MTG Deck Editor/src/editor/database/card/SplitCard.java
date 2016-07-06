package editor.database.card;

import java.util.Arrays;
import java.util.List;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class SplitCard extends MultiCard implements Card
{
	public SplitCard(List<Card> f)
	{
		super(f);
		for (Card face: f)
			if (face.layout() != CardLayout.SPLIT)
				throw new IllegalArgumentException("can't create split cards out of non-split cards");
	}
	
	public SplitCard(Card... f)
	{
		this(Arrays.asList(f));
	}
	
	@Override
	public List<String> imageNames()
	{
		return Arrays.asList(super.imageNames().get(0));
	}
}
