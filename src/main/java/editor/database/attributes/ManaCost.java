package editor.database.attributes;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import editor.database.symbol.ManaSymbol;
import editor.database.symbol.Symbol;
import editor.gui.generic.ComponentUtils;
import editor.util.Containment;

/**
 * This class represents a mana cost.  It contains a list of Symbols, which may contain duplicate elements.
 * It also calculates its mana value cost based on the number and types of Symbols it contains, and
 * can determine if it is a super- or subset of another mana cost.
 *
 * @author Alec Roelke
 * @see editor.database.symbol.Symbol
 */
public class ManaCost extends AbstractList<ManaSymbol> implements Comparable<ManaCost>
{
    /**
     * Pattern for finding mana costs in Strings.
     */
    public static final Pattern MANA_COST_PATTERN = Pattern.compile("(\\{[cwubrgCWUBRG\\/phPH\\dsSxXyYzZ]+\\})+");

    /**
     * Get the mana cost represented by the given String.  The String should only be a list of symbols,
     * and each one should be the symbol's text surrounded by {}.
     *
     * @param s String to parse
     * @return ManaCost represented by the String
     * @throws IllegalArgumentException if there are invalid characters
     */
    public static ManaCost parseManaCost(String s) throws IllegalArgumentException
    {
        return tryParseManaCost(s).orElseThrow(() -> new IllegalArgumentException('"' + s + "\" is not a mana cost"));
    }

    /**
     * Get the mana cost represented by the given String.  The String should only be a list of symbols,
     * and each one should be the symbol's text surrounded by {}.
     *
     * @param s String to parse
     * @return ManaCost represented by the String, or null if there isn't one
     */
    public static Optional<ManaCost> tryParseManaCost(String s)
    {
        var symbols = new ArrayList<ManaSymbol>();
        for (final var m : Symbol.SYMBOL_PATTERN.matcher(s).results().collect(Collectors.toList()))
        {
            s = s.replaceFirst(Pattern.quote(m.group()), "");
            var symbol = ManaSymbol.tryParseManaSymbol(m.group(1));
            if (symbol.isEmpty())
                return Optional.empty();
            else
                symbols.add(symbol.get());
        }
        return s.isEmpty() ? Optional.of(new ManaCost(symbols)) : Optional.empty();
    }

    /**
     * List of Symbols in this ManaCost.
     */
    private final List<ManaSymbol> cost;
    /**
     * Total color weight of the Symbols in this ManaCost.
     */
    private Map<ManaType, Double> intensity;

    /**
     * Create a new, empty mana cost.
     */
    public ManaCost()
    {
        this(new ArrayList<>());
    }

    /**
     * Create a new mana cost. The symbols in it will be sorted in order.
     *
     * @param symbols not-necessarily-sorted list of symbols in the cost
     */
    public ManaCost(List<ManaSymbol> symbols)
    {
        ManaSymbol.sort(symbols);
        cost = Collections.unmodifiableList(symbols);
        intensity = ManaSymbol.createIntensity();
        for (ManaSymbol sym : cost)
            for (var e : sym.colorIntensity().entrySet())
                intensity.compute(e.getKey(), (k, v) -> e.getValue() + v);
    }

    /**
     * Set of colors represented by the Symbols in this ManaCost.  It is a list, because order
     * matters.
     *
     * @return List of ManaTypes representing all the colors in this ManaCost.
     */
    public List<ManaType> colors()
    {
        return intensity.entrySet().stream().filter((e) -> e.getKey() != ManaType.COLORLESS && e.getValue() > 0).map(Map.Entry::getKey).sorted().collect(Collectors.toList());
    }

    /**
     * @return Mana value cost of this ManaCost, which is the total value of its Symbols.
     */
    public double manaValue()
    {
        return cost.stream().mapToDouble(ManaSymbol::value).sum();
    }

    /**
     * @return The number of Symbols in this ManaCost.
     */
    @Override
    public int size()
    {
        return cost.size();
    }

    /**
     * Get the Symbol at the specified index.
     *
     * @param index Index to look in
     * @return The Symbol at the specified index.
     */
    @Override
    public ManaSymbol get(int index)
    {
        return cost.get(index);
    }

    /**
     * Get the index into this ManaCost of the first occurrence of the given
     * Object.
     *
     * @param o Object to look for
     * @return The index into this ManaCost of the given Object, or
     * -1 if it doesn't exist.
     */
    @Override
    public int indexOf(Object o)
    {
        return cost.indexOf(o);
    }

    /**
     * Get the index into this ManaCost of the last occurrence of the
     * given Object.
     *
     * @param o Object to look for
     * @return The index into this ManaCost of the given Object, or
     * -1 if it doesn't exist.
     */
    @Override
    public int lastIndexOf(Object o)
    {
        return cost.lastIndexOf(o);
    }

    /**
     * @return <code>true</code> if the mana cost is empty (usually so with lands, for
     * example), and <code>false</code> otherwise.
     */
    @Override
    public boolean isEmpty()
    {
        return cost.isEmpty();
    }

    /**
     * @return This ManaCost's color intensity Map.
     */
    public Map<ManaType, Double> colorIntensity()
    {
        return intensity;
    }

    /**
     * Returns true if and only if the specified Object is a Symbol and that
     * Symbol is contained within this ManaCost.
     *
     * @param o Object to look for
     * @return <code>true</code> if this ManaCost contains the specified Object.
     */
    @Override
    public boolean contains(Object o)
    {
        return cost.contains(o);
    }

    /**
     * Returns true if and only if all of the objects in the specified collection
     * are Symbols and all of them are contained within this ManaCost.
     *
     * @param c Collection of objects to look for
     * @return <code>true</code> if this ManaCost contains all of the specified Objects.
     */
    @Override
    public boolean containsAll(Collection<?> c)
    {
        return cost.containsAll(c);
    }

    /**
     * @param o ManaCost to compare with
     * @return <code>true</code> if the symbols in this ManaCost are all in
     * the other ManaCost, and <code>false</code> otherwise.
     */
    public boolean isSubset(ManaCost o)
    {
        var myCounts = cost.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        var oCounts = o.cost.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (Symbol sym : myCounts.keySet())
            if (myCounts.get(sym) > oCounts.getOrDefault(sym, 0L))
                return false;
        return true;
    }

    /**
     * @param o ManaCost to compare with
     * @return <code>true</code> if the symbols in the other ManaCost are all in
     * this ManaCost, and <code>false</code> otherwise.
     */
    public boolean isSuperset(ManaCost o)
    {
        return o.isSubset(this);
    }

    /**
     * @param o ManaCost to compare with
     * @return A negative number if this ManaCost's mana value is less than
     * the other or if its color intensity is less, 0 if they are the same, and a positive number
     * if they are greater.
     */
    @Override
    public int compareTo(ManaCost o)
    {
        if (isEmpty() && !o.isEmpty())
            return -1;
        else if (!isEmpty() && o.isEmpty())
            return 1;
        else
        {
            // Start by sorting by mana value
            int diff = (int)(2 * (manaValue() - o.manaValue()));
            // If the two costs have the same mana value, sort them by symbol color intensity
            if (diff == 0)
            {
                var intensityList = intensity.values().stream().sorted().collect(Collectors.toList());
                var oIntensityList = o.intensity.values().stream().sorted().collect(Collectors.toList());
                for (int i = 0; i < ManaType.values().length; i++)
                    diff += (intensityList.get(i) - oIntensityList.get(i))*Math.pow(10, i);
            }
            // If the two costs have the same intensity, sort them by color
            if (diff == 0)
                for (int i = 0; diff == 0 && i < Math.min(size(), o.size()); i++)
                    diff = get(i).compareTo(o.get(i));

            return diff;
        }
    }

    /**
     * @return A String containing this ManaCost's symbols represented by HTML
     * tags for display in an HTML-enabled panel.
     */
    public String toHTMLString()
    {
        return cost.stream()
            .map((sym) -> "<img src=\"" + Symbol.class.getResource("/images/icons/" + sym.getName()) + "\" width=\"" + ComponentUtils.TextSize() + "\" height=\"" + ComponentUtils.TextSize() + "\"/>")
            .collect(Collectors.joining());
    }

    /**
     * @return A String representation of this ManaCost.
     */
    @Override
    public String toString()
    {
        return cost.stream().map(Symbol::toString).collect(Collectors.joining());
    }

    /**
     * @param other Object to compare with
     * @return <code>true</code> if the other Object is a ManaCost with the same list
     * of Symbols, and <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other.getClass() != getClass())
            return false;
        if (other == this)
            return false;
        return Containment.CONTAINS_EXACTLY.test(cost, ((ManaCost)other).cost);
    }

    /**
     * @return A unique integer for this ManaCost.
     */
    @Override
    public int hashCode()
    {
        return cost.hashCode();
    }

    /**
     * @return An iterator over the Symbols in this ManaCost.
     */
    @Override
    public Iterator<ManaSymbol> iterator()
    {
        return cost.iterator();
    }

    /**
     * @return A ListIterator over the Symbols in this ManaCost that
     * allows traversal in either direction.
     */
    @Override
    public ListIterator<ManaSymbol> listIterator()
    {
        return cost.listIterator();
    }

    /**
     * @param index Index to start at
     * @return A ListIterator over the Symbols in this ManaCost
     * that allows traversal in either direction starting at
     * the given index.
     */
    @Override
    public ListIterator<ManaSymbol> listIterator(int index)
    {
        return cost.listIterator(index);
    }

    /**
     * @param fromIndex index to start from (inclusive)
     * @param toIndex   index to end at (exclusive)
     * @return A view into this ManaCost containing the symbols between the given
     * indices (inclusive at the beginning and exclusive at the end).
     */
    @Override
    public List<ManaSymbol> subList(int fromIndex, int toIndex)
    {
        return cost.subList(fromIndex, toIndex);
    }

    /**
     * @return An array containing all of the Symbols in this ManaCost.
     */
    @Override
    public Object[] toArray()
    {
        return cost.toArray();
    }

    /**
     * Returns an array containing all of the Symbols in this ManaCost, using
     * the given array to determine runtime type.  If the given array is large
     * enough to fit the Symbols, they are put in it and the rest of the values
     * are set to null.  Otherwise, a new array is created and returned.
     *
     * @param a Array determining runtime type of the return value
     * @return An array containing all of the Symbols in this ManaCost, which is
     * null-terminated if there is extra room.
     */
    @Override
    public <T> T[] toArray(T[] a)
    {
        return cost.toArray(a);
    }
}
