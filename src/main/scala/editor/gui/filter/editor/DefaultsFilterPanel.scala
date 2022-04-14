package editor.gui.filter.editor

import editor.collection.deck.Category
import editor.database.attributes.CardAttribute
import editor.filter.FilterGroup
import editor.filter.leaf.FilterLeaf
import editor.gui.generic.ComboBoxPanel
import editor.gui.settings.SettingsDialog

import java.awt.FlowLayout
import scala.collection.immutable.ListMap

/**
 * Panel that contains shortcuts for adding filters based on the filters of the preset categories. This panel
 * doesn't actually correspond to a filter; internally, it is replaced by the contents of the filter of the
 * selected preset category.  If the filter is viewed again, this panel will be replaced by a group containing
 * the selected preset category's filter.
 *
 * @author Alec Roelke
 */
class DefaultsFilterPanel extends FilterEditorPanel[FilterLeaf] {
  setLayout(FlowLayout(FlowLayout.LEFT, 0, 0))

  private val categories = ListMap.from(SettingsDialog.settings.editor.categories.presets.map((p) => p.getName -> p))
  private val defaults = ComboBoxPanel(categories.keys.toArray)
  add(defaults)

  override val attribute = CardAttribute.DEFAULTS
  override def filter = FilterGroup(Seq(Category(categories(defaults.getSelectedItem)).getFilter))
  override def setFields(filter: FilterLeaf) = throw UnsupportedOperationException("defaults filter panel should be replaced by contents")
}