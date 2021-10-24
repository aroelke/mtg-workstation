package editor.collection.deck;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.filter.Filter;

/**
 * This class represents a set of specifications for a category.  Those specifications are its name,
 * the lists of cards to include or exclude regardless of filter, its color, its filter, and its String
 * representation.
 *
 * @author Alec Roelke
 * 
 * TOSCALA:
 * - make immutable (mutation functions return copies)
 * - def +(Card): include card
 * - def ++(Iterable[Card]): include cards
 * - def -(Card): exclude card
 * - def --(Iterable[Card]): exclude cards
 */
public class Category
{
    /**
     * Name of the category.
     */
    private String name;
    /**
     * List of cards to include in the category regardless of filter.
     */
    private Set<Card> whitelist;
    /**
     * List of cards to exclude from the category regardless of filter.
     */
    private Set<Card> blacklist;
    /**
     * Color of the category.
     */
    private Color color;
    /**
     * Filter of the category.
     */
    private Filter filter;

    /**
     * Create a new Category with the color black and a filter that passes all cards.
     */
    public Category()
    {
        this("All Cards", Color.BLACK, CardAttribute.createFilter(CardAttribute.ANY));
    }

    /**
     * Copy constructor for Category.
     *
     * @param original original Category to copy
     */
    public Category(Category original)
    {
        name = original.name;
        whitelist = new HashSet<>(original.whitelist);
        blacklist = new HashSet<>(original.blacklist);
        color = original.color;
        filter = original.filter.copy();
    }

    /**
     * Create a new Category with the given specifications.
     *
     * @param name name of the new spec
     * @param whitelist whitelist of the new spec
     * @param blacklist blacklist of the new spec
     * @param color color of the new spec
     * @param filter filter of the new spec
     */
    public Category(String name, Collection<Card> whitelist, Collection<Card> blacklist, Color color, Filter filter)
    {
        this.name = name;
        this.whitelist = new HashSet<>(whitelist);
        this.blacklist = new HashSet<>(blacklist);
        this.color = color;
        this.filter = filter;
    }

    /**
     * Create a new Category with the given specifications and an empty white- and
     * blacklist.
     *
     * @param name   name of the new spec
     * @param color  color of the new spec
     * @param filter filter of the new spec
     */
    public Category(String name, Color color, Filter filter)
    {
        this(name, new HashSet<>(), new HashSet<>(), color, filter);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (other instanceof Category o)
            return name.equals(o.name) && color.equals(o.color) && filter.equals(o.filter) && blacklist.equals(o.blacklist) && whitelist.equals(o.whitelist);
        return false;
    }

    /**
     * Exclude a card from the category, even if it passes through the filter.
     *
     * @param c card to exclude
     * @return true if the card was successfully excluded (it was added to the
     * blacklist or removed from the whitelist), and false otherwise.
     */
    public boolean exclude(Card c)
    {
        boolean changed = false;

        if (filter.test(c))
            changed |= blacklist.add(c);
        changed |= whitelist.remove(c);

        return changed;
    }

    /**
     * Get the set of cards that should not be included in the category,
     * even if they pass through the filter.
     *
     * @return the set of cards that explicitly must never pass through the
     * filter
     */
    public Set<Card> getBlacklist()
    {
        return new HashSet<>(blacklist);
    }

    /**
     * Get the category's color.
     *
     * @return the Color of the category.
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Get the category's filter for automatically including cards.
     *
     * @return the filter of the category.
     */
    public Filter getFilter()
    {
        return filter;
    }

    /**
     * Get the name of the category this Category represents.
     *
     * @return the name of the category.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the set of cards that should be included in the category even
     * if they don't pass through the filter.
     *
     * @return the set of cards that explicitly must pass through the filter.
     */
    public Set<Card> getWhitelist()
    {
        return new HashSet<>(whitelist);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, color, filter, blacklist, whitelist);
    }

    /**
     * Include a card in the category, even if it doesn't pass through the filter.
     *
     * @param c card to include
     * @return <code>true</code> if the Card was successfully included (either it
     * was added to the whitelist or removed from the blacklist), and <code>false</code>
     * otherwise.
     */
    public boolean include(Card c)
    {
        boolean changed = false;

        if (!filter.test(c))
            changed |= whitelist.add(c);
        changed |= blacklist.remove(c);

        return changed;
    }

    /**
     * Check if this Category's filter includes a card.
     *
     * @param c card to test for inclusion
     * @return <code>true</code> if this Category includes the given card, and
     * <code>false</code> otherwise.
     */
    public boolean includes(Card c)
    {
        return (filter.test(c) || whitelist.contains(c)) && !blacklist.contains(c);
    }

    /**
     * Set the Color of the category, and alert any listeners of this event.
     *
     * @param c new Color for the category
     */
    public void setColor(Color c)
    {
        color = c;
    }

    /**
     * Change the filter of the category so a new set of cards is automatically
     * included, and alert any listeners of this event.
     *
     * @param f new filter for the category
     */
    public void setFilter(Filter f)
    {
        filter = f;
    }

    /**
     * Change the name of the category and update any listeners of this event.
     *
     * @param n new name for the category
     */
    public void setName(String n)
    {
        name = n;
    }
}
