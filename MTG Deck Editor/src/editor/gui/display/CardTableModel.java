package editor.gui.display;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import editor.collection.CardList;
import editor.database.characteristics.CardData;
import editor.gui.editor.EditorFrame;
import editor.gui.editor.IncludeExcludePanel;

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
	 * List of card characteristics to display in the table.
	 */
	private List<CardData> characteristics;
	/**
	 * Editor containing this model's table.
	 */
	private EditorFrame editor;
	/**
	 * List of cards the table displays.
	 */
	private CardList list;
	
	/**
	 * Create a new CardTableModel.
	 *
	 * @param d list of cards for the new CardTableModel to show
	 * @param c list of characteristics of those cards to show
	 */
	public CardTableModel(CardList d, List<CardData> c)
	{
		this(null, d, c);
	}
	
	/**
	 * Create a new CardTableModel.
	 *
	 * @param e frame containing the table this model applies to
	 * @param d list of cards for the new CardTableModel to show
	 * @param c list of characteristics of those cards to show
	 */
	public CardTableModel(EditorFrame e, CardList d, List<CardData> c)
	{
		super();
		editor = e;
		list = d;
		characteristics = c;
	}
	
	@Override
	public Class<?> getColumnClass(int column)
	{
		return characteristics.get(column).dataType;
	}
	
	@Override
	public int getColumnCount()
	{
		return characteristics.size();
	}
	
	/**
	 * Get the type of data displayed by the given column.
	 * 
	 * @param column column to check
	 * @return the type of data being displayed in the given column.
	 */
	public CardData getColumnData(int column)
	{
		return characteristics.get(column);
	}
	
	@Override
	public String getColumnName(int column)
	{
		return characteristics.get(column).toString();
	}

	@Override
	public int getRowCount()
	{
		return list.size();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return list.getData(rowIndex).get(characteristics.get(columnIndex));
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return editor != null && (characteristics.get(column) == CardData.COUNT || characteristics.get(column) == CardData.CATEGORIES);
	}
	
	/**
	 * Set which columns are to be displayed by this CardTableModel.
	 * 
	 * @param c list of CardCharacteristics corresponding to the columns to display
	 */
	public void setColumns(List<CardData> c)
	{
		characteristics = c;
		fireTableStructureChanged();
	}
	
	/**
	 * Change the list of cards to display in this model's table.
	 * 
	 * @param d the new list to display
	 */
	public void setList(CardList d)
	{
		list = d;
	}

	@Override
	public void setValueAt(Object value, int row, int column)
	{
		if (isCellEditable(row, column))
		{
			switch (characteristics.get(column))
			{
			case COUNT:
				if (value instanceof Integer)
					list.set(list.get(row), (Integer)value);
				else
					throw new IllegalArgumentException("Illegal count value " + value);
				break;
			case CATEGORIES:
				if (value instanceof IncludeExcludePanel)
				{
					IncludeExcludePanel iePanel = (IncludeExcludePanel)value;
					editor.editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
				}
				else
					throw new IllegalArgumentException("Illegal inclusion value " + value);
				break;
			default:
				throw new IllegalArgumentException("Cannot edit data type " + characteristics.get(column));
			}
			fireTableDataChanged();
		}
	}
}
