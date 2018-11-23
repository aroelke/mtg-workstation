package editor.util;

import java.util.function.Consumer;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * TODO
 */
public interface MenuListenerFactory
{
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