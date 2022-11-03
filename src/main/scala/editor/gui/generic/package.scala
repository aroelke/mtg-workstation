package editor.gui

import javax.swing.event.DocumentEvent

/**
 * Generic GUI components and utilities that aren't specifically meant for any particular part of the application.
 * @author Alec Roelke
 */
package object generic {
  private def function2listener(f: (DocumentEvent) => Unit) = new DocumentChangeListener { override def update(e: DocumentEvent) = f(e) }

  given Conversion[(DocumentEvent) => Unit, DocumentChangeListener] = function2listener(_)
}