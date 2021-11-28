package editor.gui.generic

import javax.swing.JPanel
import javax.swing.Scrollable
import java.awt.LayoutManager
import java.awt.Dimension
import java.awt.Rectangle

object ScrollablePanel {
  val TrackWidth: Byte = 0x01
  val TrackHeight: Byte = 0x02
  val TrackBoth: Byte = (TrackWidth | TrackHeight).toByte
  val TrackNeither: Byte = 0x00
}

class ScrollablePanel(tracking: Byte = ScrollablePanel.TrackNeither, layout: LayoutManager = JPanel().getLayout) extends JPanel(layout) with Scrollable {
  @deprecated def this(tracking: Byte) = this(tracking, JPanel().getLayout)

  private var preferredScrollableViewportSize = Dimension(0, 0)

  def setPreferredScrollableViewportSize(size: Dimension) = preferredScrollableViewportSize = size

  override def getPreferredScrollableViewportSize = preferredScrollableViewportSize
  override def getScrollableUnitIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 20
  override def getScrollableBlockIncrement(visibleRect: Rectangle, orientation: Int, direction: Int) = 20
  override def getScrollableTracksViewportWidth = (tracking & ScrollablePanel.TrackWidth) != 0
  override def getScrollableTracksViewportHeight = (tracking & ScrollablePanel.TrackHeight) != 0
}