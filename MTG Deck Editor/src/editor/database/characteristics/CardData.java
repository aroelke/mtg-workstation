package editor.database.characteristics;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.table.TableCellEditor;

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
public enum CardData
{
	// TODO: Function<Card, ?> rather than BiFunction<CardList, Integer, ?> or move it into Card
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
	
	COUNT("Count", Integer.class, (e) -> (c, n) -> {
		if (n instanceof Integer)
			e.setCardCount(c, ((Integer)n).intValue());
		else
			throw new IllegalArgumentException("Illegal count value " + n);
	}),
	CATEGORIES("Categories", Set.class, (e) -> (c, p) -> {
		if (p instanceof IncludeExcludePanel)
		{
			IncludeExcludePanel iePanel = (IncludeExcludePanel)p;
			e.editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
		}
		else
			throw new IllegalArgumentException("Illegal inclusion value " + p);
	}),
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
	public final Class<?> columnClass;
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
	 * @param ef Function to edit deck values from the cell
	 * @param e Editor that should be used to perform the editing
	 */
	private CardData(String n, Class<?> c, Function<EditorFrame, BiConsumer<Card, Object>> ef)
	{
		name = n;
		columnClass = c;
		editFunc = ef;
	}
	
	/**
	 * Create a CardCharacteristic with the specified name, column class, value function, and category function.
	 * 
	 * @param n Name of the new CardCharacteristic
	 * @param c Table column class of the new CardCharacteristic
	 */
	private CardData(String n, Class<?> c)
	{
		this(n, c, null);
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
