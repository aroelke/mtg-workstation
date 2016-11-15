package editor.database.characteristics;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents an expansion set of Magic: The Gathering cards.
 * 
 * TODO: Add (some) other fields from MTGJSON and add functionality for them
 * 
 * @author Alec
 */
public class Expansion implements Comparable<Expansion>
{
	/**
	 * Array containing all block names.
	 */
	public static String[] blocks = {};
	/**
	 * Date format used to decode expansion release dates.
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-DD");
	/**
	 * Array containing all expansion names.
	 */
	public static Expansion[] expansions = {};
	
	/**
	 * Name of the block this Expansion belongs to (empty if there is none).
	 */
	public final String block;
	/**
	 * This Expansion's code.
	 */
	public final String code;
	/**
	 * Number of cards in this Expansion.
	 */
	public final int count;
	/**
	 * This expansion's code on Gatherer, or null if it's the same as the
	 * set code or isn't on Gatherer.
	 */
	public final String gathererCode;
	/**
	 * This Expansion's code on magiccards.info.
	 */
	public final String magicCardsInfoCode;
	/**
	 * This Expansion's name.
	 */
	public final String name;
	/**
	 * The old Gatherer code used for this Expansion.
	 */
	public final String oldCode;
	/**
	 * The date the expansion was released.
	 */
	public final Date releaseDate;
	
	/**
	 * Create a new Expansion.
	 * 
	 * @param name name of the new expansion
	 * @param block name of the block the new Expansion belongs to
	 * @param code code of the new Expansion (usually three letters)
	 * @param old old Gatherer code for the new Expansion
	 * @param magicCardsInfo magiccards.info code of the new Expansion
	 * @param gatherer gatherer code of the new Expansion
	 * @param count number of cards in the new Expansion
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
	
	@Override
	public int compareTo(Expansion other)
	{
		return name.compareTo(other.name);
	}
	
	/**
	 * {@inheritDoc}
	 * Expansions are equal if they have the same name.
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

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
	
	/**
	 * @return A String representation of this Expansion. 
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
