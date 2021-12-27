package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.gui.filter.FilterPanel

import scala.reflect.ClassTag

/**
 * A panel for editing a specific type of filter. It can populate its contents based on user input
 * or from an existing filter.
 * 
 * @tparam L type of [[FilterLeaf]] the panel edits
 * 
 * @author Alec Roelke
 */
trait FilterEditorPanel[L <: FilterLeaf[?] : ClassTag] extends FilterPanel[FilterLeaf[?]] {
  /** @return the card attribute filtered by the generated filter */
  private[editor] def attribute: CardAttribute

  /**
   * Set the card attribute filtered by the generated filter, if it can be changed.
   * @param a new attribute to filter
   */
  @throws[UnsupportedOperationException]("if the attribute cannot be changed")
  private[editor] def attribute_=(a: CardAttribute): Unit = throw UnsupportedOperationException()

  /**
   * Set the fields of the panel based on the contents of a filter.
   * @param filter filter to use to populate the panel
   */
  def setFields(filter: L): Unit

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case leaf: L => setFields(leaf)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a/n ${attribute} filter")
  }
}