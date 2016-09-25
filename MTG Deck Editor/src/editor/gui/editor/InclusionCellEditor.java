package editor.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;

import editor.collection.category.CategorySpec;
import editor.gui.display.CardTable;
import editor.util.MouseListenerFactory;

/**
 * This class represents an editor for a table cell containing a set of CategorySpecs for a
 * Card. It can be edited by double-clicking on it, which brings up a dialog showing the
 * available categories.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InclusionCellEditor extends AbstractCellEditor implements TableCellEditor
{
	/**
	 * EditorFrame containing the deck whose card's categories are being edited.
	 */
	private EditorFrame frame;
	/**
	 * List of categories the card belongs to.
	 */
	private List<CategorySpec> included;
	/**
	 * JPanel to show while editing is occurring.
	 */
	private JPanel editor;
	/**
	 * Panel showing the list of categories to allow editing.
	 */
	private IncludeExcludePanel iePanel;
	
	/**
	 * Create a new InclusionCellEditor from the given EditorFrame.
	 * 
	 * @param f EditorFrame containing the table the new InclusionCellEditor goes in
	 */
	public InclusionCellEditor(EditorFrame f)
	{
		frame = f;
		editor = new JPanel()
		{
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				int s = getHeight();
				for (int i = 0; i < included.size(); i++)
				{
					int x = i*(s + 1) + 1;
					int y = 1;
					g.setColor(included[i].getColor());
					g.fillRect(x, y, s - 3, s - 3);
					g.setColor(Color.BLACK);
					g.drawRect(x, y, s - 3, s - 3);
				}
			}
		};
		editor.addMouseListener(MouseListenerFactory.createPressListener((e) -> {
			if (JOptionPane.showConfirmDialog(frame, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				fireEditingStopped();
			else
				fireEditingCanceled();
			iePanel = null;
		}));
	}
	
	/**
	 * @return <code>true</code>, if the user double-clicked on the cell, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isCellEditable(EventObject eo)
	{
		if (eo instanceof MouseEvent && ((MouseEvent)eo).getClickCount() > 1)
			return true;
		else
			return false;
	}
	
	/**
	 * @return The IncludeExcludePanel containing category edit information.
	 */
	@Override
	public Object getCellEditorValue()
	{
		return iePanel;
	}

	/**
	 * Construct the IncludeExcludePanel and the appearance of the renderer panel, and then return
	 * the renderer panel.  The IncludeExcludePanel will be used when the renderer panel is clicked.
	 * 
	 * @param table Table containing the cell to edit
	 * @param value Value to set the editor component to
	 * @param isSelected Whether or not the cell is selected
	 * @param row Row of the cell being edited
	 * @param column Column of the cell being edited
	 * @return The renderer panel.
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		if (table instanceof CardTable)
		{
			CardTable cTable = (CardTable)table;
			iePanel = new IncludeExcludePanel(frame.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), frame.getCardAt(cTable, row));
			included = ((Collection<?>)value).stream().filter((o) -> o instanceof CategorySpec).map((o) -> (CategorySpec)o).collect(Collectors.toList());
			if (!table.isRowSelected(row))
				editor.setBackground(cTable.getRowColor(row));
			else
			{
				editor.setBackground(table.getSelectionBackground());
				if (table.hasFocus())
					editor.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			}
		}
		else
			iePanel = new IncludeExcludePanel(frame.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), frame.getSelectedCards());
		return editor;
	}
}
