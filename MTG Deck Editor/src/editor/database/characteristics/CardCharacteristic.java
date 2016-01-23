package editor.database.characteristics;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import javax.swing.table.TableCellEditor;

import editor.collection.CardCollection;
import editor.database.Card;
import editor.gui.editor.EditorFrame;
import editor.gui.editor.SpinnerCellEditor;
import editor.util.TriConsumer;

/**
 * This enum represents a characteristic of a Magic: The Gathering card such as name, power, toughness,
 * etc.
 * 
 * TODO: Come up with an editor for the categories column
 * 
 * @author Alec Roelke
 */
public enum CardCharacteristic
{
	NAME("Name", String.class, (l, i) -> l.get(i).name()),
	COUNT("Count", Integer.class, (l, i) -> l.count(i), (e, c, n) -> {
		if (n instanceof Integer)
			e.setCardCount(c, ((Integer)n).intValue());
		else
			throw new IllegalArgumentException("Illegal count value " + n);
	}),
	MANA_COST("Mana Cost", ManaCost.Tuple.class, (l, i) -> l.get(i).mana()),
	COLORS("Colors", MTGColor.Tuple.class, (l, i) -> l.get(i).colors()),
	COLOR_IDENTITY("Color Identity", MTGColor.Tuple.class, (l, i) -> l.get(i).colorIdentity()),
	TYPE_LINE("Type", String.class, (l, i) -> l.get(i).typeLine()),
	EXPANSION_NAME("Expansion", String.class, (l, i) -> l.get(i).expansion().toString()),
	RARITY("Rarity", Rarity.class, (l, i) -> l.get(i).rarity()),
	POWER("Power", PowerToughness.Tuple.class, (l, i) -> l.get(i).power()),
	TOUGHNESS("Toughness", PowerToughness.Tuple.class, (l, i) -> l.get(i).toughness()),
	LOYALTY("Loyalty", Loyalty.Tuple.class, (l, i) -> l.get(i).loyalty()),
	ARTIST("Artist", String.class, (l, i) -> l.get(i).artist()),
	LEGAL_IN("Legal In", List.class, (l, i) -> l.get(i).legalIn()),
	CATEGORIES("Categories", Set.class, (l, i) -> l.getCategories(i)),
	DATE_ADDED("Date Added", Date.class, (l, i) -> l.dateAdded(i));
	
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
		return new CardCharacteristic[] {NAME,
										 MANA_COST,
										 COLORS,
										 COLOR_IDENTITY,
										 TYPE_LINE,
										 EXPANSION_NAME,
										 RARITY,
										 POWER,
										 TOUGHNESS,
										 LOYALTY,
										 ARTIST,
										 LEGAL_IN};
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
	 * Function taking a list of cards and returning a characteristic of a card from a category of that list.
	 */
	public final BiFunction<CardCollection, Integer, ?> func;
	/**
	 * Function for editing the value corresponding to the characteristic.  It should be null for constant
	 * characteristics.
	 */
	public final TriConsumer<EditorFrame, Card, Object> editFunc;
	
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
	private CardCharacteristic(String n, Class<?> c, BiFunction<CardCollection, Integer, ?> f, TriConsumer<EditorFrame, Card, Object> ef)
	{
		name = n;
		columnClass = c;
		func = f;
		editFunc = ef;
	}
	
	/**
	 * Create a CardCharacteristic with the specified name, column class, value function, and category function.
	 * 
	 * @param n Name of the new CardCharacteristic
	 * @param c Table column class of the new CardCharacteristic
	 * @param f Function to get cell values from an Inventory table
	 */
	private CardCharacteristic(String n, Class<?> c, BiFunction<CardCollection, Integer, ?> f)
	{
		this(n, c, f, null);
	}
	
	/**
	 * Create an instance of the editor for cells containing this CardCharacteristic.
	 * 
	 * @param frame Frame containing the table with the cell to edit. Reserved for future use.
	 * @return An instance of the editor for this CardCharacteristic, or null if it can't be
	 * edited.
	 */
	public TableCellEditor createCellEditor(EditorFrame frame)
	{
		switch (this)
		{
		case COUNT:
			return new SpinnerCellEditor();
		default:
			return null;
		}
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
