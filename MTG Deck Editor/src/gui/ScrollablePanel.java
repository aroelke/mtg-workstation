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
	 * Track neither width nor height of the viewport.
	 */
	public static final byte TRACK_NEITHER = 0x00;
	/**
	 * Track width of the viewport.
	 */
	public static final byte TRACK_WIDTH = 0x01;
	/**
	 * Track height of the viewport.
	 */
	public static final byte TRACK_HEIGHT = 0x02;
	/**
	 * Track both width and height of the viewport (equivalent to TRACK_WIDTH | TRACK_HEIGHT).
	 */
	public static final byte TRACK_BOTH = 0x03;
	
	/**
	 * Dimension of this ScrollablePanel's viewport to track.
	 */
	private byte tracking;
	
	/**
	 * Create a new ScrollablePanel with the default layout for a JPanel that
	 * tracks neither the width nor height of its viewport.
	 */
	public ScrollablePanel()
	{
		this(TRACK_NEITHER);
	}
	
	/**
	 * Create a new ScrollablePanel with the default layout for a JPanel
	 * that tracks the specified dimension of its viewport.
	 * 
	 * @param t
	 */
	public ScrollablePanel(byte t)
	{
		super();
		tracking = t;
	}
	
	/**
	 * Create a new ScrollablePanel with the specified layout that tracks
	 * neither the width nor height of its viewport.
	 * 
	 * @param layout Layout of the new panel
	 */
	public ScrollablePanel(LayoutManager layout)
	{
		this(layout, TRACK_NEITHER);
	}
	
	/**
	 * Create a new ScrollablePanel with the specified layout that tracks
	 * the specified dimension of its viewport.
	 * 
	 * @param layout
	 * @param t
	 */
	public ScrollablePanel(LayoutManager layout, byte t)
	{
		super(layout);
		tracking = t;
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
		return (tracking&TRACK_HEIGHT) != 0x0;
	}

	/**
	 * @return <code>false</code> since the size of this ScrollablePanel should not track the
	 * viewport width.
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return (tracking&TRACK_WIDTH) != 0x0;
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
