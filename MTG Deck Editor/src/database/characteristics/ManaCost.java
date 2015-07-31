package database.characteristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.symbol.Symbol;

/**
 * This class represents a mana cost.  It contains a list of Symbols, which may contain duplicate elements.
 * It also calculates its convereted mana cost based on the number and types of Symbols it contains, and
 * can determine if it is a super- or subset of another mana cost.
 * 
 * @author Alec Roelke
 * @see database.symbol.Symbol
 */
public class ManaCost implements Comparable<ManaCost>
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
	public static class Tuple extends util.Tuple<ManaCost> implements Comparable<Tuple>
	{
		/**
		 * Create a new tuple out of the given collection of ManaCosts.
		 * 
		 * @param c Collection to create the new tuple out of
		 */
		public Tuple(Collection<? extends ManaCost> c)
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
	}
	
	/**
	 * Get the mana cost represented by the given String.  The String should only be a list of symbols,
	 * and each one should be the symbol's text surrounded by {}.
	 * 
	 * @param s String to parse.
	 * @return ManaCost represented by the String.
	 * @see database.symbol.Symbol
	 */
	public static ManaCost valueOf(String s)
	{
		return new ManaCost(s);
	}
	
	/**
	 * List of Symbols in this ManaCost.
	 */
	private List<Symbol> cost;
	/**
	 * Total color weight of the Symbols in this ManaCost.
	 */
	private Map<MTGColor, Double> weights;
	
	/**
	 * Create a new ManaCost.  The symbols will be sorted according to their natural ordering,
	 * and the total weights of all symbols will be calculated for ordering costs.
	 * 
	 * @param cost String to parse to get symbols from.
	 */
	public ManaCost(String cost)
	{
		// Populate this ManaCost's list of Symbols
		this.cost = new ArrayList<Symbol>();
		Pattern p = Pattern.compile("\\{([^}]+)\\}");
		Matcher m = p.matcher(cost);
		while (m.find())
			this.cost.add(Symbol.valueOf(m.group(1)));
		Collections.sort(this.cost);
		this.cost = Collections.unmodifiableList(this.cost);
		
		// Calculate this ManaCost's total color weights.
		weights = Symbol.createWeights(0, 0, 0, 0, 0);
		for (Symbol sym: this.cost)
			for (MTGColor col: weights.keySet())
				weights.compute(col, (k, v) -> sym.colorWeights().get(k) + v);
	}
	
	/**
	 * Set of colors represented by the Symbols in this ManaCost.  It is a list, because order
	 * matters.
	 * 
	 * @return List of MTGColors representing all the colors in this ManaCost.
	 */
	public List<MTGColor> colors()
	{
		List<MTGColor> colors = new ArrayList<MTGColor>();
		for (Symbol sym: cost)
			for (Map.Entry<MTGColor, Double> weight: sym.colorWeights().entrySet())
				if (weight.getValue() > 0 && !colors.contains(weight.getKey()))
					colors.add(weight.getKey());
		MTGColor.sort(colors);
		return colors;
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
	
	/**
	 * @return This ManaCost's list of Symbols.
	 */
	public List<Symbol> symbols()
	{
		return cost;
	}
	
	/**
	 * @return <code>true</code> if the mana cost is empty (usually so with lands, for
	 * example), and <code>false</code> otherwise.
	 */
	public boolean isEmpty()
	{
		return cost.isEmpty();
	}
	
	/**
	 * @return This ManaCost's color weight Map.
	 */
	public Map<MTGColor, Double> colorWeight()
	{
		return weights;
	}
	
	/**
	 * @param o ManaCost to compare with
	 * @return <code>true</code> if the symbols in this ManaCost are all in
	 * the other ManaCost, and <code>false</code> otherwise.
	 */
	public boolean isSubset(ManaCost o)
	{
		List<Symbol> copy = new ArrayList<Symbol>(o.cost);
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
		if (cost.size() == 0 && o.cost.size() == 0)
			return 0;
		else if (cost.size() == 0)
			return -1;
		else if (o.cost.size() == 0)
			return 1;
		else
		{
			List<Double> weightList = new ArrayList<Double>(weights.values());
			Collections.sort(weightList, (a, b) -> a.compareTo(b));
			List<Double> oWeightList = new ArrayList<Double>(o.weights.values());
			Collections.sort(oWeightList, (a, b) -> a.compareTo(b));
			
			int diff = (int)((cmc() - o.cmc())*1000000);
			for (int i = 0; i < 5; i++)
				diff += (weightList.get(i) - oWeightList.get(i))*Math.pow(10, i);
			
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
			str.append(sym.getHTML());
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
		if (!(other instanceof ManaCost))
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
		int hash = weights.hashCode();
		for (Symbol sym: cost)
			hash ^= sym.hashCode();
		return hash;
	}
}
