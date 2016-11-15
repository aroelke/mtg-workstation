package editor.gui.generic;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * This class is a JPanel except it is scroll-savvy.
 * 
 * @author Alec Roelke
 * @see JPanel
 * @see Scrollable
 */
@SuppressWarnings("serial")
public class ScrollablePanel extends JPanel implements Scrollable
{
	/**
	 * Track both width and height of the viewport (equivalent to TRACK_WIDTH | TRACK_HEIGHT).
	 */
	public static final byte TRACK_BOTH = 0x03;
	/**
	 * Track height of the viewport.
	 */
	public static final byte TRACK_HEIGHT = 0x02;
	/**
	 * Track neither width nor height of the viewport.
	 */
	public static final byte TRACK_NEITHER = 0x00;
	/**
	 * Track width of the viewport.
	 */
	public static final byte TRACK_WIDTH = 0x01;
	
	/**
	 * Preferred viewport dimensions.
	 */
	private Dimension preferredScrollableViewportSize;
	/**
	 * Whether or not to track the height of the viewport.
	 */
	private boolean trackHeight;
	/**
	 * Whether or not to track the width of the viewport.
	 */
	private boolean trackWidth;
	
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
	 * @param tracking which dimension to track
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
	 * @param layout layout of the new panel
	 */
	public ScrollablePanel(LayoutManager layout)
	{
		this(layout, TRACK_NEITHER);
	}
	
	/**
	 * Create a new ScrollablePanel with the specified layout that tracks
	 * the specified dimension of its viewport.
	 * 
	 * @param layout layout of the new panel
	 * @param tracking which dimension to track
	 */
	public ScrollablePanel(LayoutManager layout, byte tracking)
	{
		super(layout);
		trackWidth = (tracking&TRACK_WIDTH) != 0x0;
		trackHeight = (tracking&TRACK_HEIGHT) != 0x0;
		preferredScrollableViewportSize = new Dimension(0, 0);
	}
	
	/**
	 * {@inheritDoc}
	 * The preferred viewport size is 0 by default so as to not cause the scroll pane to resize
	 * according to this ScrollablePanel's size.
	 * 
	 * @return the preferred viewport size of this ScrollablePanel 
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return preferredScrollableViewportSize;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 20;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return trackHeight;
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return trackWidth;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 20;
	}

	/**
	 * Set the preferred size of this ScrollablePanel's viewport.  This will not actually
	 * resize the panel unless it is set to track the width or height of its viewport.
	 * 
	 * @param size new preferred size for the viewport
	 */
	public void setPreferredScrollableViewportSize(Dimension size)
	{
		preferredScrollableViewportSize = size;
	}
}
