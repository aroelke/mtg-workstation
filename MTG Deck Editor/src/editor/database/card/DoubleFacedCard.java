package editor.database.card;

import java.util.Arrays;
import java.util.List;

import editor.database.characteristics.ManaCost;

/**
 * TODO: Comment this
 * @author Alec Roelke
 */
public class DoubleFacedCard extends SplitCard implements Card
{
	private final Card front;
	
	public DoubleFacedCard(Card f, Card b)
	{
		super(f, b);
		front = f;
		if (front.layout() != CardLayout.DOUBLE_FACED|| b.layout() != CardLayout.DOUBLE_FACED)
			throw new IllegalArgumentException("can't join non-double-faced cards into double-faced cards");
	}
	
	@Override
	public CardLayout layout()
	{
		return CardLayout.DOUBLE_FACED;
	}
	
	@Override
	public ManaCost.Tuple manaCost()
	{
		return new ManaCost.Tuple(front.manaCost().get(0), new ManaCost());
	}
	
	@Override
	public List<Double> cmc()
	{
		return Arrays.asList(front.cmc().get(0), front.cmc().get(0));
	}
	
	@Override
	public double minCmc()
	{
		return front.minCmc();
	}
}
