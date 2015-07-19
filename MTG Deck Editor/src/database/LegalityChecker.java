package database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import database.characteristics.Legality;
import database.characteristics.MTGColor;

/**
 * This class represents a check to see which formats a deck is legal in.  It is meant to
 * only be used once and then thrown away once viewing the results are complete.  It can
 * tell what formats a deck is legal and illegal in, and give reasons for why it is
 * illegal.
 * 
 * @author Alec Roelke
 */
public class LegalityChecker
{
	/**
	 * Array containing formats the deck is legal in.
	 */
	private String[] legal;
	/**
	 * Array containing formats the deck is illegal in.
	 */
	private String[] illegal;
	/**
	 * Map of formats to reasons for being illegal in them.  Contents of the map are lists
	 * of Strings, which will be empty for legal formats.
	 */
	private Map<String, List<String>> warnings;
	
	/**
	 * Craete a new LegalityChecker.
	 */
	public LegalityChecker()
	{
		legal = new String[] {};
		illegal = new String[] {};
		warnings = new HashMap<String, List<String>>();
		for (String format: Card.formatList)
			warnings.put(format, new ArrayList<String>());
	}
	
	/**
	 * Check which formats a deck is legal in, and the reasons for why it is illegal in
	 * others.
	 * 
	 * @param deck
	 */
	public void checkLegality(Deck deck)
	{
		// Deck size
		for (String format: Card.formatList)
		{
			if (format.equalsIgnoreCase("commander"))
			{
				if (deck.total() != 100)
					warnings.get(format).add("Deck does not contain exactly 100 cards");
			}
			else
			{
				if (deck.total() < 60)
					warnings.get(format).add("Deck contains fewer than 60 cards");
			}
		}
		
		// Individual card legality and count
		for (Card c: deck)
		{
			for (String format: Card.formatList)
			{
				if (!c.legalIn(format))
					warnings.get(format).add(c.name + " is illegal in " + format);
				else if (!c.ignoreCountRestriction())
				{
					if (format.equalsIgnoreCase("commander"))
					{
						if (deck.count(c) > 1)
							warnings.get(format).add("Deck contains more than 1 copy of " + c.name);
					}
					else
					{
						if (c.legalityIn(format) == Legality.RESTRICTED && deck.count(c) > 1)
							warnings.get(format).add(c.name + " is restricted in " + format);
						else if (deck.count(c) > 4)
							warnings.get(format).add("Deck contains more than 4 copies of " + c.name);
					}
				}
			}
		}
		
		// Commander only: commander exists and matches deck color identity
		List<Card> possibleCommanders = new ArrayList<Card>();
		for (Card c: deck)
			if (c.canBeCommander())
				possibleCommanders.add(c);
		if (possibleCommanders.isEmpty())
			warnings.get("Commander").add("Deck does not contain a legendary creature");
		else
		{
			Set<MTGColor> deckColorIdentity = new HashSet<MTGColor>();
			for (Card c: deck)
				deckColorIdentity.addAll(Arrays.asList(c.colors.toArray()));
			for (Card c: new ArrayList<Card>(possibleCommanders))
				if (!c.colors.containsAll(deckColorIdentity))
					possibleCommanders.remove(c);
			if (possibleCommanders.isEmpty())
				warnings.get("Commander").add("Deck does not contain a legendary creature whose color identity contains " + deckColorIdentity.toString());
		}
			
		// Collate the legality lists
		List<String> illegalList = warnings.keySet().stream().filter((s) -> !warnings.get(s).isEmpty()).collect(Collectors.toList());
		Collections.sort(illegalList);
		List<String> legalList = new ArrayList<String>(Arrays.asList(Card.formatList));
		legalList.removeAll(illegalList);
		legal = legalList.toArray(legal);
		illegal = illegalList.toArray(illegal);
	}
	
	/**
	 * @return The list of formats the deck is legal in.
	 */
	public String[] legalFormats()
	{
		return legal;
	}
	
	/**
	 * @return The list of formats the deck is illegal in.
	 */
	public String[] illegalFormats()
	{
		return illegal;
	}
	
	/**
	 * @param format Format to check reasons for illegality
	 * @return A list of Strings containing reasons for why the deck is illegal
	 * in the given format.
	 */
	public List<String> getWarnings(String format)
	{
		return warnings.get(format);
	}
}
