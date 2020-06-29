package editor.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import editor.collection.CardList;
import editor.gui.display.CardTable;

/**
 * This class is a listener for changes to the selection in a {@link CardTable},
 * whether that selection be by mouse click or programmatically (e.g. due to changes
 * in the backing list). Only one table in the program can have a selection at a time,
 * so when one of them is clicked, all the rest have to be cleared.
 * 
 * @author Alec Roelke
 */
public class TableSelectionListener implements MouseListener, ListSelectionListener
{
    /** Main frame controlling table selection. */
    private final MainFrame frame;
    /** Table to watch. */
    private final CardTable table;
    /** Card list backing the table. */
    private final CardList list;

    /**
     * Create a new listener that listens to the given table and updates its backing
     * list.
     * 
     * @param f main frame controlling table selections
     * @param t table this listener should watch
     * @param l list backing the table
     */
    public TableSelectionListener(MainFrame f, CardTable t, CardList l)
    {
        frame = f;
        table = t;
        list = l;
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (table.rowAtPoint(e.getPoint()) < 0)
            frame.clearSelectedList();
        else if (frame.getSelectedList().filter((l) -> l == list).isEmpty())
            frame.setSelectedComponents(table, list);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (!e.getValueIsAdjusting())
        {
            if (table.getSelectedRow() >= 0)
                frame.setDisplayedCard(list.get(table.convertRowIndexToModel(table.getSelectedRow())));
            else if (frame.getSelectedTable().filter((t) -> t == table).isPresent())
                frame.clearSelectedCard();
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}