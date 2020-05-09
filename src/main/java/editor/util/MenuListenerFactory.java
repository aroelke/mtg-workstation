package editor.util;

import java.util.function.Consumer;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * This interface is a factory for creating menu listeners that don't listen for all
 * three of a menu becoming selected, deselected, or canceled.
 * 
 * @author Alec Roelke
 */
public interface MenuListenerFactory
{
    /**
     * Create a new MenuListener that only listens for cancellation.
     * 
     * @param canceled action to perform when a menu is canceled
     * @return a MenuListener that performs the action when a menu is canceled.
     */
    public static MenuListener createCanceledListener(Consumer<MenuEvent> canceled)
    {
        return new MenuListener()
        {
            @Override
            public void menuCanceled(MenuEvent e)
            {
                canceled.accept(e);
            }

            @Override
            public void menuDeselected(MenuEvent e) {}

            @Override
            public void menuSelected(MenuEvent e) {}
        };
    }

    /**
     * Create a new MenuListener that listens for a menu to be deselected.
     * 
     * @param deselected action to perform when a menu is deselected
     * @return a MenuListener that performs the action when a menu is deselected.
     */
    public static MenuListener createDeselectedListener(Consumer<MenuEvent> deselected)
    {
        return new MenuListener()
        {
            @Override
            public void menuCanceled(MenuEvent e) {}

            @Override
            public void menuDeselected(MenuEvent e)
            {
                deselected.accept(e);
            }

            @Override
            public void menuSelected(MenuEvent e) {}
        };
    }

    /**
     * Create a new MenuListener that listens for a menu to be selected.
     * 
     * @param selected action to perform when a menu is selected
     * @return a MenuListener that performs the action when a menu is selected.
     */
    public static MenuListener createSelectedListener(Consumer<MenuEvent> selected)
    {
        return new MenuListener()
        {
            @Override
            public void menuCanceled(MenuEvent e) {}

            @Override
            public void menuDeselected(MenuEvent e) {}

            @Override
            public void menuSelected(MenuEvent e)
            {
                selected.accept(e);
            }
        };
    }
}