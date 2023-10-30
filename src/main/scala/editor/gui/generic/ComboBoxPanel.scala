package editor.gui.generic

import editor.util.extensions._

import java.awt.event.ItemListener
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.ListCellRenderer

/**
 * Panel containing a combo box that is sized to fit its contents and vertically
 * centered in its parent.
 * 
 * @constructor create a new centered combo box with a specific set of items
 * @tparam E type of the combo box's contents
 * @param items items to put into the combo box
 * 
 * @author Alec Roelke
 */
class ComboBoxPanel[E <: AnyRef](items: Array[E]) extends JPanel {
  setLayout(BoxLayout(this, BoxLayout.Y_AXIS))

  add(Box.createVerticalGlue)
  private val options = JComboBox(items)
  options.setMaximumSize(options.getPreferredSize)
  add(options)
  add(Box.createVerticalGlue)

  /** @param l item listener to add to the combo box */
  def addItemListener(l: ItemListener) = options.addItemListener(l)

  /** @return the currently-selected item in the combo box */
  def getSelectedItem = options.getCurrentItem

  /** @param item new selected value of the combo box */
  def setSelectedItem(item: E) = options.setSelectedItem(item)

  def getRenderer = options.getRenderer

  def setRenderer(renderer: ListCellRenderer[? >: E]) = options.setRenderer(renderer)

  override def setToolTipText(text: String) = {
    super.setToolTipText(text)
    options.setToolTipText(text)
  }
}