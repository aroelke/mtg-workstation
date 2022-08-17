package editor.gui.generic

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * [[DocumentListener]] that does the same thing for any change to the document.
 * @author Alec Roelke
 */
trait DocumentChangeListener extends DocumentListener {
  /**
   * Action to perform when the document changes.
   * @param e event describing the document change
   */
  def update(e: DocumentEvent): Unit

  override def changedUpdate(e: DocumentEvent) = update(e)
  override def insertUpdate(e: DocumentEvent) = update(e)
  override def removeUpdate(e: DocumentEvent) = update(e)
}