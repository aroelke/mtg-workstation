package editor.gui.display;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import editor.collection.CardList;
import editor.database.characteristics.CardCharacteristic;
import editor.gui.editor.EditorFrame;

/**
 * This class represents the model for displaying the contents of a decklist.  A decklist
 * category looks like a decklist, so this is used to display those as well.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardTableModel extends AbstractTableModel
{
	/**
	 * Editor containing this model's table.
	 */
	private EditorFrame editor;
	/**
	 * List of Cards the table displays.
	 */
	private CardList list;
	/**
	 * List of Card characteristics to display in the table.
	 */
	private List<CardCharacteristic> characteristics;
	
	/**
	 * Create a new DeckTableModel.
	 *
	 * @param e EditorFrame containing the table this model applies to
	 * @param list List of Cards for the new CardTableModel to show
	 * @param c List of characteristics of those Cards to show
	 */
	public CardTableModel(EditorFrame e, CardList d, List<CardCharacteristic> c)
	{
		super();
		editor = e;
		list = d;
		characteristics = c;
	}
	
	/**
	 * Create a new DeckTableModel.
	 *
	 * @param list List of Cards for the new CardTableModel to show
	 * @param c List of characteristics of those Cards to show
	 */
	public CardTableModel(CardList d, List<CardCharacteristic> c)
	{
		this(null, d, c);
	}
	
	/**
	 * Set which columns are to be displayed by this DeckTableModel.
	 * 
	 * @param c list of CardCharacteristics corresponding to the columns to display
	 */
	public void setColumns(List<CardCharacteristic> c)
	{
		characteristics = c;
		fireTableStructureChanged();
	}
	
	/**
	 * Change the list of Cards to display in this model's table.
	 * @param d the new list to display
	 */
	public void setList(CardList d)
	{
		list = d;
	}
	
	/**
	 * Get the number of rows in the table.
	 */
	@Override
	public int getRowCount()
	{
		return list.size();
	}
	
	/**
	 * @param rowIndex Row index of the cell to get
	 * @param columnIndex Column index of the cell to get
	 * @return Value at the specified cell.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return characteristics[columnIndex].get(list, rowIndex);
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
	 * @param column Column to check
	 * @return The CardCharacteristic being displayed in the given column.
	 */
	public CardCharacteristic getColumnCharacteristic(int column)
	{
		return characteristics[column];
	}
	
	/**
	 * @param column Column to look at
	 * @return The String representation of the characteristic at the specified column.
	 */
	@Override
	public String getColumnName(int column)
	{
		return characteristics[column].toString();
	}
	
	/**
	 * @param column Column to look at
	 * @return The class of the data in the specified column.
	 */
	@Override
	public Class<?> getColumnClass(int column)
	{
		return characteristics[column].columnClass;
	}
	
	/**
	 * @param row Row of the cell to check
	 * @param column Column of the cell to check
	 * @return <code>true</code> if the cell at the specified location is editable and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isCellEditable(int row, int column)
	{
		return editor != null && characteristics[column].isEditable();
	}
	
	/**
	 * Set the value of the cell at the specified location if it is editable.
	 * 
	 * @param value Value to set
	 * @param row Row of the cell to set
	 * @param column Column of the cell to set
	 */
	@Override
	public void setValueAt(Object value, int row, int column)
	{
		if (isCellEditable(row, column))
		{
			characteristics[column].edit(editor, list[row], value);
			fireTableDataChanged();
		}
	}
}
