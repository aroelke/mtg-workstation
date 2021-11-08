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
      val tagged = cardTagPanel.getTagged
      val untagged = cardTagPanel.getUntagged
      cards.foreach((c) => {
        tagged.foreach((tag) => Card.tags.compute(c, (k, v) => {
          val set = Option(v).getOrElse(java.util.HashSet())
          set.add(tag)
          set
        }))
        untagged.foreach((tag) => Card.tags.compute(c, (k, v) => {
          var set = v
          if (set != null) {
            set.remove(tag)
            if (set.isEmpty)
              set = null
          }
          set
        }))
      })
      val removed = cardTagPanel.getRemoved
      Card.tags.keySet.asScala.foreach((c) => Card.tags.compute(c, (k, v) => {
        v.removeAll(removed.asJava)
        if (v.isEmpty) null else v
      }))
    }
  }
}

class CardTagPanel(cards: Iterable[Card]) extends ScrollablePanel(ScrollablePanel.TRACK_WIDTH) {
  private val MaxPreferredRows = 10

  setLayout(BoxLayout(this, BoxLayout.Y_AXIS))
  setBackground(Color.WHITE)

  private val tagBoxes = collection.mutable.ArrayBuffer[TristateCheckBox]()
  private val removed = collection.mutable.ArrayBuffer[String]()
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

  def addTag(tag: String, selected: Boolean) = {
    val tags = collection.mutable.Set(tagBoxes.map(_.getText).toSeq:_*)
    if (tags.add(tag)) {
      setTags(tags.toSeq.sorted)
      if (selected)
        tagBoxes.filter(_.getText == tag).foreach(_.setSelected(true))
      true
    } else false
  }

  def removeTag(tag: String) = {
    val tags = collection.mutable.Set(tagBoxes.map(_.getText).toSeq:_*)
    if (tags.remove(tag)) {
      removed += tag
      setTags(tags.toSeq.sorted)
      Option(getParent).foreach((p) => {
        p.validate()
        p.repaint()
      })
      Option(SwingUtilities.getWindowAncestor(this)).foreach(_.pack())
      true
    } else false
  }

  def getTagged = tagBoxes.collect{ case box if box.getState == TristateCheckBox.State.SELECTED => box.getText }.toSet

  def getUntagged = tagBoxes.collect{ case box if box.getState == TristateCheckBox.State.UNSELECTED => box.getText }.toSeq ++ removed.toSet

  def getRemoved = removed.toSet

  override def getPreferredScrollableViewportSize = if (tagBoxes.isEmpty) getPreferredSize else {
    val size = getPreferredSize
    size.height = preferredViewportHeight
    size
  }
}