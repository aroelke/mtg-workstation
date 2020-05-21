package editor.collection.deck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import editor.collection.CardList;
import editor.database.card.Card;

/**
 * This class represents a hand of Cards.  It is a subset of a Deck that is randomized
 * and with multiple copies represented by separate entries. It also breaks the contract
 * with CardList in that multiple copies of a Card are treated as unique entries.
 *
 * @author Alec Roelke
 */
public class Hand implements CardList
{
    /**
     * Deck containing the cards in the hand.
     */
    private Deck deck;
    /**
     * Cards to not include in a drawn hand (for example, sideboards or Commanders).
     */
    private Set<Card> exclusion;
    /**
     * Cards in the Deck, in a random order and with multiple copies represented by multiple
     * entries.
     */
    private List<Card> hand;
    /**
     * Number of cards in the drawn hand.
     */
    private int inHand;

    /**
     * Create a new Hand from the specified Deck.
     *
     * @param deck deck to draw Cards from
     */
    public Hand(Deck deck)
    {
        this(deck, new HashSet<>());
    }

    /**
     * Create a new Hand from the specified Deck, excluding the specified Cards.
     *
     * @param deck  deck to draw Cards from
     * @param cards cards to never include in the sample hand
     */
    public Hand(Deck deck, Set<Card> cards)
    {
        super();
        hand = new ArrayList<>();
        exclusion = new LinkedHashSet<>(cards);
        inHand = 0;
        this.deck = deck;
        refresh();
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
     * Remove all Cards from this hand.  New ones cannot be drawn until a refresh is performed.
     *
     * @see Hand#refresh()
     */
    @Override
    public void clear()
    {
        hand.clear();
        inHand = 0;
    }

    /**
     * Remove all Cards from the exclusion list.
     */
    public void clearExclusion()
    {
        exclusion.clear();
    }

    /**
     * Check if a card is in the drawn hand.
     *
     * @param card card to look for
     * @return true if the given card is in the drawn cards of this Hand, and false otherwise.
     */
    @Override
    public boolean contains(Card card)
    {
        return hand.subList(0, inHand).contains(card);
    }

    /**
     * Check if all of the given cards are in the drawn hand.
     *
     * @param cards collection of Cards to look for
     * @return true if the given Objects are all in the drawn cards of this Hand, and false
     * otherwise.
     */
    @Override
    public boolean containsAll(Collection<? extends Card> cards)
    {
        return hand.subList(0, inHand).containsAll(cards);
    }

    /**
     * Draw a card.
     */
    public void draw()
    {
        inHand++;
    }

    /**
     * Exclude a Card from being drawn in the sample hand.
     *
     * @param card card to exclude
     * @return true if the Card was added (which only happens if it wasn't in the exclusion list
     * already), and false otherwise.
     */
    public boolean exclude(Card card)
    {
        return exclusion.add(card);
    }

    /**
     * Get the cards that will not be drawn by this Hand.
     *
     * @return The list of cards to never draw in a hand.
     */
    public List<Card> excluded()
    {
        return new ArrayList<>(exclusion);
    }

    @Override
    public Card get(int index)
    {
        if (index >= inHand)
            throw new IndexOutOfBoundsException();
        return hand.get(index);
    }

    /**
     * {@inheritDoc}
     * The deck's metadata is used.
     */
    @Override
    public Entry getEntry(Card card)
    {
        return deck.getEntry(card);
    }

    /**
     * {@inheritDoc}
     * The Deck's metadata is used.
     */
    @Override
    public Entry getEntry(int index) throws IndexOutOfBoundsException
    {
        return deck.getEntry(get(index));
    }

    /**
     * Get the currently-drawn cards.
     *
     * @return a list of cards representing those in the sample hand.
     */
    public List<Card> getHand()
    {
        return hand.subList(0, size());
    }

    @Override
    public int indexOf(Card card)
    {
        return hand.indexOf(card);
    }

    /**
     * {@inheritDoc}
     * A Hand is also considered empty if no cards are drawn.
     */
    @Override
    public boolean isEmpty()
    {
        return hand.isEmpty() || inHand == 0;
    }

    @Override
    public Iterator<Card> iterator()
    {
        return hand.subList(0, inHand).iterator();
    }

    /**
     * Take a mulligan, or shuffle the deck and draw a new hand, but with one fewer
     * card in it.
     */
    public void mulligan()
    {
        if (inHand > 0)
        {
            Collections.shuffle(hand);
            inHand--;
        }
    }

    /**
     * Shuffle the deck and draw a new starting hand.
     *
     * @param n size of the new hand
     */
    public void newHand(int n)
    {
        refresh();
        Collections.shuffle(hand);
        inHand = Math.min(n, hand.size());
    }

    /**
     * Update the state of this Hand to exclude cards in the exclusion list.
     */
    public void refresh()
    {
        clear();
        for (Card c : deck)
            if (!exclusion.contains(c))
                for (int i = 0; i < deck.getEntry(c).count(); i++)
                    hand.add(c);
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
    public Set<Card> removeAll(Set<? extends Card> coll) throws UnsupportedOperationException
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
     * {@inheritDoc}
     * Only drawn cards count toward size.
     */
    @Override
    public int size()
    {
        return Math.min(inHand, hand.size());
    }

    /**
     * {@inheritDoc}
     * Only drawn cards will be put into the array.
     */
    @Override
    public Card[] toArray()
    {
        return hand.subList(0, inHand).toArray(new Card[inHand]);
    }

    /**
     * {@inheritDoc}
     * Only drawn cards count toward size.  Since each card is represented as a unique
     * entry, this is the same as {@link #size()}.
     */
    @Override
    public int total()
    {
        return size();
    }

    @Override
    public void sort(Comparator<? super CardList.Entry> c)
    {
        hand.sort((a, b) -> c.compare(deck.getEntry(a), deck.getEntry(b)));
    }
}
