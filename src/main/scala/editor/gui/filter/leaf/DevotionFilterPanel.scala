package editor.gui.filter.leaf

import editor.database.attributes.CardAttribute
import editor.filter.leaf.DevotionFilter
import editor.gui.ManaSetPanel
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ComboBoxPanel
import editor.util.Comparison

import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * Convenience constructors for [[DevotionFilterPanel]].
 * @author Alec Roelke
 */
object DevotionFilterPanel {
  /** @return a new, empty [[DevotionFilterPanel]]. */
  def apply(selector: FilterSelectorPanel) = new DevotionFilterPanel(selector)

  /**
   * Create a new [[DevotionFilterPanel]] pre-populated with the contents of a filter.
   * 
   * @param filter filter to use for populating the new panel
   * @return a devotion filter panel with contents taken from the filter
   */
  def apply(filter: DevotionFilter, selector: FilterSelectorPanel) = {
    val panel = new DevotionFilterPanel(selector)
    panel.setFields(filter)
    panel
  }
}

/**
 * A panel for customizing [[DevotionFilter]]s.  The colors to calculate devotion for are selected using a
 * [[ManaSetPanel]], and the comparison type and value to compare with are chosen using a combo box and
 * spinner, respectively.
 * 
 * @author Alec Roelke
 */
class DevotionFilterPanel(selector: FilterSelectorPanel) extends FilterEditorPanel[DevotionFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  // Panel for choosing colors to be devoted to
  private val colors = ManaSetPanel()
  add(colors)

  // Combo box for choosing the type of comparison to make
  private val comparison = ComboBoxPanel(Comparison.values)
  add(comparison)

  // Value to compare the card number against
  private val spinner = JSpinner()
  spinner.setModel(SpinnerNumberModel(0, 0, null, 1))
  spinner.setMaximumSize(Dimension(100, Int.MaxValue))
  add(spinner)

  protected override val attribute = CardAttribute.Devotion

  override def filter = spinner.getValue match {
    case v: Int => DevotionFilter(types = colors.selected, operation = comparison.getSelectedItem, operand = v)
  }

  override def setFields(filter: DevotionFilter) = {
    colors.selected = filter.types
    comparison.setSelectedItem(filter.operation)
    spinner.setValue(filter.operand)
  }
}
