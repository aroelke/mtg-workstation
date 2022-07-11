package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.gui.filter.FilterPanel

import scala.reflect.ClassTag

/**
 * A panel for editing a specific type of filter. It can populate its contents based on user input
 * or from an existing filter.
 * 
 * @tparam F type of [[FilterLeaf]] the panel edits
 * 
 * @author Alec Roelke
 */
trait FilterEditorPanel[F <: FilterLeaf : ClassTag] extends FilterPanel[FilterLeaf] {
  /** @return the card attribute filtered by the generated filter */
  protected def attribute: CardAttribute[?, F]

  /**
   * Set the card attribute filtered by the generated filter, if it can be changed.
   * @param a new attribute to filter
   */
  @throws[UnsupportedOperationException]("if the attribute cannot be changed")
  protected def attribute_=(a: CardAttribute[?, F]): Unit = throw UnsupportedOperationException()

  /**
   * Set the fields of the panel based on the contents of a filter.
   * @param filter filter to use to populate the panel
   */
  def setFields(filter: F): Unit

  override def setContents(filter: FilterLeaf) = filter match {
    case leaf: F => setFields(leaf)
    case _ => throw IllegalArgumentException(s"${filter.attribute} is not a/n ${attribute} filter")
  }
}