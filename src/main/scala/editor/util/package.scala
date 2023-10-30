package editor

import org.jfree.data.category.CategoryDataset

import java.awt.Color
import javax.swing.JComboBox
import scala.util.Random

import database.attributes.CardAttribute

/**
 * Miscellaneous utilities used throughout the editor.
 * @author Alec Roelke
 */
package object util {
  /** Extension methods for existing classes to improve code and/or add functionality. */
  object extensions {
    extension[E] (box: JComboBox[E]) {
      /** @return The currently-selected item of the combo box. */
      def getCurrentItem = box.getItemAt(box.getSelectedIndex)
    }

    extension (r: Random) {
      /** @return a random color. */
      def nextColor = Color(r.nextFloat, r.nextFloat, r.nextFloat)
    }

    extension (dataset: CategoryDataset) {
      /** @return the row key, column key, and value associated with the row and column index of the data set */
      def getTriple(row: Int, column: Int) = (dataset.getRowKey(row), dataset.getColumnKey(column), dataset.getValue(row, column))
    }
  }
}