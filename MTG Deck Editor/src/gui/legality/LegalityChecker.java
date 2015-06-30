package gui.legality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import database.Card;
import database.Deck;
import database.characteristics.MTGColor;

/**
 * TODO: Check for correctness
 * TODO: Comment this
 * TODO: Create the GUI elements for this
 * 
 * @author Alec Roelke
 */
public class LegalityChecker
{
	private String[] legal;
	private String[] illegal;
	private Map<String, List<String>> warnings;
	
	public LegalityChecker()
	{
		legal = new String[] {};
		illegal = new String[] {};
		warnings = new HashMap<String, List<String>>();
		for (String format: Card.formatList)
			warnings.put(format, new ArrayList<String>());
	}
	
	public void checkLegality(Deck deck)
	{
		// Deck size
		for (String format: Card.formatList)
		{
			if (format.equalsIgnoreCase("commander"))
			{
				if (deck.size() != 100)
					warnings.get(format).add("Deck does not contain exactly 100 cards");
			}
			else
			{
				if (deck.size() < 60)
					warnings.get(format).add("Deck contains fewer than 60 cards");
			}
		}
		
		// Individual card legality
		for (Card c: deck)
			for (String format: Card.formatList)
				if (!c.legalIn(format))
					warnings.get(format).add(c.name + " is illegal in this format");
		
		// Commander only: commander exists and matches deck color identity
		List<Card> possibleCommanders = new ArrayList<Card>();
		for (Card c: deck)
			if (c.supertypeContains("legendary"))
				possibleCommanders.add(c);
		if (possibleCommanders.isEmpty())
			warnings.get("Commander").add("Deck does not contain a legendary creature");
		else
		{
			Set<MTGColor> deckColorIdentity = new HashSet<MTGColor>();
			for (Card c: deck)
				deckColorIdentity.addAll(c.colors);
			for (Card c: new ArrayList<Card>(possibleCommanders))
				if (!c.colors.containsAll(deckColorIdentity))
					possibleCommanders.remove(c);
			if (possibleCommanders.isEmpty())
				warnings.get("Commander").add("Deck does not contain a legendary creature whose color identity contains " + deckColorIdentity.toString());
		}
			
		List<String> illegalList = warnings.keySet().stream().filter((s) -> !warnings.get(s).isEmpty()).collect(Collectors.toList());
		Collections.sort(illegalList);
		List<String> legalList = Arrays.asList(Card.formatList);
		legalList.removeAll(illegalList);
		legal = legalList.toArray(legal);
		illegal = illegalList.toArray(illegal);
	}
	
	public String[] legalFormats()
	{
		return legal;
	}
	
	public String[] illegalFormats()
	{
		return illegal;
	}
	
	public List<String> getWarnings(String format)
	{
		return warnings.get(format);
	}
}
