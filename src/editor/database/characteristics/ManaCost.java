package editor.database.characteristics;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import editor.database.symbol.ManaSymbol;
import editor.database.symbol.Symbol;
import editor.gui.MainFrame;
import editor.util.Containment;

/**
 * This class represents a mana cost.  It contains a list of Symbols, which may contain duplicate elements.
 * It also calculates its converted mana cost based on the number and types of Symbols it contains, and
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
        ManaCost cost = tryParseManaCost(s);
        if (cost == null)
            throw new IllegalArgumentException('"' + s + "\" is not a mana cost");
        return cost;
    }

    /**
     * Get the mana cost represented by the given String.  The String should only be a list of symbols,
     * and each one should be the symbol's text surrounded by {}.
     *
     * @param s String to parse
     * @return ManaCost represented by the String, or null if there isn't one
     */
    public static ManaCost tryParseManaCost(String s)
    {
        var symbols = new ArrayList<ManaSymbol>();
        Matcher m = Symbol.SYMBOL_PATTERN.matcher(s);
        while (m.find())
        {
            ManaSymbol symbol = ManaSymbol.tryParseManaSymbol(m.group(1));
            if (symbol == null)
                return null;
            symbols.add(symbol);
            s = s.replaceFirst(Pattern.quote(m.group()), "");
        }
        if (!s.isEmpty())
            return null;
        return new ManaCost(symbols);
    }

    /**
     * List of Symbols in this ManaCost.
     */
    private final List<ManaSymbol> cost;
    /**
     * Total color weight of the Symbols in this ManaCost.
     */
    private Map<ManaType, Double> weights;

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
        weights = ManaSymbol.createWeights();
        for (ManaSymbol sym : cost)
            for (ManaType col : weights.keySet())
                weights.compute(col, (k, v) -> sym.colorWeights().get(k) + v);
    }

    /**
     * Set of colors represented by the Symbols in this ManaCost.  It is a list, because order
     * matters.
     *
     * @return List of ManaTypes representing all the colors in this ManaCost.
     */
    public List<ManaType> colors()
    {
        var colors = new ArrayList<ManaType>();
        for (ManaSymbol sym : cost)
            for (var weight : sym.colorWeights().entrySet())
                if (weight.getKey() != ManaType.COLORLESS && weight.getValue() > 0 && !colors.contains(weight.getKey()))
                    colors.add(weight.getKey());
        return colors;
    }

    /**
     * @return Converted mana cost of this ManaCost, which is the total value of its Symbols.
     */
    public double cmc()
    {
        double cmc = 0.0;
        for (ManaSymbol sym : cost)
            cmc += sym.value();
        return cmc;
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
     * @return This ManaCost's color weight Map.
     */
    public Map<ManaType, Double> colorWeight()
    {
        return weights;
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
        var copy = new ArrayList<Symbol>(cost);
        for (Symbol sym : cost)
            if (!copy.remove(sym))
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
     * @return A negative number if this ManaCost's converted mana cost is less than
     * the other or if its color weight is less, 0 if they are the same, and a positive number
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
            int diff = (int)(2 * (cmc() - o.cmc()));
            if (diff == 0)
            {
                var weightList = new ArrayList<>(weights.values());
                weightList.sort(Double::compareTo);
                var oWeightList = new ArrayList<>(o.weights.values());
                oWeightList.sort(Double::compareTo);
                for (int i = 0; i < ManaType.values().length; i++)
                    diff += (weightList.get(i) - oWeightList.get(i)) * Math.pow(10, i);
            }
            return diff;
        }
    }

    /**
     * @return A String containing this ManaCost's symbols represented by HTML
     * tags for display in an HTML-enabled panel.
     */
    public String
    toHTMLString()
    {
        StringBuilder str = new StringBuilder();
        for (Symbol sym : cost)
            str.append("<img src=\"file:images/icons/").append(sym.getName()).append("\" width=\"").append(MainFrame.TEXT_SIZE).append("\" height=\"").append(MainFrame.TEXT_SIZE).append("\" />");
        return str.toString();
    }

    /**
     * @return A String representation of this ManaCost.
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for (Symbol sym : cost)
            str.append(sym.toString());
        return str.toString();
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
        // TODO: When sorting mana costs works, replace with just equals
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
