package editor.gui.generic

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Convenience object for allowing the specification of a [[DocumentChangeListener]] using functional
 * notation.
 * @author Alec Roelke
 */
object DocumentChangeListener {
  /**
   * Convert a function (DocumentEvent) => Unit into a [[DocumentChangeListener]] to allow the use of
   * that notation to specify one.
   * 
   * @param f function to convert
   * @return a [[DocumentChangeListener]] that calls that function on update
   */
  implicit def function2listener(f: (DocumentEvent) => Unit): DocumentChangeListener = new DocumentChangeListener { def update(e: DocumentEvent) = f(e) }
}

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