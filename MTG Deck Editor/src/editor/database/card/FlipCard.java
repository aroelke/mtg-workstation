package editor.database.card;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import editor.database.characteristics.ManaCost;

/**
 * TODO: Comment this
 * @author Alec Roelke
 */
public class FlipCard extends MultiCard implements Card
{
	private final Card top;
	
	public FlipCard(Card t, Card b)
	{
		super(t, b);
		top = t;
		if (top.layout() != CardLayout.FLIP || b.layout() != CardLayout.FLIP)
			throw new IllegalArgumentException("can't join non-flip cards into flip cards");
	}
	
	private <T> List<T> collect(Function<Card, List<T>> characteristic)
	{
		return Arrays.asList(characteristic.apply(top).get(0), characteristic.apply(top).get(0));
	}
	
	@Override
	public CardLayout layout()
	{
		return CardLayout.FLIP;
	}
	
	public ManaCost.Tuple manaCost()
	{
		return new ManaCost.Tuple(collect(Card::manaCost));
	}
	
	public List<Double> cmc()
	{
		return collect(Card::cmc);
	}
	
	public double minCmc()
	{
		return top.minCmc();
	}
}
