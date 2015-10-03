package database.characteristics;

/**
 * This class represents an expansion set of Magic: The Gathering cards.  It has a name, a block, a code,
 * and a number of cards.
 * 
 * TODO: Add the symbol for each expansion, as well as a renderer for tables
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
	 * This Expansion's name.
	 */
	public final String name;
	/**
	 * Name of the block this Expansion belongs to (empty if there is none).
	 */
	public final String block;
	/**
	 * This Expansion's code on magiccards.info.
	 */
	public final String code;
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
	 * Create a new Expansion.
	 * 
	 * @param name Name of the new expansion
	 * @param block Name of the block the new Expansion belongs to
	 * @param code Code of the new Expansion (usually three letters)
	 * @param g Gatherer code of the new Expansion
	 * @param count Number of cards in the new Expansion
	 */
	public Expansion(String name, String block, String code, String g, int count)
	{
		this.name = name;
		this.block = block;
		this.code = code;
		this.gathererCode = g;
		this.count = count;
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
		if (!(other instanceof Expansion))
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
