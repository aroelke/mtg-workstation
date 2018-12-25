package editor.collection;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
 * <p>
 * TODO: Serializing this might make initial loading of inventory faster
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
     * This class represents the data that can be transferred from an inventory via
     * drag and drop or cut/copy/paste.  It supports card and String flavors.
     *
     * @author Alec Roelke
     */
    public static class TransferData implements Transferable
    {
        /**
         * Cards to be transferred.
         */
        private Card[] cards;

        /**
         * Create a new TransferData from the given cards.
         *
         * @param cards cards to transfer
         */
        public TransferData(Card... cards)
        {
            this.cards = cards;
        }

        /**
         * Create a new TransferData from the given cards.
         *
         * @param cards cards to transfer
         */
        public TransferData(Collection<Card> cards)
        {
            this(cards.toArray(new Card[0]));
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
        {
            if (flavor.equals(Card.cardFlavor))
                return cards;
            else if (flavor.equals(DataFlavor.stringFlavor))
                return Arrays.stream(cards).map(Card::unifiedName).reduce("", (a, b) -> a + "\n" + b);
            else
                throw new UnsupportedFlavorException(flavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[]{Card.cardFlavor, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return Arrays.asList(getTransferDataFlavors()).contains(flavor);
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
    private final Map<Long, Card> ids;

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
     * Get the card in this Inventory with the given UID.
     *
     * @param id multiverseid of the Card to look for
     * @return the Card with the given multiverseid, or null if no such card exists.
     * @see Card#id
     */
    public Card get(long id)
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
    public void sort(Comparator<Card> comp)
    {
        cards.sort(comp);
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