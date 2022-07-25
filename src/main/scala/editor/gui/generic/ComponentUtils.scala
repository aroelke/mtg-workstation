package editor.gui.generic

import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Font
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JSeparator
import javax.swing.SwingConstants
import javax.swing.UIManager

/**
 * Constants and methods for creating and arranging components in the UI.
 * @author Alec Roelke
 */
object ComponentUtils {
  /** Default font size for text. */
  val TextSize = UIManager.getFont("Label.font").getSize

  /**
   * Change the font of a component and all of its child components, and so on.
   * 
   * @param component component to change
   * @param font font to change to
   */
  def propagateFont(component: Component, font: Font): Unit = {
    component.setFont(font)
    component match {
      case container: Container => container.getComponents.foreach(propagateFont(_, font))
      case _ =>
    }
  }

  def propagateColors(component: Component, fg: Color, bg: Color): Unit = {
    component.setForeground(fg)
    component.setBackground(bg)
    component match {
      case container: Container => container.getComponents.foreach(propagateColors(_, fg, bg))
      case _ =>
    }
  }

  /**
   * Create a fixed-size component with a vertical bar in the center, useful for visually dividing things.
   * 
   * @param width width of the component
   * @param height height of the component
   * @return a new component that is empty except for a vertical separator in the middle
   */
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
