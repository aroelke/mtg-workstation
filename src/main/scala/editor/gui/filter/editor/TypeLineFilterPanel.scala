package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.TypeLineFilter
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ComboBoxPanel
import editor.util.Containment

import javax.swing.BoxLayout
import javax.swing.JTextField

/**
 * Convenience constructors for [[TypeLineFilterPanel]].
 * @author Alec Roelke
 */
object TypeLineFilterPanel {
  /** @return a new [[TypeLineFilterPanel]] with no text */
  def apply(selector: FilterSelectorPanel) = new TypeLineFilterPanel(selector)

  /**
   * Create a new [[TypeLineFilterPanel]] with pre-populated text.
   * 
   * @param filter filter to get the text and containment from
   * @return a new [[TypeLineFilterPanel]] with the given filter's text and containment mode
   */
  def apply(filter: TypeLineFilter, selector: FilterSelectorPanel) = {
    val panel = new TypeLineFilterPanel(selector)
    panel.setContents(filter)
    panel
  }
}

/**
 * A filter editor panel for creating filters for all values that could appear on a card's type line.
 * @author Alec Roelke
 */
class TypeLineFilterPanel(selector: FilterSelectorPanel) extends FilterEditorPanel[TypeLineFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  private val contain = ComboBoxPanel(Containment.values)
  add(contain)

  private val line = JTextField()
  add(line)

  protected override val attribute = CardAttribute.TypeLine

  override def filter = TypeLineFilter(faces = selector.faces, contain = contain.getSelectedItem, line = line.getText)

  override def setFields(filter: TypeLineFilter) = {
    contain.setSelectedItem(filter.contain)
    line.setText(filter.line)
  }
}