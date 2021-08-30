package editor.collection.deck;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.database.card.Card;

/**
 * This class represents a deck which can have cards added and removed (in quantity) and have several category
 * views (from which cards can also be added or removed).
 *
 * @author Alec Roelke
 */
public class Deck implements CardList
{
    /**
     * This class represents a category of a deck.  If a card is added or removed using the add and remove
     * methods, the master list will be updated to reflect this only if the card passes through the Category's filter.
     *
     * @author Alec Roelke
     */
    private class CategoryCache implements CardList
    {
        /**
         * Specification for the cards contained in this Category.
         */
        private Category spec;
        /**
         * List representing the filtered view of the master list.
         */
        private List<Card> filtrate;
        /**
         * Rank of this Category.
         */
        private int rank;

        /**
         * Create a new Category.
         *
         * @param spec specifications for the new Category
         */
        public CategoryCache(Category spec)
        {
            rank = categories.size();
            update(spec);
        }

        /**
         * {@inheritDoc}
         * Only add the card if it passes through the specification's filter.  If it doesn't,
         * return <code>false</code>.
         */
        @Override
        public boolean add(Card card)
        {
            return add(card, 1);
        }

        /**
         * {@inheritDoc}
         * Only add the card if it passes through the specification's filter.  If it doesn't,
         * return <code>false</code>.
         */
        @Override
        public boolean add(Card card, int amount)
        {
            return spec.includes(card) && Deck.this.add(card, amount);
        }

        /**
         * {@inheritDoc}
         * Only add the cards that pass through the specification's filter to the deck.
         */
        @Override
        public boolean addAll(CardList cards)
        {
            return Deck.this.addAll(cards.stream().filter(spec::includes).collect(Collectors.toMap(Function.identity(), (c) -> cards.getEntry(c).count())));
        }

        /**
         * {@inheritDoc}
         * Only add the cards that pass through the specification's filter to the deck.
         */
        @Override
        public boolean addAll(Map<? extends Card, ? extends Integer> amounts)
        {
            return Deck.this.addAll(amounts.entrySet().stream().filter((e) -> spec.includes(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        /**
         * {@inheritDoc}
         * Only add the cards that pass through the specification's filter to the deck.
         */
        @Override
        public boolean addAll(Set<? extends Card> cards)
        {
            return Deck.this.addAll(cards.stream().filter(spec::includes).collect(Collectors.toSet()));
        }

        /**
         * Remove all copies of Cards that belong to this Category from the deck.
         */
        @Override
        public void clear()
        {
            Deck.this.removeAll(this);
        }

        @Override
        public boolean contains(Card card)
        {
            return filtrate.contains(card);
        }

        @Override
        public boolean containsAll(Collection<? extends Card> cards)
        {
            for (Card c : cards)
                if (!contains(c))
                    return false;
            return true;
        }

        @Override
        public Card get(int index) throws IndexOutOfBoundsException
        {
            return filtrate.get(index);
        }

        @Override
        public CardList.Entry getEntry(Card card) throws IllegalArgumentException
        {
            return spec.includes(card) ? Deck.this.getEntry(card) : null;
        }

        @Override
        public CardList.Entry getEntry(int index) throws IndexOutOfBoundsException
        {
            return getEntry(get(index));
        }

        @Override
        public int indexOf(Card card) throws IllegalArgumentException
        {
            return spec.includes(card) ? filtrate.indexOf(card) : -1;
        }

        @Override
        public boolean isEmpty()
        {
            return size() == 0;
        }

        @Override
        public Iterator<Card> iterator()
        {
            return filtrate.iterator();
        }

        /**
         * {@inheritDoc}
         * Only remove the card if it is included in this Category's specification.
         */
        @Override
        public boolean remove(Card card)
        {
            return remove(card, 1) > 0;
        }

        /**
         * {@inheritDoc}
         * Only remove the card if it is included in this Category's specification.
         */
        @Override
        public int remove(Card card, int amount)
        {
            return contains(card) ? Deck.this.remove(card, amount) : 0;
        }

        /**
         * {@inheritDoc}
         * Only remove the cards that are included in this Category's specification.
         */
        @Override
        public Map<Card, Integer> removeAll(CardList cards)
        {
            return Deck.this.removeAll(cards.stream().filter(spec::includes).collect(Collectors.toMap(Function.identity(), (c) -> cards.getEntry(c).count())));
        }

        /**
         * {@inheritDoc}
         * Only remove the cards that are included in this Category's specification.
         */
        @Override
        public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> amounts)
        {
            return Deck.this.removeAll(amounts.entrySet().stream().filter((e) -> spec.includes(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        /**
         * {@inheritDoc}
         * Only remove the cards that are included in this Category's specification.
         */
        @Override
        public Set<Card> removeAll(Set<? extends Card> cards)
        {
            return Deck.this.removeAll(cards.stream().filter(spec::includes).collect(Collectors.toSet()));
        }

        @Override
        public boolean set(Card card, int amount)
        {
            return spec.includes(card) && Deck.this.set(card, amount);
        }

        @Override
        public boolean set(int index, int amount) throws IllegalArgumentException
        {
            return set(get(index), amount);
        }

        @Override
        public int size()
        {
            return filtrate.size();
        }

        @Override
        public Card[] toArray()
        {
            return filtrate.toArray(new Card[filtrate.size()]);
        }

        /**
         * {@inheritDoc}
         *
         * @see Category#toString()
         */
        @Override
        public String toString()
        {
            return spec.toString();
        }

        @Override
        public int total()
        {
            return filtrate.stream().map(Deck.this::getEntry).mapToInt(Entry::count).sum();
        }

        /**
         * Update this category so its filtrate reflects the new filter, whitelist, and blacklist.
         */
        public void update(Category s)
        {
            spec = s;
            filtrate = masterList.stream().map((e) -> e.card).filter(spec::includes).collect(Collectors.toList());
            for (DeckEntry e : masterList)
            {
                if (spec.includes(e.card))
                    e.categories.add(this);
                else
                    e.categories.remove(this);
            }
        }

        @Override
        public void sort(Comparator<? super CardList.Entry> c)
        {
            throw new UnsupportedOperationException("Only the main deck can be sorted");
        }
    }

    /**
     * This class represents an entry into a deck.  It has a card, a number of copies of that card,
     * a set of categories the card belongs to, and the date the card was added.
     *
     * @author Alec Roelke
     */
    private class DeckEntry implements CardList.Entry
    {
        /**
         * Card in this DeckEntry.
         */
        private final Card card;
        /**
         * Number of copies of the Card.
         */
        private int count;
        /**
         * Date this DeckEntry was created (the Card was originally added).
         */
        private final LocalDate date;
        /**
         * Set of categories this DeckEntry's Card belongs to.  Implemented using a
         * LinkedHashSet so it will maintain the ordering that categories were added.
         */
        private final LinkedHashSet<CategoryCache> categories;

        /**
         * Create a new DeckEntry.
         *
         * @param card   card for this DeckEntry
         * @param amount number of initial copies in this Entry
         * @param added  date the Card was added
         */
        public DeckEntry(Card card, int amount, LocalDate added)
        {
            this.card = card;
            count = amount;
            date = added;
            categories = new LinkedHashSet<>();
        }

        /**
         * Add copies to this DeckEntry.
         *
         * @param amount copies to add
         * @return true if any copies were added, and false otherwise.
         */
        private boolean add(int amount)
        {
            if (amount < 1)
                return false;
            count += amount;
            return true;
        }

        @Override
        public Card card()
        {
            return card;
        }

        @Override
        public Set<Category> categories()
        {
            return categories.stream().map((category) -> category.spec).collect(Collectors.toSet());
        }

        @Override
        public int count()
        {
            return count;
        }

        @Override
        public LocalDate dateAdded()
        {
            return date;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (other instanceof DeckEntry o)
                return card.equals(o.card) && o.count == count && o.date.equals(date);
            return false;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(card, count, date);
        }

        /**
         * Remove copies from this DeckEntry.  There can't be fewer than
         * 0 copies.
         *
         * @param amount Number of copies to remove.
         * @return the number of copies that were actually removed (in case there
         * are now 0).
         */
        private int remove(int amount)
        {
            if (amount < 1)
                return 0;
            int old = count;
            count -= Math.min(count, amount);
            return old - count;
        }
    }

    /**
     * Formatter for dates, usually for formatting the add date of a card.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    /**
     * List of cards in this Deck.
     */
    private List<DeckEntry> masterList;
    /**
     * Categories in this Deck.
     */
    private Map<String, CategoryCache> categories;
    /**
     * Total number of cards in this Deck, accounting for multiples.
     */
    private int total;

    /**
     * Create a new, empty Deck with no categories.
     */
    public Deck()
    {
        masterList = new ArrayList<>();
        categories = new LinkedHashMap<>();
        total = 0;
    }

    /**
     * Create a new Deck that is a copy of another Deck.
     * 
     * @param d Deck to copy
     */
    public Deck(Deck d)
    {
        this();
        for (DeckEntry e : d.masterList)
            add(e.card, e.count, e.date);
        for (CategoryCache c : d.categories.values())
            this.addCategory(c.spec);
    }

    @Override
    public boolean add(Card card)
    {
        return add(card, 1);
    }

    @Override
    public boolean add(Card card, int amount)
    {
        return add(card, amount, LocalDate.now());
    }

    /**
     * Add some number of cards to this deck.  If the number is not positive, then no changes are
     * made.  Normally this should only be used when loading a deck, and will not affects an
     * existing card's add date.
     *
     * @param card   card to add
     * @param amount number of copies to add
     * @param date   date the card was originally added
     * @return true if the Deck changed as a result, and false otherwise, which is when the number
     * to add is less than 1.
     */
    public boolean add(Card card, int amount, LocalDate date)
    {
        if (amount < 1)
            return false;

        DeckEntry entry = (DeckEntry)getEntry(card);
        if (entry.count == 0)
        {
            masterList.add(entry = new DeckEntry(card, 0, date));
            for (CategoryCache category : categories.values())
            {
                if (category.spec.includes(card))
                {
                    category.filtrate.add(card);
                    entry.categories.add(category);
                }
            }
        }
        entry.add(amount);
        total += amount;

        return true;
    }

    @Override
    public boolean addAll(CardList d)
    {
        var added = new HashMap<Card, Integer>();
        for (Card card : d)
            if (add(card, d.getEntry(card).count(), d.getEntry(card).dateAdded()))
                added.put(card, d.getEntry(card).count());
        return !added.isEmpty();
    }

    @Override
    public boolean addAll(Map<? extends Card, ? extends Integer> amounts)
    {
        var added = new HashMap<Card, Integer>();
        for (Card card : amounts.keySet())
            if (add(card, amounts.get(card), LocalDate.now()))
                added.put(card, amounts.get(card));
        return !added.isEmpty();
    }

    @Override
    public boolean addAll(Set<? extends Card> cards)
    {
        return addAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> 1)));
    }

    /**
     * Add a new Category.
     *
     * @param spec specification for the new Category
     * @return the new Category, or the old one if one with that name already existed.
     */
    public CardList addCategory(Category spec)
    {
        createCategory(spec);
        return categories.get(spec.getName());
    }

    /**
     * Add a new category at the specified rank.  If there's already a category
     * with that rank, attempt to swap with it (so it will be the highest rank).
     * 
     * @param spec specification for the new Category
     * @param rank rank of the new category
     * @return the new Category, or the old one if it already exists
     * @throws IllegalArgumentException if there already is a category with that
     * name and the rank can't be switched.
     */
    public CardList addCategory(Category spec, int rank)
    {
        if (createCategory(spec))
        {
            CategoryCache c = categories.get(spec.getName());
            c.rank = rank;
            return c;
        }
        else if (categories.get(spec.getName()).rank == rank)
            return categories.get(spec.getName());
        else if (swapCategoryRanks(spec.getName(), rank))
            return categories.get(spec.getName());
        else
            throw new IllegalArgumentException("Could not add new category " + spec.getName() + " at rank " + rank);
    }

    /**
     * Get all the categories.
     *
     * @return a collection of all of the specifications of the categories in the deck,
     * in no particular order.
     */
    public Collection<Category> categories()
    {
        return categories.values().stream().map((category) -> category.spec).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Also remove all categories.
     */
    @Override
    public void clear()
    {
        masterList.clear();
        categories.clear();
        total = 0;
    }

    @Override
    public boolean contains(Card card)
    {
        return getEntry(card).count() > 0;
    }

    @Override
    public boolean containsAll(Collection<? extends Card> cards)
    {
        for (Card c : cards)
            if (!contains(c))
                return false;
        return true;
    }

    /**
     * Check if the deck contains a category with the given name.
     *
     * @param name name of the category to look for
     * @return true if this Deck has a Category with the given name, and false otherwise.
     */
    public boolean containsCategory(String name)
    {
        return categories.containsKey(name);
    }

    /**
     * Add a new Category.
     *
     * @param spec specification for the new Category
     * @return <code>true</code> if the category was created, and <code>false</code>
     * otherwise
     */
    private boolean createCategory(Category spec)
    {
        if (!categories.containsKey(spec.getName()))
        {
            CategoryCache c = new CategoryCache(spec);
            categories.put(spec.getName(), c);
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (other instanceof Deck o)
            return o.masterList.equals(masterList) && o.categories().equals(categories());
        return false;
    }

    /**
     * Exclude a card from a category, even if it passes through its filter.
     *
     * @param name name of the category to exclude from
     * @param card card to exclude
     * @return true if the card was successfully excluded from the category, and false
     * otherwise.
     */
    public boolean exclude(String name, Card card)
    {
        return contains(card) && categories.get(name).spec.exclude(card);
    }

    @Override
    public Card get(int index) throws IndexOutOfBoundsException
    {
        return masterList.get(index).card;
    }

    /**
     * Get the category with the given name.
     *
     * @param name name of the category to get
     * @return a {@link CardList} containing cards in the category with the given name.
     */
    public CardList getCategoryList(String name)
    {
        return categories.get(name);
    }

    /**
     * Get the rank of the category with the given name.
     *
     * @param name name of the category to search for
     * @return the rank of the given category, or -1 if no category with the given name
     * exists.
     */
    public int getCategoryRank(String name)
    {
        return containsCategory(name) ? categories.get(name).rank : -1;
    }

    /**
     * Get the specification for the category with the given name.
     *
     * @param name name of the category whose specification is desired
     * @return a copy of the specification of the category with the given name.
     * @throws IllegalArgumentException if no such category exists
     */
    public Category getCategorySpec(String name) throws IllegalArgumentException
    {
        if (categories.containsKey(name))
            return new Category(categories.get(name).spec);
        else
            throw new IllegalArgumentException("No category named " + name + " found");
    }

    @Override
    public CardList.Entry getEntry(Card card)
    {
        for (DeckEntry e : masterList)
            if (e.card.equals(card))
                return e;
        return new DeckEntry(card, 0, null);
    }

    @Override
    public CardList.Entry getEntry(int index)
    {
        return masterList.get(index);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(masterList, categories);
    }

    @Override
    public int indexOf(Card card)
    {
        return masterList.indexOf(getEntry(card));
    }

    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    public Iterator<Card> iterator()
    {
        return masterList.stream().map(DeckEntry::card).iterator();
    }

    /**
     * Get the number of categories in the deck.
     *
     * @return the number of categories.
     */
    public int numCategories()
    {
        return categories.size();
    }

    @Override
    public boolean remove(Card card)
    {
        return remove(card, Integer.MAX_VALUE) > 0;
    }

    @Override
    public int remove(Card card, int amount)
    {
        if (amount < 1)
            return 0;

        DeckEntry entry = (DeckEntry)getEntry(card);
        if (entry.count == 0)
            return 0;

        int removed = entry.remove(amount);
        if (removed > 0)
        {
            if (entry.count == 0)
            {
                for (CategoryCache category : categories.values())
                {
                    if (category.spec.getWhitelist().contains(card))
                        category.spec.exclude(card);
                    if (category.spec.getBlacklist().contains(card))
                        category.spec.include(card);
                    category.filtrate.remove(card);
                }
                masterList.remove(entry);
            }
            total -= removed;
        }

        return removed;
    }

    /**
     * Remove a category from the deck.
     *
     * @param spec specification of the category to remove
     * @return <code>true</code> if the deck changed as a result, and <code>false</code>
     * otherwise.
     */
    public boolean removeCategory(Category spec)
    {
        CategoryCache c = categories.get(spec.getName());
        if (c != null)
        {
            for (DeckEntry e : masterList)
                e.categories.remove(c);
            var oldRanks = new HashMap<String, Integer>();
            for (CategoryCache category : categories.values())
            {
                if (category.rank > c.rank)
                {
                    oldRanks.put(category.spec.getName(), category.rank);
                    category.rank--;
                }
            }
            categories.remove(spec.getName());

            if (!oldRanks.isEmpty())
                oldRanks.put(c.spec.getName(), c.rank);
            return true;

        }
        else
            return false;
    }

    /**
     * Remove a category from the deck.
     * 
     * @param name name of the category to remove
     * @return <code>true</code> if the deck changed as a result, and <code>false</code>
     * otherwise.
     */
    public boolean removeCategory(String name)
    {
        if (categories.containsKey(name))
            return removeCategory(getCategorySpec(name));
        else
            return false;
    }

    @Override
    public Map<Card, Integer> removeAll(CardList cards)
    {
        return removeAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> cards.getEntry(c).count())));
    }

    @Override
    public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> amounts)
    {
        var removed = new HashMap<Card, Integer>();
        for (Card card : new HashSet<Card>(amounts.keySet()))
        {
            int r = remove(card, amounts.get(card));
            if (r > 0)
                removed.put(card, r);
        }
        return removed;
    }

    @Override
    public Set<Card> removeAll(Set<? extends Card> cards)
    {
        return removeAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> 1))).keySet();
    }

    @Override
    public boolean set(Card card, int amount)
    {
        if (amount < 0)
            amount = 0;
        DeckEntry e = (DeckEntry)getEntry(card);
        if (e.count == 0)
            return add(card, amount);
        else if (e.count == amount)
            return false;
        else
        {
            total += amount - e.count;

            var change = new HashMap<Card, Integer>();
            change.put(card, amount - e.count);

            e.count = amount;
            if (e.count == 0)
            {
                masterList.remove(e);
                for (CategoryCache category : categories.values())
                {
                    category.filtrate.remove(e.card);
                    category.spec.getWhitelist().remove(e.card);
                    category.spec.getBlacklist().remove(e.card);
                }
            }

            return true;
        }
    }

    @Override
    public boolean set(int index, int amount)
    {
        return set(masterList.get(index).card, amount);
    }

    @Override
    public int size()
    {
        return masterList.size();
    }

    /**
     * Change the rank of the category with the given name to the target value.  The
     * category that has that value will have its rank changed to that of the one with
     * the given name.
     *
     * @param name   name of the category whose rank should be changed
     * @param target new rank for the category
     * @return true if ranks were successfully changed, and false otherwise (such as if the
     * named category doesn't exist, the target rank is too high, the target rank is negative,
     * or the target rank is the named category's rank).
     */
    public boolean swapCategoryRanks(String name, int target)
    {
        if (!categories.containsKey(name) || categories.get(name).rank == target
                || target >= categories.size() || target < 0)
            return false;
        else
        {
            for (CategoryCache second : categories.values())
            {
                if (second.rank == target)
                {
                    var oldRanks = new HashMap<String, Integer>();
                    oldRanks.put(name, categories.get(name).rank);
                    oldRanks.put(second.spec.getName(), second.rank);

                    second.rank = categories.get(name).rank;
                    categories.get(name).rank = target;

                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Card[] toArray()
    {
        return stream().toArray(Card[]::new);
    }

    @Override
    public int total()
    {
        return total;
    }

    /**
     * Change the specification of a category and update its list of contained cards.
     * 
     * @param name current name of the category to update
     * @param spec new specification for the category
     * @return the old specification for the category
     */
    public Category updateCategory(String name, Category spec)
    {
        if (categories.containsKey(name))
        {
            CategoryCache c = categories.remove(name);
            Category old = new Category(c.spec);
            c.update(spec);
            categories.put(spec.getName(), c);
            return old;
        }
        else
            throw new IllegalArgumentException("No category named " + name + " found");
    }

    @Override
    public void sort(Comparator<? super CardList.Entry> c)
    {
        masterList.sort(c);
        for (CategoryCache category : categories.values())
            category.filtrate.sort((a, b) -> c.compare(getEntry(a), getEntry(b)));
    }
}
