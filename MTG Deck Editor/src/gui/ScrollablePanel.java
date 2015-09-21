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
	 * Whether or not to track the width of the viewport.
	 */
	private boolean trackWidth;
	/**
	 * Whether or not to track the height of the viewport.
	 */
	private boolean trackHeight;
	/**
	 * Preferred viewport dimensions.
	 */
	private Dimension preferredScrollableViewportSize;
	
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
	 * @param tracking
	 */
	public ScrollablePanel(byte tracking)
	{
		super();
		trackWidth = (tracking&TRACK_WIDTH) != 0x0;
		trackHeight = (tracking&TRACK_HEIGHT) != 0x0;
		preferredScrollableViewportSize = new Dimension(0, 0);
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
	 * @param tracking
	 */
	public ScrollablePanel(LayoutManager layout, byte tracking)
	{
		super(layout);
		trackWidth = (tracking&TRACK_WIDTH) != 0x0;
		trackHeight = (tracking&TRACK_HEIGHT) != 0x0;
		preferredScrollableViewportSize = new Dimension(0, 0);
	}
	
	/**
	 * @return The preferred viewport size of this ScrollablePanel, which is 0 by default
	 * so as to not cause the scroll pane to resize according to this ScrollablePanel's size. 
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return preferredScrollableViewportSize;
	}

	/**
	 * Set the preferred size of this ScrollablePanel's viewport.  This will not actually
	 * resize the panel unless it is set to track the width or height of its viewport.
	 * 
	 * @param size New preferred size for the viewport
	 */
	public void setPreferredScrollableViewportSize(Dimension size)
	{
		preferredScrollableViewportSize = size;
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
		return trackHeight;
	}

	/**
	 * @return <code>false</code> since the size of this ScrollablePanel should not track the
	 * viewport width.
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return trackWidth;
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
