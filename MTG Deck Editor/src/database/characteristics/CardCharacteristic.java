package database.characteristics;

import java.util.List;
import java.util.function.BiFunction;

import util.TriConsumer;
import database.Deck;
import database.Inventory;
import database.ManaCost;

/**
 * This enum represents a characteristic of a Magic: The Gathering card such as name, power, toughness,
 * etc.
 * 
 * @author Alec Roelke
 */
public enum CardCharacteristic
{
	NAME("Name", String.class, (l, i) -> l.get(i).name, (l, i) -> l.get(i).name, null),
	EXPANSION_NAME("Expansion", String.class, (l, i) -> l.get(i).set.name, (l, i) -> l.get(i).set.name, null),
	MANA_COST("Mana Cost", ManaCost.class, (l, i) -> l.get(i).mana, (l, i) -> l.get(i).mana, null),
	TYPE_LINE("Type", String.class, (l, i) -> l.get(i).typeLine, (l, i) -> l.get(i).typeLine, null),
	COUNT("Count", Integer.class, null, (l, i) -> l.count(i), (l, i, n) -> {
		if (n instanceof Integer)
			l.setCount(i, (Integer)n);
		else
			throw new IllegalArgumentException("Illegal count value " + n);
	}),
	RARITY("Rarity", Rarity.class, (l, i) -> l.get(i).rarity, (l, i) -> l.get(i).rarity, null),
	LEGAL_IN("Legal In", List.class, (l, i) -> l.get(i).legalIn(), (l, i) -> l.get(i).legalIn(), null);
	
	/**
	 * Parse a String for a CardCharacteristic.
	 * 
	 * @param s String to parse
	 * @return The CardCharacteristic that corresponds to the given String.
	 */
	public static CardCharacteristic get(String s)
	{
		for (CardCharacteristic c: CardCharacteristic.values())
			if (c.toString().equalsIgnoreCase(s))
				return c;
		throw new IllegalArgumentException("Illegal characteristic string \"" + s + "\"");
	}
	
	/**
	 * Name of the characteristic
	 */
	private final String name;
	/**
	 * Class of the data that will appear in table columns containing data of this characteristic.
	 */
	public final Class<?> columnClass;
	/**
	 * Function taking a list of cards and returning a characteristic of a card from that list.
	 */
	public final BiFunction<Inventory, Integer, ?> inventoryFunc;
	/**
	 * Function taking a list of cards and returning a characteristic of a card from a category of that list.
	 */
	public final BiFunction<Deck, Integer, ?> deckFunc;
	/**
	 * Function for editing the value corresponding to the characteristic.  It should be null for constant
	 * characteristics.
	 */
	public final TriConsumer<Deck, Integer, Object> editFunc;
	
	/**
	 * Create a CardCharacteristic with the specified name, column class, value function, and category function.
	 * 
	 * @param n Name of the new CardCharacteristic
	 * @param c Table column class of the new CardCharacteristic
	 * @param f Function to get cell values from an Inventory table
	 * @param cf Function to get cell values from a Deck table
	 */
	private CardCharacteristic(String n, Class<?> c, BiFunction<Inventory, Integer, ?> f, BiFunction<Deck, Integer, ?> cf, TriConsumer<Deck, Integer, Object> ef)
	{
		name = n;
		columnClass = c;
		inventoryFunc = f;
		deckFunc = cf;
		editFunc = ef;
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
