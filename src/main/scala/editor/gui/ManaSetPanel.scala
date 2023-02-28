package editor.gui

import javax.swing.JPanel
import editor.database.attributes.ManaType
import collection.immutable.ListSet
import java.awt.FlowLayout
import java.awt.event.ActionListener

class ManaSetPanel(types: Set[ManaType] = Set.empty, available: ListSet[ManaType] = ListSet(ManaType.colors:_*)) extends JPanel(FlowLayout(FlowLayout.LEADING, 0, 0)) {
  private val buttons = available.map((m) => {
    val button = SymbolButton(m, types.contains(m))
    add(button)
    m -> button
  }).toMap

  def selected = buttons.collect{ case (m, b) if b.isSelected => m }.toSet
  def selected_=(types: Set[ManaType]) = buttons.foreach{ case (m, b) => b.setSelected(types.contains(m)) }

  def addActionListener(listener: ActionListener, types: Set[ManaType] = available) = types.foreach(buttons(_).addActionListener(listener))
  def removeActionListener(listener: ActionListener, types: Set[ManaType] = available) = types.foreach(buttons(_).removeActionListener(listener))
}