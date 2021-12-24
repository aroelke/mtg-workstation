package editor.gui.filter.editor

import editor.filter.leaf.FilterLeaf
import java.awt.FlowLayout
import editor.collection.deck.Category
import editor.gui.settings.SettingsDialog
import scala.collection.immutable.ListMap
import editor.gui.generic.ComboBoxPanel

class DefaultsFilterPanel extends FilterEditorPanel[FilterLeaf[?]] {
  setLayout(FlowLayout(FlowLayout.LEFT, 0, 0))

  private val categories = ListMap.from(SettingsDialog.settings.editor.categories.presets.map((p) => p.getName -> p))
  private val defaults = ComboBoxPanel(categories.keys.toArray)
  add(defaults)

  override def filter = Category(categories(defaults.getSelectedItem)).getFilter
  override def setContents(filter: FilterLeaf[?]) = {}
}