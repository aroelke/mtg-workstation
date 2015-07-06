package gui.editor;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import database.Deck;
import database.characteristics.CardCharacteristic;

/**
 * This class represents the model for displaying the contents of a decklist.  A decklist
 * category looks like a decklist, so this is used to display those as well.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class DeckTableModel extends AbstractTableModel
{
	/**
	 * Master list of cards in the deck.
	 */
	private Deck cardList;
	/**
	 * List of Card characteristics to display in the table.
	 */
	private List<CardCharacteristic> characteristics;
	
	/**
	 * Create a new DeckTableModel.
	 * 
	 * @param list List of Cards for the new DeckTableModel to show
	 * @param c List of characteristics of those Cards to show
	 */
	public DeckTableModel(Deck list, List<CardCharacteristic> c)
	{
		super();
		cardList = list;
		characteristics = c;
	}
	
	/**
	 * Get the number of rows in the table.
	 */
	@Override
	public int getRowCount()
	{
		return cardList.size();
	}
	
	/**
	 * @param rowIndex Row index of the cell to get
	 * @param columnIndex Column index of the cell to get
	 * @return Value at the specified cell.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return characteristics.get(columnIndex).deckFunction.apply(cardList, rowIndex);
	}

	/**
	 * @return The number of columns in this DeckTableModel.
	 */
	@Override
	public int getColumnCount()
	{
		return characteristics.size();
	}
	
	/**
	 * @param column Column to look at
	 * @return The String representation of the characteristic at the specified column.
	 */
	@Override
	public String getColumnName(int column)
	{
		return characteristics.get(column).toString();
	}
	
	/**
	 * @param column Column to look at
	 * @return The class of the data in the specified column.
	 */
	@Override
	public Class<?> getColumnClass(int column)
	{
		return characteristics.get(column).columnClass;
	}
	
	/**
	 * TODO: Comment this
	 */
	@Override
	public boolean isCellEditable(int row, int column)
	{
		// TODO: Make a field in CardCharacteristic for this
		return characteristics.get(column).equals(CardCharacteristic.COUNT);
	}
	
	@Override
	public void setValueAt(Object value, int row, int column)
	{
		// TODO: Make a field in CardCharacteristic for this
		if (isCellEditable(row, column))
		{
			if (value instanceof Integer)
			{
				Integer v = (Integer)value;
				cardList.setCount(row, v);
			}
		}
	}
}
