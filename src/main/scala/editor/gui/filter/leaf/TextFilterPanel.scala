package editor.gui.filter.leaf

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.TextFilter
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ComboBoxPanel
import editor.util.Containment

import java.awt.Color
import java.util.regex.PatternSyntaxException
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document
import scala.util.Try
import scala.util.matching._

/**
 * Convenience constructors for [[TextFilterPanel]].
 * @author Alec Roelke
 */
object TextFilterPanel {
  /** @return a new, empty [[TextFilterPanel]] for card names */
  def apply(selector: FilterSelectorPanel) = new TextFilterPanel(selector)

  /**
   * Create a new [[TextFilterPanel]] pre-populated with the contents of a filter.
   * 
   * @param filter filter to use to populate the panel
   * @return a [[TextFilterPanel]] with the values and attribute from the filter
   */
  def apply(filter: TextFilter, selector: FilterSelectorPanel) = {
    val panel = new TextFilterPanel(selector)
    panel.setContents(filter)
    panel
  }
}

/**
 * A filter editor panel for customizing the values of filters for text attributes. Contains a combo box
 * indicating how the text of the filter should be matched by the card attribute, and a check box indicating
 * if the text box is a regular expression instead.
 * 
 * @author Alec Roelke
 */
class TextFilterPanel(selector: FilterSelectorPanel) extends FilterEditorPanel[TextFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  private val contain = ComboBoxPanel(Containment.values)
  add(contain)

  private val text = JTextField()
  private val regex = JCheckBox("regex")

  private def prefilter = attribute.filter.copy(faces = selector.faces, contain = contain.getSelectedItem)
  private def tryfilter = prefilter.copy(text = text.getText, regex = regex.isSelected)

  private def showError() = text.setBackground(if (Try{ tryfilter.matches("") }.isFailure) Color.PINK else Color.WHITE)
  text.getDocument.addDocumentListener(new DocumentListener {
    override def changedUpdate(e: DocumentEvent) = showError()
    override def insertUpdate(e: DocumentEvent) = showError()
    override def removeUpdate(e: DocumentEvent) = showError()
  })
  add(text)

  regex.addActionListener(_ => { contain.setVisible(!regex.isSelected); showError() })
  add(regex)

  protected override var attribute = CardAttribute.Name

  override def filter = try {
    val f = tryfilter
    f.matches("")
    f
  } catch {
    case _ => prefilter.copy(text = "", regex = regex.isSelected)
  }
  override def setFields(filter: TextFilter) = {
    attribute = filter.attribute
    contain.setSelectedItem(filter.contain)
    text.setText(filter.text)
    regex.setSelected(filter.regex)
    contain.setVisible(!filter.regex)
  }
}