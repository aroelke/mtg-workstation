package editor.database.card;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import editor.util.Lazy;

/**
 * This class represents a card that has several faces all printed on the front.
 * 
 * TODO: Change this to have CMC the sum of its parts (and potentially this means all cards only have one CMC now)
 * 
 * @author Alec Roelke
 */
public class SplitCard extends MultiCard
{
	/**
	 * Converted mana costs of this SplitCard's faces. 
	 */
	private Lazy<List<Double>> cmc;
	
	/**
	 * Create a new SplitCard with the given Cards as faces.
	 * 
	 * @param f cards to use as faces
	 */
	public SplitCard(Card... f)
	{
		this(Arrays.asList(f));
	}
	
	/**
	 * Create a new SplitCard with the given Cards as faces.  They should all indicate
	 * with their layouts that they are split cards.
	 * 
	 * @param f list of Cards representing faces
	 */
	public SplitCard(List<Card> f)
	{
		super(CardLayout.SPLIT, f);
		for (Card face: f)
			if (face.layout() != CardLayout.SPLIT)
				throw new IllegalArgumentException("can't create split cards out of non-split cards");
		
		cmc = new Lazy<>(() -> Collections.unmodifiableList(Collections.nCopies(f.size(), f.stream().mapToDouble((c) -> c.cmc().get(0)).sum())));
	}
	
	/**
	 * {@inheritDoc}
	 * The converted mana cost of a split card is the sum of the converted mana costs of all
	 * of its faces.
	 */
	@Override
	public List<Double> cmc()
	{
		return cmc.get();
	}
	
	/**
	 * {@inheritDoc}
	 * All of the faces of a SplitCard are on the front, so there is only one image for it.
	 */
	@Override
	public List<String> imageNames()
	{
		return super.imageNames().subList(0, 1);
	}
	
	/**
	 * {@inheritDoc}
	 * All of the faces of a SplitCard are on the front, so only one multiverseid is necessary.
	 */
	@Override
	public List<Integer> multiverseid()
	{
		return super.multiverseid().subList(0, 1);
	}
}
