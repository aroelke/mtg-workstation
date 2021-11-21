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

import editor.collection.deck.Category;
import editor.gui.display.CardTable;
import editor.util.MouseListenerFactory;
import edu.emory.mathcs.backport.java.util.Collections;
import scala.jdk.javaapi.CollectionConverters;

/**
 * This class represents an editor for a table cell containing a set of {@link Category}s for a
 * card. It can be edited by double-clicking on it, which brings up a dialog showing the
 * available categories.
 *
 * @author Alec Roelke
 */
public class InclusionCellEditor extends AbstractCellEditor implements TableCellEditor
{
    /**
     * Panel to show while editing is occurring.
     */
    private JPanel editor;
    /**
     * {@link EditorFrame} containing the deck whose card's categories are being edited.
     */
    private EditorFrame frame;
    /**
     * Panel showing the list of categories to allow editing.
     */
    private IncludeExcludePanel iePanel;
    /**
     * List of categories the card belongs to.
     */
    private List<Category> included;

    /**
     * Create a new InclusionCellEditor from the given frame.
     *
     * @param f frame containing the table the new InclusionCellEditor goes in
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
                    int x = i * (s + 1) + 1;
                    int y = 1;
                    g.setColor(included.get(i).getColor());
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

    @Override
    public Object getCellEditorValue()
    {
        return iePanel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        if (table instanceof CardTable cTable)
        {
            iePanel = new IncludeExcludePanel(
                CollectionConverters.asScala(frame.getCategories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList())).toSeq(),
                CollectionConverters.asScala(Collections.singletonList(frame.getCardAt(cTable, row))).toSeq()
            );
            included = ((Collection<?>)value).stream().filter((o) -> o instanceof Category).map((o) -> (Category)o).collect(Collectors.toList());
            if (!table.isRowSelected(row))
                editor.setBackground(cTable.rowColor(row));
            else
            {
                editor.setBackground(table.getSelectionBackground());
                if (table.hasFocus())
                    editor.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            }
        }
        else
            iePanel = new IncludeExcludePanel(
                CollectionConverters.asScala(frame.getCategories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList())).toSeq(),
                frame.getSelectedCards()
            );
        return editor;
    }

    /**
     * {@inheritDoc}
     * Double-clicking activates editing.
     */
    @Override
    public boolean isCellEditable(EventObject eo)
    {
        return eo instanceof MouseEvent m && m.getClickCount() > 1;
    }
}
