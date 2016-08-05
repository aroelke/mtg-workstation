package editor.util;

import java.util.function.Consumer;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This interface is a factory for creating popup menu listeners that don't listen for
 * all three of cancellation, invisible, and visible events.
 * 
 * @author Alec Roelke
 */
public interface PopupMenuListenerFactory
{
	/**
	 * Create a new PopupMenuListener that only listens for cancellation.
	 * 
	 * @param popupMenuCanceled Action to perform when the menu is canceled
	 * @return A PopupMenuListener that performs the given event when the menu is canceled.
	 */
	public static PopupMenuListener createCancellationListener(Consumer<PopupMenuEvent> popupMenuCanceled)
	{
		return new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{
				popupMenuCanceled.accept(e);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{}
		};
	}
	
	/**
	 * Create a PopupMenuListener that only listens for when a popup menu is about to become
	 * invisible.
	 * 
	 * @param popupMenuWillBecomeInvisible Action to perform before the menu becomes invisible
	 * @return A PopupMenuListener that performs the given action before a menu becomes invisible
	 */
	public static PopupMenuListener createInvisibleListener(Consumer<PopupMenuEvent> popupMenuWillBecomeInvisible)
	{
		return new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
				popupMenuWillBecomeInvisible.accept(e);
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{}
		};
	}
	
	/**
	 * Create a PopupMenuListener that only listens to when a popup menu is about to become
	 * visible.
	 * 
	 * @param popupMenuWillBecomeVisible Action to perform before the menu becomes visible
	 * @return A PopupMenuListener that performs the given action just before a popup menu
	 * becomes visible.
	 */
	public static PopupMenuListener createVisibleListener(Consumer<PopupMenuEvent> popupMenuWillBecomeVisible)
	{
		return new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				popupMenuWillBecomeVisible.accept(e);
			}
		};
	}
}
