package editor.gui.filter.leaf

import editor.gui.filter.FilterSelectorPanel
import editor.filter.leaf.BooleanFilter
import editor.filter.Filter
import editor.database.attributes.CardAttribute
import java.awt.BorderLayout
import javax.swing.JCheckBox

object BooleanFilterPanel {
  /** @return a new, empty [[BooleanFilterPanel]] */
  def apply(attribute: CardAttribute[?, BooleanFilter], selector: FilterSelectorPanel) = new BooleanFilterPanel(attribute, selector, false)

  /**
   * Create a new [[BooleanFilterPanel]] and pre-populate it with the contents of a [[BooleanFilter]].
   * 
   * @param filter filter to use to populate the new panel
   * @return a new boolean filter panel with contents taken from the filter
   */
  def apply(filter: BooleanFilter, selector: FilterSelectorPanel) = {
    val panel = new BooleanFilterPanel(filter.attribute, selector, filter.yes)
    panel.setContents(filter)
    panel
  }
}

/**
 * A panel for customizing a filter that filters by a boolean attribute.
 * @param yes
 */
class BooleanFilterPanel(protected override val attribute: CardAttribute[?, BooleanFilter], selector: FilterSelectorPanel, yes: Boolean) extends FilterEditorPanel[BooleanFilter] {
  setLayout(BorderLayout())
  private val box = JCheckBox("", yes)
  add(box, BorderLayout.WEST)

  override def setFields(filter: BooleanFilter) = box.setSelected(filter.yes)
  override def filter: Filter = attribute.filter.copy(yes = box.isSelected(), faces = selector.faces)
}