package editor.gui

import _root_.editor.collection.CardList
import _root_.editor.collection.CardListEntry
import _root_.editor.collection.Categorization
import _root_.editor.collection.`export`.DelimitedCardListFormat
import _root_.editor.collection.`export`.TextCardListFormat
import _root_.editor.collection.immutable.Inventory
import _root_.editor.collection.mutable.Deck
import _root_.editor.database.attributes.CardAttribute
import _root_.editor.database.attributes.Expansion
import _root_.editor.database.attributes.Rarity
import _root_.editor.database.card.Card
import _root_.editor.database.version.DatabaseVersion
import _root_.editor.database.version.UpdateFrequency
import _root_.editor.database.{symbol => mtg}
import _root_.editor.filter.Filter
import _root_.editor.filter.leaf.TextFilter
import _root_.editor.gui.ccp.CCPItems
import _root_.editor.gui.ccp.data.DataFlavors
import _root_.editor.gui.ccp.handler.InventoryExportHandler
import _root_.editor.gui.deck.DeckLoadException
import _root_.editor.gui.deck.DeckSerializer
import _root_.editor.gui.deck.EditorFrame
import _root_.editor.gui.display.CardImagePanel
import _root_.editor.gui.display.CardTable
import _root_.editor.gui.display.CardTableCellRenderer
import _root_.editor.gui.display.CardTableModel
import _root_.editor.gui.filter.FilterGroupPanel
import _root_.editor.gui.generic.CardMenuItems
import _root_.editor.gui.generic.ComponentUtils
import _root_.editor.gui.generic.OverwriteFileChooser
import _root_.editor.gui.generic.ScrollablePanel
import _root_.editor.gui.generic.TableMouseAdapter
import _root_.editor.gui.generic.TristateCheckBox
import _root_.editor.gui.generic.TristateCheckBoxState
import _root_.editor.gui.generic.VerticalButtonList
import _root_.editor.gui.generic.WizardDialog
import _root_.editor.gui.generic.WizardResult
import _root_.editor.gui.generic.given
import _root_.editor.gui.inventory.InventoryDownloader
import _root_.editor.gui.inventory.InventoryLoader
import _root_.editor.gui.settings.Settings
import _root_.editor.gui.settings.SettingsDialog
import _root_.editor.gui.settings.SettingsObserver
import _root_.editor.unicode.{_, given}
import _root_.editor.util.MenuListenerFactory
import _root_.editor.util.MouseListenerFactory
import _root_.editor.util.PopupMenuListenerFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.SystemColor
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.beans.PropertyVetoException
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Properties
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JDesktopPane
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.KeyStroke
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.WindowConstants
import javax.swing.event.DocumentEvent
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.text.BadLocationException
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import scala.collection.mutable.LinkedHashMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.Failure
import scala.util.Success

/** Possible result of checking for an inventory update. */
sealed trait UpdateStatus
/** Inventory is out-of-date and an update was confirmed. */
case object UpdateNeeded extends UpdateStatus
/** Inventory is up-to-date. */
case object NoUpdate extends UpdateStatus
/** Inventory may be out-of-date, but the update was canceled. */
case object UpdateCancelled extends UpdateStatus

/**
 * Companion object to [[MainFrame]] containing global constants and the inventory (for convenience so other UI
 * classes that reference it don't need references to the [[MainFrame]] instance).
 * 
 * @author Alec Roelke
 */
object MainFrame {
  /** Initial ratio of space allocated for card image to inventory table in left pane. */
  val DefaultCardHeight = 1.0/3.0
  /** Maximum height of a filter panel before a scroll bar is activated. */
  val MaxFilterHeight = 300
}

/**
 * Main frame of the editor, containing the inventory table, currently-selected card, and [[EditorFrame]]s. Also provides a global
 * space for actions like adding or removing cards, undoing and redoing actions, and viewing preferences, all in the top menu bar.
 * 
 * @constructor create a new main frame
 * @param files files to open when the window opens
 * 
 * @author Alec Roelke
 */
class MainFrame(files: Seq[File]) extends JFrame with SettingsObserver {
  import MainFrame._

  /**
   * Renderer for a table displaying the inventory.  If an [[EditorFrame]] is selected, it will bold and/or italicize card names to
   * indicate if the card is present in the main deck and/or in a sideboard, respectively.
   * 
   * @author Alec Roelke
   */
  private class InventoryTableCellRenderer extends CardTableCellRenderer {
    override def getTableCellRendererComponent(table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = {
      val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
      val card = Inventory(table.convertRowIndexToModel(row)).card
      val main = selectedFrame.exists(_.deck.contains(card))
      val extra = selectedFrame.exists(_.extras.exists(_.contains(card)))
      try {
        ComponentUtils.propagateFont(c, c.getFont.deriveFont((if (main) Font.BOLD else 0) | (if (extra) Font.ITALIC else 0)))
      } catch case e: NullPointerException => {
        System.err.println(s"renderer component ${c.getClass} for value $value does not have a font")
      }
      c
    }
  }

  private var selectedTable: Option[CardTable] = None
  private var selectedList: Option[CardList] = None
  private var untitled = 0
  private var selectedFrame: Option[EditorFrame] = None
  private val editors = collection.mutable.ArrayBuffer[EditorFrame]()
  private val recentItems = collection.mutable.Queue[JMenuItem]()
  private val recents = collection.mutable.HashMap[JMenuItem, File]()

  // Initialize properties to their default values, then load the current values
  // from the properties file
  try {
    SettingsDialog.load()
  } catch {
    case e: MalformedURLException =>
      JOptionPane.showMessageDialog(this, s"Bad file URL: ${SettingsDialog.settings.inventory.url}", "Warning", JOptionPane.WARNING_MESSAGE)
      SettingsDialog.settings = Settings()
    case e @ (_: IOException | _: JsonParseException) =>
      var ex: Throwable = e
      while (ex.getCause != null)
        ex = ex.getCause
      JOptionPane.showMessageDialog(this, s"Error opening ${SettingsDialog.PropertiesFile}: ${ex.getMessage}.", "Warning", JOptionPane.WARNING_MESSAGE)
      SettingsDialog.settings = Settings()
  }

  setTitle("MTG Workstation")
  setIconImages((4 to 8).map(i => ImageIcon(getClass.getResource(s"/icon/${1 << i}.png")).getImage).asJava)
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  private val screenRes = Toolkit.getDefaultToolkit.getScreenSize
  setBounds(50, 50, screenRes.width - 100, screenRes.height - 100)

  /* MENU BAR */
  private val myMenuBar = JMenuBar()
  setJMenuBar(myMenuBar)

  // File menu
  private val fileMenu = JMenu("File")
  private val fileChooser = OverwriteFileChooser(SettingsDialog.settings.cwd)
  myMenuBar.add(fileMenu)

  // New file menu item
  private val newItem = JMenuItem("New")
  newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK))
  newItem.addActionListener(_ => selectFrame(createEditor()))
  fileMenu.add(newItem)

  // Open file menu item
  private val openItem = JMenuItem("Open...")
  openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK))
  openItem.addActionListener(_ => open())
  fileMenu.add(openItem)

  // Close file menu item
  private val closeItem = JMenuItem("Close")
  closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK))
  closeItem.addActionListener(_ => selectedFrame.foreach(close(_)))
  fileMenu.add(closeItem)

  // Close all files menu item
  private val closeAllItem = JMenuItem("Close All")
  closeAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK))
  closeAllItem.addActionListener(_ => closeAll())
  fileMenu.add(closeAllItem)

  fileMenu.add(JSeparator())

  // Save file menu item
  private val saveItem = JMenuItem("Save")
  saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK))
  saveItem.addActionListener(_ => selectedFrame.foreach(save(_)))
  fileMenu.add(saveItem)

  // Save file as menu item
  private val saveAsItem = JMenuItem("Save As...")
  saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0))
  saveAsItem.addActionListener(_ => selectedFrame.foreach(saveAs(_)))
  fileMenu.add(saveAsItem)

  // Save all files menu item
  private val saveAllItem = JMenuItem("Save All")
  saveAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK))
  saveAllItem.addActionListener(_ => saveAll())
  fileMenu.add(saveAllItem)

  // Recent files menu
  private val recentsMenu = JMenu("Open Recent")
  recentsMenu.setEnabled(false)
  SettingsDialog.settings.editor.recents.files.foreach(f => updateRecents(File(f)))
  fileMenu.add(recentsMenu)

  fileMenu.add(JSeparator())

  // Import and export items
  private val text = FileNameExtensionFilter("Text (*.txt)", "txt")
  private val delimited = FileNameExtensionFilter("Delimited (*.csv, *.txt)", "csv", "txt")
  private val importItem = JMenuItem("Import...")
  importItem.addActionListener(_ => {
    val importChooser = JFileChooser()
    importChooser.setAcceptAllFileFilterUsed(false)
    importChooser.addChoosableFileFilter(text)
    importChooser.addChoosableFileFilter(delimited)
    importChooser.setDialogTitle("Import")
    importChooser.setCurrentDirectory(fileChooser.getCurrentDirectory())
    importChooser.showOpenDialog(this) match {
      case JFileChooser.APPROVE_OPTION =>
        val format = if (importChooser.getFileFilter == text) {
          Some(TextCardListFormat(""))
        } else if (importChooser.getFileFilter == delimited) {
          val dataPanel = JPanel(BorderLayout())
          val optionsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
          optionsPanel.add(JLabel("Delimiter: "))
          val delimiterBox = JComboBox(DelimitedCardListFormat.Delimiters.toArray)
          delimiterBox.setEditable(true)
          optionsPanel.add(delimiterBox)
          val includeCheckBox = JCheckBox("Read Headers")
          includeCheckBox.setSelected(true)
          optionsPanel.add(includeCheckBox)
          dataPanel.add(optionsPanel, BorderLayout.NORTH)
          val headersList = JList(CardAttribute.displayableValues.toArray)
          headersList.setEnabled(!includeCheckBox.isSelected)
          val headersPane = JScrollPane(headersList)
          val headersPanel = Box(BoxLayout.X_AXIS)
          headersPanel.setBorder(BorderFactory.createTitledBorder("Column Data:"))
          val rearrangeButtons = VerticalButtonList(Seq(UpArrow, DownArrow))
          rearrangeButtons.foreach(_.setEnabled(!includeCheckBox.isSelected))
          headersPanel.add(rearrangeButtons)
          headersPanel.add(Box.createHorizontalStrut(5))
          val selectedHeadersModel = DefaultListModel[CardAttribute[?, ?]]()
          selectedHeadersModel.addElement(CardAttribute.Name)
          selectedHeadersModel.addElement(CardAttribute.Expansion)
          selectedHeadersModel.addElement(CardAttribute.CardNumber)
          selectedHeadersModel.addElement(CardAttribute.Count)
          selectedHeadersModel.addElement(CardAttribute.DateAdded)
          val selectedHeadersList = JList(selectedHeadersModel)
          selectedHeadersList.setEnabled(!includeCheckBox.isSelected)
          headersPanel.add(new JScrollPane(selectedHeadersList) {
              override def getPreferredSize = headersPane.getPreferredSize
          })
          headersPanel.add(Box.createHorizontalStrut(5))
          val moveButtons = VerticalButtonList(Seq(LeftArrow, RightArrow))
          moveButtons.foreach(_.setEnabled(!includeCheckBox.isSelected))
          headersPanel.add(moveButtons)
          headersPanel.add(Box.createHorizontalStrut(5))
          headersPanel.add(headersPane)
          dataPanel.add(headersPanel, BorderLayout.CENTER)
          rearrangeButtons(UpArrow).addActionListener(_ => {
            val selected = selectedHeadersList.getSelectedValuesList.asScala
            var ignore = 0
            for (index <- selectedHeadersList.getSelectedIndices) {
              if (index == ignore) {
                ignore += 1
              } else {
                val temp = selectedHeadersModel.getElementAt(index - 1)
                selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index - 1)
                selectedHeadersModel.setElementAt(temp, index)
              }
            }
            selectedHeadersList.clearSelection()
            selected.map(selectedHeadersModel.indexOf).foreach((i) => selectedHeadersList.addSelectionInterval(i, i))
          })
          rearrangeButtons(DownArrow).addActionListener(_ => {
            val selected = selectedHeadersList.getSelectedValuesList.asScala
            val indices = selectedHeadersList.getSelectedIndices.reverse
            var ignore = selectedHeadersModel.size() - 1
            for (index <- indices) {
              if (index == ignore) {
                ignore -= 1
              } else {
                val temp = selectedHeadersModel.getElementAt(index + 1)
                selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index + 1)
                selectedHeadersModel.setElementAt(temp, index)
              }
            }
            selectedHeadersList.clearSelection()
            selected.map(selectedHeadersModel.indexOf).foreach((i) => selectedHeadersList.addSelectionInterval(i, i))
          })
          moveButtons(LeftArrow).addActionListener(_ => {
            for (selected <- headersList.getSelectedValuesList.asScala)
              if (!selectedHeadersModel.contains(selected))
                selectedHeadersModel.addElement(selected)
            headersList.clearSelection()
          })
          moveButtons(RightArrow).addActionListener(_ => selectedHeadersList.getSelectedValuesList.asScala.foreach(selectedHeadersModel.removeElement(_)))
          includeCheckBox.addActionListener(_ => {
            headersList.setEnabled(!includeCheckBox.isSelected)
            selectedHeadersList.setEnabled(!includeCheckBox.isSelected)
            rearrangeButtons.foreach(_.setEnabled(!includeCheckBox.isSelected))
            moveButtons.foreach(_.setEnabled(!includeCheckBox.isSelected))
          })

          val previewPanel = JPanel(BorderLayout())
          previewPanel.setBorder(BorderFactory.createTitledBorder("Data to Import:"))
          val previewTable = new JTable {
              override def getPreferredScrollableViewportSize = Dimension(0, 0)
              override def getScrollableTracksViewportWidth = getPreferredSize.width < getParent.getWidth
          }
          previewTable.setAutoCreateRowSorter(true)
          previewPanel.add(JScrollPane(previewTable))

          val updateTable: ActionListener = _ => {
            try {
              val model = DefaultTableModel()
              val lines = Files.readAllLines(importChooser.getSelectedFile.toPath)
              if (includeCheckBox.isSelected) {
                val columns = lines.remove(0).split(delimiterBox.getSelectedItem.toString).map(_.asInstanceOf[Object])
                val data = lines.asScala.map((s) => DelimitedCardListFormat.split(delimiterBox.getSelectedItem.toString, s).map(_.asInstanceOf[Object]).toArray).toArray
                model.setDataVector(data, columns)
              } else {
                val columns = (0 until selectedHeadersModel.size).map(selectedHeadersModel.getElementAt(_).asInstanceOf[Object]).toArray
                val data = lines.asScala.map((s) => DelimitedCardListFormat.split(delimiterBox.getSelectedItem.toString, s).map(_.asInstanceOf[Object]).toArray).toArray
                model.setDataVector(data, columns)
              }
              previewTable.setModel(model)
            } catch {
              case x: IOException => JOptionPane.showMessageDialog(this, s"Could not import ${importChooser.getSelectedFile()}: ${x.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
            }
          }
          delimiterBox.addActionListener(updateTable)
          includeCheckBox.addActionListener(updateTable)
          rearrangeButtons.foreach(_.addActionListener(updateTable))
          moveButtons.foreach(_.addActionListener(updateTable))
          updateTable.actionPerformed(null)

          if (WizardDialog.showWizardDialog(this, "Import Wizard", dataPanel, previewPanel) == WizardResult.FinishOption) {
            val selected = (0 until selectedHeadersModel.size).map(selectedHeadersModel.getElementAt)
            Some(DelimitedCardListFormat(delimiterBox.getSelectedItem.toString, if (includeCheckBox.isSelected) Seq.empty else selected))
          }
          else None
        } else {
          JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + '.', "Error", JOptionPane.ERROR_MESSAGE)
          None
        }
        format.foreach((fmt) => {
          val manager = try {
            DeckSerializer.importList(fmt, importChooser.getSelectedFile, this)
          } catch case x: DeckLoadException => {
            JOptionPane.showMessageDialog(this, s"Could not import ${importChooser.getSelectedFile}: ${x.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
            DeckSerializer()
          }
          selectFrame(createEditor(manager))
        })
      case JFileChooser.CANCEL_OPTION =>
      case JFileChooser.ERROR_OPTION => JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + '.', "Error", JOptionPane.ERROR_MESSAGE)
    }
  })
  fileMenu.add(importItem)
  val exportItem = JMenuItem("Export...")
  exportItem.addActionListener(_ => selectedFrame match {
    case Some(f) =>
      val exportChooser = OverwriteFileChooser()
      exportChooser.setAcceptAllFileFilterUsed(false)
      exportChooser.addChoosableFileFilter(text)
      exportChooser.addChoosableFileFilter(delimited)
      exportChooser.setDialogTitle("Export")
      exportChooser.setCurrentDirectory(fileChooser.getCurrentDirectory())
      exportChooser.showSaveDialog(this) match {
        case JFileChooser.APPROVE_OPTION =>
          // Common pieces of the wizard
          val sortPanel = JPanel(FlowLayout(FlowLayout.CENTER))
          val sortCheck = JCheckBox("Sort by:", true)
          sortPanel.add(sortCheck)
          val sortBox = JComboBox(CardAttribute.displayableValues.toArray)
          sortBox.setSelectedItem(CardAttribute.Name)
          sortCheck.addItemListener(_ => sortBox.setEnabled(sortCheck.isSelected))
          sortPanel.add(sortBox)

          val extras = collection.mutable.LinkedHashMap(f.extras.map(_.name -> true):_*)
          val extrasPanel = JPanel(BorderLayout())
          extrasPanel.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, extrasPanel.getBackground))
          val includeExtras = TristateCheckBox("Include additional lists:", TristateCheckBoxState.Selected)
          extrasPanel.add(includeExtras, BorderLayout.NORTH)
          extrasPanel.setBackground(UIManager.getColor("List.background"))
          val extrasList = Box(BoxLayout.Y_AXIS)
          extrasList.setBorder(BorderFactory.createLineBorder(UIManager.getColor("List.dropLineColor")))
          for ((extra, _) <- extras) {
            val extraBox = JCheckBox(extra, extras(extra))
            extraBox.setBackground(extrasPanel.getBackground())
            extraBox.addActionListener(_ => {
              extras(extra) = extraBox.isSelected
              val n = extras.count(_._2)
              if (n == 0)
                includeExtras.setSelected(false)
              else if (n < extras.size)
                includeExtras.state = TristateCheckBoxState.Indeterminate
              else // n == extra.size
                includeExtras.setSelected(true)
              SwingUtilities.invokeLater(() => includeExtras.repaint())
            })
            includeExtras.addActionListener(_ => {
              extraBox.setSelected(includeExtras.state == TristateCheckBoxState.Selected)
              extras(extra) = extraBox.isSelected
              SwingUtilities.invokeLater(() => extraBox.repaint())
            })
            extrasList.add(extraBox)
          }
          extrasPanel.add(extrasList, BorderLayout.CENTER)

          // File-format-specific pieces of the wizard
          val format = if (exportChooser.getFileFilter == text) {
            val wizardPanel = Box(BoxLayout.Y_AXIS)
            val fieldPanel = Box(BoxLayout.Y_AXIS)
            fieldPanel.setBorder(BorderFactory.createTitledBorder("List Format:"))
            val formatField = JTextField(TextCardListFormat.DefaultFormat)
            formatField.setFont(Font(Font.MONOSPACED, Font.PLAIN, formatField.getFont().getSize()))
            formatField.setColumns(50)
            fieldPanel.add(formatField)
            val addDataPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            addDataPanel.add(new JLabel("Add Data: "))
            val addDataBox = JComboBox(CardAttribute.displayableValues.toArray)
            fieldPanel.add(addDataPanel)
            addDataPanel.add(addDataBox)
            wizardPanel.add(fieldPanel)

            if (f.deck.total > 0 || f.allExtras.total > 0) {
              val previewPanel = JPanel(BorderLayout())
              previewPanel.setBorder(BorderFactory.createTitledBorder("Preview:"))
              val previewArea = JTextArea()
              val previewPane = JScrollPane(previewArea)
              previewArea.setText(new TextCardListFormat(formatField.getText()).format(if (f.deck.total > 0) f.deck else f.allExtras))
              previewArea.setRows(1)
              previewArea.setCaretPosition(0)
              previewPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
              previewPanel.add(previewPane, BorderLayout.CENTER)
              wizardPanel.add(previewPanel)

              addDataBox.addActionListener(_ => {
                val pos = formatField.getCaretPosition
                val data = s"{${addDataBox.getSelectedItem}}".toLowerCase
                val t = formatField.getText.substring(0, pos) + data + (
                  if (pos < formatField.getText.length)
                    formatField.getText.substring(formatField.getCaretPosition)
                  else
                    ""
                )
                formatField.setText(t)
                formatField.setCaretPosition(pos + data.length())
                formatField.requestFocusInWindow()
              })

              formatField.getDocument().addDocumentListener((e: DocumentEvent) => {
                previewArea.setText(TextCardListFormat(formatField.getText()).format(if (f.deck.total > 0) f.deck else f.allExtras))
                previewArea.setCaretPosition(0)
              })
            }

            if (!extras.isEmpty)
              wizardPanel.add(extrasPanel)

            if (f.deck.total > 0 || f.allExtras.total > 0)
              wizardPanel.add(sortPanel)

            if (WizardDialog.showWizardDialog(this, "Export Wizard", wizardPanel) == WizardResult.FinishOption)
              Some(TextCardListFormat(formatField.getText))
            else
              None
          } else if (exportChooser.getFileFilter == delimited) {
            val panels = collection.mutable.ArrayBuffer[JComponent]()
            val dataPanel = Box(BoxLayout.Y_AXIS)
            val headersList = JList(CardAttribute.displayableValues.toArray)
            val headersPane = JScrollPane(headersList)
            val headersPanel = Box(BoxLayout.X_AXIS)
            headersPanel.setBorder(BorderFactory.createTitledBorder("Column Data:"))
            val rearrangeButtons = VerticalButtonList(Seq(UpArrow, DownArrow))
            headersPanel.add(rearrangeButtons)
            headersPanel.add(Box.createHorizontalStrut(5))
            val selectedHeadersModel = DefaultListModel[CardAttribute[?, ?]]()
            selectedHeadersModel.addElement(CardAttribute.Name)
            selectedHeadersModel.addElement(CardAttribute.Expansion)
            selectedHeadersModel.addElement(CardAttribute.CardNumber)
            selectedHeadersModel.addElement(CardAttribute.Count)
            selectedHeadersModel.addElement(CardAttribute.DateAdded)
            val selectedHeadersList = JList(selectedHeadersModel.toArray)
            headersPanel.add(new JScrollPane(selectedHeadersList) {
              override def getPreferredSize = headersPane.getPreferredSize
            })
            headersPanel.add(Box.createHorizontalStrut(5))
            val moveButtons = VerticalButtonList(Seq(LeftArrow, RightArrow))
            headersPanel.add(moveButtons)
            headersPanel.add(Box.createHorizontalStrut(5))
            headersPanel.add(headersPane)
            dataPanel.add(headersPanel)

            rearrangeButtons(UpArrow).addActionListener(_ => {
              val selected = selectedHeadersList.getSelectedValuesList.asScala
              var ignore = 0
              for (index <- selectedHeadersList.getSelectedIndices) {
                if (index == ignore) {
                  ignore += 1
                } else {
                  val temp = selectedHeadersModel.getElementAt(index - 1)
                  selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index - 1)
                  selectedHeadersModel.setElementAt(temp, index)
                }
              }
              selectedHeadersList.clearSelection()
              selected.map(selectedHeadersModel.indexOf).foreach((i) => selectedHeadersList.addSelectionInterval(i, i))
            })
            rearrangeButtons(DownArrow).addActionListener(_ => {
              val selected = selectedHeadersList.getSelectedValuesList.asScala
              val indices = selectedHeadersList.getSelectedIndices.reverse
              var ignore = selectedHeadersModel.size - 1
              for (index <- indices) {
                if (index == ignore) {
                  ignore -= 1
                } else {
                  val temp = selectedHeadersModel.getElementAt(index + 1)
                  selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index + 1)
                  selectedHeadersModel.setElementAt(temp, index)
                }
              }
              selectedHeadersList.clearSelection()
              selected.map(selectedHeadersModel.indexOf).foreach((i) => selectedHeadersList.addSelectionInterval(i, i))
            })
            moveButtons(LeftArrow).addActionListener(_ => {
              for (selected <- headersList.getSelectedValuesList.asScala)
                if (!selectedHeadersModel.contains(selected))
                  selectedHeadersModel.addElement(selected)
              headersList.clearSelection()
            })
            moveButtons(RightArrow).addActionListener(_ => {
              selectedHeadersList.getSelectedValuesList.asScala.foreach(selectedHeadersModel.removeElement(_))
            })

            val optionsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            optionsPanel.add(JLabel("Delimiter: "))
            val delimiterBox = JComboBox(DelimitedCardListFormat.Delimiters.toArray)
            delimiterBox.setEditable(true)
            optionsPanel.add(delimiterBox)
            val includeCheckBox = JCheckBox("Include Headers")
            includeCheckBox.setSelected(true)
            optionsPanel.add(includeCheckBox)
            dataPanel.add(optionsPanel)

            dataPanel.add(sortPanel)
            panels += dataPanel

            if (!extras.isEmpty)
                panels += extrasPanel

            if (WizardDialog.showWizardDialog(this, "Export Wizard", panels.toSeq:_*) == WizardResult.FinishOption) {
              val selected = (0 until selectedHeadersModel.size).map(selectedHeadersModel.getElementAt)
              Some(DelimitedCardListFormat(delimiterBox.getSelectedItem.toString, if (includeCheckBox.isSelected) selected else Seq.empty))
            } else None
          } else {
            JOptionPane.showMessageDialog(this, s"Could not export ${f.deck.name}.", "Error", JOptionPane.ERROR_MESSAGE)
            None
          }
          format.foreach((fmt) => {
            val sorted = new Ordering[CardListEntry] { def compare(a: CardListEntry, b: CardListEntry) = sortBox.getItemAt(sortBox.getSelectedIndex).comparingEntry.compare(a, b) }
            val unsorted = new Ordering[CardListEntry] { def compare(a: CardListEntry, b: CardListEntry) = 0 }
            f.exportList(fmt, if (sortCheck.isSelected) sorted else unsorted, extras.collect{ case (e, s) if s => e }.toSeq, exportChooser.getSelectedFile) match {
              case Success(_) =>
              case Failure(x) => JOptionPane.showMessageDialog(this, s"Could not export ${f.deck.name}: ${x.getMessage}", "Error", JOptionPane.ERROR_MESSAGE)
            }
          })
        case JFileChooser.CANCEL_OPTION =>
        case JFileChooser.ERROR_OPTION => JOptionPane.showMessageDialog(this, s"Could not export ${f.deck.name}.", "Error", JOptionPane.ERROR_MESSAGE)
      }
    case None =>
  })
  fileMenu.add(exportItem)

  fileMenu.add(JSeparator())

  // Exit menu item
  private val exitItem = JMenuItem("Exit")
  exitItem.addActionListener(_ => exit())
  exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK))
  fileMenu.add(exitItem)

  // File menu listener
  fileMenu.addMenuListener(MenuListenerFactory.createMenuListener(selected = _ => {
    closeItem.setEnabled(selectedFrame.isDefined)
    closeAllItem.setEnabled(!editors.isEmpty)
    saveItem.setEnabled(selectedFrame.map(_.unsaved).getOrElse(false))
    saveAsItem.setEnabled(selectedFrame.isDefined)
    saveAllItem.setEnabled(editors.map(_.unsaved).fold(false)((a, b) => a || b))
    exportItem.setEnabled(selectedFrame.isDefined)
  }))
  // Items are enabled while hidden so their listeners can be used
  fileMenu.addMenuListener(MenuListenerFactory.createMenuListener(deselected = _ => {
    closeItem.setEnabled(true)
    closeAllItem.setEnabled(true)
    saveItem.setEnabled(true)
    saveAsItem.setEnabled(true)
    saveAllItem.setEnabled(true)
    exportItem.setEnabled(true)
  }))

  // Edit menu
  private val editMenu = JMenu("Edit")
  myMenuBar.add(editMenu)

  // Cut, copy, paste
  private val editCCP = CCPItems(selectedTable.get, true)
  editMenu.add(editCCP.cut)
  editMenu.add(editCCP.copy)
  editMenu.add(editCCP.paste)
  editMenu.add(JSeparator())

  // Undo menu item
  private val undoItem = JMenuItem("Undo")
  undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK))
  undoItem.addActionListener(_ => selectedFrame.foreach(_.undo()))
  editMenu.add(undoItem)

  // Redo menu item
  private val redoItem = JMenuItem("Redo")
  redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK))
  redoItem.addActionListener(_ => selectedFrame.foreach(_.redo()))
  editMenu.add(redoItem)

  editMenu.add(JSeparator())

  // Preferences menu item
  private val settings = Future{ SettingsDialog(this) }(ExecutionContext.global)
  private val preferencesItem = JMenuItem("Preferences...")
  preferencesItem.addActionListener(_ => try {
      val s = Await.result(settings, 0.nanos)
      s.setLocationRelativeTo(this)
      s.updateAndShow()
  } catch {
    case x @ (_: TimeoutException | _: InterruptedException) =>
      JOptionPane.showMessageDialog(this, "Error creating preferences dialog. Please restart the program.", "Error", JOptionPane.ERROR_MESSAGE)
      x.printStackTrace
      System.exit(1)
  })
  editMenu.add(preferencesItem)

  // Edit menu listener
  editMenu.addMenuListener(MenuListenerFactory.createMenuListener(selected = _ => {
    editCCP.cut.setEnabled(selectedList.contains(Inventory) && !getSelectedCards.isEmpty)
    editCCP.copy.setEnabled(!getSelectedCards.isEmpty)
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    editCCP.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor))

    undoItem.setEnabled(selectedFrame.isDefined)
    redoItem.setEnabled(selectedFrame.isDefined)
  }))
  // Items are enabled while hidden so their listeners can be used
  editMenu.addMenuListener(MenuListenerFactory.createMenuListener(deselected = _ => {
    undoItem.setEnabled(true)
    redoItem.setEnabled(true)
  }))

  // Deck menu
  private val deckMenu = JMenu("Deck")
  deckMenu.setEnabled(false)
  myMenuBar.add(deckMenu)

  // Add/Remove card menus
  private val addMenu = JMenu("Add Cards")
  deckMenu.add(addMenu)
  private val removeMenu = JMenu("Remove Cards")
  deckMenu.add(removeMenu)
  private val deckMenuCardItems = CardMenuItems(selectedFrame, getSelectedCards.map(_.card), true)
  deckMenuCardItems.addAddItems(addMenu)
  deckMenuCardItems.addRemoveItems(removeMenu)
  deckMenuCardItems.addOne.setAccelerator(KeyStroke.getKeyStroke('+'))
  deckMenuCardItems.fillPlayset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK))
  deckMenuCardItems.removeOne.setAccelerator(KeyStroke.getKeyStroke('-'))
  deckMenuCardItems.removeAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK))

  // Sideboard menu
  private val sideboardMenu = JMenu("Sideboard")
  deckMenu.add(sideboardMenu)
  private val sideboardMenuItems = CardMenuItems(selectedFrame, getSelectedCards.map(_.card), false)
  sideboardMenu.add(sideboardMenuItems.addOne)
  sideboardMenu.add(sideboardMenuItems.addN)
  sideboardMenu.add(sideboardMenuItems.removeOne)
  sideboardMenu.add(sideboardMenuItems.removeAll)

  // Categorization menu
  private val categoryMenu = JMenu("Categorization")
  deckMenu.add(categoryMenu)

  // Add category item
  private val addCategoryItem = JMenuItem("Add...")
  addCategoryItem.addActionListener(_ => selectedFrame.foreach((f) => f.createCategory.foreach(f.categories += _)))
  categoryMenu.add(addCategoryItem)

  // Edit category item
  private val editCategoryItem = new JMenuItem("Edit...")
  editCategoryItem.addActionListener(_ => selectedFrame.foreach((f) => {
    val contentPanel = JPanel(BorderLayout())
    contentPanel.add(JLabel("Choose a category to edit:"), BorderLayout.NORTH)
    val categories = JList(f.categories.map(_.name).toArray.sorted)
    categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    contentPanel.add(JScrollPane(categories), BorderLayout.CENTER)
    if (JOptionPane.showConfirmDialog(this, contentPanel, "Edit Categorization", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
      f.editCategory(categories.getSelectedValue)
  }))
  categoryMenu.add(editCategoryItem)

  // Remove category item
  private val removeCategoryItem = JMenuItem("Remove...")
  removeCategoryItem.addActionListener(_ => selectedFrame.foreach((f) => {
    val contentPanel = JPanel(BorderLayout())
    contentPanel.add(JLabel("Choose a category to remove:"), BorderLayout.NORTH)
    val categories = JList(f.categories.map(_.name).toArray.sorted)
    categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    contentPanel.add(JScrollPane(categories), BorderLayout.CENTER)
    if (JOptionPane.showConfirmDialog(this, contentPanel, "Edit Categorization", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
      f.categories -= categories.getSelectedValue
  }))
  categoryMenu.add(removeCategoryItem)

  // Preset categories menu
  private val presetMenu = JMenu("Add Preset")
  categoryMenu.add(presetMenu)

  // Deck menu listener
  deckMenu.addMenuListener(MenuListenerFactory.createMenuListener(selected = _ => {
    addMenu.setEnabled(selectedFrame.isDefined && !getSelectedCards.isEmpty)
    removeMenu.setEnabled(selectedFrame.isDefined && !getSelectedCards.isEmpty)
    sideboardMenu.setEnabled(selectedFrame.map(!_.sideboard.name.isEmpty).getOrElse(false) && !getSelectedCards.isEmpty)
    presetMenu.setEnabled(presetMenu.getMenuComponentCount > 0)
  }))
  // Items are enabled while hidden so their listeners can be used.
  deckMenu.addMenuListener(MenuListenerFactory.createMenuListener(deselected = _ => {
    addMenu.setEnabled(true)
    removeMenu.setEnabled(true)
  }))

  // Help menu
  private val helpMenu = JMenu("Help")
  myMenuBar.add(helpMenu)

  // About dialog
  private val aboutItem = JMenuItem("About...")
  helpMenu.add(aboutItem)
  aboutItem.addActionListener(_ => {
    val is = getClass.getResourceAsStream("/project.properties")
    val version = try {
      val p = Properties()
      p.load(is)
      p.getProperty("version")
    } catch {
      case _: FileNotFoundException => "0.0.0:fnf"
      case _: IOException => "0.0.0:io"
      case _: Exception => "0.0.0:x"
    } finally {
      is.close()
    }
    JOptionPane.showMessageDialog(MainFrame.this, s"""|MTG Workstation version $version.
                                                      |Created by Alec Roelke (alec.roelke@gmail.com).
                                                      |File bug reports at https://github.com/aroelke/mtg-workstation.""".stripMargin, "About", JOptionPane.INFORMATION_MESSAGE)
  })
  helpMenu.add(JSeparator())

  // Inventory update item
  private val updateInventoryItem = JMenuItem("Check for inventory update...")
  updateInventoryItem.addActionListener(_ => {
    checkForUpdate(UpdateFrequency.Daily) match {
      case (version, UpdateNeeded) => if (updateInventory()) {
        SettingsDialog.settings = SettingsDialog.settings.copy(inventory = SettingsDialog.settings.inventory.copy(version = version))
        loadInventory()
      }
      case (_, NoUpdate) => JOptionPane.showMessageDialog(this, "Inventory is up to date.")
      case (_, UpdateCancelled) =>
    }
  })
  helpMenu.add(updateInventoryItem)

  // Reload inventory item
  private val reloadInventoryItem = JMenuItem("Reload inventory...")
  reloadInventoryItem.addActionListener(_ => loadInventory())
  helpMenu.add(reloadInventoryItem)

  helpMenu.add(JSeparator())

  // Show expansions item
  private val showExpansionsItem = JMenuItem("Show Expansions...")
  showExpansionsItem.addActionListener(_ => {
    val expansionTableModel = new AbstractTableModel {
      private val columns = Array("Expansion", "Block", "Code", "Cards", "Release Date")

      override def getColumnCount = columns.length
      override def getColumnName(index: Int) = columns(index)
      override def getRowCount = Expansion.expansions.size

      override def getValueAt(rowIndex: Int, columnIndex: Int) = columnIndex match {
        case 0 => Expansion.expansions(rowIndex).name
        case 1 => if (Expansion.expansions(rowIndex).block == Expansion.NoBlock) "" else Expansion.expansions(rowIndex).block
        case 2 => Expansion.expansions(rowIndex).code
        case 3 => Expansion.expansions(rowIndex).count
        case 4 => Expansion.expansions(rowIndex).released.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        case _ => throw IndexOutOfBoundsException()
      }
    }
    val expansionTable = new JTable(expansionTableModel) {
      override def prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int) = {
        val component = super.prepareRenderer(renderer, row, column)
        val width = component.getPreferredSize.width
        val tableColumn = getColumnModel.getColumn(column)
        tableColumn.setPreferredWidth(Math.max(width + getIntercellSpacing.width, tableColumn.getPreferredWidth))
        component
      }
    }
    expansionTable.setShowGrid(false)
    expansionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    expansionTable.setAutoCreateRowSorter(true)
    expansionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS)

    JOptionPane.showMessageDialog(this, JScrollPane(expansionTable), "Expansions", JOptionPane.PLAIN_MESSAGE)
  })
  helpMenu.add(showExpansionsItem)

  /* CONTENT PANE */
  // Panel containing all content
  private val contentPane = JPanel(BorderLayout())
  contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK), "Next Frame")
  contentPane.getActionMap.put("Next Frame", new AbstractAction {
    override def actionPerformed(e: ActionEvent) = {
      if (!editors.isEmpty)
        selectedFrame.fold(selectFrame(editors.last))((f) => selectFrame(editors((editors.indexOf(f) + 1) % editors.size)))
    }
  })
  contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK), "Previous Frame")
  contentPane.getActionMap.put("Previous Frame", new AbstractAction {
      override def actionPerformed(e: ActionEvent) =  {
        if (!editors.isEmpty) {
          val next = selectedFrame.map((f) => editors.indexOf(f) - 1).getOrElse(0)
          selectFrame(editors(if (next < 0) editors.size - 1 else next))
        }
      }
  })
  setContentPane(contentPane)

  // DesktopPane containing editor frames
  private val decklistDesktop = JDesktopPane()
  decklistDesktop.setBackground(SystemColor.controlShadow)
  private val cardPane = JTabbedPane()

  // Panel showing the image of the currently-selected card
  private val imagePanel = CardImagePanel()
  cardPane.addTab("Image", imagePanel)
  setImageBackground(SettingsDialog.settings.inventory.background)

  // Pane displaying the Oracle text
  private val oracleTextPane = JTextPane()
  oracleTextPane.setEditable(false)
  oracleTextPane.setCursor(Cursor(Cursor.TEXT_CURSOR))
  cardPane.addTab("Oracle Text", JScrollPane(oracleTextPane))

  private val printedTextPane = JTextPane()
  printedTextPane.setEditable(false)
  printedTextPane.setCursor(Cursor(Cursor.TEXT_CURSOR))
  cardPane.addTab("Printed Text", JScrollPane(printedTextPane))

  private val rulingsPane = JTextPane()
  rulingsPane.setEditable(false)
  rulingsPane.setCursor(Cursor(Cursor.TEXT_CURSOR))
  cardPane.addTab("Rulings", JScrollPane(rulingsPane))

  // Oracle text pane popup menu
  private val oraclePopupMenu = JPopupMenu()
  oracleTextPane.setComponentPopupMenu(oraclePopupMenu)
  printedTextPane.setComponentPopupMenu(oraclePopupMenu)
  imagePanel.setComponentPopupMenu(oraclePopupMenu)

  // Copy
  private val oracleCCP = CCPItems(imagePanel, false)
  oraclePopupMenu.add(oracleCCP.copy)
  oraclePopupMenu.add(JSeparator())

  // Add the card to the main deck
  private val oracleMenuCardItems = CardMenuItems(selectedFrame, getSelectedCards.headOption.map(_.card), true)
  val oracleMenuCardSeparators = Seq(JSeparator(), JSeparator())
  oracleMenuCardItems.addAddItems(oraclePopupMenu)
  oraclePopupMenu.add(oracleMenuCardSeparators(0))
  oracleMenuCardItems.addRemoveItems(oraclePopupMenu)
  oraclePopupMenu.add(oracleMenuCardSeparators(0))

  // Add the card to the sideboard
  private val oracleMenuSBCardItems = CardMenuItems(selectedFrame, getSelectedCards.headOption.map(_.card), false)
  oracleMenuSBCardItems.addOne.setText("Add to Sideboard")
  oraclePopupMenu.add(oracleMenuSBCardItems.addOne)
  oracleMenuSBCardItems.addN.setText("Add to Sideboard...")
  oraclePopupMenu.add(oracleMenuSBCardItems.addN)
  oracleMenuSBCardItems.removeOne.setText("Remove from Sideboard")
  oraclePopupMenu.add(oracleMenuSBCardItems.removeOne)
  oracleMenuSBCardItems.removeAll.setText("Remove All from Sideboard")
  oraclePopupMenu.add(oracleMenuSBCardItems.removeAll)
  private val oracleMenuSBSeparator = JSeparator()
  oraclePopupMenu.add(oracleMenuSBSeparator)

  private val oracleEditTagsItem = JMenuItem("Edit Tags...")
  oracleEditTagsItem.addActionListener(_ => CardTagPanel.editTags(getSelectedCards.map(_.card), this))
  oraclePopupMenu.add(oracleEditTagsItem)

  // Popup listener for oracle popup menu
  oraclePopupMenu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
    oracleCCP.copy.setEnabled(!getSelectedCards.isEmpty)
    oracleMenuCardItems.setVisible(selectedFrame.isDefined && !getSelectedCards.isEmpty)
    oracleMenuCardSeparators.foreach(_.setVisible(selectedFrame.isDefined && !getSelectedCards.isEmpty))
    oracleMenuSBCardItems.setVisible(selectedFrame.fold(false)((f) => !f.extras.isEmpty) && !getSelectedCards.isEmpty)
    oracleMenuSBSeparator.setVisible(selectedFrame.fold(false)((f) => !f.extras.isEmpty) && !getSelectedCards.isEmpty)
    oracleEditTagsItem.setEnabled(!getSelectedCards.isEmpty)
  }))

  // Copy handler for image panel
  imagePanel.setTransferHandler(InventoryExportHandler(getSelectedCards.headOption.map(_.card)))

  // Panel containing inventory and image of currently-selected card
  private val inventoryPanel = JPanel(BorderLayout(0, 0))
  inventoryPanel.setPreferredSize(Dimension(getWidth/4, getHeight*3/4))

  // Panel containing the inventory and the quick-filter bar
  private val tablePanel = JPanel(BorderLayout(0, 0))
  inventoryPanel.add(tablePanel, BorderLayout.CENTER)

  // Panel containing the quick-filter bar
  private val filterPanel = Box(BoxLayout.X_AXIS)

  // Text field for quickly filtering by name
  private val nameFilterField = JTextField()
  filterPanel.add(nameFilterField)

  // Button for clearing the filter
  private val clearButton = JButton("X")
  filterPanel.add(clearButton)
  
  // Button for opening the advanced filter dialog
  private val advancedFilterButton = JButton("Advanced...")
  filterPanel.add(advancedFilterButton)
  tablePanel.add(filterPanel, BorderLayout.NORTH)

  // Create the inventory and put it in the table
  private val inventoryModel = CardTableModel(Inventory, Settings().inventory.columns)
  private val inventoryTable = CardTable(inventoryModel)
  CardAttribute.values.foreach((a) => inventoryTable.setDefaultRenderer(a.dataType, InventoryTableCellRenderer()))
  inventoryTable.stripe = SettingsDialog.settings.inventory.stripe
  inventoryTable.addMouseListener(MouseListenerFactory.createMouseListener(clicked = (e) => selectedFrame.foreach((f) => {
    if (e.getClickCount % 2 == 0)
      f.deck ++= getSelectedCards.map((c) => CardListEntry(c.card, 1))
  })))
  inventoryTable.setTransferHandler(InventoryExportHandler(getSelectedCards.map(_.card)))
  inventoryTable.setDragEnabled(true)
  tablePanel.add(JScrollPane(inventoryTable), BorderLayout.CENTER)

  // Table popup menu
  private val inventoryMenu = JPopupMenu()
  inventoryTable.addMouseListener(TableMouseAdapter(inventoryTable, inventoryMenu))

  // Copy
  private val inventoryCCP = CCPItems(inventoryTable, true)
  inventoryMenu.add(inventoryCCP.copy)
  inventoryMenu.add(JSeparator())

  // Add cards to the main deck
  private val inventoryMenuCardItems = CardMenuItems(selectedFrame, getSelectedCards.map(_.card), true)
  private val inventoryMenuCardSeparators = Array(JSeparator(), JSeparator())
  inventoryMenuCardItems.addAddItems(inventoryMenu)
  inventoryMenu.add(inventoryMenuCardSeparators(0))
  inventoryMenuCardItems.addRemoveItems(inventoryMenu)
  inventoryMenu.add(inventoryMenuCardSeparators(1))

  // Add cards to the sideboard
  private val inventoryMenuSBItems = CardMenuItems(selectedFrame, getSelectedCards.map(_.card), false)
  inventoryMenuSBItems.addOne.setText("Add to Sideboard")
  inventoryMenu.add(inventoryMenuSBItems.addOne)
  inventoryMenuSBItems.addN.setText("Add to Sideboard...")
  inventoryMenu.add(inventoryMenuSBItems.addN)
  inventoryMenuSBItems.removeOne.setText("Remove from Sideboard")
  inventoryMenu.add(inventoryMenuSBItems.removeOne)
  inventoryMenuSBItems.removeAll.setText("Remove All from Sideboard")
  inventoryMenu.add(inventoryMenuSBItems.removeAll)
  private val inventoryMenuSBSeparator = JSeparator()
  inventoryMenu.add(inventoryMenuSBSeparator)

  // Edit tags item
  private val editTagsItem = JMenuItem("Edit Tags...")
  editTagsItem.addActionListener(_ => CardTagPanel.editTags(getSelectedCards.map(_.card), this))
  inventoryMenu.add(editTagsItem)

  // Inventory menu listener
  inventoryMenu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
    inventoryMenuCardItems.setVisible(selectedFrame.isDefined && !getSelectedCards.isEmpty)
    inventoryMenuCardSeparators.foreach(_.setVisible(selectedFrame.isDefined && !getSelectedCards.isEmpty))
    inventoryMenuSBItems.setVisible(selectedFrame.fold(false)((f) => !f.extras.isEmpty) && !getSelectedCards.isEmpty)
    inventoryMenuSBSeparator.setVisible(selectedFrame.fold(false)((f) => !f.extras.isEmpty) && !getSelectedCards.isEmpty)
    editTagsItem.setEnabled(!getSelectedCards.isEmpty)
  }))

  // Action to be taken when the user presses the Enter key after entering text into the quick-filter bar
  nameFilterField.addActionListener(_ => {
    Inventory.filter = TextFilter(CardAttribute.Name, nameFilterField.getText.toLowerCase)
    inventoryModel.fireTableDataChanged()
  })

  // Action to be taken when the clear button is pressed (reset the filter)
  clearButton.addActionListener(_ => {
    nameFilterField.setText("")
    Inventory.filter = CardAttribute.AnyCard.filter
    inventoryModel.fireTableDataChanged()
  })

  // Action to be taken when the advanced filter button is pressed (show the advanced filter dialog)
  advancedFilterButton.addActionListener(_ => {
    val panel = FilterGroupPanel()
    if (Inventory.filter.attribute == CardAttribute.AnyCard)
      panel.setContents(CardAttribute.Name.filter)
    else
      panel.setContents(Inventory.filter)
    panel.listeners += ((c) => SwingUtilities.getWindowAncestor(c.getSource.asInstanceOf[Component]).pack())

    val panelPanel = new ScrollablePanel(ScrollablePanel.TrackWidth, BorderLayout()) {
      override def getPreferredScrollableViewportSize = {
        val size = panel.getPreferredSize
        size.height = Math.min(MaxFilterHeight, size.height)
        size
      }
    }
    panelPanel.add(panel, BorderLayout.CENTER)

    val panelPane = JScrollPane(panelPanel)
    panelPane.setBorder(BorderFactory.createEmptyBorder())
    if (JOptionPane.showConfirmDialog(this, panelPane, "Advanced Filter", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
      nameFilterField.setText("")
      Inventory.filter = panel.filter
      inventoryModel.fireTableDataChanged()
    }
  })

  // Split panes dividing the panel into three sections.  They can be resized at will.
  private val inventorySplit = JSplitPane(JSplitPane.VERTICAL_SPLIT, cardPane, inventoryPanel)
  inventorySplit.setOneTouchExpandable(true)
  inventorySplit.setContinuousLayout(true)
  SwingUtilities.invokeLater(() => inventorySplit.setDividerLocation(MainFrame.DefaultCardHeight))
  private val editorSplit = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inventorySplit, decklistDesktop)
  editorSplit.setOneTouchExpandable(true)
  editorSplit.setContinuousLayout(true)
  contentPane.add(editorSplit, BorderLayout.CENTER)

  contentPane.add(CardImagePanel.createStatusBar, BorderLayout.SOUTH)

  // File chooser
  // private val fileChooser = OverwriteFileChooser(SettingsDialog.settings.cwd)
  fileChooser.setMultiSelectionEnabled(false)
  fileChooser.addChoosableFileFilter(FileNameExtensionFilter("Deck (*.json)", "json"))
  fileChooser.setAcceptAllFileFilterUsed(true)

  // Handle what happens when the window tries to close and when it opens.
  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) = exit()

    override def windowOpened(e: WindowEvent) = {
      val (version, update) = checkForUpdate(SettingsDialog.settings.inventory.update)
      if (update == UpdateNeeded && updateInventory()) {
        SettingsDialog.settings = SettingsDialog.settings.copy(inventory = SettingsDialog.settings.inventory.copy(version = version))
      }
      loadInventory()
      val listener = TableSelectionListener(MainFrame.this, inventoryTable, Inventory)
      inventoryTable.addMouseListener(listener)
      inventoryTable.getSelectionModel.addListSelectionListener(listener)

      if (!Inventory.isEmpty) {
        for (spec <- SettingsDialog.settings.editor.categories.presets) {
          val categoryItem = JMenuItem(spec.name)
          categoryItem.addActionListener(_ => selectedFrame.foreach(_.categories += spec))
          presetMenu.add(categoryItem)
        }
        files.foreach(open(_))
      }
    }
  })

  startObserving()

  /**
   * Add a new preset category to the preset categories list. If the category to add has anything in its white- or blacklist, warn that it will be removed
   * first.
   *
   * @param category new preset category to add
   */
  def addPreset(category: Categorization) = {
    if (!((!category.whitelist.isEmpty || !category.blacklist.isEmpty) && JOptionPane.showConfirmDialog(
      this,
      s"Categorization ${category.name} contains cards in its whitelist or blacklist which will not be included in the preset category. Make this category a preset category?",
      "Add to Presets",
      JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION
    )) {
      val spec = category.copy(whitelist = Set.empty, blacklist = Set.empty)
      SettingsDialog.addPresetCategory(spec)
      val categoryItem = JMenuItem(spec.name)
      categoryItem.addActionListener(_ => selectedFrame.foreach(_.categories += spec))
      presetMenu.add(categoryItem)
    }
  }

  override def applySettings(oldSettings: Settings, newSettings: Settings) = {
    applyChanges(oldSettings, newSettings)(_.inventory.columns)(inventoryModel.columns = _)
                                          (_.inventory.stripe)(inventoryTable.stripe = _)
                                          (_.editor.categories.presets){ presets =>
                                            presetMenu.removeAll()
                                            for (spec <- presets) {
                                              val categoryItem = JMenuItem(spec.name)
                                              categoryItem.addActionListener(_ => selectedFrame.foreach(_.categories += spec))
                                              presetMenu.add(categoryItem)
                                            }
                                          }
                                          (_.inventory.background)(setImageBackground(_))
                                          (_.editor.hand.background)(setHandBackground(_))
    revalidate()
    repaint()
  }

  /**
   * Check to see if the inventory needs to be updated.  If it does, ask the user if it should be.
   *
   * @param freq desired frequency for downloading updates
   * @return An [[UpdateStatus]] indicating if a new inventory should be downloaded
   */
  def checkForUpdate(freq: UpdateFrequency) = try {
    if (!SettingsDialog.settings.inventory.inventoryFile.exists()) {
      JOptionPane.showMessageDialog(this, s"${SettingsDialog.settings.inventory.inventoryFile.getName} not found.  It will be downloaded.", "Update", JOptionPane.WARNING_MESSAGE)
      val in = BufferedReader(InputStreamReader(SettingsDialog.settings.inventory.versionSite.openStream()))
      val data = (new JsonParser).parse(in.lines.collect(Collectors.joining)).getAsJsonObject
      in.close()
      (DatabaseVersion.parseVersion((if (data.has("data")) data.get("data").getAsJsonObject else data).get("version").getAsString), UpdateNeeded)
    } else if (SettingsDialog.settings.inventory.update != UpdateFrequency.Never) {
      val in = BufferedReader(InputStreamReader(SettingsDialog.settings.inventory.versionSite.openStream()))
      val data = (new JsonParser).parse(in.lines.collect(Collectors.joining)).getAsJsonObject
      in.close()
      val latest = DatabaseVersion.parseVersion((if (data.has("data")) data.get("data").getAsJsonObject else data).get("version").getAsString)
      if (latest.needsUpdate(SettingsDialog.settings.inventory.version, freq)) {
        if (JOptionPane.showConfirmDialog(
          this,
          s"""|Inventory is out of date:
              |$Bullet Current version: ${SettingsDialog.settings.inventory.version}
              |$Bullet Latest version: $latest
              |
              |Download update?""".stripMargin,
          "Update",
          JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION) (latest, UpdateNeeded) else (latest, UpdateCancelled)
      } else {
        (SettingsDialog.settings.inventory.version, NoUpdate)
      }
    } else {
      (SettingsDialog.settings.inventory.version, NoUpdate)
    }
  } catch {
    case e: IOException =>
      JOptionPane.showMessageDialog(this, s"Error connecting to server: ${e.getMessage}.", "Connection Error", JOptionPane.ERROR_MESSAGE)
      (SettingsDialog.settings.inventory.version, NoUpdate)
    case e: ParseException =>
      JOptionPane.showMessageDialog(this, s"Could not parse version \"${e.getMessage}\"", "Error", JOptionPane.ERROR_MESSAGE)
      (SettingsDialog.settings.inventory.version, NoUpdate)
  }

  /**
   * Attempt to close the specified frame.
   *
   * @param frame frame to close
   * @return true if the frame was closed, and false otherwise.
   */
  def close(frame: EditorFrame) = {
    if (!editors.contains(frame) || !frame.close()) {
      false
    } else {
      if (frame.hasSelectedCards) {
        selectedTable = None
        selectedList = None
      }
      editors -= frame
      if (editors.size > 0)
        selectFrame(editors(0))
      else {
        selectedFrame = None
        deckMenu.setEnabled(false)
      }
      revalidate()
      repaint()
      System.gc()
      true
    }
  }

  /**
   * Attempt to close all of the open editors.  If any can't be closed for whatever reason, they will remain open, but the rest will still be closed.
   *
   * @return true if all open editors were successfully closed, and false otherwise.
   */
  def closeAll() = Seq(editors.toArray:_*).foldLeft(true)(_ & close(_))

  /** Exit the application if all open editors successfully close. */
  def exit() = if (closeAll()) {
    saveSettings()
    setVisible(false)
    dispose()
  }

  /** @return a list containing each currently-selected card in the inventory table */
  def getSelectedCards = selectedList.flatMap((l) => selectedTable.map(t => t.getSelectedRows.map((r) => l(t.convertRowIndexToModel(r))).toSeq)).getOrElse(Seq.empty)

  /** @return the list containing the currently selected cards, or None if there isn't one */
  def getSelectedList = selectedList

  /** @return the table with the selected cards, or None if there isn't one */
  def getSelectedTable = selectedTable

  /**
   * Check whether or not the inventory has a selection.
   * @return true if the inventory has a selection, and false otherwise
   */
  def hasSelectedCards = selectedList.fold(false)(_ == Inventory)

  /**
   * Load the inventory and initialize the inventory table.
   * @see InventoryLoadDialog
   */
  def loadInventory() = {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
    val data = InventoryLoader.loadInventory(this, SettingsDialog.settings.inventory.inventoryFile)
    Inventory.cards = data.inventory
    Expansion.expansions = data.expansions.toArray
    CardAttribute.Supertype.options = data.supertypes.toArray
    CardAttribute.CardType.options = data.types
    CardAttribute.Subtype.options = data.subtypes.toArray
    SettingsDialog.inventoryWarnings = data.warnings

    inventoryModel.list = Inventory
    inventoryModel.columns = SettingsDialog.settings.inventory.columns
    setCursor(Cursor.getDefaultCursor)
    System.gc()
  }

  /**
   * Create a new editor frame.  It will not be visible or selected.
   *
   * @param manager file manager containing the deck to display
   * @see EditorFrame
   */
  def createEditor(manager: DeckSerializer = DeckSerializer()) = {
    untitled += 1
    val frame = EditorFrame(this, untitled, manager)
    editors += frame
    decklistDesktop.add(frame)
    frame
  }

  /**
   * Open the file chooser to select a file, and if a file was selected, parse it and initialize a [[Deck]] from it.
   * @return the [[EditorFrame]] containing the opened deck, or None if one wasn't opened
   */
  def open(): Option[EditorFrame] = fileChooser.showOpenDialog(this) match {
    case JFileChooser.APPROVE_OPTION =>
      val frame = open(fileChooser.getSelectedFile)
      frame.foreach(_ => updateRecents(fileChooser.getSelectedFile))
      frame
    case JFileChooser.CANCEL_OPTION => None
    case JFileChooser.ERROR_OPTION => None
    case _ => None
  }

  /**
   * Open the specified file and create an editor for it.
   * @return the EditorFrame containing the opened deck, or None if opening was canceled.
   */
  def open(f: File) = editors.find(_.file.contains(f)).orElse{
    val frame = try {
      Some(createEditor(DeckSerializer.load(f, this)))
    } catch {
      case e: CancellationException => None
      case e: DeckLoadException =>
        e.printStackTrace
        JOptionPane.showMessageDialog(this, s"Error opening ${f.getName}: ${e.getMessage}.", "Error", JOptionPane.ERROR_MESSAGE)
        Some(createEditor())
    } finally {
      System.gc()
    }
    frame.foreach(e => {
      SettingsDialog.settings = SettingsDialog.settings.copy(cwd = f.getParent)
      fileChooser.setCurrentDirectory(f.getParentFile())
      selectFrame(e)
    })
    frame
  }

  /**
   * If specified editor frame has a file associated with it, save it to that file.  Otherwise, open the file dialog and save it
   * to whatever is chosen (save as).
   *
   * @param frame [[EditorFrame]] containing the deck to save
   */
  def save(frame: EditorFrame) = if (!frame.save()) saveAs(frame)

  /**
   * Attempt to [[save]] all open editors.  For each that needs a file, ask for a file to save to.
   */
  def saveAll() = editors.foreach(save(_))

  /**
   * Save the specified editor frame to a file chosen from a [[JFileChooser]].
   * @param frame frame to save
   */
  def saveAs(frame: EditorFrame) = {
    fileChooser.showSaveDialog(this) match {
      case JFileChooser.APPROVE_OPTION =>
        val f = fileChooser.getSelectedFile
        frame.save(f)
        updateRecents(f)
      case JFileChooser.CANCEL_OPTION =>
      case JFileChooser.ERROR_OPTION =>
        JOptionPane.showMessageDialog(this, s"Could not save ${frame.deck.name}.", "Error", JOptionPane.ERROR_MESSAGE)
    }
    SettingsDialog.settings = SettingsDialog.settings.copy(cwd = fileChooser.getCurrentDirectory.getPath)
  }

  /** Write the latest values of the settings to the settings file. */
  def saveSettings() = {
    SettingsDialog.settings = SettingsDialog.settings.copy(editor = SettingsDialog.settings.editor.copy(recents = SettingsDialog.settings.editor.recents.copy(files = recentItems.map(recents(_).getPath).toSeq)))
    val out = FileOutputStream(SettingsDialog.PropertiesFile.toString)
    try {
      SettingsDialog.save()
    } catch  {
      case e: IOException => JOptionPane.showMessageDialog(this, s"Error writing ${SettingsDialog.PropertiesFile}: ${e.getMessage}.", "Error", JOptionPane.ERROR_MESSAGE)
    }
  }

  /**
   * Set the currently-active frame.  This is the one that will be operated on when single-deck actions are taken from the main frame, such as saving
   * and closing.
   *
   * @param frame [[EditorFrame]] to operate on from now on
   */
  def selectFrame(frame: EditorFrame) = try {
    frame.setSelected(true)
    frame.setVisible(true)
    deckMenu.setEnabled(true)
    selectedFrame = Some(frame)
    revalidate()
    repaint()
  } catch {
    case e: PropertyVetoException =>
  }

  /**
   * Set the background color of the editor panels containing sample hands.
   * @param col new color for sample hand panels
   */
  def setHandBackground(col: Color) = editors.foreach(_.setHandBackground(col))

  /** Sets that there is no selected list, clearing the selection of the currently-selected table if there is one. */
  def clearSelectedList() = {
    selectedTable.foreach(_.clearSelection)
    selectedList = None
  }

  /**
   * Set the selected table and backing list.  Make sure they belong to the same editor or are the inventory.
   * 
   * @param table table that contains the selection
   * @param list list backing that table
   */
  def setSelectedComponents(table: CardTable, list: CardList) = {
    selectedList = Some(list)
    selectedTable = Some(table)
    if (table != inventoryTable)
      inventoryTable.clearSelection()
    editors.foreach(_.clearTableSelections(table))
  }

  /**
   * Set the card to display in the image panel, along with its information in the other tabs.
   * @param card card to display
   */
  def setDisplayedCard(card: Card) = {
    clearSelectedCard()

    val oracleDocument = oracleTextPane.getDocument.asInstanceOf[StyledDocument]
    val oracleTextStyle = oracleDocument.addStyle("text", null)
    StyleConstants.setFontFamily(oracleTextStyle, UIManager.getFont("Label.font").getFamily)
    StyleConstants.setFontSize(oracleTextStyle, ComponentUtils.TextSize)
    var reminderStyle = oracleDocument.addStyle("reminder", oracleTextStyle)
    StyleConstants.setItalic(reminderStyle, true)
    card.formatDocument(oracleDocument, false)
    oracleTextPane.setCaretPosition(0)

    val printedDocument = printedTextPane.getDocument.asInstanceOf[StyledDocument]
    val printedTextStyle = printedDocument.addStyle("text", null)
    StyleConstants.setFontFamily(printedTextStyle, UIManager.getFont("Label.font").getFamily())
    StyleConstants.setFontSize(printedTextStyle, ComponentUtils.TextSize)
    reminderStyle = printedDocument.addStyle("reminder", oracleTextStyle)
    StyleConstants.setItalic(reminderStyle, true)
    card.formatDocument(printedDocument, true)
    printedTextPane.setCaretPosition(0)

    val format = SimpleDateFormat("yyyy-MM-dd")
    val rulingsDocument = rulingsPane.getDocument.asInstanceOf[StyledDocument]
    val rulingStyle = oracleDocument.addStyle("ruling", null)
    StyleConstants.setFontFamily(rulingStyle, UIManager.getFont("Label.font").getFamily())
    StyleConstants.setFontSize(rulingStyle, ComponentUtils.TextSize)
    val dateStyle = rulingsDocument.addStyle("date", rulingStyle)
    StyleConstants.setBold(dateStyle, true)
    if (!card.rulings.isEmpty) {
      try {
        for ((date, rulings) <- card.rulings) {
          for (ruling <- rulings) {
            rulingsDocument.insertString(rulingsDocument.getLength, s"$Bullet ", rulingStyle)
            rulingsDocument.insertString(rulingsDocument.getLength, format.format(date), dateStyle)
            rulingsDocument.insertString(rulingsDocument.getLength, ": ", rulingStyle)
            var start = 0
            for (i <- 0 until ruling.length) {
              ruling(i) match {
                case '{' =>
                  rulingsDocument.insertString(rulingsDocument.getLength(), ruling.substring(start, i), rulingStyle)
                  start = i + 1
                case '}'=>
                  mtg.Symbol.parse(ruling.substring(start, i)) match {
                    case Some(symbol) =>
                      val symbolStyle = rulingsDocument.addStyle(symbol.toString, null)
                      StyleConstants.setIcon(symbolStyle, symbol.scaled(ComponentUtils.TextSize))
                      rulingsDocument.insertString(rulingsDocument.getLength(), " ", symbolStyle)
                    case None =>
                      System.err.println(s"Unexpected symbol {${ruling.substring(start, i)}} in ruling for ${card.name}.")
                      rulingsDocument.insertString(rulingsDocument.getLength, ruling.substring(start, i), rulingStyle)
                  }
                  start = i + 1
                case _ =>
              }
              if (i == ruling.length - 1 && ruling(i) != '}')
                rulingsDocument.insertString(rulingsDocument.getLength, s"${ruling.substring(start, i + 1)}\n", rulingStyle)
            }
          }
        }
      } catch {
        case e: BadLocationException => e.printStackTrace
      }
    }
    rulingsPane.setCaretPosition(0)
    imagePanel.setCard(card)
  }

  /** Clear the card display panel. */
  def clearSelectedCard() = {
    oracleTextPane.setText("")
    printedTextPane.setText("")
    rulingsPane.setText("")
    imagePanel.clearCard()
  }

  /**
   * Set the background color of the panel containing the card image.
   * @param col new color for the card image panel
   */
  def setImageBackground(col: Color) = imagePanel.setBackground(col)

  /** Update the inventory table to bold the cards that are in the currently-selected editor. */
  def updateCardsInDeck() = inventoryTable.repaint()

  /**
   * Download the latest list of cards from the inventory site (default mtgjson.com).  If the download is taking a while, a progress bar will appear.
   * @return true if the download was successful, and false otherwise.
   */
  def updateInventory() = try {
    InventoryDownloader.downloadInventory(this, SettingsDialog.settings.inventory.url, SettingsDialog.settings.inventory.inventoryFile)
  } catch {
    case e: IOException =>
      JOptionPane.showMessageDialog(this, "Error connecting to inventory site: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE)
      false
  }

  /**
   * Update the recently-opened files to add the most recently-opened one, and delete the oldest one if too many are there.
   * @param f [[File]] to add to the list
   */
  def updateRecents(f: File) = if (!recents.exists(_._2 == f)) {
    recentsMenu.setEnabled(true)
    if (recentItems.size >= SettingsDialog.settings.editor.recents.count){
      val eldest = recentItems.dequeue
      recents.remove(eldest)
      recentsMenu.remove(eldest)
    }
    val mostRecent = JMenuItem(f.getPath)
    recentItems.enqueue(mostRecent)
    recents.put(mostRecent, f)
    mostRecent.addActionListener(_ => open(f))
    recentsMenu.add(mostRecent)
  }
}