package gui;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * This class is a JPanel except it is scroll-savvy.
 * 
 * @author Alec Roelke
 * @see javax.swing.JPanel
 * @see javax.swing.JScrollPane
 * @see javax.swing.Scrollable
 */
@SuppressWarnings("serial")
public class ScrollablePanel extends JPanel implements Scrollable
{
	/**
	 * Create a new ScrollablePanel with the default layout for a JPanel.
	 */
	public ScrollablePanel()
	{
		super();
	}
	
	/**
	 * Create a new ScrollablePanel with the specified layout.
	 * 
	 * @param layout Layout of the new panel
	 */
	public ScrollablePanel(LayoutManager layout)
	{
		super(layout);
	}
	
	/**
	 * @return The preferred viewport size of this ScrollablePanel, which is the same
	 * as its preferred size.
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	/**
	 * @see Scrollable#getScrollableBlockIncrement(Rectangle, int, int)
	 */
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 20;
	}

	/**
	 * @return <code>false</code> since the size of this ScrollablePanel should not track the
	 * viewport height.
	 */
	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	/**
	 * @return <code>false</code> since the size of this ScrollablePanel should not track the
	 * viewport width.
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	/**
	 * @see Scrollable#getScrollableUnitIncrement(Rectangle, int, int)
	 */
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 20;
	}
}
