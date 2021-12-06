package editor.gui.generic

import javax.swing.UIManager
import java.awt.Component
import java.awt.Font
import java.awt.Container
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JSeparator
import javax.swing.SwingConstants
import java.awt.Dimension

object ComponentUtils {
  val TextSize = UIManager.getFont("Label.font").getSize

  def changeFontRecursive(component: Component, font: Font): Unit = {
    component.setFont(font)
    component match {
      case container: Container => container.getComponents.foreach(changeFontRecursive(_, font))
      case _ =>
    }
  }

  def createHorizontalSeparator(width: Int, height: Int = 0) = {
    val panel = Box(BoxLayout.X_AXIS)
    panel.setAlignmentY(Component.CENTER_ALIGNMENT)
    val separator = JSeparator(SwingConstants.VERTICAL)
    panel.add(Box.createHorizontalStrut((width - separator.getPreferredSize.width)/2))
    panel.add(separator)
    panel.add(Box.createHorizontalStrut((width - separator.getPreferredSize.width)/2))
    panel.setPreferredSize(Dimension(panel.getPreferredSize.width, height))
    panel.setMaximumSize(Dimension(panel.getPreferredSize.width, if (height > 0) height else panel.getMaximumSize.height))
    panel
  }
}
