package editor.gui.filter.editor

import editor.filter.leaf.FilterLeaf
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import editor.database.attributes.CardAttribute

class BinaryFilterPanel(allow: Boolean) extends FilterEditorPanel[FilterLeaf[?]] {
  val All = "This clause will match every card."
  val None = "This clause will not match any card."

  setLayout(GridLayout(1, 1))
  setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0))
  add(JLabel(if (allow) All else None))

  override lazy val filter = CardAttribute.createFilter(if (allow) CardAttribute.ANY else CardAttribute.NONE)
  override def setContents(filter: FilterLeaf[?]) = {}
}
