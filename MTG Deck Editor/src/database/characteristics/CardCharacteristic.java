package database.characteristics;

import gui.editor.EditorFrame;
import gui.editor.SpinnerCellEditor;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.swing.table.TableCellEditor;

import util.TriConsumer;
import database.Card;
import database.Deck;
import database.Inventory;

/**
 * This enum represents a characteristic of a Magic: The Gathering card such as name, power, toughness,
 * etc.
 * 
 * TODO: Make the classes and functions be arrays for multi-face cards where appropriate
 * 
 * @author Alec Roelke
 */
public enum CardCharacteristic
{
	NAME("Name", String.class, (l, i) -> l.get(i).name(), (l, i) -> l.get(i).name()),
	COUNT("Count", Integer.class, null, (l, i) -> l.count(i), (e, c, n) -> {
		if (n instanceof Integer)
			e.setCardCount(c, ((Integer)n).intValue());
		else
			throw new IllegalArgumentException("Illegal count value " + n);
	}, new SpinnerCellEditor()),
	MANA_COST("Mana Cost", ManaCost.Tuple.class, (l, i) -> l.get(i).mana(), (l, i) -> l.get(i).mana()),
	COLORS("Colors", MTGColor.Tuple.class, (l, i) -> l.get(i).colors(), (l, i) -> l.get(i).colors()),
	COLOR_IDENTITY("Color Identity", MTGColor.Tuple.class, (l, i) -> l.get(i).colorIdentity(), (l, i) -> l.get(i).colorIdentity()),
	TYPE_LINE("Type", String.class, (l, i) -> l.get(i).typeLine(), (l, i) -> l.get(i).typeLine()),
	EXPANSION_NAME("Expansion", String.class, (l, i) -> l.get(i).expansion().name, (l, i) -> l.get(i).expansion().name),
	RARITY("Rarity", Rarity.class, (l, i) -> l.get(i).rarity(), (l, i) -> l.get(i).rarity()),
	POWER("Power", PowerToughness.Tuple.class, (l, i) -> l.get(i).power(), (l, i) -> l.get(i).power()),
	TOUGHNESS("Toughness", PowerToughness.Tuple.class, (l, i) -> l.get(i).toughness(), (l, i) -> l.get(i).toughness()),
	LOYALTY("Loyalty", Loyalty.Tuple.class, (l, i) -> l.get(i).loyalty(), (l, i) -> l.get(i).loyalty()),
	ARTIST("Artist", String.class, (l, i) -> l.get(i).artist(), (l, i) -> l.get(i).artist()),
	LEGAL_IN("Legal In", List.class, (l, i) -> l.get(i).legalIn(), (l, i) -> l.get(i).legalIn()),
	CATEGORIES("Categories", List.class, null, (l, i) -> l.getCategories(i)),
	DATE_ADDED("Date Added", Date.class, null, (l, i) -> l.dateAdded(i));
	
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
	 * @return An array containing the CardCharacteristics that can be shown in the inventory table.
	 */
	public static CardCharacteristic[] inventoryValues()
	{
		List<CardCharacteristic> characteristics = Arrays.stream(values()).filter((v) -> v.inventoryFunc != null).collect(Collectors.toList());
		return characteristics.toArray(new CardCharacteristic[characteristics.size()]);
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
	public final TriConsumer<EditorFrame, Card, Object> editFunc;
	/**
	 * The editor that should be used to edit this characteristic.
	 */
	public final TableCellEditor editor;
	
	/**
	 * Create a CardCharacteristic with the specified name, column class, value function, and category function.
	 * 
	 * @param n Name of the new CardCharacteristic
	 * @param c Table column class of the new CardCharacteristic
	 * @param f Function to get cell values from an Inventory table
	 * @param cf Function to get cell values from a Deck table
	 * @param ef Function to edit deck values from the cell
	 * @param e Editor that should be used to perform the editing
	 */
	private CardCharacteristic(String n, Class<?> c, BiFunction<Inventory, Integer, ?> f, BiFunction<Deck, Integer, ?> cf, TriConsumer<EditorFrame, Card, Object> ef, TableCellEditor e)
	{
		name = n;
		columnClass = c;
		inventoryFunc = f;
		deckFunc = cf;
		editFunc = ef;
		editor = e;
	}
	
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
		deckFunc = cf;
		editFunc = null;
		editor = null;
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
