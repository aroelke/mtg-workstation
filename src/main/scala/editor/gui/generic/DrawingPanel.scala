package editor.gui.generic

import java.awt.Graphics
import javax.swing.JPanel

/**
 * A panel class used for simplifying code that instantiates a new panel just to draw something custom in it.
 * 
 * @constructor create a new panel for drawing
 * @param paintFunction function defining what to draw using a [[Graphics]] object and this panel for size information
 * @param convert how to convert from [[Graphics]] to G
 * @tparam G the type of [[Graphics]] to use if more advanced drawing functions are needed
 * 
 * @author Alec Roelke
 */
class DrawingPanel[G <: Graphics](paintFunction: (G, DrawingPanel[G]) => Unit, convert: (Graphics) => G = _.asInstanceOf[G]) extends JPanel {
  override def paintComponent(g: Graphics) = {
    super.paintComponent(g)
    paintFunction(convert(g), this)
  }
}