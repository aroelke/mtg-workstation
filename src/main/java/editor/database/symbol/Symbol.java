package editor.database.symbol;

import java.awt.Image;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * This class represents a symbol that might appear on a card in Magic: The Gathering.  It has a weight for each
 * color, which is equal to 1 divided by the number of colors the symbol has, or zero if it does not have that
 * color.  Symbol cannot be instantiated; rather, instances of it are accessed using static variables that contain
 * all possible values those symbols can attain.  For colored symbols, those variables will be Maps mapping colors
 * onto the correct symbols.  For constant generic symbols, they will be an array containing the colorless symbol
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
     * @param s String representation of the new symbol, not surrounded by {}
     * @return a new symbol that the specified String represents
     * @throws IllegalArgumentException if the String doesn't correspond to a symbol
     */
    public static Symbol parseSymbol(String s) throws IllegalArgumentException
    {
        return tryParseSymbol(s).orElseThrow(() -> new IllegalArgumentException('"' + s + "\" is not a symbol"));
    }

    /**
     * Create a Symbol from a String.
     *
     * @param s String representation of the new symbol, not surrounded by {}
     * @return a new symbol that the specified String represents, or null if there is
     * no such symbol.
     */
    public static Optional<? extends Symbol> tryParseSymbol(String s)
    {
        var mana = ManaSymbol.tryParseManaSymbol(s);
        if (mana.isPresent())
            return mana;
        var functional = FunctionalSymbol.tryParseFunctionalSymbol(s);
        if (functional.isPresent())
            return functional;
        return Optional.empty();
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
     * @param iconName name of the icon file for the new Symbol
     */
    protected Symbol(String iconName, String t)
    {
        ImageIcon img;
        try
        {
            img = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/images/icons/" + iconName)));
        }
        catch (IOException e)
        {
            img = new ImageIcon();
            e.printStackTrace();
        }
        icon = img;
        name = iconName;
        text = t;
    }

    @Override
    public boolean equals(Object other)
    {
        return other != null && (other == this || other.getClass() == getClass() && toString().equals(other.toString()));
    }

    /**
     * Get the icon that should be used to display this Symbol.
     *
     * @return this Symbol's icon with its default width.
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Get this Symbol's icon with the specified width.  The height will be scaled accordingly as well.
     *
     * @param newSize width of the icon
     * @return the resized icon
     */
    public Icon getIcon(int newSize)
    {
        return new ImageIcon(icon.getImage().getScaledInstance(-1, newSize, Image.SCALE_SMOOTH));
    }

    /**
     * Get the name of this Symbol.
     *
     * @return the name of this Symbol
     */
    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    /**
     * {@inheritDoc}
     * The String representation of this Symbol is its text surrounded by {}.
     */
    @Override
    public String toString()
    {
        return "{" + text + "}";
    }
}
