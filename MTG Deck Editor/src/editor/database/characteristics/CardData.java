package editor.database.characteristics;

import java.util.Date;
import java.util.List;
import java.util.Set;

import editor.database.card.CardLayout;

/**
 * This enum represents a characteristic of a Magic: The Gathering card such as name, power, toughness,
 * etc.
 * 
 * @author Alec Roelke
 */
public enum CardData
{
	NAME("Name", String.class),
	LAYOUT("Layout", CardLayout.class),
	MANA_COST("Mana Cost", ManaCost.Tuple.class),
	CMC("CMC", List.class),
	COLORS("Colors", ManaType.Tuple.class),
	COLOR_IDENTITY("Color Identity", ManaType.Tuple.class),
	TYPE_LINE("Type", String.class),
	EXPANSION_NAME("Expansion", String.class),
	RARITY("Rarity", Rarity.class),
	POWER("Power", PowerToughness.Tuple.class),
	TOUGHNESS("Toughness", PowerToughness.Tuple.class),
	LOYALTY("Loyalty", Loyalty.Tuple.class),
	ARTIST("Artist", String.class),
	LEGAL_IN("Legal In", List.class),
	
	COUNT("Count", Integer.class),
	CATEGORIES("Categories", Set.class),
	DATE_ADDED("Date Added", Date.class);
	
	/**
	 * Parse a String for a CardCharacteristic.
	 * 
	 * @param s String to parse
	 * @return The CardCharacteristic that corresponds to the given String.
	 */
	public static CardData get(String s)
	{
		for (CardData c: CardData.values())
			if (c.toString().equalsIgnoreCase(s))
				return c;
		throw new IllegalArgumentException("Illegal characteristic string \"" + s + "\"");
	}
	
	/**
	 * @return An array containing the CardCharacteristics that can be shown in the inventory table.
	 */
	public static CardData[] inventoryValues()
	{
		return new CardData[] {NAME,
							   LAYOUT,
							   MANA_COST,
							   CMC,
							   COLORS,
							   COLOR_IDENTITY,
							   TYPE_LINE,
							   EXPANSION_NAME,
							   RARITY,
							   POWER,
							   TOUGHNESS,
							   LOYALTY,
							   ARTIST,
							   LEGAL_IN,
							   DATE_ADDED};
	}
	
	/**
	 * Name of the characteristic
	 */
	private final String name;
	/**
	 * Class of the data that will appear in table columns containing data of this characteristic.
	 */
	public final Class<?> dataType;
	
	/**
	 * Create a CardCharacteristic with the specified name, column class, value function, and category function.
	 * 
	 * @param n Name of the new CardCharacteristic
	 * @param c Table column class of the new CardCharacteristic
	 * @param ef Function to edit deck values from the cell
	 * @param e Editor that should be used to perform the editing
	 */
	private CardData(String n, Class<?> c)
	{
		name = n;
		dataType = c;
	}
	
	/**
	 * @return <code>true</code> if this CardCharacteristic can be edited, and <code>false</code>
	 * otherwise.
	 */
	public boolean isEditable()
	{
		return this == COUNT || this == CATEGORIES;
	}
	
	/**
	 * @return A String representation of this CardCharacteristic (its name).
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
