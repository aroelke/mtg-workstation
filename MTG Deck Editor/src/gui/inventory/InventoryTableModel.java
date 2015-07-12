package gui.inventory;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import database.Inventory;
import database.characteristics.CardCharacteristic;

/**
 * This class represents a table model for displaying information about a list of cards.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InventoryTableModel extends AbstractTableModel
{
	/**
	 * List of cards to display.
	 */
	private Inventory inventory;
	/**
	 * List of characteristics to display.
	 */
	private List<CardCharacteristic> characteristics;
	
	/**
	 * Create a new CardListTableModel.
	 * 
	 * @param list List of Cards to display
	 * @param c List of characteristics of those Cards to display
	 */
	public InventoryTableModel(Inventory i, List<CardCharacteristic> c)
	{
		super();
		inventory = i;
		characteristics = c;
	}
	
	/**
	 * Set which columns are to be displayed by this InventoryTableModel.
	 * 
	 * @param c list of CardCharacteristics corresponding to the columns to display
	 */
	public void setColumns(List<CardCharacteristic> c)
	{
		characteristics = c;
		fireTableStructureChanged();
	}
	
	/**
	 * @return The number of columns in the table, which is the number of characteristics
	 * to display.
	 */
	@Override
	public int getColumnCount()
	{
		return characteristics.size();
	}

	/**
	 * @return The number of rows in the table, which is the number of Cards in it.
	 */
	@Override
	public int getRowCount()
	{
		return inventory.size();
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
	 * @param rowIndex The row of the cell to look at
	 * @param columnIndex The column of the cell to look at
	 * @return The data contained in the cell at the specified row and column.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return characteristics.get(columnIndex).inventoryFunc.apply(inventory, rowIndex);
	}
}
