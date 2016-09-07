package editor.database.card;

import java.util.Arrays;
import java.util.List;

/**
 * This class represents a card that has several faces all printed on the front.
 * 
 * @author Alec Roelke
 */
public class SplitCard extends MultiCard
{
	/**
	 * Create a new SplitCard with the given Cards as faces.  They should all indicate
	 * with their layouts that they are split cards.
	 * 
	 * @param f List of Cards representing faces
	 */
	public SplitCard(List<Card> f)
	{
		super(CardLayout.SPLIT, f);
		for (Card face: f)
			if (face.layout() != CardLayout.SPLIT)
				throw new IllegalArgumentException("can't create split cards out of non-split cards");
	}
	
	/**
	 * Create a new SplitCard with the given Cards as faces.
	 * 
	 * @param f Cards to use as faces
	 */
	public SplitCard(Card... f)
	{
		this(Arrays.asList(f));
	}
	
	/**
	 * All of the faces of a SplitCard are on the front, so there is only one image for it.
	 * 
	 * @return The name of this SplitCard's image.
	 */
	@Override
	public List<String> imageNames()
	{
		return super.imageNames().subList(0, 1);
	}
}
