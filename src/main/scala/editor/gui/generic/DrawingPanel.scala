package editor.gui.generic

import java.awt.Graphics
import javax.swing.JPanel

class DrawingPanel[G <: Graphics](paintFunction: (G, DrawingPanel[G]) => Unit, convert: (Graphics) => G = _.asInstanceOf[G]) extends JPanel {
  override def paintComponent(g: Graphics) = {
    super.paintComponent(g)
    paintFunction(convert(g), this)
  }
}