package editor.gui.generic

import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import scala.collection.IterableOnceOps

/**
 * Panel containing a series of buttons arranged in vertical order, vertically centered. The buttons will horizontally
 * stretch to fit the width of the panel. Individual buttons can be accessed by index vertically from the top of the
 * panel or by text. Buttons cannot be added or removed from the list.
 * 
 * @constructor create a new vertical button list with the given set of texts
 * @param texts strings to display in the buttons; if an ordered collection is used, that order will be preserved
 * 
 * @author Alec Roelke
 */
class VerticalButtonList(texts: Iterable[String]) extends Box(BoxLayout.Y_AXIS) with IterableOnce[JButton] {
  private val buttonList = texts.map(JButton(_)).toSeq
  private val buttonMap = buttonList.map((b) => b.getText -> b).toMap

  add(Box.createVerticalGlue)
  buttonList.foreach{ b =>
    add(b)
    b.setMaximumSize(Dimension(Int.MaxValue, b.getMaximumSize.height))
  }
  add(Box.createVerticalGlue)

  /**
   * Get a button by index from the top of the list.
   *
   * @param index index into the list
   * @return the button at the given index
   */
  def apply(index: Int) = buttonList(index)

  /**
   * Get a button by text.
   *
   * @param text text of the button
   * @return the button with the given text
   */
  def apply(text: String) = buttonMap(text)

  override def iterator = buttonList.iterator
}
