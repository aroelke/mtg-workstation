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
	 * Create a new LegalityChecker.
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
	 * TODO: Add deck construction rules for:
	 * - Tribal Wars Legacy/Standard (1/3 of the cards of the deck must have the same creature type)
	 * 
	 * @param deck
	 */
	public void checkLegality(Deck deck)
	{
		// Deck size
		for (String format: Card.formatList)
		{
			if (format.equalsIgnoreCase("prismatic"))
			{
				if (deck.total() < 250)
					warnings.get(format).add("Deck contains fewer than 250 cards");
			}
			else if (format.equalsIgnoreCase("commander"))
			{
				if (deck.total() != 100)
					warnings.get(format).add("Deck does not contain exactly 100 cards");
			}
			else if (format.equalsIgnoreCase("singleton 100"))
			{
				if (deck.total() < 100)
					warnings.get(format).add("Deck does not contain exactly 100 cards");
				else if (deck.total() > 115)
					warnings.get(format).add("Sideboard is greater than 15 cards");
			}
			else if (format.equalsIgnoreCase("freeform"))
			{
				if (deck.total() < 40)
					warnings.get(format).add("Deck contains fewer than 40 cards");
			}
			else
			{
				if (deck.total() < 60)
					warnings.get(format).add("Deck contains fewer than 60 cards");
			}
		}
		
		// Individual card legality and count
		// TODO: Fix this to work based on card name and not on unique cards
		for (Card c: deck)
		{
			for (String format: Card.formatList)
			{
				if (!c.legalIn(format))
					warnings.get(format).add(c.name + " is illegal in " + format);
				else if (!c.ignoreCountRestriction())
				{
					if (format.equalsIgnoreCase("commander") || format.equalsIgnoreCase("singleton 100"))
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
		List<Card> possibleCommanders = deck.stream().filter(Card::canBeCommander).collect(Collectors.toList());
		if (possibleCommanders.isEmpty())
			warnings.get("Commander").add("Deck does not contain a legendary creature");
		else
		{
			Set<MTGColor> deckColorIdentitySet = new HashSet<MTGColor>();
			for (Card c: deck)
				deckColorIdentitySet.addAll(c.colors);
			MTGColor.Tuple deckColorIdentity = new MTGColor.Tuple(deckColorIdentitySet);
			for (Card c: new ArrayList<Card>(possibleCommanders))
				if (!c.colors.containsAll(deckColorIdentity))
					possibleCommanders.remove(c);
			if (possibleCommanders.isEmpty())
				warnings.get("Commander").add("Deck does not contain a legendary creature whose color identity contains " + deckColorIdentity.toString());
		}
		
		// Prismatic only: there are at least 20 cards of each color, and multicolored cards only count once
		// TODO: Make the algorithm for deciding where to put multicolored cards better
		HashMap<MTGColor, Integer> colorCount = new HashMap<MTGColor, Integer>();
		colorCount.put(MTGColor.WHITE, 0);
		colorCount.put(MTGColor.BLUE, 0);
		colorCount.put(MTGColor.BLACK, 0);
		colorCount.put(MTGColor.RED, 0);
		colorCount.put(MTGColor.GREEN, 0);
		List<Card> multicoloredCards = new ArrayList<Card>();
		for (Card c: deck)
		{
			if (c.colors.size() == 1)
				colorCount.compute(c.colors.get(0), (k, v) -> v += deck.count(c));
			else if (c.colors.size() > 1)
				for (int i = 0; i < deck.count(c); i++)
					multicoloredCards.add(c);
		}
		if (!colorCount.values().stream().allMatch((v) -> v >= 20))
		{
			Collections.sort(multicoloredCards, (a, b) -> a.colors.size() - b.colors.size());
			for (Card c: multicoloredCards)
			{
				int smallest = Integer.MAX_VALUE;
				MTGColor color = null;
				for (MTGColor col: c.colors)
					if (colorCount.get(col) < smallest)
						smallest = colorCount.get(color = col);
				colorCount.compute(color, (k, v) -> ++v);
			}
		}
		for (MTGColor color: MTGColor.values())
			if (colorCount.get(color) < 20)
				warnings.get("Prismatic").add("Deck contains fewer than 20 " + color.toString().toLowerCase() + " cards");
		
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
