package editor.gui.generic

import editor.util.given

import java.awt.Color
import java.awt.Graphics
import javax.swing.JButton
import scala.util.Random

/**
 * Button showing a color instead of text or an icon.
 * 
 * @constructor create a new color button
 * @param col color of the button; defaults to random if not specified
 * @param border distance between the edge of the button and the region of color to draw
 * 
 * @author Alec Roelke
 */
class ColorButton(private var col: Color = Random.nextColor, border: Int = 5) extends JButton(" ") {
  /** @return the current color of the button */
  def color = col

  /**
   * Change the color of the button and update the UI to reflect the change.
   * @param c the new color of the button
   */
  def color_=(c: Color) = {
    col = c
    repaint()
  }

  override def paintComponent(g: Graphics) = {
    super.paintComponent(g)
    g.setColor(color)
    g.fillRect(border, border, getWidth - 2*border, getHeight - 2*border)
  }
}