package editor.gui.filter.editor

import editor.filter.leaf.ManaCostFilter
import javax.swing.BoxLayout
import java.awt.Color
import editor.gui.generic.ComboBoxPanel
import editor.util.Containment
import javax.swing.JTextField
import editor.gui.generic.DocumentChangeListener
import editor.database.attributes.ManaCost
import scala.jdk.OptionConverters._
import editor.filter.leaf.FilterLeaf
import javax.swing.event.DocumentEvent

object ManaCostFilterPanel {
  def apply() = new ManaCostFilterPanel

  def apply(filter: ManaCostFilter) = {
    val panel = new ManaCostFilterPanel
    panel.setContents(filter)
    panel
  }
}

class ManaCostFilterPanel extends FilterEditorPanel[ManaCostFilter] {
  private val Valid = Color.WHITE
  private val Invalid = Color.PINK

  setLayout(BoxLayout(this, BoxLayout.X_AXIS))
  
  private val contain = ComboBoxPanel(Containment.values)
  add(contain)
  private val cost = JTextField()
  cost.getDocument.addDocumentListener(new DocumentChangeListener {
    override def update(e: DocumentEvent) = cost.setBackground(ManaCost.tryParseManaCost(cost.getText).toScala.map(_ => Valid).getOrElse(Invalid))
  })
  add(cost)

  override def filter = {
    val filter = ManaCostFilter()
    filter.contain = contain.getSelectedItem
    filter.cost = ManaCost.tryParseManaCost(cost.getText).toScala.getOrElse(ManaCost())
    filter
  }

  override def setContents(filter: ManaCostFilter) = {
    contain.setSelectedItem(filter.contain)
    cost.setText(filter.cost.toString)
  }

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case cost: ManaCostFilter => setContents(cost)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a mana cost filter")
  }
}
