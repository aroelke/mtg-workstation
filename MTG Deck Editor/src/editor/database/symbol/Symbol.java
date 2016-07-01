package editor.database.symbol;

import java.awt.Image;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
	/**
	 * Pattern for finding an individual symbol in a string.
	 */
	public static final Pattern SYMBOL_PATTERN = Pattern.compile("\\{([^}]+)\\}");
	
	/**
	 * List of symbol types in the order they should appear in.
	 */
	public static final List<Class<?>> ORDER = Arrays.asList(
			VariableSymbol.class,
			GenericSymbol.class,
			HalfColorSymbol.class,
			TwobridSymbol.class,
			HybridSymbol.class,
			PhyrexianSymbol.class,
			ColorSymbol.class);
	
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
	 * are their weights (Doubles).
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
		Symbol value;
		if ((value = ColorSymbol.get(s)) != null)
			return value;
		else if ((value = GenericSymbol.get(s)) != null)
			return value;
		else if ((value = HalfColorSymbol.get(s)) != null)
			return value;
		else if ((value = HybridSymbol.get(s)) != null)
			return value;
		else if ((value = PhyrexianSymbol.get(s)) != null)
			return value;
		else if ((value = TwobridSymbol.get(s)) != null)
			return value;
		else if ((value = VariableSymbol.get(s)) != null)
			return value;
		else if ((value = StaticSymbol.get(s)) != null)
			return value;
		else
			return null;
	}
	
	/**
	 * Create a new Symbol.
	 * 
	 * @param iconName Name of the icon file for the new Symbol.
	 */
	public Symbol(String iconName)
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
		if (other.getClass() != getClass())
			return false;
		return toString().equals(other.toString());
	}
	
	/**
	 * @return An integer uniquely identifying this Symbol.  It should be
	 * consistent with equals.
	 */
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
}
