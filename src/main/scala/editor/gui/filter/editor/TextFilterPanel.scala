package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.TextFilter
import editor.gui.generic.ComboBoxPanel
import editor.util.Containment

import java.awt.Color
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

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
  text.getDocument.addDocumentListener(new DocumentListener {
    def update(e: DocumentEvent) = {
      text.setBackground(Color.WHITE)
      if (regex.isSelected) {
        try {
          Pattern.compile(text.getText, Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
        } catch case _: PatternSyntaxException => text.setBackground(Color.PINK)
      }
    }

    override def changedUpdate(e: DocumentEvent) = update(e)
    override def insertUpdate(e: DocumentEvent) = update(e)
    override def removeUpdate(e: DocumentEvent) = update(e)
  })
  add(text)

  regex.addActionListener(_ => contain.setVisible(!regex.isSelected))
  add(regex)

  private[editor] override var attribute = CardAttribute.NAME

  override def filter = CardAttribute.createFilter(attribute) match {
    case tf: TextFilter =>
      tf.contain = contain.getSelectedItem
      tf.text = text.getText
      tf.regex = regex.isSelected
      tf
  }

  override def setFields(filter: TextFilter) = {
    attribute = filter.`type`
    contain.setSelectedItem(filter.contain)
    text.setText(filter.text)
    regex.setSelected(filter.regex)
    contain.setVisible(!filter.regex)
  }
}