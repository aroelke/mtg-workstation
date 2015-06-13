package database.characteristics;

import java.util.function.BiFunction;

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
	NAME("Name", String.class, (l, i) -> l.get(i).name, (l, i) -> l.get(i).name),
	EXPANSION_NAME("Expansion", String.class, (l, i) -> l.get(i).set.name, (l, i) -> l.get(i).set.name),
	MANA_COST("Mana Cost", ManaCost.class, (l, i) -> l.get(i).mana, (l, i) -> l.get(i).mana),
	TYPE_LINE("Type", String.class, (l, i) -> l.get(i).typeLine, (l, i) -> l.get(i).typeLine),
	COUNT("Count", Integer.class, (l, i) -> 1, (l, i) -> l.count(i)),
	RARITY("Rarity", Rarity.class, (l, i) -> l.get(i).rarity, (l, i) -> l.get(i).rarity),
	LEGAL_IN("Legal In", Legality.class, (l, i) -> l.get(i).legalIn(), (l, i) -> l.get(i).legalIn());
	
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
	public final BiFunction<Deck, Integer, ?> deckFunction;
	
	/**
	 * Create a CardCharacteristic with the specified name, column class, value function, and category function.
	 * 
	 * @param n Name of the new CardCharacteristic
	 * @param c Table column class of the new CardCharacteristic
	 * @param f Function to get cell values from an Inventory table
	 * @param cf Function to get cell values from a Deck table
	 */
	private CardCharacteristic(String n, Class<?> c, BiFunction<Inventory, Integer, ?> f, BiFunction<Deck, Integer, ?> cf)
	{
		name = n;
		columnClass = c;
		inventoryFunc = f;
		deckFunction = cf;
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
