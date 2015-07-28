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

import util.Containment;
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
		Map<Card, Integer> isoNameCounts = new HashMap<Card, Integer>();
		for (Card c: deck)
		{
			boolean counted = false;
			for (Card name: isoNameCounts.keySet())
			{
				if (name.compareName(c) == 0)
				{
					isoNameCounts.compute(name, (k, v) -> v += deck.count(name));
					counted = true;
					break;
				}
			}
			if (!counted)
				isoNameCounts.put(c, deck.count(c));
		}
		for (Card c: deck)
		{
			for (String format: Card.formatList)
			{
				if (!c.legalIn(format))
					warnings.get(format).add(c.name + " is illegal in " + format);
				else if (isoNameCounts.containsKey(c) && !c.ignoreCountRestriction())
				{
					if (format.equalsIgnoreCase("commander") || format.equalsIgnoreCase("singleton 100"))
					{
						if (isoNameCounts.get(c) > 1)
							warnings.get(format).add("Deck contains more than 1 copy of " + c.name);
					}
					else
					{
						if (c.legalityIn(format) == Legality.RESTRICTED && isoNameCounts.get(c) > 1)
							warnings.get(format).add(c.name + " is restricted in " + format);
						else if (isoNameCounts.get(c) > 4)
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
		HashMap<MTGColor, List<Card>> colorBins = new HashMap<MTGColor, List<Card>>();
		for (MTGColor color: MTGColor.values())
			colorBins.put(color, new ArrayList<Card>());
		for (Card c: deck.stream().sorted((a, b) -> a.colors.size() - b.colors.size()).collect(Collectors.toList()))
			for (int i = 0; i < deck.count(c); i++)
				binCard(c, colorBins, new ArrayList<MTGColor>());
		for (MTGColor bin: colorBins.keySet())
		{
			System.out.println(bin + ": " + colorBins.get(bin).size());
			for (Card c: colorBins.get(bin))
				System.out.println("\t" + c.name);
		}
		for (MTGColor color: MTGColor.values())
			if (colorBins.get(color).size() < 20)
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
	 * Find the color to classify a Card as to make the deck as close to 20 of each color, with
	 * no Cards repeating, as possible.  If all classifications have 20 or more Cards, then remove
	 * one and reclassify it.
	 * 
	 * TODO: This almost works; but make it better
	 * @param c Card to classify
	 * @param bins List of colors the Card can be classified as
	 * @param exclusion List of colors the Card should not be classified as
	 */
	private void binCard(Card c, HashMap<MTGColor, List<Card>> bins, List<MTGColor> exclusion)
	{
		if (c.colors.isEmpty())
			return;
		else if (c.colors.size() == 1)
			bins.get(c.colors.get(0)).add(c);
		else
		{
			MTGColor bin = null;
			for (MTGColor color: c.colors)
				if (bin == null || bins.get(color).size() < bins.get(bin).size())
					bin = color;
			if (bins.get(bin).size() < 20)
				bins.get(bin).add(c);
			else
			{
				Card next = bins.get(bin).stream().filter((card) -> !Containment.CONTAINS_ANY_OF.test(card.colors, exclusion)).findFirst().orElse(null);
				bins.get(bin).add(c);
				exclusion.add(bin);
				if (next != null)
					binCard(next, bins, exclusion);
			}
		}
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
