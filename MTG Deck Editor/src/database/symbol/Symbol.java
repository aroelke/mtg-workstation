package database.symbol;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import database.characteristics.MTGColor;

/**
 * This class represents a symbol that might appear on a card in Magic: The Gathering.  It has a weight for each
 * color, which is equal to 1 divided by the number of colors the symbol has, or zero if it does not have that
 * color.  Instances of Symbol cannot be instantiated; rather, they are accessed using static variables that contain
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
	/**
	 * Icon to show when displaying this Symbol
	 */
	private ImageIcon icon;
	/**
	 * Name of the icon to show.
	 */
	private String iconName;
	
	/**
	 * Create a map of color weights for a Symbol, where the keys are MTGColors and the values
	 * are their weights (Doubles).
	 * 
	 * @param w Weight for white
	 * @param u Weight for blue
	 * @param b Weight for black
	 * @param r Weight for red
	 * @param g Weight for green
	 * @return The Map of MTGColors onto weights.
	 */
	public static Map<MTGColor, Double> createWeights(double w, double u, double b, double r, double g)
	{
		Map<MTGColor, Double> weights = new HashMap<MTGColor, Double>();
		weights.put(MTGColor.WHITE, w);
		weights.put(MTGColor.BLUE, u);
		weights.put(MTGColor.BLACK, b);
		weights.put(MTGColor.RED, r);
		weights.put(MTGColor.GREEN, g);
		return weights;
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
		// The symbol is not hybrid if it doesn't have a /
		if (!s.contains("/"))
		{
			// First try to make a colored symbol
			try
			{
				return ColorSymbol.SYMBOLS.get(MTGColor.get(s));
			}
			catch (IllegalArgumentException e)
			{
				// If that failed, try to make a colorless symbol
				try
				{
					int n = Integer.valueOf(s);
					if (n == 1000000)
						return ColorlessSymbol.MILLION;
					else if (n == 100)
						return ColorlessSymbol.HUNDRED;
					else
						return ColorlessSymbol.N[n];
				}
				catch (NumberFormatException n)
				{
					// If that failed, then the symbol must be X, Y, Z, a nonmana symbol, or a half-mana symbol
					char sym = s.toUpperCase().charAt(0);
					switch (sym)
					{
					case 'X':
						return XSymbol.X;
					case 'Y':
						return YSymbol.Y;
					case 'Z':
						return ZSymbol.Z;
					case 'S':
						return SnowSymbol.SNOW;
					case 'T':
						return TapSymbol.TAP;
					case 'Q':
						return UntapSymbol.UNTAP;
					case 'C':
						return ChaosSymbol.CHAOS;
					case 'P':
						return PhiSymbol.PHI;
					case 'H':
						return HalfColorSymbol.SYMBOLS.get(MTGColor.get(s.substring(1)));
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
			// If the String has a / in it, then ether it is a type of hybrid symbol or it is a colorless 1/2 mana
			// symbol
			String[] cols = s.split("/");
			if (cols[0].equals("1") && cols[1].equals("2"))
				return HalfManaSymbol.HALF_MANA;  
			try
			{
				if (cols[0].equals("2"))
					return TwobridSymbol.SYMBOLS.get(MTGColor.get(cols[1]));
				else if (cols[0].equalsIgnoreCase("P"))
					return PhyrexianSymbol.SYMBOLS.get(MTGColor.get(cols[1]));
				else if (cols[1].equalsIgnoreCase("P"))
					return PhyrexianSymbol.SYMBOLS.get(MTGColor.get(cols[0]));
				else
					return HybridSymbol.SYMBOLS.get(new MTGColor.Pair(MTGColor.get(cols[0]), MTGColor.get(cols[1])));
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
		this.iconName = iconName;
		this.icon = new ImageIcon("images/icons-large/" + iconName);
/*
		try
		{
			icon = ImageIO.read(new File("images/icons-large/" + iconName));
		}
		catch (IOException e)
		{
			System.out.println("Can't read file images/icons-large/" + iconName);
			e.printStackTrace();
			System.exit(1);
		}
*/
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
	public Map<MTGColor, Double> colorWeights()
	{
		return createWeights(0, 0, 0, 0, 0);
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
		return "<img src=\"images/icons-small/" + iconName + "\" />";
	}
	
	/**
	 * @return A String representation of this Symbol: its test surrounded by {}.
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
	public abstract int compareTo(Symbol other);
	
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
