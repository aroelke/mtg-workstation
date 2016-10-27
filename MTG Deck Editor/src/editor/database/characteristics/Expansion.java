package editor.database.characteristics;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents an expansion set of Magic: The Gathering cards.  It has a name, a block, a code,
 * and a number of cards.
 * 
 * TODO: Add (some) other fields from MTGJSON and add functionality for them
 * 
 * @author Alec
 */
public class Expansion implements Comparable<Expansion>
{
	/**
	 * Array containing all expansion names.
	 */
	public static Expansion[] expansions = {};
	/**
	 * Array containing all block names.
	 */
	public static String[] blocks = {};
	/**
	 * Date format used to decode expansion release dates.
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-DD");
	
	/**
	 * This Expansion's name.
	 */
	public final String name;
	/**
	 * Name of the block this Expansion belongs to (empty if there is none).
	 */
	public final String block;
	/**
	 * This Expansion's code.
	 */
	public final String code;
	/**
	 * The old Gatherer code used for this Expansion.
	 */
	public final String oldCode;
	/**
	 * This Expansion's code on magiccards.info.
	 */
	public final String magicCardsInfoCode;
	/**
	 * This expansion's code on Gatherer, or null if it's the same as the
	 * set code or isn't on Gatherer.
	 */
	public final String gathererCode;
	/**
	 * Number of cards in this Expansion.
	 */
	public final int count;
	/**
	 * The date the expansion was released.
	 */
	public final Date releaseDate;
	
	/**
	 * Create a new Expansion.
	 * 
	 * @param name Name of the new expansion
	 * @param block Name of the block the new Expansion belongs to
	 * @param code Code of the new Expansion (usually three letters)
	 * @param old Old Gatherer code for the new Expansion
	 * @param magicCardsInfo magiccards.info code of the new Expansion
	 * @param gatherer Gatherer code of the new Expansion
	 * @param count Number of cards in the new Expansion
	 */
	public Expansion(String name, String block, String code, String old, String magicCardsInfo, String gatherer, int count, Date date)
	{
		this.name = name;
		this.block = block;
		this.code = code;
		this.oldCode = old;
		this.magicCardsInfoCode = magicCardsInfo;
		this.gathererCode = gatherer;
		this.count = count;
		this.releaseDate = date;
	}
	
	/**
	 * @return A String representation of this Expansion. 
	 */
	@Override
	public String toString()
	{
		return name;
	}
	
	/**
	 * @param other Object to test.
	 * @return <code>true</code> if this Expansion has the same name as the other one, and <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other.getClass() != getClass())
			return false;
		if (other == this)
			return true;
		
		Expansion o = (Expansion)other;
		return name.equals(o.name);
	}
	
	/**
	 * @return An integer uniquely identifying this Expansion.
	 */
	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
	
	@Override
	public int compareTo(Expansion other)
	{
		return name.compareTo(other.name);
	}
}
