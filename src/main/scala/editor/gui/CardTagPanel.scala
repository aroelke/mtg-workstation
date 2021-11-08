package editor.gui

import _root_.editor.gui.generic.ScrollablePanel
import _root_.editor.database.card.Card
import javax.swing.BoxLayout
import java.awt.Color
import _root_.editor.gui.generic.TristateCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.UIManager
import java.awt.BorderLayout
import _root_.editor.util.UnicodeSymbols
import _root_.editor.util.MouseListenerFactory
import scala.jdk.CollectionConverters._
import javax.swing.SwingUtilities
import java.awt.Component
import javax.swing.JScrollPane
import javax.swing.Box
import javax.swing.JTextField
import javax.swing.JButton
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JOptionPane

object CardTagPanel {
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

class CardTagPanel(cards: Iterable[Card]) extends ScrollablePanel(ScrollablePanel.TRACK_WIDTH) {
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

  def addTag(tag: String, selected: Boolean) = if (tagBoxes.exists(_.getText == tag)) false else {
    setTags((tagBoxes.map(_.getText) :+ tag).toSeq.sorted)
    if (selected)
      tagBoxes.filter(_.getText == tag).foreach(_.setSelected(true))
    true
  }

  def removeTag(tag: String) = if (!tagBoxes.exists(_.getText == tag)) false else {
    _removed += tag
    setTags((tagBoxes.collect{ case box if box.getText != tag => box.getText }).toSeq.sorted)
    Option(getParent).foreach((p) => { p.validate(); p.repaint() })
    Option(SwingUtilities.getWindowAncestor(this)).foreach(_.pack())
    true
  }

  def tagged = tagBoxes.collect{ case box if box.getState == TristateCheckBox.State.SELECTED => box.getText }.toSet

  def untagged = tagBoxes.collect{ case box if box.getState == TristateCheckBox.State.UNSELECTED => box.getText }.toSeq ++ _removed.toSet

  def removed = _removed.toSet

  override def getPreferredScrollableViewportSize = if (tagBoxes.isEmpty) getPreferredSize else {
    val size = getPreferredSize
    size.height = preferredViewportHeight
    size
  }
}