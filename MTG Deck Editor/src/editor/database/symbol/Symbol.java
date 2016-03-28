package editor.database.symbol;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import editor.database.characteristics.ManaType;

/**
 * This class represents a symbol that might appear on a card in Magic: The Gathering.  It has a weight for each
 * color, which is equal to 1 divided by the number of colors the symbol has, or zero if it does not have that
 * color.  Symbol cannot be instantiated; rather, instances of it are accessed using static variables that contain
 * all possible values those symbols can attain.  For colored symbols, those variables will be Maps mapping colors
 * onto the correct symbols.  For constant colorless symbols, they will be an array containing the colorless symbol
 * corresponding to the index into the array and others for special symbols (100 and 1,000,000).  Nonmana symbols,
 * variable symbols, and the half-mana symbol all have a single static variable that contains an instance of that
 * class.
 * 
 * @author Alec Roelke
 */
public abstract class Symbol implements Comparable<Symbol>
{
	public static final List<Class<? extends Symbol>> ORDER = new ArrayList<Class<? extends Symbol>>();
	static
	{
		ORDER.add(VariableSymbol.class);
		ORDER.add(HalfManaSymbol.class);
		ORDER.add(GenericSymbol.class);
		ORDER.add(HalfColorSymbol.class);
		ORDER.add(SnowSymbol.class);
		ORDER.add(TwobridSymbol.class);
		ORDER.add(HybridSymbol.class);
		ORDER.add(PhyrexianSymbol.class);
		ORDER.add(ColorSymbol.class);
	}
	
	/**
	 * Icon to show when displaying this Symbol.
	 */
	private ImageIcon icon;
	/**
	 * Name of the file containing the icon (not including parent directory).
	 */
	private String name;
	
	/**
	 * Create a map of color weights for a Symbol, where the keys are ManaTypes and the values
	 * are their weights (Doubles).  There is also an entry with a <code>null</code> key for
	 * colorless (which is not a color, so it doesn't have a ManaType).
	 * 
	 * @param weights Initial weights to use.
	 * @return The Map of ManaTypes onto weights.
	 */
	public static Map<ManaType, Double> createWeights(ColorWeight... weights)
	{
		Map<ManaType, Double> weightsMap = new HashMap<ManaType, Double>();
		weightsMap.put(ManaType.COLORLESS, 0.0);
		weightsMap.put(ManaType.WHITE, 0.0);
		weightsMap.put(ManaType.BLUE, 0.0);
		weightsMap.put(ManaType.BLACK, 0.0);
		weightsMap.put(ManaType.RED, 0.0);
		weightsMap.put(ManaType.GREEN, 0.0);
		for (ColorWeight w: weights)
			weightsMap.put(w.color, w.weight);
		return weightsMap;
	}
	
	/**
	 * Create a Symbol from a String.
	 * 
	 * @param s String representation of the new Symbol, surrounded by {}
	 * @return A new Symbol that the specified String represents.
	 * @throw IllegalArgumentException If the specified String does not represent a Symbol.
	 */
	public static Symbol valueOf(String s)
	{
		if (s.equalsIgnoreCase(ChaosSymbol.CHAOS.toString()))
			return ChaosSymbol.CHAOS;
		else if (!s.contains("/")) // The symbol is not hybrid if it doesn't have a /
		{
			// First try to make a colored symbol
			try
			{
				return ColorSymbol.get(ManaType.get(s));
			}
			catch (IllegalArgumentException e)
			{
				// If that failed, try to make a generic symbol
				try
				{
					int n = Integer.valueOf(s);
					if (n == 1000000)
						return GenericSymbol.MILLION;
					else if (n == 100)
						return GenericSymbol.HUNDRED;
					else
						return GenericSymbol.N[n];
				}
				catch (NumberFormatException n)
				{
					// If that failed, then the symbol must be X, Y, Z, a nonmana symbol, or a half-mana symbol
					char sym = s.toUpperCase().charAt(0);
					switch (sym)
					{
					case 'X': case 'Y': case 'Z':
						return VariableSymbol.SYMBOLS.get(sym);
					case 'S':
						return SnowSymbol.SNOW;
					case 'T':
						return TapSymbol.TAP;
					case 'Q':
						return UntapSymbol.UNTAP;
					case 'P':
						return PhiSymbol.PHI;
					case 'H':
						return HalfColorSymbol.get(ManaType.get(s.substring(1)));
					case '∞':
						return InfinityManaSymbol.INFINITY_MANA;
					case '½':
						return HalfManaSymbol.HALF_MANA;
					default:
						throw new IllegalArgumentException("Illegal symbol string \"" + s + "\"");
					}
				}
			}
		}
		else
		{
			// If the String has a / in it, then either it is a type of hybrid symbol or it is a colorless 1/2 mana
			// symbol
			String[] cols = s.split("/");
			if (cols[0].equals("1") && cols[1].equals("2"))
				return HalfManaSymbol.HALF_MANA;
			try
			{
				if (cols[0].equals("2"))
					return TwobridSymbol.get(ManaType.get(cols[1]));
				else if (cols[0].equalsIgnoreCase("P"))
					return PhyrexianSymbol.get(ManaType.get(cols[1]));
				else if (cols[1].equalsIgnoreCase("P"))
					return PhyrexianSymbol.SYMBOLS.get(ManaType.get(cols[0]));
				else
					return HybridSymbol.get(ManaType.get(cols[0]), ManaType.get(cols[1]));
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Illegal symbol string \"" + s + "\"");
			}
		}
	}
	
	/**
	 * Create a new Symbol.
	 * 
	 * @param iconName Name of the icon file for the new Symbol.
	 */
	protected Symbol(String iconName)
	{
		icon = new ImageIcon("images/icons-large/" + iconName);
		name = iconName;
	}
	
	/**
	 * @return A String representation of this Symbol, without {}.
	 */
	public abstract String getText();
	
	/**
	 * @return How much mana this Symbol is worth for calculating converted mana costs.  This
	 * is only defined for symbols that can appear in mana costs; otherwise, it will return
	 * 0.
	 */
	public double value()
	{
		return 0;
	}
	
	/**
	 * @return This Symbol's color weight map.
	 */
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights();
	}
	
	public Icon getIcon()
	{
		return icon;
	}
	
	/**
	 * Get this Symbol's icon with the specified width.  The height will be scaled accordingly as well.
	 * 
	 * @param newSize Width of the icon
	 * @return The resized icon.
	 */
	public Icon getIcon(int newSize)
	{
		return new ImageIcon(icon.getImage().getScaledInstance(-1, newSize, Image.SCALE_SMOOTH));
	}
	
	/**
	 * @return An HTML image tag that will display this Symbol's icon.
	 */
	public String getHTML()
	{
		return "<img src=\"file:images/icons-small/" + name + "\" />";
	}
	
	/**
	 * @return A String representation of this Symbol: its text surrounded by {}.
	 */
	@Override
	public String toString()
	{
		return "{" + getText() + "}";
	}
	
	/**
	 * @param other Symbol to compare with
	 * @return A negative number if this Symbol should appear before the other in a list,
	 * 0 if the two symbols are the same or if their order doesn't matter,
	 * and a positive number if this Symbol should appear after it.
	 */
	@Override
	public int compareTo(Symbol other)
	{
		if (ORDER.contains(getClass()) && ORDER.contains(other.getClass()))
			return ORDER.indexOf(getClass()) - ORDER.indexOf(other.getClass());
		else if (!ORDER.contains(getClass()))
			return 1;
		else if (!ORDER.contains(other.getClass()))
			return -1;
		else
			return 0;
	}
	
	/**
	 * @param other Symbol to compare with
	 * @return <code>true</code> if this Symbol is the same Symbol as the other, and <code>false</code>
	 * otherwise.
	 */
	public abstract boolean sameSymbol(Symbol other);
	
	/**
	 * @param other Object to compare to
	 * @return <code>true</code> if this Symbol is the same as the other Object, and <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof Symbol))
			return false;
		return sameSymbol((Symbol)other);
	}
	
	/**
	 * @return An integer uniquely identifying this Symbol.  It should be
	 * consistent with equals.
	 */
	@Override
	public int hashCode()
	{
		return toString().hashCode()^icon.hashCode()^colorWeights().hashCode()^new Double(value()).hashCode();
	}
}
