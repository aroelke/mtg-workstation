package editor.database.card;

import java.util.Collections;
import java.util.List;

import editor.util.Lazy;

/**
 * This class represents a split card that has two faces printed on the front, with
 * one of them sideways.
 * 
 * @author Alec Roelke
 */
public class AftermathCard extends MultiCard
{
	/**
	 * Aftermath face of this AftermathCard.
	 */
	private final Card aftermath;
	/**
	 * Converted mana cost of this AftermathCard.
	 */
	private Lazy<List<Double>> cmc;
	/**
	 * Face of this AftermathCard that is cast from the hand.
	 */
	private final Card first;
	
	/**
	 * Create a new AftermathCard with the given Cards as faces.
	 * 
	 * @param f cards to use as faces
	 */
	public AftermathCard(Card f, Card b)
	{
		super(CardLayout.AFTERMATH, f, b);
		first = f;
		aftermath = b;
		
		if (first.layout() != CardLayout.AFTERMATH || aftermath.layout() != CardLayout.AFTERMATH)
			throw new IllegalArgumentException("can't join non-aftermath cards into aftermath cards");
		
		cmc = new Lazy<>(() -> Collections.unmodifiableList(Collections.nCopies(2, first.cmc().get(0) + aftermath.cmc().get(0))));
	}
	
	/**
	 * {@inheritDoc}
	 * The converted mana cost of an aftermath card is the sum of those of its two faces.
	 */
	@Override
	public List<Double> cmc()
	{
		return cmc.get();
	}
	
	/**
	 * {@inheritDoc}
	 * All of the faces of an AftermathCard are on the front, so there is only one image for it.
	 */
	@Override
	public List<String> imageNames()
	{
		return super.imageNames().subList(0, 1);
	}
	
	/**
	 * {@inheritDoc}
	 * All of the faces of an AftermathCard are on the front, so only one multiverseid is necessary. 
	 */
	@Override
	public List<Integer> multiverseid()
	{
		return super.multiverseid().subList(0, 1);
	}
}
