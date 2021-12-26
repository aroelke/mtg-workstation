package editor.gui.filter

import _root_.editor.filter.Filter
import javax.swing.JPanel
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent

trait FilterPanel[F <: Filter] extends JPanel {
  private[filter] var group: FilterGroupPanel = null
  
  val listeners = collection.mutable.Set[ChangeListener]()
  @deprecated def addChangeListener(listener: ChangeListener) = listeners += listener
  @deprecated def removeChangeListener(listener: ChangeListener) = listeners -= listener

  def firePanelsChanged(): Unit = {
    Option(group).foreach(_.firePanelsChanged())
    listeners.foreach(_.stateChanged(ChangeEvent(this)))
  }

  def filter: Filter
  
  def setContents(filter: F): Unit
}