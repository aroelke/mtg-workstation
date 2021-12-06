package editor.gui.generic

import javax.swing.Box
import javax.swing.JButton
import javax.swing.BoxLayout
import java.awt.Dimension
import scala.collection.IterableOnceOps

class VerticalButtonList(texts: Iterable[String]) extends Box(BoxLayout.Y_AXIS) with IterableOnce[JButton] {
  private val buttonList = texts.map(JButton(_)).toSeq
  private val buttonMap = buttonList.map((b) => b.getText -> b).toMap

  add(Box.createVerticalGlue)
  buttonList.foreach{ b =>
    add(b)
    b.setMaximumSize(Dimension(Int.MaxValue, b.getMaximumSize.height))
  }
  add(Box.createVerticalGlue)

  def apply(index: Int) = buttonList(index)

  def apply(text: String) = buttonMap(text)

  override def iterator = buttonList.iterator
}
