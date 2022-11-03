package editor.gui.generic

import editor.collection.CardListEntry
import editor.database.card.Card
import editor.gui.deck.EditorFrame
import editor.gui.settings.SettingsDialog

import java.awt.BorderLayout
import java.awt.Container
import java.time.LocalDate
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import scala.collection.immutable.ListMap

/**
 * Convenience class that initializes a set of menu items that control adding cards to and removing them from a deck.
 * There are six menu items:
 * 1. Add a single copy of a card
 * 2. Fill out a playset of cards
 * 3. Add a number of copies of a card specified by a spinner
 * 4. Remove a single copy of a card
 * 5. Remove all copies of a card
 * 6. Remove a number of copies of a card specified by a spinner
 * 
 * @constructor create a new set of menu items
 * @param monitor [[EditorFrame]] containing the deck whose cards should be modified
 * @param cards list of cards to modify
 * @param main whether or not the main deck or a side list should be modified
 * 
 * @author Alec Roelke
 */
class CardMenuItems(monitor: => Option[EditorFrame], cards: => Iterable[? <: Card], main: Boolean) extends Iterable[JMenuItem] {
  private val add = (n: Int) => monitor.foreach(f => (if (main) f.deck else f.sideboard) ++= cards.map(CardListEntry(_, n)))
  private val remove = (n: Int) => monitor.foreach(f => (if (main) f.deck else f.sideboard) --= cards.map(CardListEntry(_, n)))

  private val items = Seq(
    JMenuItem("Add Single Copy"), JMenuItem("Fill Playset"), JMenuItem("Add Copies..."),
    JMenuItem("Remove Single Copy"), JMenuItem("Remove All Copies"), JMenuItem("Remove Copies...")
  )

  addOne.addActionListener(_ => add(1))
  fillPlayset.addActionListener(_ => monitor.foreach(f => {
    val l = if (main) f.deck else f.sideboard
    l ++= cards.map((c) => CardListEntry(
      c,
      math.max(SettingsDialog.PlaysetSize - l.find(_.card == c).map(_.count).getOrElse(0), SettingsDialog.PlaysetSize)
    ))
  }))
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

  /** @return the menu item that adds a single copy of the selected card to the deck. */
  def addOne = items(0)

  /** @return the menu item that fills out a playset of the selected card in the deck. */
  def fillPlayset = items(1)

  /** @return the menu item that adds a user-specified number of copies of the selected card to the deck. */
  def addN = items(2)

  /** @return the menu item that removes a single copy of the selected card to the deck. */
  def removeOne = items(3)

  /** @return the menu item that removes all copies of the selected card from the deck. */
  def removeAll = items(4)

  /** @return the menu item that removes a user-specified number of copies of the selected card from the deck. */
  def removeN = items(5)

  /**
   * Convenience method to add the menu items that add cards to the deck to a menu.
   * @param menu menu to add the items to
   */
  def addAddItems(menu: Container) = {
    menu.add(addOne)
    menu.add(fillPlayset)
    menu.add(addN)
  }

  /**
   * Convenience method to add the menu items that remove cards from the deck to a menu.
   * @param menu menu to add the items to
   */
  def addRemoveItems(menu: Container) = {
    menu.add(removeOne)
    menu.add(removeAll)
    menu.add(removeN)
  }

  /**
   * Enable or disable all of the menu items.
   * @param enable whether the items should be enabled or not
   */
  def setEnabled(enable: Boolean) = items.foreach(_.setEnabled(enable))

  /**
   * Control the visibility of all of the menu items.
   * @param visible whether or not the items should be visible
   */
  def setVisible(visible: Boolean) = items.foreach(_.setVisible(visible))

  override def iterator = items.iterator
}
