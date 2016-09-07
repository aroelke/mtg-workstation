package editor.database.card;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import editor.database.characteristics.ManaCost;

/**
 * This class represents a flip card, which has two faces:  Top and bottom.
 * 
 * @author Alec Roelke
 */
public class FlipCard extends MultiCard
{
	/**
	 * Card representing the top face of this FlipCard.
	 */
	private final Card top;
	
	/**
	 * Create a new FlipCard with the given Cards as its faces.  They should indicate that
	 * their layouts are flip card layouts.
	 * 
	 * @param t Top face of this FlipCard
	 * @param b Bottom face of this FlipCard
	 */
	public FlipCard(Card t, Card b)
	{
		super(CardLayout.FLIP, t, b);
		top = t;
		if (top.layout() != CardLayout.FLIP || b.layout() != CardLayout.FLIP)
			throw new IllegalArgumentException("can't join non-flip cards into flip cards");
	}
	
	/**
	 * @param characteristic Characteristic to collect
	 * @return A list containing the given characteristic repeated twice (once for the front
	 * face, and once for the back).
	 */
	private <T> List<T> collect(Function<Card, List<T>> characteristic)
	{
		return Arrays.asList(characteristic.apply(top).get(0), characteristic.apply(top).get(0));
	}
	
	/**
	 * @return A list containing the mana costs of this FlipCard, which are both that of
	 * the top face.
	 */
	@Override
	public ManaCost.Tuple manaCost()
	{
		return new ManaCost.Tuple(collect(Card::manaCost));
	}
	
	/**
	 * @return A list containing the converted mana costs of this FlipCard, which are both
	 * that of the top face.
	 */
	@Override
	public List<Double> cmc()
	{
		return collect(Card::cmc);
	}
	
	/**
	 * @return The converted mana cost of this FlipCard's top face.
	 */
	@Override
	public double minCmc()
	{
		return top.minCmc();
	}
}
