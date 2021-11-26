package editor.gui.generic

import javax.swing.JButton
import scala.util.Random
import java.awt.Color

import RandomWithColors._
import java.awt.Graphics

class ColorButton(private var col: Color = Random.nextColor, border: Int = 5) extends JButton(" ") {
  def color = col
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

object RandomWithColors {
  implicit def randomToColors(r: Random): RandomWithColors = new RandomWithColors(r)
}

class RandomWithColors(r: Random) {
  def nextColor = Color(r.nextFloat, r.nextFloat, math.sqrt(r.nextFloat).toFloat)
}