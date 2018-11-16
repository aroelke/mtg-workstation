package editor.collection.category;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterAttribute;
import editor.filter.FilterGroup;
import editor.gui.MainFrame;

import java.awt.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

/**
 * This class represents a set of specifications for a category.  Those specifications are its name,
 * the lists of cards to include or exclude regardless of filter, its color, its filter, and its String
 * representation.
 * 
 * TODO: Consider making this immutable (editing functions will return copies)
 *
 * @author Alec Roelke
 */
public class CategorySpec implements Externalizable
{
    // TODO: Remove this
    public static final Map<FilterAttribute, String> CODES = Map.ofEntries(new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.CARD_TYPE, "cardtype"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.ANY, "*"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.FORMAT_LEGALITY, "legal"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.TYPE_LINE, "type"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.BLOCK, "b"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.EXPANSION, "x"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.LAYOUT, "L"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.MANA_COST, "m"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.NAME, "n"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.NONE, "0"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.RARITY, "r"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.SUBTYPE, "sub"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.SUPERTYPE, "super"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.TAGS, "tag"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.LOYALTY, "l"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.ARTIST, "a"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.CARD_NUMBER, "#"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.CMC, "cmc"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.COLOR, "c"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.COLOR_IDENTITY, "ci"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.FLAVOR_TEXT, "f"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.POWER, "p"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.PRINTED_TEXT, "ptext"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.PRINTED_TYPES, "ptypes"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.RULES_TEXT, "o"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.TOUGHNESS, "t"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.GROUP, "group"),
                                                                           new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.DEFAULTS, ""));

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
     * Create a new CategorySpec with the color black and a filter that passes all cards.
     */
    public CategorySpec()
    {
        this("All Cards", Color.BLACK, FilterAttribute.createFilter(FilterAttribute.ANY));
    }

    /**
     * Copy constructor for CategorySpec, except the copy has no listeners.
     *
     * @param original original CategorySpec to copy
     */
    public CategorySpec(CategorySpec original)
    {
        name = original.name;
        whitelist = new HashSet<>(original.whitelist);
        blacklist = new HashSet<>(original.blacklist);
        color = original.color;
        filter = original.filter.copy();
    }

    /**
     * Create a new CategorySpec with the given specifications.
     *
     * @param name      name of the new spec
     * @param whitelist whitelist of the new spec
     * @param blacklist blacklist of the new spec
     * @param color     color of the new spec
     * @param filter    filter of the new spec
     */
    public CategorySpec(String name, Collection<Card> whitelist, Collection<Card> blacklist, Color color, Filter filter)
    {
        this.name = name;
        this.whitelist = new HashSet<>(whitelist);
        this.blacklist = new HashSet<>(blacklist);
        this.color = color;
        this.filter = filter;
    }

    /**
     * Create a new CategorySpec with the given specifications and an empty white- and
     * blacklist.
     *
     * @param name   name of the new spec
     * @param color  color of the new spec
     * @param filter filter of the new spec
     */
    public CategorySpec(String name, Color color, Filter filter)
    {
        this(name, new HashSet<>(), new HashSet<>(), color, filter);
    }

    /**
     * Copy the name, whitelist, blacklist, color, and filter from the given
     * CategorySpec, discarding those values from this one, alerting any
     * listeners of this event.
     *
     * @param other CategorySpec to copy
     * @return true if any changes were made to this CategorySpec, and false
     * otherwise.
     */
    public boolean copy(CategorySpec other)
    {
        CategorySpec old = new CategorySpec(this);

        name = other.name;
        whitelist.clear();
        whitelist.addAll(other.whitelist);
        blacklist.clear();
        blacklist.addAll(other.blacklist);
        color = other.color;
        filter = new FilterGroup();
        filter = other.filter.copy();

        return !equals(old);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof CategorySpec))
            return false;
        CategorySpec o = (CategorySpec)other;
        return name.equals(o.name) && color.equals(o.color) && filter.equals(o.filter)
                && blacklist.equals(o.blacklist) && whitelist.equals(o.whitelist);
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
     * Get the name of the category this CategorySpec represents.
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
     * @return true if the Card was successfully included (either it was added to
     * the whitelist or removed from the blacklist), and false otherwise.
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
     * Check if this CategorySpec's filter includes a card.
     *
     * @param c card to test for inclusion
     * @return true if this CategorySpec includes the given card, and false otherwise.
     */
    public boolean includes(Card c)
    {
        return (filter.test(c) || whitelist.contains(c)) && !blacklist.contains(c);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        int n;

        blacklist.clear();
        whitelist.clear();

        name = in.readUTF();
        color = (Color)in.readObject();

        String code = in.readUTF();
        for (FilterAttribute type: FilterAttribute.values())
        {
            if (code.equals(CODES.get(type)))
            {
                if (type == FilterAttribute.GROUP)
                    filter = new FilterGroup();
                else
                    filter = FilterAttribute.createFilter(type);
                filter.readExternal(in);
                break;
            }
        }

        n = in.readInt();
        for (int i = 0; i < n; i++)
            blacklist.add(MainFrame.inventory().get(in.readUTF()));
        n = in.readInt();
        for (int i = 0; i < n; i++)
            whitelist.add(MainFrame.inventory().get(in.readUTF()));
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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(name);
        out.writeObject(color);

        out.writeUTF(CODES.get(filter.type()));
        filter.writeExternal(out);

        out.writeInt(blacklist.size());
        for (Card card : blacklist)
            out.writeUTF(card.id());
        out.writeInt(whitelist.size());
        for (Card card : whitelist)
            out.writeUTF(card.id());
    }
}
