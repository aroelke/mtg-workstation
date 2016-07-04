package editor.database.characteristics;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import editor.database.card.CardInterface;
import editor.database.symbol.Symbol;
import editor.gui.MainFrame;

/**
 * This class represents a mana cost.  It contains a list of Symbols, which may contain duplicate elements.
 * It also calculates its converted mana cost based on the number and types of Symbols it contains, and
 * can determine if it is a super- or subset of another mana cost.
 * 
 * @author Alec Roelke
 * @see editor.database.symbol.Symbol
 */
public class ManaCost implements Comparable<ManaCost>, List<Symbol>
{
	/**
	 * Pattern for finding mana costs in Strings.
	 */
	public static final Pattern MANA_COST_PATTERN = Pattern.compile("(\\{[wubrgWUBRG\\/phPH\\dqQtTcCsSxXyYzZ]+\\})+");
	
	/**
	 * This class represents a tuple of ManaCosts.  It is useful for displaying and sorting
	 * the mana costs of cards that may have mulitiple faces.
	 * 
	 * @author Alec Roelke
	 */
	public static class Tuple extends editor.util.Tuple<ManaCost> implements Comparable<Tuple>
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
			super(c);
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
				return get(0).compareTo(o.get(0));
		}
		
		/**
		 * @return A String representation of this tuple, which is the HTML String
		 * representations of its non-empty ManaCosts separated by card face
		 * separators.
		 */
		@Override
		public String toString()
		{
			StringJoiner join = new StringJoiner(" " + CardInterface.FACE_SEPARATOR + " ");
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
	 * @return ManaCost represented by the String, or null if there are invalid characters.
	 * @see editor.database.symbol.Symbol
	 */
	public static ManaCost valueOf(String s)
	{
		try
		{
			return new ManaCost(s);
		}
		catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	/**
	 * List of Symbols in this ManaCost.
	 */
	private final Symbol[] cost;
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
		List<Symbol> symbols = new ArrayList<Symbol>();
		Matcher m = Symbol.SYMBOL_PATTERN.matcher(s);
		String copy = new String(s);
		while (m.find())
		{
			symbols.add(Symbol.valueOf(m.group(1)));
			copy = copy.replaceFirst(Pattern.quote(m.group()), "");
		}
		if (!copy.isEmpty())
			throw new IllegalArgumentException("Illegal cost string \"" + s + "\"");
		Collections.sort(symbols);
		cost = symbols.toArray(new Symbol[symbols.size()]);
		
		// Calculate this ManaCost's total color weights.
		weights = Symbol.createWeights();
		for (Symbol sym: cost)
			for (ManaType col: weights.keySet())
				weights.compute(col, (k, v) -> sym.colorWeights().get(k) + v);
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
		for (Symbol sym: cost)
			for (Map.Entry<ManaType, Double> weight: sym.colorWeights().entrySet())
				if (weight.getValue() > 0)
					colors.add(weight.getKey());
		return new ManaType.Tuple(colors);
	}
	
	/**
	 * @return Converted mana cost of this ManaCost, which is the total value of its Symbols.
	 */
	public double cmc()
	{
		double cmc = 0.0;
		for (Symbol sym: cost)
			cmc += sym.value();
		return cmc;
	}
	
	@Override
	public int size()
	{
		return cost.length;
	}
	
	/**
	 * Get the Symbol at the specified index.
	 * 
	 * @param index Index to look in
	 * @return The Symbol at the specified index.
	 */
	@Override
	public Symbol get(int index)
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
		for (int i = 0; i < cost.length; i++)
			if (cost[i].equals(o))
				return i;
		return -1;
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
		for (int i = cost.length - 1; i >= 0; i--)
			if (cost[i].equals(o))
				return i;
		return -1;
	}
	
	/**
	 * @return <code>true</code> if the mana cost is empty (usually so with lands, for
	 * example), and <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return cost.length == 0;
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
		for (Symbol symbol: cost)
			if (symbol.equals(o))
				return true;
		return false;
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
		for (Object o: c)
			if (!contains(o))
				return false;
		return true;
	}
	
	/**
	 * @param o ManaCost to compare with
	 * @return <code>true</code> if the symbols in this ManaCost are all in
	 * the other ManaCost, and <code>false</code> otherwise.
	 */
	public boolean isSubset(ManaCost o)
	{
		List<Symbol> copy = Arrays.asList(o.cost);
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
				Collections.sort(weightList, (a, b) -> a.compareTo(b));
				List<Double> oWeightList = new ArrayList<Double>(o.weights.values());
				Collections.sort(oWeightList, (a, b) -> a.compareTo(b));
				for (int i = 0; i < ManaType.values().length; i++)
					diff += (weightList.get(i) - oWeightList.get(i))*Math.pow(10, i);	
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
		return Arrays.equals(cost, ((ManaCost)other).cost);
	}
	/**
	 * @return A unique integer for this ManaCost.
	 */
	@Override
	public int hashCode()
	{
		return Arrays.hashCode(cost);
	}
	
	/**
	 * @return An iterator over the Symbols in this ManaCost.
	 */
	@Override
	public Iterator<Symbol> iterator()
	{
		return new CostIterator();
	}
	
	/**
	 * @return A ListIterator over the Symbols in this ManaCost that
	 * allows traversal in either direction.
	 */
	@Override
	public ListIterator<Symbol> listIterator()
	{
		return new CostIterator();
	}
	
	/**
	 * @param index Index to start at
	 * @return A ListIterator over the Symbols in this ManaCost
	 * that allows traversal in either direction starting at
	 * the given index.
	 */
	@Override
	public ListIterator<Symbol> listIterator(int index)
	{
		return new CostIterator(index);
	}
	
	/**
	 * @param fromIndex index to start from (inclusive)
	 * @param toIndex index to end at (exclusive)
	 * @return A view into this ManaCost containing the symbols between the given
	 * indices (inclusive at the beginning and exclusive at the end).
	 */
	@Override
	public List<Symbol> subList(int fromIndex, int toIndex)
	{
		return new SubCost(fromIndex, toIndex);
	}
	
	/**
	 * @return An array containing all of the Symbols in this ManaCost.
	 */
	@Override
	public Object[] toArray()
	{
		return Arrays.copyOf(cost, cost.length);
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
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a)
	{
		T[] array;
		if (a.length < size())
			array = (T[])Array.newInstance(a.getClass().getComponentType(), size());
		else
			array = a;
		for (int i = 0; i < array.length; i++)
			array[i] = i < size() ? (T)cost[i] : null;
		return array;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean add(Symbol e)
	{
		throw new UnsupportedOperationException("add");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void add(int index, Symbol element)
	{
		throw new UnsupportedOperationException("add");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean addAll(Collection<? extends Symbol> c)
	{
		throw new UnsupportedOperationException("addAll");
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean addAll(int index, Collection<? extends Symbol> c)
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
	public Symbol remove(int index)
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
	public Symbol set(int index, Symbol element)
	{
		throw new UnsupportedOperationException("set");
	}
	
	/**
	 * This class represents an iterator over a list of Symbols that can go both
	 * forward and backward.  By default, it will iterate over the parent ManaCost,
	 * but it can also iterate over sublists of it.
	 * 
	 * @author Alec Roelke
	 */
	private class CostIterator implements ListIterator<Symbol>
	{
		private List<Symbol> parent;
		private int index;
		
		public CostIterator(List<Symbol> p, int i)
		{
			parent = p;
			if (i < 0 || i >= cost.length)
				throw new IndexOutOfBoundsException("index " + i + ", size " + cost.length);
			index = i;
		}
		
		public CostIterator(int i)
		{
			this(ManaCost.this, i);
		}
		
		public CostIterator(List<Symbol> p)
		{
			this(p, 0);
		}
		
		public CostIterator()
		{
			this(0);
		}
		
		@Override
		public boolean hasNext()
		{
			return index < parent.size();
		}

		@Override
		public int nextIndex()
		{
			return Math.min(index, parent.size());
		}
		
		@Override
		public Symbol next()
		{
			if (!hasNext())
				throw new NoSuchElementException("index " + index + ", size " + parent.size());
			return cost[index++];
		}

		@Override
		public boolean hasPrevious()
		{
			return index > 0;
		}
		
		@Override
		public int previousIndex()
		{
			return Math.max(index - 1, -1);
		}
		
		@Override
		public Symbol previous()
		{
			if (!hasPrevious())
				throw new NoSuchElementException("index 0");
			return cost[--index];
		}
		
		@Override
		public void add(Symbol e)
		{
			throw new UnsupportedOperationException("add");
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("remove");
		}

		@Override
		public void set(Symbol e)
		{
			throw new UnsupportedOperationException("set");
		}
	}
	
	/**
	 * This class represents a smaller view of a ManaCost.
	 * 
	 * @author Alec Roelke
	 */
	private class SubCost implements List<Symbol>
	{
		private List<Symbol> parent;
		private int start;
		private int end;
		
		public SubCost(List<Symbol> p, int s, int e)
		{
			if (s < 0 || s >= p.size())
				throw new IndexOutOfBoundsException("index " + s + ", size " + p.size());
			if (e < 0 || e >= p.size())
				throw new IndexOutOfBoundsException("index " + e + ", size " + p.size());
			
			parent = p;
			start = s;
			end = e;
		}
		
		public SubCost(int s, int e)
		{
			this(ManaCost.this, s, e);
		}

		@Override
		public boolean contains(Object o)
		{
			for (int i = start; i < end; i++)
				if (parent.get(i).equals(o))
					return true;
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			for (Object o: c)
				if (!contains(o))
					return false;
			return true;
		}

		@Override
		public Symbol get(int index)
		{
			if (start + index >= end)
				throw new IndexOutOfBoundsException("index " + index + ", size " + size());
			return parent.get(start + index);
		}

		@Override
		public int indexOf(Object o)
		{
			for (int i = 0; i < size(); i++)
				if (get(i).equals(o))
					return i;
			return -1;
		}

		@Override
		public boolean isEmpty()
		{
			return size() == 0;
		}

		@Override
		public Iterator<Symbol> iterator()
		{
			return new CostIterator(this);
		}

		@Override
		public int lastIndexOf(Object o)
		{
			for (int i = size() - 1; i >= 0; i--)
				if (get(i).equals(o))
					return i;
			return -1;
		}

		@Override
		public ListIterator<Symbol> listIterator()
		{
			return new CostIterator(this);
		}

		@Override
		public ListIterator<Symbol> listIterator(int index)
		{
			return new CostIterator(this, index);
		}

		@Override
		public int size()
		{
			return end - start;
		}

		@Override
		public List<Symbol> subList(int fromIndex, int toIndex)
		{
			return new SubCost(this, fromIndex, toIndex);
		}

		@Override
		public Object[] toArray()
		{
			Object[] array = new Object[size()];
			for (int i = 0; i < size(); i++)
				array[i] = get(i);
			return array;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] a)
		{
			T[] array;
			if (a.length < size())
				array = (T[])Array.newInstance(a.getClass().getComponentType(), size());
			else
				array = a;
			for (int i = 0; i < array.length; i++)
				array[i] = i < size() ? (T)get(i) : null;
			return array;
		}
		
		@Override
		public boolean add(Symbol e)
		{
			throw new UnsupportedOperationException("add");
		}

		@Override
		public void add(int index, Symbol element)
		{
			throw new UnsupportedOperationException("add");
		}

		@Override
		public boolean addAll(Collection<? extends Symbol> c)
		{
			throw new UnsupportedOperationException("addAll");
		}

		@Override
		public boolean addAll(int index, Collection<? extends Symbol> c)
		{
			throw new UnsupportedOperationException("addAll");
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException("clear");
		}

		@Override
		public boolean remove(Object o)
		{
			throw new UnsupportedOperationException("remove");
		}

		@Override
		public Symbol remove(int index)
		{
			throw new UnsupportedOperationException("remove");
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			throw new UnsupportedOperationException("removeAll");
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			throw new UnsupportedOperationException("retainAll");
		}

		@Override
		public Symbol set(int index, Symbol element)
		{
			throw new UnsupportedOperationException("set");
		}
	}
}
