package editor.gui.filter.editor

import editor.filter.leaf.TextFilter
import javax.swing.BoxLayout
import editor.gui.generic.ComboBoxPanel
import editor.util.Containment
import javax.swing.JTextField
import javax.swing.event.DocumentListener
import javax.swing.event.DocumentEvent
import java.awt.Color
import javax.swing.JCheckBox
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import editor.database.attributes.CardAttribute
import javax.swing.text.Document
import editor.filter.leaf.FilterLeaf

object TextFilterPanel {
  def apply() = new TextFilterPanel

  def apply(filter: TextFilter) = {
    val panel = new TextFilterPanel
    panel.setContents(filter)
    panel
  }
}

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

  private var attribute = CardAttribute.NAME

  override def filter = CardAttribute.createFilter(attribute) match {
    case tf: TextFilter =>
      tf.contain = contain.getSelectedItem
      tf.text = text.getText
      tf.regex = regex.isSelected
      tf
  }

  override def setContents(filter: TextFilter) = {
    attribute = filter.`type`
    contain.setSelectedItem(filter.contain)
    text.setText(filter.text)
    regex.setSelected(filter.regex)
    contain.setVisible(!filter.regex)
  }

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case text: TextFilter => setContents(text)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a text filter")
  }
}