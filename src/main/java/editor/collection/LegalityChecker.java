package editor.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
        warnings = Arrays.stream(LegalityFilter.formatList).collect(Collectors.toMap(Function.identity(), (f) -> new ArrayList<String>()));
    }

    /**
     * Check which formats a deck is legal in, and the reasons for why it is illegal in
     * others.
     *
     * @param deck deck to check
     */
    public void checkLegality(Deck deck)
    {
        for (Card c : deck)
        {
            System.out.println(c.normalizedName() + ": " + c.legalIn());
        }

        // Deck size
        for (String format : LegalityFilter.formatList)
        {
            if (format.equals("commander"))
            {
                if (deck.total() != 100)
                    warnings.get(format).add("Deck does not contain exactly 100 cards");
            }
            else if (format.equals("brawl"))
            {
                if (deck.total() != 60)
                    warnings.get(format).add("Deck does not contain exactly 60 cards");
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
                if (!c.legalityIn(format).isLegal)
                    warnings.get(format).add(c.unifiedName() + " is illegal in " + format);
                else if (isoNameCounts.containsKey(c) && !c.ignoreCountRestriction())
                {
                    if (format.equals("commander"))
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
        var possibleCommanders = deck.stream().filter(Card::canBeCommander).collect(Collectors.toList());
        if (possibleCommanders.isEmpty())
            warnings.get("commander").add("Deck does not contain a legendary creature");
        else
        {
            List<ManaType> deckColorIdentityList = new ArrayList<>();
            for (Card c : deck)
                deckColorIdentityList.addAll(c.colors());
            var deckColorIdentity = new ArrayList<>(deckColorIdentityList);
            for (Card c : new ArrayList<>(possibleCommanders))
                if (!c.colors().containsAll(deckColorIdentity))
                    possibleCommanders.remove(c);
            if (possibleCommanders.isEmpty())
                warnings.get("commander").add("Deck does not contain a legendary creature whose color identity contains " + deckColorIdentity.toString());
        }

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
