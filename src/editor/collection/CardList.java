package editor.collection;

import java.awt.datatransfer.DataFlavor;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import editor.collection.category.CategorySpec;
import editor.database.card.Card;
import editor.database.characteristics.CardAttribute;

/**
 * This class represents a collection of cards.  Each card should be represented by a single entry
 * that keeps track of information such as how many copies are in the CardList.  If an implementation
 * of Card list doesn't do this, that should be noted by its documentation.
 * <p>
 * This does not implement {@link Collection}<Card>, {@link List}<Card>, or {@link Set}<Card> because
 * its behavior for add and remove are slightly different than any of them.  For example, if a card is
 * added twice, it should only take up one place in the list (like Set), but when if it is removed once,
 * it should still be part of the CardList (like List).  It doesn't implement {@link Collection}<Card>
 * because Collection is bound by legacy definitions like remove taking an Object and not being generic.
 *
 * @author Alec Roelke
 */
public interface CardList extends Iterable<Card>
{
    /**
     * This class represents an entry in a {@link CardList} whose actual implementation is dependent on the parent
     * list.  It contains metadata about a card such as count and date added.
     *
     * @author Alec Roelke
     */
    interface Entry
    {
        /**
         * Get this Entry's card.
         *
         * @return the Card this Entry has data for.
         */
        Card card();

        /**
         * Get the categories this Entry's card belongs to.
         *
         * @return the CategorySpecs in the parent CardList this Entry's Card matches (optional
         * operation)
         * @throws UnsupportedOperationException if this operation is not supported
         */
        Set<CategorySpec> categories() throws UnsupportedOperationException;

        /**
         * Get the number of copies of this Entry's card in the parent {@link CardList}.
         *
         * @return the number of copies in the parent CardList of this Entry's Card (optional operation).
         * @throws UnsupportedOperationException if this operation is not supported
         */
        int count() throws UnsupportedOperationException;

        /**
         * Get the date this Entry's card was added to the parent {@link CardList}.
         *
         * @return the date this Entry's Card was added (optional operation).
         * @throws UnsupportedOperationException if this operation is not supported
         */
        LocalDate dateAdded() throws UnsupportedOperationException;

        /**
         * Get some information about this Entry's Card.
         *
         * @param data type of information to get
         * @return the value of the given information about this Entry's Card.
         */
        default Object get(CardAttribute data)
        {
            switch (data)
            {
            case NAME:
                return card().unifiedName();
            case LAYOUT:
                return card().layout();
            case MANA_COST:
                return card().manaCost();
            case CMC:
                return card().cmc();
            case COLORS:
                return card().colors();
            case COLOR_IDENTITY:
                return card().colorIdentity();
            case TYPE_LINE:
                return card().unifiedTypeLine();
            case EXPANSION_NAME:
                return card().expansion().toString();
            case RARITY:
                return card().rarity();
            case POWER:
                return card().power();
            case TOUGHNESS:
                return card().toughness();
            case LOYALTY:
                return card().loyalty();
            case ARTIST:
                return card().artist().get(0);
            case CARD_NUMBER:
                return String.join(' ' + Card.FACE_SEPARATOR + ' ', card().number());
            case LEGAL_IN:
                return card().legalIn();
            case COUNT:
                return count();
            case CATEGORIES:
                return categories();
            case DATE_ADDED:
                return dateAdded();
            default:
                throw new IllegalArgumentException("Unknown data type " + data);
            }
        }
    }

    /**
     * Data flavor representing entries in a deck.  Transfer data will appear as a
     * map of cards onto an integer representing the number of copies to transfer.
     */
    DataFlavor entryFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Map.class.getName() + "\"", "Deck Entries");

    /**
     * Add one copy of a Card to this CardList (optional operation).
     *
     * @param card card to add
     * @return true if a copy of the Card was added, and false otherwise.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    boolean add(Card card) throws UnsupportedOperationException;

    /**
     * Add some number of copies of a Card to this CardList (optional operation).
     *
     * @param card   card to add copies of
     * @param amount Number of copies to add
     * @return true if any copies were added, and false otherwise.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    boolean add(Card card, int amount) throws UnsupportedOperationException;

    /**
     * Add all cards in the given CardList to this CardList.
     *
     * @param cards CardList of cards to add
     * @return true if any of the Cards were added, and false otherwise.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    boolean addAll(CardList cards) throws UnsupportedOperationException;

    /**
     * Add some numbers of copies of each of the given cards to this CardList (optional operation).
     *
     * @param amounts map of Cards onto amounts of them to add to this CardList
     * @return true if any Cards were added, and false otherwise.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    boolean addAll(Map<? extends Card, ? extends Integer> amounts) throws UnsupportedOperationException;

    /**
     * Add one copy of each of the given set of Cards to this CardList (optional operation).
     *
     * @param cards set of Cards to add
     * @return true if any of the Cards were added, and false otherwise.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    boolean addAll(Set<? extends Card> cards) throws UnsupportedOperationException;

    /**
     * Remove all Cards from this CardList (optional operation).
     *
     * @throws UnsupportedOperationException if this operation is not supported
     */
    void clear() throws UnsupportedOperationException;

    /**
     * Check if this CardList contains a particular card.
     *
     * @param card card to look for
     * @return true if this CardList contains the specified card, and false otherwise.
     */
    boolean contains(Card card);

    /**
     * Check if this CardLIst contains all of the given cards.
     *
     * @param cards collection of Cards to look for
     * @return true if this CardCollection contains all of the specified cards, and false
     * otherwise.
     */
    boolean containsAll(Collection<? extends Card> cards);

    /**
     * Get a card from the CardList.
     *
     * @param index index of the Card to look for
     * @return the Card at the given index
     * @throws IndexOutOfBoundsException if the index is less than 0 or is too big
     */
    Card get(int index) throws IndexOutOfBoundsException;

    /**
     * Get the metadata of the given card.
     *
     * @param card card to look up
     * @return the {@link Entry} corresponding to the given card, or null if no such card exists in
     * this CardList.
     */
    Entry getData(Card card);

    /**
     * Get the metadata of the card at a specific position in this CardList
     *
     * @param index index to look up
     * @return the {@link Entry} corresponding to the Card at the given index
     * @throws IndexOutOfBoundsException if the index is less than 0 or is too big
     */
    Entry getData(int index) throws IndexOutOfBoundsException;

    /**
     * Find the index of the given card in this CardList.
     *
     * @param card card to look for
     * @return the index of the given card in this CardList, or -1 if it isn't in it.
     */
    int indexOf(Card card);

    /**
     * Check if there are no cards in this CardList.
     *
     * @return true if this CardList contains no cards, and false otherwise.
     */
    boolean isEmpty();

    @Override
    Iterator<Card> iterator();

    /**
     * Returns a possibly parallel Stream with this CardList as its source.
     *
     * @return A parallel Stream of the Cards in this CardList.
     * @see Collection#parallelStream()
     */
    default Stream<Card> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * Remove a copy of a Card from this CardList (optional operation).
     *
     * @param card card to remove
     * @return true if the Card was removed, and false otherwise.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    boolean remove(Card card) throws UnsupportedOperationException;

    /**
     * Remove some number of copies of a Card from this CardList (optional operation).
     *
     * @param card   card to remove
     * @param amount number of copies to remove
     * @return the actual number of copies that were removed.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    int remove(Card card, int amount) throws UnsupportedOperationException;

    /**
     * For each card in the given CardList, remove the number of copies of that Card in
     * that CardList from this CardList (optional operation).
     *
     * @param cards CardList of cards to remove
     * @return a map containing the cards that had copies removed and the number of
     * copies of each one that were removed.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Map<Card, Integer> removeAll(CardList cards) throws UnsupportedOperationException;

    /**
     * Remove some numbers of copies of the given Cards from this CardList (optional
     * operation).
     *
     * @param cards cards to remove and the number of copies of each one to remove
     * @return A map containing the Cards that had copies removed and the number of copies
     * of each one that were removed.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> cards) throws UnsupportedOperationException;

    /**
     * Remove one copy of each of the given Cards from this CardList (optional operation).
     *
     * @param cards cards to remove
     * @return the set of Cards that had a copy removed.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Set<Card> removeAll(Set<? extends Card> cards) throws UnsupportedOperationException;

    /**
     * Set the number of copies of a Card to the specified number (optional
     * operation).  If the number is 0, then the Card is removed entirely.
     *
     * @param card   card to set the count of
     * @param amount number of copies to set
     * @return true if this CardList changed as a result, and false otherwise.
     * @throws UnsupportedOperationException if this operation is not supported
     */
    boolean set(Card card, int amount) throws UnsupportedOperationException;

    /**
     * Set the number of copies of the Card at the specified index (optional
     * operation).
     *
     * @param index  index of the Card to set the number of
     * @param amount number of copies of the Card to set
     * @return true if this CardList changed as a result, and false otherwise.
     * @throws IndexOutOfBoundsException     if the index is less than 0 or is too big
     * @throws UnsupportedOperationException if this operation is not supported
     */
    boolean set(int index, int amount) throws IndexOutOfBoundsException, UnsupportedOperationException;

    /**
     * Get the number of unique cards in this CardList.
     *
     * @return The number of unique card {@link Entry}s in this CardList.
     * @see CardList#total()
     */
    int size();

    /**
     * Returns a sequential Stream with this CardList its source.
     *
     * @return A Stream of the Cards in this CardList that is not necessarily parallel.
     * @see Collection#stream()
     */
    default Stream<Card> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Convert this CardList into an array containing the unique cards in it.
     *
     * @return an array containing all of the cards in this CardList.
     */
    Card[] toArray();

    /**
     * Get the total number of cards in this CardList, accounting for multiple copies.
     *
     * @return the total number of cards in this CardList.
     * @see CardList#size()
     */
    int total();
}
