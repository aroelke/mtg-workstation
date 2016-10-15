package editor.database.characteristics;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.table.TableCellEditor;

import editor.collection.CardList;
import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.gui.editor.EditorFrame;
import editor.gui.editor.IncludeExcludePanel;
import editor.gui.editor.InclusionCellEditor;
import editor.gui.generic.SpinnerCellEditor;

/**
 * This enum represents a characteristic of a Magic: The Gathering card such as name, power, toughness,
 * etc.
 * 
 * @author Alec Roelke
 */
public enum CardCharacteristic
{
	NAME("Name", String.class, (l, i) -> l[i].unifiedName()),
	COUNT("Count", Integer.class, (l, i) -> l.getData(i).count(), (e) -> (c, n) -> {
		if (n instanceof Integer)
			e.setCardCount(c, ((Integer)n).intValue());
		else
			throw new IllegalArgumentException("Illegal count value " + n);
	}),
	LAYOUT("Layout", CardLayout.class, (l, i) -> l[i].layout()),
	MANA_COST("Mana Cost", ManaCost.Tuple.class, (l, i) -> l[i].manaCost()),
	CMC("CMC", List.class, (l, i) -> l[i].cmc()),
	COLORS("Colors", ManaType.Tuple.class, (l, i) -> l[i].colors()),
	COLOR_IDENTITY("Color Identity", ManaType.Tuple.class, (l, i) -> l[i].colorIdentity()),
	TYPE_LINE("Type", String.class, (l, i) -> l[i].unifiedTypeLine()),
	EXPANSION_NAME("Expansion", String.class, (l, i) -> l[i].expansion().toString()),
	RARITY("Rarity", Rarity.class, (l, i) -> l[i].rarity()),
	POWER("Power", PowerToughness.Tuple.class, (l, i) -> l[i].power()),
	TOUGHNESS("Toughness", PowerToughness.Tuple.class, (l, i) -> l[i].toughness()),
	LOYALTY("Loyalty", Loyalty.Tuple.class, (l, i) -> l[i].loyalty()),
	ARTIST("Artist", String.class, (l, i) -> l[i].artist()[0]),
	LEGAL_IN("Legal In", List.class, (l, i) -> l[i].legalIn()),
	CATEGORIES("Categories", Set.class, (l, i) -> l.getData(i).categories(), (e) -> (c, p) -> {
		if (p instanceof IncludeExcludePanel)
		{
			IncludeExcludePanel iePanel = (IncludeExcludePanel)p;
			e.editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
		}
		else
			throw new IllegalArgumentException("Illegal inclusion value " + p);
	}),
	DATE_ADDED("Date Added", Date.class, (l, i) -> l.getData(i).dateAdded());
	
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
	public final Class<?> columnClass;
	/**
	 * Function taking a list of cards and returning a characteristic of a card from a category of that list.
	 */
	private final BiFunction<CardList, Integer, ?> func;
	/**
	 * Function for editing the value corresponding to the characteristic.  It should be null for constant
	 * characteristics.
	 */
	private final Function<EditorFrame, BiConsumer<Card, Object>> editFunc;
	
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
	private CardCharacteristic(String n, Class<?> c, BiFunction<CardList, Integer, ?> f, Function<EditorFrame, BiConsumer<Card, Object>> ef)
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
	private CardCharacteristic(String n, Class<?> c, BiFunction<CardList, Integer, ?> f)
	{
		this(n, c, f, null);
	}
	
	/**
	 * Get the value of the CardCharacteristic of the card at the given index into the
	 * collection
	 * 
	 * @param c CardCollection to search
	 * @param i Index into the collection
	 * @return The value of the CardCharacteristic.
	 */
	public Object get(CardList c, int i)
	{
		return func.apply(c, i);
	}
	
	/**
	 * Create an instance of the editor for cells containing this CardCharacteristic.
	 * 
	 * @param frame Frame containing the table with the cell to edit
	 * @return An instance of the editor for this CardCharacteristic, or null if it can't be
	 * edited.
	 */
	public TableCellEditor createCellEditor(EditorFrame frame)
	{
		switch (this)
		{
		case COUNT:
			return new SpinnerCellEditor();
		case CATEGORIES:
			return new InclusionCellEditor(frame);
		default:
			return null;
		}
	}
	
	/**
	 * @return <code>true</code> if this CardCharacteristic can be edited, and <code>false</code>
	 * otherwise.
	 */
	public boolean isEditable()
	{
		return editFunc != null;
	}
	
	/**
	 * Edit the value of a CardCharacteristic in a table of the given EditorFrame.
	 * 
	 * @param frame EditorFrame containing the table cell to edit
	 * @param c Card to edit the value of
	 * @param value New value for the CardCharacteristic
	 */
	public void edit(EditorFrame frame, Card c, Object value)
	{
		editFunc.apply(frame).accept(c, value);
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
