package editor.gui.generic

import javax.swing.event.DocumentListener
import javax.swing.event.DocumentEvent

trait DocumentChangeListener extends DocumentListener {
  def update(e: DocumentEvent): Unit

  override def changedUpdate(e: DocumentEvent) = update(e)
  override def insertUpdate(e: DocumentEvent) = update(e)
  override def removeUpdate(e: DocumentEvent) = update(e)
}