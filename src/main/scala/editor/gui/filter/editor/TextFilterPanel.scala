package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.TextFilter
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
  def apply() = new TextFilterPanel

  /**
   * Create a new [[TextFilterPanel]] pre-populated with the contents of a filter.
   * 
   * @param filter filter to use to populate the panel
   * @return a [[TextFilterPanel]] with the values and attribute from the filter
   */
  def apply(filter: TextFilter) = {
    val panel = new TextFilterPanel
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
class TextFilterPanel extends FilterEditorPanel[TextFilter] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  private val contain = ComboBoxPanel(Containment.values)
  add(contain)

  private val text = JTextField()
  private val regex = JCheckBox("regex")
  private def isError = regex.isSelected && Try(text.getText.r).isFailure
  private def showError() = text.setBackground(if (isError) Color.PINK else Color.WHITE)
  text.getDocument.addDocumentListener(new DocumentListener {
    override def changedUpdate(e: DocumentEvent) = showError()
    override def insertUpdate(e: DocumentEvent) = showError()
    override def removeUpdate(e: DocumentEvent) = showError()
  })
  add(text)

  regex.addActionListener(_ => { contain.setVisible(!regex.isSelected); showError() })
  add(regex)

  protected override var attribute = CardAttribute.NAME

  override def filter = CardAttribute.createFilter(attribute) match {
    case tf: TextFilter =>
      tf.contain = contain.getSelectedItem
      tf.text = if (isError) "" else text.getText
      tf.regex = regex.isSelected
      tf
  }

  override def setFields(filter: TextFilter) = {
    attribute = filter.attribute
    contain.setSelectedItem(filter.contain)
    text.setText(filter.text)
    regex.setSelected(filter.regex)
    contain.setVisible(!filter.regex)
  }
}