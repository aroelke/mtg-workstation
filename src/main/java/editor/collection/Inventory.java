package editor.collection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.collection.deck.CategorySpec;
import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.leaf.BinaryFilter;

/**
 * This class represents an inventory of cards that can be added to decks.
 *
 * @author Alec Roelke
 */
public class Inventory implements CardList
{
    /**
     * This class represents a card's entry in the Inventory.  It can only tell a Card's
     * date "added," which is the date its expansion was released.
     *
     * @author Alec Roelke
     */
    private class InventoryEntry implements Entry
    {
        /**
         * Card for this InventoryEntry.
         */
        private final Card card;

        /**
         * Create a new InventoryEntry.
         *
         * @param card card corresponding to the new entry
         */
        private InventoryEntry(Card card)
        {
            this.card = card;
        }

        @Override
        public Card card()
        {
            return card;
        }

        @Override
        public Set<CategorySpec> categories()
        {
            throw new UnsupportedOperationException("Inventory cannot have categories.");
        }

        @Override
        public int count()
        {
            throw new UnsupportedOperationException("Inventory does not count card copies.");
        }

        /**
         * {@inheritDoc}
         * The date will be the date the card's expansion was added.
         */
        @Override
        public LocalDate dateAdded()
        {
            return card.expansion().releaseDate;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (!(other instanceof InventoryEntry))
                return false;
            InventoryEntry o = (InventoryEntry)other;
            return card.equals(o.card);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(card);
        }
    }

    /**
     * Master list of cards.
     */
    private final List<Card> cards;
    /**
     * Filter for Cards in the Inventory pane.
     */
    private Filter filter;
    /**
     * Filtered view of the master list.
     */
    private List<Card> filtrate;
    /**
     * Map of Card multiverseids onto their cards.
     */
    private final Map<Integer, Card> ids;

    /**
     * Create an empty Inventory.  Be careful, because Inventories are immutable.
     */
    public Inventory()
    {
        this(new ArrayList<>());
    }

    /**
     * Create a new Inventory with the given list of cards.
     *
     * @param list List of Cards
     */
    public Inventory(Collection<Card> list)
    {
        cards = new ArrayList<>(list);
        ids = cards.stream().collect(Collectors.toMap((c) -> c.multiverseid().get(0), Function.identity()));
        filter = new BinaryFilter(true);
        filtrate = cards;
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean add(Card card) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean add(Card card, int amount) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean addAll(CardList cards) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean addAll(Map<? extends Card, ? extends Integer> amounts) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean addAll(Set<? extends Card> cards) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public void clear() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Card card)
    {
        return ids.values().contains(card);
    }

    /**
     * Determine if there is a card with the given Gatherer ID.
     * 
     * @param multiverseid ID to check
     * @return <code>true</code> if a Card with the given Gatherer ID exists in the
     * inventory, and <code>false</code> otherwise.
     */
    public boolean contains(int multiverseid)
    {
        return ids.keySet().contains(multiverseid);
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean containsAll(Collection<? extends Card> cards) throws UnsupportedOperationException
    {
        return this.cards.containsAll(cards);
    }

    @Override
    public Card get(int index) throws IndexOutOfBoundsException
    {
        return filtrate.get(index);
    }

    /**
     * Get the card in this Inventory with the given multiverseid.
     *
     * @param id multiverseid of the Card to look for
     * @return the Card with the given multiverseid, or null if no such card exists.
     * @see Card#id
     */
    public Card find(int id)
    {
        return ids.get(id);
    }

    /**
     * {@inheritDoc}
     * The metadata will only have the card's release date.
     */
    @Override
    public Entry getEntry(Card card)
    {
        return new InventoryEntry(card);
    }

    /**
     * {@inheritDoc}
     * The metadata will only have the card's release date.
     */
    @Override
    public Entry getEntry(int index) throws IndexOutOfBoundsException
    {
        return new InventoryEntry(get(index));
    }

    /**
     * Get the filter for the cards in the inventory.
     *
     * @return the current filter for cards in the inventory.
     */
    public Filter getFilter()
    {
        return filter.copy();
    }

    @Override
    public int indexOf(Card card)
    {
        return filtrate.indexOf(card);
    }

    /**
     * {@inheritDoc}
     * An Inventory is considered empty if its filter filters out all cards.
     */
    @Override
    public boolean isEmpty()
    {
        return filtrate.isEmpty();
    }

    @Override
    public Iterator<Card> iterator()
    {
        return cards.iterator();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean remove(Card card) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public int remove(Card card, int amount) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public Map<Card, Integer> removeAll(CardList cards) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> amounts) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public Set<Card> removeAll(Set<? extends Card> cards) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean set(Card card, int amount) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Not supported.
     */
    @Override
    public boolean set(int index, int amount) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @return The number of Cards in this Inventory.
     */
    @Override
    public int size()
    {
        return filtrate.size();
    }

    /**
     * Sort the list using the specified Comparator.
     *
     * @param comp Comparator to use for sorting
     */
    public void sort(Comparator<? super CardList.Entry> c)
    {
        cards.sort((a, b) -> c.compare(new InventoryEntry(a), new InventoryEntry(b)));
    }

    /**
     * @return An array containing all the cards in the inventory.
     */
    @Override
    public Card[] toArray()
    {
        return cards.toArray(new Card[size()]);
    }

    /**
     * @return The total number of cards in the inventory, even ones that are filtered
     * out.
     */
    @Override
    public int total()
    {
        return cards.size();
    }

    /**
     * Update the filtered view of this Inventory.
     *
     * @param filter New filter
     */
    public void updateFilter(Filter f)
    {
        filter = f;
        filtrate = cards.stream().filter(filter).collect(Collectors.toList());
    }
}