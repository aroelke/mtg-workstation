package editor.database.symbol;

import java.awt.Image;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

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
public abstract class Symbol
{
	/**
	 * Pattern for finding an individual symbol in a string.
	 */
	public static final Pattern SYMBOL_PATTERN = Pattern.compile("\\{([^}]+)\\}");
	
	/**
	 * Create a Symbol from a String.
	 * 
	 * @param s String representation of the new Symbol, not surrounded by {}
	 * @return A new Symbol that the specified String represents, or null if there is
	 * no such Symbol.
	 */
	public static Symbol get(String s)
	{
		Symbol value;
		if ((value = ManaSymbol.get(s)) != null)
			return value;
		else if ((value = StaticSymbol.get(s)) != null)
			return value;
		else
			return null;
	}
	
	/**
	 * Icon to show when displaying this Symbol.
	 */
	private final ImageIcon icon;
	/**
	 * Name of the file containing the icon (not including parent directory).
	 */
	private final String name;
	/**
	 * TODO: Comment this;
	 */
	private final boolean mana;
	
	/**
	 * Create a new Symbol.
	 * 
	 * @param iconName Name of the icon file for the new Symbol
	 * @param m Whether or not this Symbol can appear in a mana cost
	 */
	protected Symbol(String iconName, boolean m)
	{
		icon = new ImageIcon("images/icons/" + iconName);
		name = iconName;
		mana = m;
	}
	
	/**
	 * @return A String representation of this Symbol, without {}.
	 */
	public abstract String getText();
	
	/**
	 * TODO: comment this
	 * @return
	 */
	public boolean isMana()
	{
		return mana;
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
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
	 * @return The name of this Symbol.
	 */
	public String getName()
	{
		return name;
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
