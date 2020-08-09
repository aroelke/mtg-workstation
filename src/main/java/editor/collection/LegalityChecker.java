package editor.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import editor.collection.deck.Deck;
import editor.database.attributes.Legality;
import editor.database.attributes.ManaType;
import editor.database.card.Card;
import editor.filter.leaf.options.multi.LegalityFilter;


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
     * Array containing formats the deck is illegal in.
     */
    private String[] illegal;
    /**
     * Array containing formats the deck is legal in.
     */
    private String[] legal;
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
        legal = new String[]{};
        illegal = new String[]{};
        warnings = new HashMap<>();
        for (String format : LegalityFilter.formatList)
            warnings.put(format, new ArrayList<>());
    }

    /**
     * Check which formats a deck is legal in, and the reasons for why it is illegal in
     * others.
     *
     * @param deck deck to check
     */
    public void checkLegality(Deck deck)
    {
        // Deck size
        for (String format : LegalityFilter.formatList)
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
        Map<Card, Integer> isoNameCounts = new HashMap<>();
        for (Card c : deck)
        {
            boolean counted = false;
            for (Card name : isoNameCounts.keySet())
            {
                if (name.compareName(c) == 0)
                {
                    isoNameCounts.compute(name, (k, v) -> v += deck.getEntry(name).count());
                    counted = true;
                    break;
                }
            }
            if (!counted)
                isoNameCounts.put(c, deck.getEntry(c).count());
        }
        for (Card c : deck)
        {
            for (String format : LegalityFilter.formatList)
            {
                if (!c.legalIn(format))
                    warnings.get(format).add(c.unifiedName() + " is illegal in " + format);
                else if (isoNameCounts.containsKey(c) && !c.ignoreCountRestriction())
                {
                    if (format.equalsIgnoreCase("commander") || format.equalsIgnoreCase("singleton 100"))
                    {
                        if (isoNameCounts.get(c) > 1)
                            warnings.get(format).add("Deck contains more than 1 copy of " + c.unifiedName());
                    }
                    else
                    {
                        if (c.legalityIn(format) == Legality.RESTRICTED && isoNameCounts.get(c) > 1)
                            warnings.get(format).add(c.unifiedName() + " is restricted in " + format);
                        else if (isoNameCounts.get(c) > 4)
                            warnings.get(format).add("Deck contains more than 4 copies of " + c.unifiedName());
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
            List<ManaType> deckColorIdentityList = new ArrayList<>();
            for (Card c : deck)
                deckColorIdentityList.addAll(c.colors());
            List<ManaType> deckColorIdentity = new ArrayList<>(deckColorIdentityList);
            for (Card c : new ArrayList<>(possibleCommanders))
                if (!c.colors().containsAll(deckColorIdentity))
                    possibleCommanders.remove(c);
            if (possibleCommanders.isEmpty())
                warnings.get("Commander").add("Deck does not contain a legendary creature whose color identity contains " + deckColorIdentity.toString());
        }

        // Prismatic only: there are at least 20 cards of each color, and multicolored cards only count once
        /*
         * Pseudocode:
         * For each card:
         *   if the card's colors have not been tested yet:
         *     count the number of cards in the deck that share a color with the card
         *     if that number < 20 * number of colors the card has:
         *       return false
         * return true
         */

        // Collate the legality lists
        List<String> illegalList = warnings.keySet().stream().filter((s) -> !warnings.get(s).isEmpty()).collect(Collectors.toList());
        Collections.sort(illegalList);
        List<String> legalList = new ArrayList<>(Arrays.asList(LegalityFilter.formatList));
        legalList.removeAll(illegalList);
        legal = legalList.toArray(legal);
        illegal = illegalList.toArray(illegal);
    }

    /**
     * Get the reasons the deck last checked is illegal in a format.
     *
     * @param format Format to check reasons for illegality
     * @return A list of Strings containing reasons for why the deck is illegal
     * in the given format.
     */
    public List<String> getWarnings(String format)
    {
        return warnings.get(format);
    }

    /**
     * Get the formats the deck last checked is illegal in.
     *
     * @return the list of formats the deck is illegal in.
     */
    public String[] illegalFormats()
    {
        return illegal;
    }

    /**
     * Get the formats the deck last checked is legal in.
     *
     * @return the list of formats the deck is legal in.
     */
    public String[] legalFormats()
    {
        return legal;
    }
}
