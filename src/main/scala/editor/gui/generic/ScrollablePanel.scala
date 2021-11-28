package editor.gui.generic

import javax.swing.JPanel
import javax.swing.Scrollable
import java.awt.LayoutManager
import java.awt.Dimension
import java.awt.Rectangle

/**
 * Collection of constants used for configuring a [[ScrollablePanel]].
 * @author Alec Roelke
 */
object ScrollablePanel {
  /** Configure a [[ScrollablePanel]] to force its view to track viewport width. */
  val TrackWidth: Byte = 0x01
  /** Configure a [[ScrollablePanel]] to force its view to track viewport height. */
  val TrackHeight: Byte = 0x02
  /** Configure a [[ScrollablePanel]] to force its view to track both viewport width and height. */
  val TrackBoth: Byte = (TrackWidth | TrackHeight).toByte
  /** Configure a [[ScrollablePanel]] to force its view to track neither viewport width nor height. */
  val TrackNeither: Byte = 0x00
}

/**
 * A panel that works inside a [[JScrollPane]] or similar component, allowing its contents to stretch larger
 * than the viewport size with some means of scrolling them (like a scroll bar) to see them all.
 * 
 * @constructor create a new scrollable panel that scrolls vertically and/or horizontally and with
 * a given layout
 * @param tracking which dimension(s) of the viewport should be tracked
 * @param layout initial layout of the viewport
 * 
 * @author Alec Roelke
 */
class ScrollablePanel(tracking: Byte = ScrollablePanel.TrackNeither, layout: LayoutManager = JPanel().getLayout) extends JPanel(layout) with Scrollable {
  @deprecated def this(tracking: Byte) = this(tracking, JPanel().getLayout)

  private var preferredScrollableViewportSize = Dimension(0, 0)

  /** @param size new preferred size of the viewport (contents can be different depending on tracking) */
  def setPreferredScrollableViewportSize(size: Dimension) = preferredScrollableViewportSize = size

  override def getPreferredScrollableViewportSize = preferredScrollableViewportSize
  override def getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 20
  override def getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 20
  override def getScrollableTracksViewportWidth = (tracking & ScrollablePanel.TrackWidth) != 0
  override def getScrollableTracksViewportHeight = (tracking & ScrollablePanel.TrackHeight) != 0
}