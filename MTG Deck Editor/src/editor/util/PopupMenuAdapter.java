package editor.util;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This class represents a listener for a popup menu.  It can do things
 * as the menu is about to become visible, as it is about to disappear,
 * and when it is canceled.  It is a convenience class for popup menu
 * listeners that don't want to implement all of the methods of
 * PopupMenuListener.
 * @author alecr
 *
 */
public abstract class PopupMenuAdapter implements PopupMenuListener
{
	/**
	 * Perform an action when the popup menu is canceled.
	 * 
	 * @param e Event containing information about the cancellation
	 */
	@Override
	public void popupMenuCanceled(PopupMenuEvent e)
	{}

	/**
	 * Perform an action when the popup menu is about to become invisible.
	 * 
	 * @param e Event containing information about the disappearance
	 */
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
	{}

	/**
	 * Performan an action when the popup menu is about to become visible.
	 * 
	 * @param e Event containing information about the appearance
	 */
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e)
	{}
}
