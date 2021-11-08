package editor.gui.generic

import javax.swing.JMenuItem
import editor.gui.editor.EditorFrame
import editor.database.card.Card
import editor.gui.settings.SettingsDialog
import javax.swing.JPanel
import java.awt.BorderLayout
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.JOptionPane
import javax.swing.JLabel
import java.awt.Container

class CardMenuItems(monitor: => Option[EditorFrame], cards: => Iterable[? <: Card], main: Boolean) extends Iterable[JMenuItem] {
  private val add = (n: Int) => monitor.foreach(f => (if (main) f.deck else f.sideboard) ++= (cards -> n))
  private val fill = () => monitor.foreach(f => {
    val l = if (main) f.deck else f.sideboard
    l %%= cards.map((c) => c -> (if (l.contains(c)) Math.max(0, SettingsDialog.PlaysetSize - l.getEntry(c).count) else SettingsDialog.PlaysetSize)).toMap
  })
  private val remove = (n: Int) => monitor.foreach(f => (if (main) f.deck else f.sideboard) --= (cards -> n))

  val items = Seq(
    JMenuItem("Add Single Copy"), JMenuItem("Fill Playset"), JMenuItem("Add Copies..."),
    JMenuItem("Remove Single Copy"), JMenuItem("Remove All Copies"), JMenuItem("Remove Copies...")
  )

  addOne.addActionListener(_ => add(1))
  fillPlayset.addActionListener(_ => fill())
  addN.addActionListener(_ => {
    val contentPanel = JPanel(BorderLayout());
    contentPanel.add(JLabel("Copies to add:"), BorderLayout.WEST);
    val spinner = JSpinner(SpinnerNumberModel(1, 0, Int.MaxValue, 1));
    contentPanel.add(spinner, BorderLayout.SOUTH);
    val container = if (monitor.isDefined) monitor.get else null
    if (JOptionPane.showConfirmDialog(container, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
      add(spinner.getValue.asInstanceOf[Int]);
  })

  removeOne.addActionListener(_ => remove(1))
  removeAll.addActionListener(_ => remove(Int.MaxValue))
  removeN.addActionListener(_ => {
    val contentPanel = JPanel(BorderLayout());
    contentPanel.add(JLabel("Copies to remove:"), BorderLayout.WEST);
    val spinner = JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
    contentPanel.add(spinner, BorderLayout.SOUTH);
    val container = if (monitor.isDefined) monitor.get else null
    if (JOptionPane.showConfirmDialog(container, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
        remove(spinner.getValue.asInstanceOf[Int]);
  })

  def addOne = items(0)
  def fillPlayset = items(1)
  def addN = items(2)
  def removeOne = items(3)
  def removeAll = items(4)
  def removeN = items(5)

  def addAddItems(menu: Container) = {
    menu.add(addOne)
    menu.add(fillPlayset)
    menu.add(addN)
  }

  def addRemoveItems(menu: Container) = {
    menu.add(removeOne)
    menu.add(removeAll)
    menu.add(removeN)
  }

  def setEnabled(enable: Boolean) = items.foreach(_.setEnabled(enable))

  def setVisible(visible: Boolean) = items.foreach(_.setVisible(visible))

  override def iterator = items.iterator
}
