package editor.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

/**
 * This interface represents a factory for creating mouse listeners that don't listen for
 * all three of mouse pressed, released, and clicked events.
 *
 * @author Alec Roelke
 */
public interface MouseListenerFactory
{
    /**
     * TODO
     * @param listeners
     * @return
     */
    static MouseListener composeListeners(MouseListener... listeners)
    {
        return new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                for (MouseListener listener : listeners)
                    listener.mouseClicked(e);
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                for (MouseListener listener : listeners)
                    listener.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                for (MouseListener listener : listeners)
                    listener.mouseExited(e);
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                for (MouseListener listener : listeners)
                    listener.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                for (MouseListener listener : listeners)
                    listener.mouseReleased(e);
            }
        };
    }

    /**
     * Create a MouseListener that only listens for mouse click events (press followed
     * by release).
     *
     * @param mouseClicked action to perform when the mouse is clicked
     * @return a MouseListener that performs the given action when the mouse is clicked.
     */
    static MouseListener createClickListener(Consumer<MouseEvent> mouseClicked)
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                mouseClicked.accept(e);
            }
        };
    }

    /**
     * TODO
     */
    static MouseListener createDoubleClickListener(Consumer<MouseEvent> mouseClicked)
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                    mouseClicked.accept(e);
            }
        };
    }

    /**
     * TODO
     * @param pressed
     * @param released
     * @return
     */
    static MouseListener createHoldListener(Consumer<MouseEvent> pressed, Consumer<MouseEvent> released)
    {
        return new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                pressed.accept(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                released.accept(e);
            }
        };
    }

    /**
     * TODO
     * @param entered
     * @param exited
     * @return
     */
    static MouseListener createMotionListener(Consumer<MouseEvent> entered, Consumer<MouseEvent> exited)
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                entered.accept(e);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                exited.accept(e);
            }
        };
    }

    /**
     * Create a MouseListener that only listens for mouse press events.
     *
     * @param mousePressed action to perform when the mouse is pressed
     * @return a MouseListener that performs the given action when the mouse is pressed.
     */
    static MouseListener createPressListener(Consumer<MouseEvent> mousePressed)
    {
        return new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                mousePressed.accept(e);
            }
        };
    }

    /**
     * Create a MouseListener that only listens for mouse release events.
     *
     * @param mouseReleased action to perform when the mouse is released
     * @return a MouseListener that performs the given action when the mouse is released.
     */
    static MouseListener createReleaseListener(Consumer<MouseEvent> mouseReleased)
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                mouseReleased.accept(e);
            }
        };
    }

    /**
     * TODO
     * @param handler
     * @return
     */
    static MouseListener createUniversalListener(Consumer<MouseEvent> handler)
    {
        return new MouseListener(){
        
            @Override
            public void mouseReleased(MouseEvent e)
            {
                handler.accept(e);
            }
        
            @Override
            public void mousePressed(MouseEvent e)
            {
                handler.accept(e);
            }
        
            @Override
            public void mouseExited(MouseEvent e)
            {
                handler.accept(e);
            }
        
            @Override
            public void mouseEntered(MouseEvent e)
            {
                handler.accept(e);
            }
        
            @Override
            public void mouseClicked(MouseEvent e)
            {
                handler.accept(e);
            }
        };
    }
}
