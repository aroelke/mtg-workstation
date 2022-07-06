package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaCost
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.ManaCostFilter
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ComboBoxPanel
import editor.gui.generic.DocumentChangeListener
import editor.util.Containment

import java.awt.Color
import javax.swing.BoxLayout
import javax.swing.JTextField
import javax.swing.event.DocumentEvent

/**
 * Convenience constructors for [[ManaCostFilterPanel]].
 * @author Alec Roelke
 */
object ManaCostFilterPanel {
  /** @return a new, empty [[ManaCostFilterPanel]] */
  def apply(selector: FilterSelectorPanel) = new ManaCostFilterPanel(selector)

  /**
   * Create a new [[ManaCostFilterPanel]] pre-populated with the contents of a filter.
   * 
   * @param filter filter to use to populate the new panel
   * @return a new mana cost filter panel containing the string version of the filter's cost
   */
  def apply(filter: ManaCostFilter, selector: FilterSelectorPanel) = {
    val panel = new ManaCostFilterPanel(selector)
    panel.setContents(filter)
    panel
  }
}

/**
 * A panel for customizing a [[ManaCostFilter]] by setting the symbols in it and how it should contain them.
 * Symbols are set using text, with each of their strings surrounded by braces ({}).
 * 
 * @author Alec Roelke
 */
class ManaCostFilterPanel(selector: FilterSelectorPanel) extends FilterEditorPanel[ManaCostFilter] {
  private val Valid = Color.WHITE
  private val Invalid = Color.PINK

  setLayout(BoxLayout(this, BoxLayout.X_AXIS))
  
  private val contain = ComboBoxPanel(Containment.values)
  add(contain)
  private val cost = JTextField()
  cost.getDocument.addDocumentListener(new DocumentChangeListener {
    override def update(e: DocumentEvent) = cost.setBackground(ManaCost.parse(cost.getText).map(_ => Valid).getOrElse(Invalid))
  })
  add(cost)

  protected override val attribute = CardAttribute.ManaCost

  override def filter = {
    val filter = ManaCostFilter()
    filter.faces = selector.faces
    filter.contain = contain.getSelectedItem
    filter.cost = ManaCost.parse(cost.getText).getOrElse(ManaCost())
    filter
  }

  override def setFields(filter: ManaCostFilter) = {
    contain.setSelectedItem(filter.contain)
    cost.setText(filter.cost.toString)
  }
}
