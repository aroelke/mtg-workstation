package editor.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
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
import javax.swing.table.TableCellEditor;

import editor.collection.category.CategorySpec;
import editor.gui.CardTable;

/**
 * TODO: Comment this
 * @author Alec
 */
@SuppressWarnings("serial")
public class InclusionCellEditor extends AbstractCellEditor implements TableCellEditor
{
	EditorFrame frame;
	List<CategorySpec> included;
	JPanel editor;
	IncludeExcludePanel iePanel;
	
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
					g.setColor(included.get(i).getColor());
					g.fillRect(x, y, s - 3, s - 3);
					g.setColor(Color.BLACK);
					g.drawRect(x, y, s - 3, s - 3);
				}
			}
		};
		editor.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (JOptionPane.showConfirmDialog(frame, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
					fireEditingStopped();
				else
					fireEditingCanceled();
			}
		});
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
	
	@Override
	public Object getCellEditorValue()
	{
		return iePanel;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		if (table instanceof CardTable)
		{
			CardTable cTable = (CardTable)table;
			iePanel = new IncludeExcludePanel(frame.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), frame.getCardAt(cTable, row));
			included = ((Collection<?>)value).stream().filter((o) -> o instanceof CategorySpec).map((o) -> (CategorySpec)o).collect(Collectors.toList());
			editor.setBackground(cTable.getRowColor(row));
		}
		else
			iePanel = new IncludeExcludePanel(frame.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), frame.getSelectedCards());
		return editor;
	}
}
