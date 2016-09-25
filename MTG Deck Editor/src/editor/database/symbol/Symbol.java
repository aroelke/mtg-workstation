package editor.database.symbol;

import java.awt.Image;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	 * Name for a Symbol whose icon file can't be found.
	 */
	public static final String UNKNOWN_ICON = "unknown.png";
	
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
		else if ((value = FunctionalSymbol.get(s)) != null)
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
	 * The textual representation of this Symbol.
	 */
	private final String text;
	
	/**
	 * Create a new Symbol.
	 * 
	 * @param iconName Name of the icon file for the new Symbol
	 */
	protected Symbol(String iconName, String t)
	{
		if (Files.notExists(Paths.get("images/icons/" + iconName)))
		{
			System.err.println("Could not load file images/icons/" + iconName);
			iconName = UNKNOWN_ICON;
		}
		icon = new ImageIcon("images/icons/" + (name = iconName));
		text = t;
	}
	
	/**
	 * @return This Symbol's icon with its default with.
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
		return "{" + text + "}";
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
