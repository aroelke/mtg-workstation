package editor.gui.filter

import editor.filter.Filter

import javax.swing.JPanel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * A panel for customizing a filter for use with filtering the inventory or creating a category.
 * @tparam F type of filter being customized
 * 
 * @author Alec Roelke
 */
trait FilterPanel[F <: Filter] extends JPanel {
  private[filter] var group: Option[FilterGroupPanel] = None

  /** Listeners for changes to the filter, such as if a group has children added or removed. */
  val listeners = collection.mutable.Set[ChangeListener]()

  /**
   * Alert all listeners to this filter that its contents have changed, then forward the event to this
   * filter's parents.
   */
  def firePanelsChanged(): Unit = {
    group.foreach(_.firePanelsChanged())
    listeners.foreach(_.stateChanged(ChangeEvent(this)))
  }

  /** @return a filter with fields set to the values contained by this panel's components. */
  def filter: Filter
  
  /**
   * Populate the UI fields of this panel based on the fields of a filter.
   * @param filter filter to use for populating the fields
   */
  def setContents(filter: F): Unit
}