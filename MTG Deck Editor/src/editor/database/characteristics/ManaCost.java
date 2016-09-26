package editor.database.characteristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import editor.database.card.Card;
import editor.database.symbol.ManaSymbol;
import editor.database.symbol.Symbol;
import editor.gui.MainFrame;
import editor.util.UnmodifiableList;

/**
 * This class represents a mana cost.  It contains a list of Symbols, which may contain duplicate elements.
 * It also calculates its converted mana cost based on the number and types of Symbols it contains, and
 * can determine if it is a super- or subset of another mana cost.
 * 
 * @author Alec Roelke
 * @see editor.database.symbol.Symbol
 */
public class ManaCost implements Comparable<ManaCost>, List<ManaSymbol>
{
	/**
	 * Pattern for finding mana costs in Strings.
	 */
	public static final Pattern MANA_COST_PATTERN = Pattern.compile("(\\{[cwubrgCWUBRG\\/phPH\\dsSxXyYzZ]+\\})+");
	
	/**
	 * This class represents a tuple of ManaCosts.  It is useful for displaying and sorting
	 * the mana costs of cards that may have mulitiple faces.
	 * 
	 * @author Alec Roelke
	 */
	@SuppressWarnings("serial")
	public static class Tuple extends UnmodifiableList<ManaCost> implements Comparable<Tuple>
	{
		/**
		 * Create a new tuple out of the given collection of ManaCosts.
		 * 
		 * @param c Collection to create the new tuple out of
		 */
		public Tuple(List<? extends ManaCost> c)
		{
			super(c);
		}
		
		/**
		 * Create a new tuple out of the given ManaCosts.
		 * 
		 * @param c ManaCosts to create the new tuple out of
		 */
		public Tuple(ManaCost... c)
		{
			super(Arrays.asList(c));
		}
		
		/**
		 * @param o Tuple to compare to (must be a ManaCost tuple)
		 * @return A negative number if the other tuple is empty and this one is not or if the first
		 * mana cost in this one is less than the first mana cost in the other one, a positive number
		 * if the opposite is true, or 0 if both costs are the same or if both tuples are empty.
		 */
		@Override
		public int compareTo(Tuple o)
		{
			if (isEmpty() && o.isEmpty())
				return 0;
			else if (isEmpty())
				return -1;
			else if (o.isEmpty())
				return 1;
			else
				return this[0].compareTo(o[0]);
		}
		
		/**
		 * @return A String representation of this tuple, which is the HTML String
		 * representations of its non-empty ManaCosts separated by card face
		 * separators.
		 */
		@Override
		public String toString()
		{
			StringJoiner join = new StringJoiner(" " + Card.FACE_SEPARATOR + " ");
			for (ManaCost cost: this)
				if (!cost.isEmpty())
					join.add(cost.toHTMLString());
			return join.toString();
		}
	}
	
	/**
	 * Get the mana cost represented by the given String.  The String should only be a list of symbols,
	 * and each one should be the symbol's text surrounded by {}.
	 * 
	 * @param s String to parse.
	 * @return ManaCost represented by the String.
	 * @throws IllegalArgumentException If there are invalid characters.
	 * @see editor.database.symbol.Symbol
	 */
	public static ManaCost valueOf(String s)
	{
		try
		{
			return new ManaCost(s);
		}
		catch (IllegalArgumentException | StringIndexOutOfBoundsException e)
		{
			throw new IllegalArgumentException("Illegal mana cost string \"" + s + "\"");
		}
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
	 * Create a new ManaCost.  The symbols will be sorted according to their natural ordering,
	 * and the total weights of all symbols will be calculated for ordering costs.
	 * 
	 * @param s String to parse to get symbols from.
	 */
	public ManaCost(String s)
	{
		// Populate this ManaCost's list of Symbols
		List<ManaSymbol> symbols = new ArrayList<ManaSymbol>();
		Matcher m = Symbol.SYMBOL_PATTERN.matcher(s);
		while (m.find())
		{
			symbols.add(ManaSymbol.valueOf(m.group(1)));
			s = s.replaceFirst(Pattern.quote(m.group()), "");
		}
		
		int index = -1;
		do
		{
			if ((index = s.indexOf('/')) > -1)
			{
				String sym = s.substring(index - 1, index + 2);
				s = s.replaceFirst(Pattern.quote(sym), "");
				symbols.add(ManaSymbol.valueOf(sym));
			}
		} while (index > -1);
		do
		{
			if ((index = s.indexOf('H')) > -1 || (index = s.indexOf('h')) > -1)
			{
				String sym = s.substring(index, index + 2);
				s = s.replaceFirst(Pattern.quote(sym), "");
				symbols.add(ManaSymbol.valueOf(sym));
			}
		} while (index > -1);
		for (char sym: s.toCharArray())
			symbols.add(ManaSymbol.valueOf(String.valueOf(sym)));
		
		ManaSymbol.sort(symbols);
		cost = Collections.unmodifiableList(symbols);
		
		// Calculate this ManaCost's total color weights.
		weights = ManaSymbol.createWeights();
		for (ManaSymbol sym: cost)
			for (ManaType col: weights.keySet())
				weights.compute(col, (k, v) -> sym.colorWeights()[k] + v);
	}
	
	/**
	 * Create an empty ManaCost containing no Symbols.
	 */
	public ManaCost()
	{
		this("");
	}
	
	/**
	 * Set of colors represented by the Symbols in this ManaCost.  It is a list, because order
	 * matters.
	 * 
	 * @return List of ManaTypes representing all the colors in this ManaCost.
	 */
	public ManaType.Tuple colors()
	{
		List<ManaType> colors = new ArrayList<ManaType>();
		for (ManaSymbol sym: cost)
			for (Map.Entry<ManaType, Double> weight: sym.colorWeights().entrySet())
				if (weight.getKey() != ManaType.COLORLESS && weight.getValue() > 0)
					colors.add(weight.getKey());
		return new ManaType.Tuple(colors);
	}
	
	/**
	 * @return Converted mana cost of this ManaCost, which is the total value of its Symbols.
	 */
	public double cmc()
	{
		double cmc = 0.0;
		for (ManaSymbol sym: cost)
			cmc += sym.value();
		return cmc;
	}
	
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
		return cost[index];
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
		List<Symbol> copy = new ArrayList<Symbol>(cost);
		for (Symbol sym: cost)
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
			int diff = (int)(2*(cmc() - o.cmc()));
			if (diff == 0)
			{
				List<Double> weightList = new ArrayList<Double>(weights.values());
				Collections.sort(weightList, Double::compareTo);
				List<Double> oWeightList = new ArrayList<Double>(o.weights.values());
				Collections.sort(oWeightList, Double::compareTo);
				for (int i = 0; i < ManaType.values().length; i++)
					diff += (weightList[i] - oWeightList[i])*Math.pow(10, i);	
			}
			return diff;
		}
	}
	
	/**
	 * @return A String containing this ManaCost's symbols represented by HTML
	 * tags for display in an HTML-enabled panel.
	 */
	public String toHTMLString()
	{
		StringBuilder str = new StringBuilder();
		for (Symbol sym: cost)
			str.append("<img src=\"file:images/icons/" + sym.getName() + "\" width=\"" + MainFrame.TEXT_SIZE + "\" height=\"" + MainFrame.TEXT_SIZE + "\" />");
		return str.toString();
	}
	
	/**
	 * @return A String representation of this ManaCost.
	 */
	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		for (Symbol sym: cost)
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
		return cost.equals(((ManaCost)other).cost);
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
	 * @param toIndex index to end at (exclusive)
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

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean add(ManaSymbol e)
	{
		throw new UnsupportedOperationException("add");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void add(int index, ManaSymbol element)
	{
		throw new UnsupportedOperationException("add");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean addAll(Collection<? extends ManaSymbol> c)
	{
		throw new UnsupportedOperationException("addAll");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean addAll(int index, Collection<? extends ManaSymbol> c)
	{
		throw new UnsupportedOperationException("addAll");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("clear");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean remove(Object e)
	{
		throw new UnsupportedOperationException("remove");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public ManaSymbol remove(int index)
	{
		throw new UnsupportedOperationException("remove");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException("removeAll");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException("removeAll");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public ManaSymbol set(int index, ManaSymbol element)
	{
		throw new UnsupportedOperationException("set");
	}
}
