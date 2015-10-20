package editor.gui;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * This class represents a listener for mouse events on a table and opens up
 * a pop-up menu when the appropriate button is clicked.
 * 
 * TODO: Try to make right clicking have all the features of left clicking.
 * Currently you can select single rows (or add the single row to the selection by
 * holding ctrl) with the right button, but you can't extend a selection (with shift)
 * or drag it using the right mouse button
 * TODO: This does weird things with DnD
 * 
 * @author Alec Roelke
 */
public class TableMouseAdapter extends MouseAdapter
{
	/**
	 * The JTable this TableMouseAdapter operates on.
	 */
	private JTable table;
	/**
	 * The menu that will open on right-click.
	 */
	private JPopupMenu menu;
	
	/**
	 * Create a new TableMouseAdapter.
	 * 
	 * @param t Table to operate on
	 * @param m Menu to open
	 */
	public TableMouseAdapter(JTable t, JPopupMenu m)
	{
		super();
		
		table = t;
		menu = m;
	}
	
	/**
	 * If the event that generated the click was a pop-up event (usually
	 * right-click), open the menu.  If the mouse is over an empty row,
	 * then select that row first.  If the ctrl key is not held down,
	 * deselect all other rows.
	 * 
	 * @param e MouseEvent corresponding to the click
	 */
	public void popupClick(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			int r = table.rowAtPoint(e.getPoint());
			if (r >= 0 && !table.isRowSelected(r))
			{
				if ((e.getModifiers()&InputEvent.CTRL_MASK) == 0)
					table.setRowSelectionInterval(r, r);
				else
					table.addRowSelectionInterval(r, r);
			}
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	/**
	 * Event for pressing the mouse button.
	 * 
	 * @param e MouseEvent corresponding to the click
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		popupClick(e);
	}
	
	/**
	 * Event for releasing the mouse button.  In addition to the pop-up
	 * menu, clear the table if the mouse button is released off of it.
	 * 
	 * @param e MouseEvent corresponding to the click
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		popupClick(e);
		if (table.rowAtPoint(e.getPoint()) < 0)
			table.clearSelection();
	}
}
