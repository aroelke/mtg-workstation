package editor.gui

import _root_.editor.database.card.Card
import _root_.editor.gui.generic.ScrollablePanel
import _root_.editor.gui.generic.TristateCheckBox
import _root_.editor.util.MouseListenerFactory
import _root_.editor.util.UnicodeSymbols

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.UIManager
import scala.jdk.CollectionConverters._

/**
 * Companion object including convenience methods for [[CardTagPanel]].
 * @author Alec Roelke
 */
object CardTagPanel {
  /**
   * Show a dialog including a [[CardTagPanel]] to allow the user to edit tags for a selection of cards. If the user confirms
   * their selection, tags are added, removed, and deleted accordingly.
   * 
   * @param cards cards whose tags should be edited
   * @param parent parent component of the dialog, used for positioning
   */
  def editTags(cards: Seq[Card], parent: Component) = {
    val contentPanel = JPanel(new BorderLayout());
    val cardTagPanel = CardTagPanel(cards);
    contentPanel.add(JScrollPane(cardTagPanel), BorderLayout.CENTER);
    val lowerPanel = Box(BoxLayout.X_AXIS);
    val newTagField = JTextField();
    lowerPanel.add(newTagField);
    val newTagButton = JButton("Add");

    val addListener: ActionListener = _ => {
      if (!newTagField.getText.isEmpty && cardTagPanel.addTag(newTagField.getText, true)) {
        newTagField.setText("");
        cardTagPanel.revalidate();
        cardTagPanel.repaint();
        SwingUtilities.getWindowAncestor(cardTagPanel).pack();
      }
    }
    newTagButton.addActionListener(addListener);
    newTagField.addActionListener(addListener);
    lowerPanel.add(newTagButton);
    contentPanel.add(lowerPanel, BorderLayout.SOUTH);
    if (JOptionPane.showConfirmDialog(parent, contentPanel, "Edit Card Tags", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
      val tagged = cardTagPanel.tagged
      val untagged = cardTagPanel.untagged
      cards.foreach((c) => {
        tagged.foreach((tag) => Card.tags.compute(c, (_, v) => (Option(v).getOrElse(java.util.HashSet()).asScala + tag).asJava))
        untagged.foreach((tag) => Card.tags.compute(c, (_, v) => if (v == null || (v.size == 1 && v.contains(tag))) null else (v.asScala - tag).asJava))
      })
      val removed = cardTagPanel.removed
      Card.tags.keySet.asScala.foreach((c) => Card.tags.compute(c, (_, v) => if (v == null || v.asScala == removed) null else (v.asScala -- removed).asJava))
    }
  }
}

/**
 * Panel allowing the user to edit tags applied to a collection of cards, add new tags, and remove existing tags.
 * Tags are shown as check boxes, with those where some cards in the collection have them and some don't initially
 * shown as being in indeterminate state.  Any left in that state will remain unchanged when the tagging is finished.
 * Note that, after tags are applied, any tags that aren't applied to any cards will be entirely deleted.
 * 
 * @constructor create a new panel editing tags of a given set of cards
 * @param cards cards whose tags should be edited
 * 
 * @author Alec Roelke
 */
class CardTagPanel(cards: Iterable[Card]) extends ScrollablePanel(ScrollablePanel.TrackWidth) {
  private val MaxPreferredRows = 10

  setLayout(BoxLayout(this, BoxLayout.Y_AXIS))
  setBackground(Color.WHITE)

  private val tagBoxes = collection.mutable.ArrayBuffer[TristateCheckBox]()
  private val _removed = collection.mutable.HashSet[String]()
  private var preferredViewportHeight = 0

  setTags(Card.tags().asScala.toSeq.sorted)

  tagBoxes.foreach((box) => {
    val matches = cards.count((c) => Option(Card.tags.get(c)).exists(_.contains(box.getText)))
    if (matches == 0)
      box.setState(TristateCheckBox.State.UNSELECTED)
    else if (matches < cards.size)
      box.setState(TristateCheckBox.State.INDETERMINATE)
    else
      box.setState(TristateCheckBox.State.SELECTED)
  })

  private def setTags(tags: Seq[String]): Unit = {
    tagBoxes --= tagBoxes.filter((box) => !tags.contains(box.getText))
    removeAll()
    preferredViewportHeight = 0
    if (tags.isEmpty)
      add(JLabel("<html><i>No tags have been created.</i></html>"))
    else {
      tags.foreach((tag) => {
        val tagPanel = JPanel(BorderLayout())
        tagPanel.setBackground(UIManager.getColor("List.background"))

        val tagBox = tagBoxes.find(_.getText == tag).getOrElse(new TristateCheckBox(tag))
        tagBox.setBackground(tagPanel.getBackground)
        tagPanel.add(tagBox, BorderLayout.WEST)
        tagBoxes += tagBox

        val deleteButton = JLabel(s"${UnicodeSymbols.MINUS} ")
        deleteButton.setForeground(Color.RED)
        deleteButton.addMouseListener(MouseListenerFactory.createPressListener(_ => removeTag(tag)))
        tagPanel.add(deleteButton, BorderLayout.EAST)

        preferredViewportHeight = Math.min(preferredViewportHeight + tagPanel.getPreferredSize.height, tagPanel.getPreferredSize.height*MaxPreferredRows)
        add(tagPanel)
      })
    }
  }

  /**
   * Add a new tag and optionally apply it to all the selected cards.
   * 
   * @param tag tag to add
   * @param selected whether or not it should be applied to all the cards
   * @return true if the tag was added, or false otherwise (because it was already there).
   * @note If the tag is added unselected and then the choice is confirmed, it won't be added to the tag pool.
   */
  def addTag(tag: String, selected: Boolean) = if (tagBoxes.exists(_.getText == tag)) false else {
    setTags((tagBoxes.map(_.getText) :+ tag).toSeq.sorted)
    if (selected)
      tagBoxes.filter(_.getText == tag).foreach(_.setSelected(true))
    true
  }

  /**
   * Remove a tag entirely from the tag pool.  This differs from simply unchecking a tag box in that
   * it removes it from all cards and not just from the selected ones.
   * 
   * @param tag tag to remove
   * @return true if the tag was removed, and false otherwise (because it wasn't there to begin with)
   */
  def removeTag(tag: String) = if (!tagBoxes.exists(_.getText == tag)) false else {
    _removed += tag
    setTags((tagBoxes.collect{ case box if box.getText != tag => box.getText }).toSeq.sorted)
    Option(getParent).foreach((p) => { p.validate(); p.repaint() })
    Option(SwingUtilities.getWindowAncestor(this)).foreach(_.pack())
    true
  }

  /** @return the set of tags that are checked (to be applied to the selected cards) */
  def tagged = tagBoxes.collect{ case box if box.getState == TristateCheckBox.State.SELECTED => box.getText }.toSet

  /** @return the set of tags that are unchecked (to be removed from the selected cards) */
  def untagged = tagBoxes.collect{ case box if box.getState == TristateCheckBox.State.UNSELECTED => box.getText }.toSeq ++ _removed.toSet

  /** @return the set of tags that are to be removed from all cards, and hence deleted */
  def removed = _removed.toSet

  override def getPreferredScrollableViewportSize = if (tagBoxes.isEmpty) getPreferredSize else {
    val size = getPreferredSize
    size.height = preferredViewportHeight
    size
  }
}