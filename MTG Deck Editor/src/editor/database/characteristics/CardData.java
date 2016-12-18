package editor.database.characteristics;

import java.util.Date;
import java.util.List;
import java.util.Set;

import editor.collection.Inventory;
import editor.database.card.CardLayout;

/**
 * This enum represents a characteristic of a Magic: The Gathering card such as name, power, toughness,
 * etc.
 * 
 * @author Alec Roelke
 */
public enum CardData
{
	/**
	 * Name of a card.
	 */
	NAME("Name", String.class),
	/**
	 * {@link CardLayout} of a card.
	 */
	LAYOUT("Layout", CardLayout.class),
	/**
	 * Mana cost of a card.
	 */
	MANA_COST("Mana Cost", List.class),
	/**
	 * Converted mana cost of a card.
	 */
	CMC("CMC", List.class),
	/**
	 * Colors of all faces of a card.
	 */
	COLORS("Colors", ManaType.Tuple.class),
	/**
	 * Color identity of a card.
	 */
	COLOR_IDENTITY("Color Identity", ManaType.Tuple.class),
	/**
	 * Type line of a card.
	 */
	TYPE_LINE("Type", String.class),
	/**
	 * Name of the expansion a card was released in.
	 */
	EXPANSION_NAME("Expansion", String.class),
	/**
	 * Rarity of a card in its expansion.
	 */
	RARITY("Rarity", Rarity.class),
	/**
	 * Power of a creature card.
	 */
	POWER("Power", List.class),
	/**
	 * Toughness of a creature card.
	 */
	TOUGHNESS("Toughness", List.class),
	/**
	 * Loyalty of a planeswalker card.
	 */
	LOYALTY("Loyalty", List.class),
	/**
	 * Artist of a card.
	 */
	ARTIST("Artist", String.class),
	/**
	 * Set of formats a card is legal in.
	 */
	LEGAL_IN("Legal In", List.class),
	/**
	 * Categories in a deck in which a card belongs.
	 */
	CATEGORIES("Categories", Set.class),
	/**
	 * Number of copies of a card in a deck.
	 */
	COUNT("Count", Integer.class),
	/**
	 * Date a card was added to a deck.
	 */
	DATE_ADDED("Date Added", Date.class);
	
	/**
	 * Parse a String for a CardData.
	 * 
	 * @param s String to parse
	 * @return the CardData that corresponds to the given String.
	 * @throws IllegalArgumentException if no such CardData exists
	 */
	public static CardData parseCardData(String s) throws IllegalArgumentException
	{
		for (CardData c: CardData.values())
			if (c.toString().equalsIgnoreCase(s))
				return c;
		throw new IllegalArgumentException("Illegal characteristic string \"" + s + "\"");
	}
	
	/**
	 * Get the types of CardData that can be returned by an {@link Inventory}.
	 * 
	 * @return An array containing the CardData that can be shown in the inventory table.
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
	 * Class of the data that will appear in table columns containing data of this characteristic.
	 */
	public final Class<?> dataType;
	/**
	 * Name of the characteristic
	 */
	private final String name;
	
	/**
	 * Create a CardCharacteristic with the specified name and column class.
	 * 
	 * @param n name of the new CardData
	 * @param c class of the corresponding information on a card
	 */
	private CardData(String n, Class<?> c)
	{
		name = n;
		dataType = c;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
