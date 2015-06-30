package gui.legality;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.Card;
import database.Deck;

/**
 * TODO: Implement this
 * 
 * @author Alec Roelke
 */
public class LegalityChecker
{
	private List<String> legal;
	private List<String> illegal;
	private Map<String, List<String>> warnings;
	
	public LegalityChecker()
	{
		legal = Arrays.asList(Card.formatList);
		illegal = Arrays.asList(Card.formatList);
		warnings = new HashMap<String, List<String>>();
	}
	
	public void checkLegality(Deck deck)
	{
		for (Card c: deck)
			legal.retainAll(c.legalIn());
		illegal.removeAll(legal);
		for (String format: illegal)
			warnings.put(format, Arrays.asList("One or more cards is illegal in this format"));
	}
}
