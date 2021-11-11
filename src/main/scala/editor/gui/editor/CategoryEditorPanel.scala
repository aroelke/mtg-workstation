package editor.gui.editor

import javax.swing.JPanel
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JTextField
import editor.gui.generic.ColorButton;
import editor.gui.filter.FilterGroupPanel
import java.awt.GridLayout
import javax.swing.JScrollPane
import editor.gui.display.CardJList;
import editor.collection.deck.Category;
import javax.swing.JColorChooser
import java.util.stream.Collectors
import scala.jdk.CollectionConverters._
import java.awt.Container
import javax.swing.SwingUtilities
import java.awt.Component
import editor.gui.generic.ScrollablePanel;
import javax.swing.JOptionPane

object CategoryEditorPanel {
  val MaxHeight = 500

  def showCategoryEditor(parent: Container, s: Option[Category] = None) = {
    val editor = CategoryEditorPanel(s)
    editor.filter.addChangeListener(_.getSource match {
      case c: Component => SwingUtilities.getWindowAncestor(c).pack()
      case _ =>
    })
    val editorPanel = new ScrollablePanel(BorderLayout(), ScrollablePanel.TRACK_WIDTH) {
      override def getPreferredScrollableViewportSize = {
        val size = editor.getPreferredSize
        size.height = Math.min(MaxHeight, size.height);
        size;
      }
    };
    editorPanel.add(editor, BorderLayout.CENTER);

    var spec: Option[Category] = None
    var done = false
    while (!done) {
      val editorPane = JScrollPane(editorPanel);
      editorPane.setBorder(BorderFactory.createEmptyBorder);
      if (JOptionPane.showConfirmDialog(parent, editorPane, "Category Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
        if (editor.nameField.getText.isEmpty) {
          JOptionPane.showMessageDialog(editor, "Category must have a name.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
          spec = Some(editor.spec)
          done = true
        }
      } else {
        done = true
      }
    }
    spec
  }
}

class CategoryEditorPanel(specification: Option[Category] = None) extends JPanel(BorderLayout()) {
  private val namePanel = Box(BoxLayout.X_AXIS);
  namePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  namePanel.add(JLabel("Category Name: "));
  private val nameField = JTextField()
  namePanel.add(nameField);
  namePanel.add(Box.createHorizontalStrut(5));
  private val colorButton = ColorButton()
  namePanel.add(colorButton);
  colorButton.addActionListener(_ =>  Option(JColorChooser.showDialog(null, "Choose a Color", colorButton.color)).foreach((c) => {
    colorButton.setColor(c);
    colorButton.repaint();
  }));
  add(namePanel, BorderLayout.NORTH);

  private val filter = FilterGroupPanel()
  add(filter, BorderLayout.CENTER)

  private val listPanel = JPanel(GridLayout(0, 2));
  private val whitelistPanel = JPanel(BorderLayout());
  whitelistPanel.setBorder(BorderFactory.createTitledBorder("Whitelist"));
  private val whitelist = CardJList()
  whitelistPanel.add(JScrollPane(whitelist), BorderLayout.CENTER);
  listPanel.add(whitelistPanel);
  private val blacklistPanel = JPanel(BorderLayout());
  blacklistPanel.setBorder(BorderFactory.createTitledBorder("Blacklist"));
  private val blacklist = CardJList()
  blacklistPanel.add(JScrollPane(blacklist), BorderLayout.CENTER);
  listPanel.add(blacklistPanel);
  add(listPanel, BorderLayout.SOUTH);

  specification.foreach(spec = _)

  def spec = Category(nameField.getText, whitelist.getCards, blacklist.getCards, colorButton.color, filter.filter)

  def spec_=(s: Category) = {
    nameField.setText(s.getName)
    colorButton.setColor(s.getColor)
    filter.setContents(s.getFilter)
    whitelist.setCards(s.getWhitelist.asScala.toSeq.sortBy(_.unifiedName).asJava)
    blacklist.setCards(s.getBlacklist.asScala.toSeq.sortBy(_.unifiedName).asJava)
  }
}