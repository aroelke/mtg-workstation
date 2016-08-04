package editor.gui.generic;

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
	 * Create a MouseListener that only listens for mouse press events.
	 * 
	 * @param mousePressed Action to perform when the mouse is pressed
	 * @return A MouseListener that performs the given action when the mouse is pressed.
	 */
	public static MouseListener createPressListener(Consumer<MouseEvent> mousePressed)
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
	 * @param mouseReleased Action to perform when the mouse is released
	 * @return A MouseListener that performs the given action when the mouse is released.
	 */
	public static MouseListener createReleaseListener(Consumer<MouseEvent> mouseReleased)
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
	 * Create a MouseListener that only listens for mouse click events (press followed
	 * by release).
	 * 
	 * @param mouseClicked Action to perform when the mouse is clicked
	 * @return A MouseListener that performs the given action when the mouse is clicked.
	 */
	public static MouseListener createClickListener(Consumer<MouseEvent> mouseClicked)
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
}
