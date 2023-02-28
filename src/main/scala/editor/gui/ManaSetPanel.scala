package editor.gui

import editor.database.attributes.ManaType

import java.awt.FlowLayout
import java.awt.event.ActionListener
import javax.swing.JPanel

import collection.immutable.ListSet

/**
 * Panel containing a set of mana symbol [[SymbolButton]]s for filling out a set of mana types.
 *
 * @constructor create a new mana set panel
 * @param types initial set of types
 * @param available set of available types, in order of appearance
 * 
 * @author Alec Roelke
 */
class ManaSetPanel(types: Set[ManaType] = Set.empty, available: ListSet[ManaType] = ListSet(ManaType.colors:_*)) extends JPanel(FlowLayout(FlowLayout.LEADING, 0, 0)) {
  private val buttons = available.map((m) => {
    val button = SymbolButton(m, types.contains(m))
    add(button)
    m -> button
  }).toMap

  /** @return the set of selected mana types */
  def selected = buttons.collect{ case (m, b) if b.isSelected => m }.toSet

  /**
   * Set the selected set of mana types
   * @param types new selected set
   */
  def selected_=(types: Set[ManaType]) = buttons.foreach{ case (m, b) => b.setSelected(types.contains(m)) }

  /**
   * Add an action listener that listens for actions on specific buttons.
   * 
   * @param listener listener to add
   * @param types types to listen for the action
   */
  def addActionListener(listener: ActionListener, types: Set[ManaType] = available) = types.foreach(buttons(_).addActionListener(listener))

  /**
   * Remove an action listener from a specific set of types, if they have it.
   * 
   * @param listener listener to remove
   * @param types types corresponding to buttons to remove the listener from
   */
  def removeActionListener(listener: ActionListener, types: Set[ManaType] = available) = types.foreach(buttons(_).removeActionListener(listener))
}