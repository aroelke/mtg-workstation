package editor.gui.filter.editor

import editor.filter.leaf.FilterLeaf
import editor.gui.filter.FilterPanel

/**
 * A panel for editing a specific type of filter. It can populate its contents based on user input
 * or from an existing filter.
 * 
 * @tparam F type of [[FilterLeaf]] the panel edits.
 * 
 * @author Alec Roelke
 */
trait FilterEditorPanel[F <: FilterLeaf[?]] extends FilterPanel[F] {
  /**
   * Set the fields of the panel based on the contents of a filter.
   * @param filter filter to use to populate the panel
   */
  def setContents(filter: FilterLeaf[?]): Unit
}