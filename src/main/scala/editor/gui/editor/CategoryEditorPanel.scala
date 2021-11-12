package editor.gui.editor

import editor.collection.deck.Category
import editor.gui.display.CardJList
import editor.gui.filter.FilterGroupPanel
import editor.gui.generic.ColorButton
import editor.gui.generic.ScrollablePanel

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import java.awt.GridLayout
import java.util.stream.Collectors
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JColorChooser
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import scala.jdk.CollectionConverters._

/**
 * Companion object to [[CategoryEditorPanel]] containing global information about it and a convenience function
 * for creating a dialog with one.
 * 
 * @author Alec Roelke
 */
object CategoryEditorPanel {
  /** Maximum height the panel is allowed to have before a scroll bar appears. */
  val MaxHeight = 500

  /**
   * Show a dialog containing a [[CategoryEditorPanel]], optionally pre-populated with a categorization. If the user doesn't enter
   * a name, the dialog will reappear until a name is entered or the operation is canceled.
   * 
   * @param parent container instantiating the dialog, used for positioning
   * @param specification if defined, pre-populate the editor panel
   * @return the new specification created in the editor dialog, or None if the process was canceled
   */
  def showCategoryEditor(parent: Container, specification: Option[Category] = None) = {
    val editor = CategoryEditorPanel(specification)
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

/**
 * A panel for editing deck categories.  Allows the user to set the name and color of a category as well as customize its
 * automatic filter using a [[FilterGroupPanel]].  It also displays the blacklist and whitelist of a category, although
 * those lists cannot be changed from here.
 * 
 * @constructor create a new category editing panel and optionally populate it
 * @param specification if defined, pre-populate the contents of the category editor
 * 
 * @author Alec Roelke
 */
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

  /** @return the categorization as currently defined by the GUI elements in the panel. */
  def spec = Category(nameField.getText, whitelist.getCards, blacklist.getCards, colorButton.color, filter.filter)

  /**
   * Set the contents of the panel based on a given categorization.
   * @param s categorization to use to populate the panel
   */
  def spec_=(s: Category) = {
    nameField.setText(s.getName)
    colorButton.setColor(s.getColor)
    filter.setContents(s.getFilter)
    whitelist.setCards(s.getWhitelist.asScala.toSeq.sortBy(_.unifiedName).asJava)
    blacklist.setCards(s.getBlacklist.asScala.toSeq.sortBy(_.unifiedName).asJava)
  }
}