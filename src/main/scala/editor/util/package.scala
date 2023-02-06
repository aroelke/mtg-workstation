package editor

import database.attributes.CardAttribute

import javax.swing.JComboBox
import scala.util.Random

/**
 * Miscellaneous utilities used throughout the editor.
 * @author Alec Roelke
 */
package object util {
  given Conversion[Random, RandomColors] = RandomColors(_)

  /** Extension methods for existing classes to improve code and/or add functionality. */
  object extensions {
    extension[E] (box: JComboBox[E]) {
      /** @return The currently-selected item of the combo box. */
      def getCurrentItem = box.getItemAt(box.getSelectedIndex)
    }
  }
}