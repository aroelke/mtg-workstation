package editor.gui.generic

import javax.swing.JPanel
import javax.swing.BoxLayout
import javax.swing.Box
import javax.swing.JComboBox
import java.awt.event.ItemListener

class ComboBoxPanel[E <: AnyRef](items: Array[E]) extends JPanel {
  setLayout(BoxLayout(this, BoxLayout.Y_AXIS))

  add(Box.createVerticalGlue)
  private val options = JComboBox(items)
  options.setMaximumSize(options.getPreferredSize)
  add(options)
  add(Box.createVerticalGlue)

  def addItemListener(l: ItemListener) = options.addItemListener(l)

  def getSelectedItem = options.getItemAt(options.getSelectedIndex)

  def setSelectedItem(item: E) = options.setSelectedItem(item)
}