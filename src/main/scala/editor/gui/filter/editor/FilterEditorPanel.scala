package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.gui.filter.FilterPanel

import scala.reflect.ClassTag

/**
 * A panel for editing a specific type of filter. It can populate its contents based on user input
 * or from an existing filter.
 * 
 * @tparam F type of [[FilterLeaf]] the panel edits.
 * 
 * @author Alec Roelke
 */
trait FilterEditorPanel[L <: FilterLeaf[?] : ClassTag] extends FilterPanel[FilterLeaf[?]] {
  private[editor] def attribute: CardAttribute

  private[editor] def attribute_=(a: CardAttribute): Unit = throw UnsupportedOperationException()

  def setFields(filter: L): Unit

  /**
   * Set the fields of the panel based on the contents of a filter.
   * @param filter filter to use to populate the panel
   */
  override def setContents(filter: FilterLeaf[?]) = filter match {
    case leaf: L => setFields(leaf)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a/n ${attribute} filter")
  }
}