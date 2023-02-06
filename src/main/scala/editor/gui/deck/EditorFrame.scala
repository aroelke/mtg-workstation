package editor.gui.deck

import editor.collection.CardList
import editor.collection.CardListEntry
import editor.collection.Categorization
import editor.collection.format.CardListFormat
import editor.collection.mutable.Deck
import editor.collection.mutable.Hand
import editor.database.attributes.ManaType
import editor.database.card.Card
import editor.gui.CardTagPanel
import editor.gui.MainFrame
import editor.gui.TableSelectionListener
import editor.gui.ccp.CCPItems
import editor.gui.ccp.data.CategoryTransferData
import editor.gui.ccp.data.DataFlavors
import editor.gui.ccp.handler.CategoryTransferHandler
import editor.gui.ccp.handler.EditorFrameTransferHandler
import editor.gui.ccp.handler.EditorTableTransferHandler
import editor.gui.display.CardImagePanel
import editor.gui.display.CardTable
import editor.gui.display.CardTableModel
import editor.gui.generic.CardMenuItems
import editor.gui.generic.ChangeTitleListener
import editor.gui.generic.ComponentUtils
import editor.gui.generic.EditablePanel
import editor.gui.generic.ScrollablePanel
import editor.gui.generic.TableMouseAdapter
import editor.gui.generic.VerticalButtonList
import editor.gui.settings.Settings
import editor.gui.settings.SettingsDialog
import editor.gui.settings.SettingsObserver
import editor.stats
import editor.unicode.{_, given}
import editor.util.MouseListenerFactory
import editor.util.PopupMenuListenerFactory
import editor.util.StringUtils
import editor.util.UndoableAction
import editor.util.extensions._
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.labels.StandardCategoryToolTipGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.chart.renderer.category.StackedBarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.data.category.DefaultCategoryDataset

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.Paint
import java.awt.PaintContext
import java.awt.PopupMenu
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.ScrollPane
import java.awt.Toolkit
import java.awt.Transparency
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.ColorModel
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.Comparator
import java.util.Date
import java.util.NoSuchElementException
import java.util.stream.Collectors
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.DropMode
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JColorChooser
import javax.swing.JComboBox
import javax.swing.JInternalFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.JTextArea
import javax.swing.ScrollPaneConstants
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.WindowConstants
import javax.swing.border.EtchedBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.InternalFrameAdapter
import javax.swing.event.InternalFrameEvent
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import javax.swing.table.AbstractTableModel
import scala.collection.immutable.ListMap
import scala.util.Using

object EditorFrame {
  val MainDeck = 0
}

class EditorFrame(parent: MainFrame, u: Int, manager: DesignSerializer = DesignSerializer()) extends JInternalFrame(manager.file.map(_.getName).getOrElse(s"Untitled $u"), true, true, true, true)
  with SettingsObserver
{
  import EditorFrame._

  setBounds(((u - 1) % 5)*30, ((u - 1) % 5)*30, 600, 600)
  setLayout(BorderLayout(0, 0))
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  /****************************
   * PERFORM/UNDO/REDO ACTIONS
   ****************************/

  private val undoBuffer = collection.mutable.Stack[UndoableAction[Boolean, Boolean]]()
  private val redoBuffer = collection.mutable.Stack[UndoableAction[Boolean, Boolean]]()

  /**
   * Peform an action that can be undone.
   * 
   * @param action action to perform and its inverse
   * @return true if the action was successful, and false otherwise
   */
  private def performAction(action: UndoableAction[Boolean, Boolean]): Boolean = {
    redoBuffer.clear()
    undoBuffer.push(action)
    action.redo()
  }

  /**
   * Peform an action that can be undone.
   * 
   * @param redo action to perform; this gets performed upon calling this method and stored for later in case it needs to be redone
   * @param undo action to perform to undo the original
   * @return true if the action was successful, and false otherwise
   */
  private def performAction(redo: () => Boolean, undo: () => Boolean): Boolean = performAction(UndoableAction(() => {
    val done = redo()
    unsaved = true
    update()
    done
  }, () => {
    val done = undo()
    unsaved = true
    update()
    done
  }))

  /**
   * Redo the last action that was undone, assuming nothing was done between then and now.
   * @return true if an action was redone, and false otherwise
   */
  @throws[RuntimeException]("if the redone action failed")
  def redo() = if (!redoBuffer.isEmpty) {
    val action = redoBuffer.pop()
    if (action.redo()) {
      undoBuffer.push(action)
      true
    } else throw RuntimeException("error redoing action")
  } else false

  /**
   * Undo the last action that was performed on the deck.
   * @return true if an action was undone, and false otherwise
   */
  @throws[RuntimeException]("if the undone action failed")
  def undo() = if (!undoBuffer.isEmpty) {
    var action = undoBuffer.pop()
    if (action.undo()) {
      redoBuffer.push(action)
      true
    } else throw RuntimeException("error undoing action")
  } else false

  /********************
   * DECK MANIPULATION
   ********************/
  private object DeckData {
    def apply(id: Int, name: String, deck: Deck = Deck()): DeckData = {
      val original = Deck()
      original.addAll(deck)
      DeckData(id, name, deck, original)
    }
  }

  case class DeckData private[EditorFrame](
    private[EditorFrame] val id: Int,
    private var _name: String,
    private[EditorFrame] val current: Deck,
    private[EditorFrame] val original: Deck
  ) extends editor.collection.mutable.CardList {
    def name = _name
    private[EditorFrame] def name_=(n: String) = _name = n

    private def preserveTables[T](action: => T) = {
      val selected = parent.getSelectedCards.map(_.card)
      val result = action
      updateTables(selected)
      result
    }

    class Entry private[DeckData](override val card: Card) extends CardListEntry {
      private def entry = _lists(id).map(_.current(card)).get

      def count_=(n: Int) = {
        val old = entry.count
        performAction(
          () => preserveTables{ try { entry.count = n; true } catch case _: NoSuchElementException => false },
          () => preserveTables{ try { entry.count = old; true } catch case _: NoSuchElementException => false }
        )
      }

      override def count: Int = entry.count
      override def dateAdded: LocalDate = entry.dateAdded
      override def categories: Set[Categorization] = entry.categories
    }

    override def addOne(card: CardListEntry) = {
      if (card.count > 0) {
        performAction(
          () => preserveTables{ _lists(id).map(_.current += card).isDefined },
          () => preserveTables{ _lists(id).map(_.current -= card).isDefined }
        )
      }
      this
    }

    override def addAll(cards: IterableOnce[CardListEntry]) = {
      val added = cards.filter(_.count > 0).toSeq
      if (!added.isEmpty) {
        performAction(
          () => preserveTables{ _lists(id).map(_.current ++= added).isDefined },
          () => preserveTables{ _lists(id).map(_.current --= added).isDefined }
        )
      }
      this
    }

    override def subtractOne(card: CardListEntry) = {
      val capped = card.copy(count = math.min(card.count, current.find(_.card == card.card).map(_.count).getOrElse(0)))
      if (capped.count > 0) {
        performAction(
          () => preserveTables{ _lists(id).map(_.current -= capped).isDefined },
          () => preserveTables{ _lists(id).map(_.current += capped).isDefined }
        )
      }
      this
    }

    override def subtractAll(cards: IterableOnce[CardListEntry]) = {
      val capped = cards.map((e) => e.copy(count = math.min(e.count, current.find(_.card == e.card).map(_.count).getOrElse(0)))).filter(_.count > 0).toSeq
      if (!capped.isEmpty) {
        performAction(
          () => preserveTables{ _lists(id).map(_.current --= capped).isDefined },
          () => preserveTables{ _lists(id).map(_.current ++= capped).isDefined }
        )
      }
      this
    }

    override def update(index: Int, card: CardListEntry) = if (card != current(index)) {
      val orig = current(index)
      performAction(
        () => preserveTables{ _lists(id).map(_.current(index) = card).isDefined },
        () => preserveTables{ _lists(id).map(_.current(index) = orig).isDefined }
      )
    }

    override def clear() = {
      val cleared = Deck()
      cleared ++= current
      cleared.categories ++= current.categories.map(_.categorization)
      performAction(() => preserveTables{ current.clear(); true }, () => preserveTables{
        current ++= cleared
        current.categories ++= cleared.categories.map(_.categorization)
        true
      })
    }

    def move(moves: IterableOnce[(Card, Int)])(target: DeckData) = {
      val tid = target.id
      val capped = moves.flatMap{ case (card, count) => current.find(_.card == card).map((e) => CardListEntry(
        card,
        math.min(count, e.count),
        e.dateAdded
      )) }.filter(_.count > 0).toSeq
      if (!capped.isEmpty) {
        performAction(() => preserveTables{
          _lists(id).foreach(_.current --= capped)
          _lists(tid).foreach(_.current ++= capped)
          true
        }, () => preserveTables{
          _lists(tid).foreach(_.current --= capped)
          _lists(id).foreach(_.current ++= capped)
          true
        })
      }
    }

    override def apply(index: Int): Entry = Entry(current(index).card)
    override def length = current.length
    override def total = current.total

    def changes = {
      val changes: StringBuilder = StringBuilder()
      original.foreach((e) => {
        val has = current.find(_.card == e.card).map(_.count).getOrElse(0)
        if (has < e.count)
          changes ++= s"-${e.count - has}x ${e.card.name} (${e.card.expansion.name})\n"
      })
      current.foreach((e) => {
        val had = original.find(_.card == e.card).map(_.count).getOrElse(0)
        if (had < e.count)
          changes ++= s"+${e.count - had}x ${e.card.name} (${e.card.expansion.name})\n"
      })
      changes.result
    }

    private[EditorFrame] lazy val model = CardTableModel(this, SettingsDialog.settings.editor.columns, EditorFrame.this)
    private[EditorFrame] lazy val table = {
      val table = CardTable(model)
      table.stripe = SettingsDialog.settings.editor.stripe
      // When a card is selected in a table, mark it for adding
      val listener = TableSelectionListener(parent, table, current)
      table.addMouseListener(listener)
      table.getSelectionModel.addListSelectionListener(listener)
      // Create cell editors for applicable table columns
      for (i <- 0 until table.getColumnCount)
        model.columns(i).cellEditor(EditorFrame.this).foreach(table.getColumn(model.getColumnName(i)).setCellEditor)
      // Set up drag-and-drop for the table
      table.setTransferHandler(EditorTableTransferHandler(EditorFrame.this, id))
      table.setDragEnabled(true)
      table.setDropMode(DropMode.ON)

      table
    }

    private[EditorFrame] lazy val ccp = CCPItems(table, true)
    private[EditorFrame] lazy val cards = CardMenuItems(Some(EditorFrame.this), parent.getSelectedCards.map(_.card), id == MainDeck)
    private[EditorFrame] lazy val editTags = {
      val item = JMenuItem("Edit Tags...")
      item.addActionListener(_ => CardTagPanel.editTags(parent.getSelectedCards.map(_.card), parent))
      item
    }
    private[EditorFrame] def setMenuEnables = {
      ccp.cut.setEnabled(!parent.getSelectedCards.isEmpty)
      ccp.copy.setEnabled(!parent.getSelectedCards.isEmpty)
      val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
      ccp.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor))
      cards.setEnabled(!parent.getSelectedCards.isEmpty)
      editTags.setEnabled(!parent.getSelectedCards.isEmpty)
    }
    private[EditorFrame] lazy val popup = {
      val menu = JPopupMenu()
      table.addMouseListener(TableMouseAdapter(table, menu))
      menu.add(ccp.cut)
      menu.add(ccp.copy)
      menu.add(ccp.paste)
      menu.add(JSeparator())
      cards.addAddItems(menu)
      menu.add(JSeparator())
      cards.addRemoveItems(menu)
      menu.add(JSeparator())
      menu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => setMenuEnables))
      menu
    }
  }

  // Actual lists of DeckData. Index 0 will always be defined and will contain the main deck
  private val _lists = collection.mutable.ArrayBuffer[Option[DeckData]]()
  _lists += Some(DeckData(id = MainDeck, name = getTitle, deck = manager.deck))

  /** @return a mapping of list IDs onto lists in the deck */
  def lists = _lists.zipWithIndex.collect{ case (Some(l), i) => i -> l }.toMap

  /**
   * @param name name of the list to get
   * @return the list with the given name
   * @note this method is meant to resemble an apply(String) method added to the mapping of Int -> CardList to search by name rather than index
   */
  @throws[NoSuchElementException]("if there is no list with the given name")
  def lists(name: String) = _lists.flatten.find(_.name == name).getOrElse(throw NoSuchElementException(name))
  
  /** @return the main deck data */
  def deck = _lists.head.get
  private def deck_=(d: DeckData) = _lists(MainDeck) = Some(d)

  /** @return the extra lists */
  def extras = _lists.tail.flatten.toSeq

  private var _sideboard: Option[DeckData] = None
  /** @return the extra list currently displayed by the bottom pane, or an empty list if there isn't one */
  def sideboard = _sideboard.getOrElse(DeckData(-1, ""))
  private def sideboard_=(d: Option[DeckData]) = _sideboard = d

  /** @return a [[CardList]] containing all of the cards in extra lists */
  def allExtras: CardList = {
    val sideboard = Deck()
    extras.foreach(e => sideboard.addAll(e.current))
    sideboard
  }

  /**
   * Auxiliary object for controlling the categories of the deck.
   * @author Alec Roelke
   */
  object categories extends Iterable[Categorization] {
    /**
     * Add a new category to the main deck.
     * 
     * @param spec specification for the new category
     * @return true if adding the category was successful, and false otherwise
     */
    def +=(spec: Categorization) = if (contains(spec.name)) false else {
      performAction(() => {
        if (contains(spec.name))
          throw RuntimeException(s"attempting to add duplicate category ${spec.name}")
        else
          do_addCategory(spec)
      }, () => do_removeCategory(spec))
    }

    /**
     * Remove a category from the deck.
     * 
     * @param name name of the category to remove
     * @return an [[Option]] containing the category that was removed, or None if nothing was removed
     */
    def -=(name: String) = Option.when(contains(name)){
      val spec = deck.current.categories(name).categorization
      performAction(() => do_removeCategory(spec), () => {
        if (contains(name))
          throw RuntimeException(s"duplicate category $name found when attempting to undo removal")
        else
          do_addCategory(spec)
      })
      spec
    }

    /**
     * Change a category in the deck, updating UI elements as necessary.
     * 
     * @param name name of the category to change
     * @param spec new specification for the category (is allowed to have a different name)
     * @return the old category specification, even if no change were made
     */
    @throws[NoSuchElementException]("if no category with the given name exists")
    def update(name: String, spec: Categorization): Option[Categorization] = update(Map(name -> spec)).get(name)

    /**
     * Change several categories in the deck at once, updating UI elements as necessary. All changes
     * are made in the same action, so all will be undone/redone with one command.
     * 
     * @param specs mapping of old category names onto their new specifications (which can have new names)
     * @return a mapping of the old category names onto their old specifications before the change
     */
    @throws[NoSuchElementException]("if any names don't have corresponding categories in the deck")
    def update(specs: Map[String, Categorization]): Map[String, Categorization] = if (specs.forall{ case (name, _) => contains(name) }) {
      val changes = specs.filter{ case (name, spec) => apply(name) != spec }
      val old = specs.map{ case (name, _) => name -> apply(name) }
      if (!changes.isEmpty) {
        performAction(() => {
          changes.foreach{ case (name, spec) => deck.current.categories(name) = spec }
          for (panel <- categoryPanels) {
            if (changes.contains(panel.name)) {
              panel.name = changes(panel.name).name
              panel.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged()
            }
          }
          updateCategoryPanel()
          true
        }, () => {
          changes.foreach{ case (name, spec) => deck.current.categories(spec.name) = old(name) }
          for (panel <- categoryPanels) {
            if (changes.map{ case (_, spec) => spec.name }.toSet.contains(panel.name)) {
              specs.find{ case (_, spec) => spec.name == panel.name }.foreach{ case (name, _) => panel.name = name }
              panel.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged()
            }
          }
          updateCategoryPanel()
          true
        })
      }
      old
    } else throw NoSuchElementException(specs.filter{ case (name, _) => !contains(name) }.mkString(","))

    /**
     * @param name name of the category to get
     * @return the specification for the chosen category
     */
    @throws[IllegalArgumentException]("if no category with that name exists")
    def apply(name: String) = deck.current.categories(name).categorization

    /**
     * @param name name of the category to look for
     * @return an [[Option]] containing the category with the given name, or None if there isn't one
     */
    def get(name: String) = Option.when(deck.current.categories.contains(name))(deck.current.categories(name).categorization)

    /**
     * @param name name of the category to check
     * @return true if the deck contains a category with the given name, and false otherwise
     */
    def contains(name: String) = deck.current.categories.contains(name)

    def iterator = deck.current.categories.map(_.categorization).iterator
  }

  /*****************
   * GUI DEFINITION
   *****************/

  private class TableCategoriesPopupListener(addToCategoryMenu: JMenu, removeFromCategoryMenu: JMenu, editCategoriesItem: JMenuItem, menuSeparator: JSeparator, table: CardTable) extends PopupMenuListener {
    override def popupMenuCanceled(e: PopupMenuEvent) = ()
    override def popupMenuWillBecomeInvisible(e: PopupMenuEvent) = {
      addToCategoryMenu.removeAll()
      removeFromCategoryMenu.removeAll()
    }
    override def popupMenuWillBecomeVisible(e: PopupMenuEvent) = {
      if (parent.getSelectedTable.exists(_ == table)) {
        if (parent.getSelectedCards.size == 1) {
          val card = parent.getSelectedCards(0)

          for (category <- deck.current.categories.map(_.categorization)) {
            if (!category(card.card)) {
              val categoryItem = JMenuItem(category.name)
              categoryItem.addActionListener(_ => categories(category.name) = category + card.card)
              addToCategoryMenu.add(categoryItem)
            }
          }
          addToCategoryMenu.setVisible(addToCategoryMenu.getItemCount > 0)

          for (category <- deck.current.categories.map(_.categorization)) {
            if (category(card.card)) {
              val categoryItem = JMenuItem(category.name)
              categoryItem.addActionListener(_ => categories(category.name) = category - card.card)
              removeFromCategoryMenu.add(categoryItem)
            }
          }
          removeFromCategoryMenu.setVisible(removeFromCategoryMenu.getItemCount > 0)
        } else {
          addToCategoryMenu.setVisible(false)
          removeFromCategoryMenu.setVisible(false)
        }

        editCategoriesItem.setVisible(!parent.getSelectedCards.isEmpty && !deck.current.categories.isEmpty)
        menuSeparator.setVisible(addToCategoryMenu.isVisible || removeFromCategoryMenu.isVisible || editCategoriesItem.isVisible)
      }
    }
  }

  private var _file: Option[File] = None

  /** @return the [[File]] to save the deck to */
  def file = _file

  /**
   * Change the file to save to. There must not be unsaved changes. Also updates the frame's title to reflect the new file name.
   * @param f new file to save to
   */
  @throws[RuntimeException]("if there are unsaved changes to the existing file")
  def file_=(f: Option[File]): Unit = {
    if (!f.isDefined)
      throw IllegalArgumentException("can't unset the file of a deck")
    if (unsaved)
      throw RuntimeException("can't change the file of an unsaved deck")
    _file = f
    deck.name = f.get.getName
    setTitle(deck.name)
  }

  def file_=(f: File): Unit = { file = Some(f) }

  private var _unsaved = false

  /** @return true if there are unsaved changes and false otherwise */
  def unsaved = _unsaved

  /**
   * Set whether or not there are unsaved changes. If there are, add an asterisk after the frame title.
   * @param u whether or not there are unsaved changes
   */
  private def unsaved_=(u: Boolean) = {
    if (u && !unsaved)
      setTitle(s"${deck.name} *")
    _unsaved = u
  }

  if (manager.file.isDefined)
    file = manager.file
  else
    unsaved = true

  private enum EditorTab(val title: String) {
    case MainTable    extends EditorTab("Cards")
    case Categories   extends EditorTab("Categories")
    case ManaAnalysis extends EditorTab("Mana Analysis")
    case SampleHand   extends EditorTab("Sample Hand")
    case Notes        extends EditorTab("Notes")
    case Changelog    extends EditorTab("Change Log")
  }
  import EditorTab._
  private val listTabs = JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
  add(listTabs, BorderLayout.CENTER)

  /* MAIN DECK TAB */
  private val mainPanel = JPanel(BorderLayout())

  private val mainDeckPane = JScrollPane(deck.table)
  mainDeckPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED))
  mainPanel.add(mainDeckPane, BorderLayout.CENTER)

  private val deckButtons = VerticalButtonList(Seq("+", Minus, "X"))
  deckButtons("+").addActionListener(_ => deck ++= parent.getSelectedCards.map((e) => CardListEntry(e.card, 1)))
  deckButtons(Minus).addActionListener(_ => deck --= parent.getSelectedCards.map((e) => CardListEntry(e.card, 1)))
  deckButtons("X").addActionListener(_ => deck --= parent.getSelectedCards.map((e) => CardListEntry(e.card, Int.MaxValue)))
  mainPanel.add(deckButtons, BorderLayout.WEST)

  private val southLayout = CardLayout()
  private val southPanel = JPanel(southLayout)
  mainPanel.add(southPanel, BorderLayout.SOUTH)

  private val extrasPanel = JPanel()
  extrasPanel.setLayout(BorderLayout())
  southPanel.add(extrasPanel, "extras")

  private val extrasButtons = VerticalButtonList(Seq("+", Minus, "X"))
  extrasButtons("+").addActionListener(_ => sideboard ++= parent.getSelectedCards.map((e) => CardListEntry(e.card, 1)))
  extrasButtons(Minus).addActionListener(_ => sideboard --= parent.getSelectedCards.map((e) => CardListEntry(e.card, 1)))
  extrasButtons("X").addActionListener(_ => sideboard --= parent.getSelectedCards.map((e) => CardListEntry(e.card, Int.MaxValue)))
  extrasPanel.add(extrasButtons, BorderLayout.WEST)

  private val extrasPane = JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
  extrasPanel.add(extrasPane, BorderLayout.CENTER)

  private val emptyPanel = JPanel(BorderLayout())
  emptyPanel.setBorder(BorderFactory.createEtchedBorder())
  private val emptyLabel = JLabel("Click to add a sideboard.")
  emptyLabel.setHorizontalAlignment(SwingConstants.CENTER)
  emptyPanel.add(emptyLabel, BorderLayout.CENTER)
  southPanel.add(emptyPanel, "empty")
  southLayout.show(southPanel, "empty")

  listTabs.addTab(MainTable.title, mainPanel)

  /* Main table popup menu */
  // Move cards to sideboard
  private val moveToMenu = JMenu("Move to")
  deck.popup.add(moveToMenu)
  private val moveAllToMenu = JMenu("Move all to")
  deck.popup.add(moveAllToMenu)
  private val moveSeparator = JSeparator()
  deck.popup.add(moveSeparator)

  // Quick edit categories
  private val addToCategoryMenu = JMenu("Include in")
  deck.popup.add(addToCategoryMenu)
  private val removeFromCategoryMenu = JMenu("Exclude from")
  deck.popup.add(removeFromCategoryMenu)

  // Edit categories item
  private val editCategoriesItem = JMenuItem("Edit Categories...")
  editCategoriesItem.addActionListener(_ => {
    val iePanel = IncludeExcludePanel(deck.current.categories.map(_.categorization).toSeq.sortBy(_.name.toLowerCase), parent.getSelectedCards.map(_.card))
    if (JOptionPane.showConfirmDialog(this, JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
      categories.update(iePanel.updates.map((c) => c.name -> c).toMap)
  })
  deck.popup.add(editCategoriesItem)

  private val categoriesSeparator = JSeparator()
  deck.popup.add(categoriesSeparator)

  // Edit card tags item
  deck.popup.add(deck.editTags)

  // Table memu popup listeners
  deck.popup.addPopupMenuListener(TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu, editCategoriesItem, categoriesSeparator, deck.table))
  deck.popup.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
    deck.setMenuEnables
    moveToMenu.setVisible(!extras.isEmpty)
    moveAllToMenu.setVisible(!extras.isEmpty)
    moveSeparator.setVisible(!extras.isEmpty)
    addToCategoryMenu.setEnabled(!categoryPanels.isEmpty)
    removeFromCategoryMenu.setEnabled(!categoryPanels.isEmpty)
    editCategoriesItem.setEnabled(!categoryPanels.isEmpty)

    moveToMenu.removeAll()
    moveAllToMenu.removeAll()
    for (i <- 1 until _lists.size) {
      _lists(i).foreach((l) => {
        val id = i
        val moveToItem = JMenuItem(l.name)
        moveToItem.addActionListener(_ => _lists(id).map(deck.move(parent.getSelectedCards.map(_.card -> 1))(_)).getOrElse(throw NoSuchElementException(id.toString)))
        moveToItem.setEnabled(!parent.getSelectedCards.isEmpty)
        moveToMenu.add(moveToItem)
        val moveAllToItem = JMenuItem(l.name)
        moveAllToItem.addActionListener(_ => _lists(id).map(deck.move(parent.getSelectedCards.map(e => (e.card -> deck.current.find(_.card == e.card).map(_.count).getOrElse(0))))(_)).getOrElse(throw NoSuchElementException(id.toString)))
        moveAllToItem.setEnabled(!parent.getSelectedCards.isEmpty)
        moveAllToMenu.add(moveAllToItem)
      })
    }
    moveToMenu.setEnabled(!parent.getSelectedCards.isEmpty)
    moveAllToMenu.setEnabled(!parent.getSelectedCards.isEmpty)
  }))

  /* CATEGORIES TAB */
  private val categoriesPanel = JPanel(BorderLayout())
  private val categoriesMainPanel = JPanel(BorderLayout())
  categoriesPanel.add(categoriesMainPanel, BorderLayout.CENTER)
  listTabs.addTab(Categories.title, categoriesPanel)

  // Panel containing components above the category panel
  private val categoryHeaderPanel = Box(BoxLayout.X_AXIS)
  categoriesMainPanel.add(categoryHeaderPanel, BorderLayout.NORTH)

  // Button to add a new category
  private val addCategoryPanel = JPanel(FlowLayout(FlowLayout.LEFT))
  private val addCategoryButton = JButton("Add")
  addCategoryButton.addActionListener(_ => createCategory.foreach(categories += _))
  addCategoryPanel.add(addCategoryButton)
  categoryHeaderPanel.add(addCategoryPanel)

  // Combo box to change category sort order
  private enum CategoryOrder(override val toString: String, order: (Deck) => Ordering[Categorization]) {
    def apply(d: Deck) = order(d)

    case AtoZ       extends CategoryOrder("A-Z", (d) => (a, b) => a.name.compare(b.name))
    case ZtoA       extends CategoryOrder("Z-A", (d) => (a, b) => -a.name.compare(b.name))
    case Ascending  extends CategoryOrder("Ascending Size", (d) => (a, b) => d.categories(a.name).list.total.compare(d.categories(b.name).list.total))
    case Descending extends CategoryOrder("Descending Size", (d) => (a, b) => -d.categories(a.name).list.total.compare(d.categories(b.name).list.total))
    case Priority   extends CategoryOrder("Increasing Rank", (d) => (a, b) => d.categories(a.name).rank.compare(d.categories(b.name).rank))
    case Reverse    extends CategoryOrder("Decreasing Rank", (d) => (a, b) => -d.categories(a.name).rank.compare(d.categories(b.name).rank))
  }
  private val sortCategoriesPanel = JPanel(FlowLayout(FlowLayout.CENTER))
  sortCategoriesPanel.add(JLabel("Display order:"))
  private val sortCategoriesBox = JComboBox(CategoryOrder.values)
  sortCategoriesBox.addActionListener(_ => if (sortCategoriesBox.isPopupVisible) {
    updateCategoryPanel()
    update()
  })
  sortCategoriesPanel.add(sortCategoriesBox)
  categoryHeaderPanel.add(sortCategoriesPanel)

  // Combo box to switch to a different category
  private val switchCategoryPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
  private val switchCategoryModel = DefaultComboBoxModel[String]()
  private val switchCategoryBox = JComboBox(switchCategoryModel)
  switchCategoryBox.setEnabled(false)
  switchCategoryBox.addActionListener(_ => if (switchCategoryBox.isPopupVisible) {
    getCategoryPanel(switchCategoryBox.getCurrentItem).foreach((c) => {
      c.scrollRectToVisible(Rectangle(c.getSize()))
      c.flash()
    })
  })
  switchCategoryPanel.add(JLabel("Go to category:"))
  switchCategoryPanel.add(switchCategoryBox)
  categoryHeaderPanel.add(switchCategoryPanel)

  // Make sure all parts of the category panel fit inside the window (this is necessary because
  // JScrollPanes do weird things with non-scroll-savvy components)
  private val categoriesSuperContainer = ScrollablePanel(ScrollablePanel.TrackWidth, BorderLayout())
  private val categoriesContainer = Box(BoxLayout.Y_AXIS)
  private val categoryPanels = collection.mutable.ArrayBuffer[CategoryPanel]()

  // The category panel is a vertically-scrollable panel that contains all categories stacked vertically
  // The categories should have a constant height, but fit the container horizontally
  categoriesSuperContainer.add(categoriesContainer, BorderLayout.NORTH)
  private val categoriesPane = JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  categoriesMainPanel.add(categoriesPane, BorderLayout.CENTER)

  // Transfer handler for the category box
  // We explicitly use null here to cause exceptions if cutting or copying, as that should never happen
  categoriesPane.setTransferHandler(CategoryTransferHandler(null, (c) => categories.contains(c.name), categories += _, null))

  // Popup menu for category container
  private val categoriesMenu = JPopupMenu()
  private val categoriesCCP = CCPItems(categoriesPane, false)
  categoriesCCP.paste.setText("Paste Categorization")
  categoriesMenu.add(categoriesCCP.paste)
  categoriesMenu.add(JSeparator())
  private val categoriesCreateItem = JMenuItem("Add Categorization...")
  categoriesCreateItem.addActionListener(_ => createCategory.foreach(categories += _))
  categoriesMenu.add(categoriesCreateItem)
  categoriesMenu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    try {
      categoriesCCP.paste.setEnabled(!categories.contains((clipboard.getData(DataFlavors.categoryFlavor)).asInstanceOf[CategoryTransferData].spec.name))
    } catch {
      case _ @ (_: UnsupportedFlavorException | _: IOException) => categoriesCCP.paste.setEnabled(false)
    }
  }))
  categoriesPane.setComponentPopupMenu(categoriesMenu)

  private val categoryButtons = VerticalButtonList(Seq("+", Minus, "X"))
  categoryButtons("+").addActionListener(_ => deck ++= parent.getSelectedCards.map((e) => CardListEntry(e.card, 1)))
  categoryButtons(Minus).addActionListener(_ => deck --= parent.getSelectedCards.map((e) => CardListEntry(e.card, 1)))
  categoryButtons("X").addActionListener(_ => deck --= parent.getSelectedCards.map((e) => CardListEntry(e.card, Int.MaxValue)))
  categoriesPanel.add(categoryButtons, BorderLayout.WEST)

  /* MANA ANALYSIS TAB */
  private enum ManaCurveSection(override val toString: String) {
    case ByNothing    extends ManaCurveSection("Nothing")
    case ByColorGroup extends ManaCurveSection("Color Group")
    case ByColors     extends ManaCurveSection("Color(s)")
    case ByType       extends ManaCurveSection("Card Type")    
  }
  private enum LandAnalysisChoice(override val toString: String) {
    case Played      extends LandAnalysisChoice("Expected Lands Played")
    case Drawn       extends LandAnalysisChoice("Expected Lands Drawn")
    case Probability extends LandAnalysisChoice("Probability of Drawing Lands")
  }
  import ManaCurveSection._
  import LandAnalysisChoice._
  private val manaAnalysisPanel = JPanel(BorderLayout())

  // Data set and axis creation
  private val manaCurve = DefaultCategoryDataset()
  private val landDrops = DefaultCategoryDataset()
  private val manaCurveRenderer = StackedBarRenderer()
  manaCurveRenderer.setBarPainter(StandardBarPainter())
  manaCurveRenderer.setDefaultToolTipGenerator(StandardCategoryToolTipGenerator("{0}: {2}", DecimalFormat()))
  manaCurveRenderer.setDrawBarOutline(true)
  manaCurveRenderer.setDefaultOutlinePaint(Color.BLACK)
  manaCurveRenderer.setShadowVisible(false)
  private val landRenderer = LineAndShapeRenderer()
  landRenderer.setDefaultItemLabelGenerator(StandardCategoryItemLabelGenerator())
  landRenderer.setDefaultItemLabelsVisible(true)
  landRenderer.setSeriesPaint(0, SettingsDialog.settings.editor.manaAnalysis.line)
  private val manaValueAxis = CategoryAxis("Mana Value/Turn")
  private val frequencyAxis = NumberAxis("Mana Value Frequency")
  private val landAxis = NumberAxis("Expected Land Plays")

  // Plot creation
  private val manaCurvePlot = CategoryPlot()
  manaCurvePlot.setDataset(0, manaCurve)
  manaCurvePlot.setDataset(1, landDrops)
  manaCurvePlot.setRenderers(Array(manaCurveRenderer, landRenderer))
  manaCurvePlot.setDomainAxis(manaValueAxis)
  manaCurvePlot.setRangeAxes(Array(frequencyAxis, landAxis))
  manaCurvePlot.mapDatasetToRangeAxis(0, 0)
  manaCurvePlot.mapDatasetToRangeAxis(1, 1)
  manaCurvePlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD)
  manaCurvePlot.setRangeGridlinesVisible(false)
  private val manaCurveChart = JFreeChart("Mana Curve", JFreeChart.DEFAULT_TITLE_FONT, manaCurvePlot, true)
  private val manaCurvePanel = ChartPanel(manaCurveChart)
  manaCurvePanel.setPopupMenu(null)
  manaAnalysisPanel.add(manaCurvePanel, BorderLayout.CENTER)

  // Analysis settings panel (how to divide bar graph and what land analysis to show)
  private val analysisConfigPanel = JPanel(BorderLayout())

  private val categoryAnalysisPanel = Box(BoxLayout.X_AXIS)
  categoryAnalysisPanel.setBorder(BorderFactory.createTitledBorder("Mana Analysis"))
  categoryAnalysisPanel.add(Box.createHorizontalGlue)
  private val sectionsBox = JComboBox(ManaCurveSection.values)
  sectionsBox.addActionListener(_ => updateStats())
  sectionsBox.setMaximumSize(sectionsBox.getPreferredSize)
  categoryAnalysisPanel.add(JLabel("Divide bars by:"))
  categoryAnalysisPanel.add(Box.createHorizontalStrut(2))
  categoryAnalysisPanel.add(sectionsBox)
  categoryAnalysisPanel.add(Box.createHorizontalStrut(15))
  private val analyzeCategoryBox = JCheckBox("Analyze category:", false)
  analyzeCategoryBox.addActionListener(_ => {
    analyzeCategoryCombo.setEnabled(analyzeCategoryBox.isSelected)
    updateStats()
  })
  categoryAnalysisPanel.add(analyzeCategoryBox)
  private val analyzeCategoryCombo = JComboBox[String]()
  analyzeCategoryCombo.setEnabled(false)
  analyzeCategoryCombo.addActionListener(_ => if (analyzeCategoryCombo.getItemCount > 0) updateStats())
  categoryAnalysisPanel.add(analyzeCategoryCombo)
  categoryAnalysisPanel.add(Box.createHorizontalGlue)
  analysisConfigPanel.add(categoryAnalysisPanel, BorderLayout.NORTH)

  private val landAnalysisPanel = Box(BoxLayout.X_AXIS)
  landAnalysisPanel.setBorder(BorderFactory.createTitledBorder("Land Analysis"))
  landAnalysisPanel.add(Box.createHorizontalGlue)
  landAnalysisPanel.add(JLabel("Show:"))
  landAnalysisPanel.add(Box.createHorizontalStrut(2))
  private val landsBox = JComboBox(LandAnalysisChoice.values)
  landsBox.setMaximumSize(landsBox.getPreferredSize)
  landsBox.addActionListener(_ => updateStats())
  landAnalysisPanel.add(landsBox)
  landAnalysisPanel.add(Box.createHorizontalGlue)
  analysisConfigPanel.add(landAnalysisPanel, BorderLayout.SOUTH)

  manaAnalysisPanel.add(analysisConfigPanel, BorderLayout.SOUTH)

  listTabs.addTab(ManaAnalysis.title, manaAnalysisPanel)

  /* SAMPLE HAND TAB */
  private val handPanel = JPanel(BorderLayout())

  // Table showing the cards in hand
  private val hand = Hand(deck.current)

  private val imagePanel = ScrollablePanel(ScrollablePanel.TrackHeight)
  imagePanel.setLayout(BoxLayout(imagePanel, BoxLayout.X_AXIS))
  private val imagePane = JScrollPane(imagePanel)
  imagePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
  setHandBackground(SettingsDialog.settings.editor.hand.background)

  // Control panel for manipulating the sample hand
  private val handModPanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 5))
  private val newHandButton = JButton("New Hand")
  newHandButton.addActionListener(_ => {
    hand.newHand(SettingsDialog.settings.editor.hand.size)

    imagePanel.removeAll()
    hand.foreach((c) => {
      val panel = CardImagePanel()
      panel.setCard(c)
      panel.setBackground(SettingsDialog.settings.editor.hand.background)
      imagePanel.add(panel)
      imagePanel.add(Box.createHorizontalStrut(10))
    })
    imagePanel.validate()
    update()
  })
  handModPanel.add(newHandButton)
  private val mulliganButton = JButton("Mulligan")
  mulliganButton.addActionListener(_ => {
    hand.mulligan()

    imagePanel.removeAll()
    hand.foreach((c) => {
      val panel = CardImagePanel()
      imagePanel.add(panel)
      panel.setCard(c)
      panel.setBackground(SettingsDialog.settings.editor.hand.background)
      imagePanel.add(Box.createHorizontalStrut(10))
    })
    imagePanel.validate()
    update()
  })
  handModPanel.add(mulliganButton)
  private val drawCardButton = JButton("Draw a Card")
  drawCardButton.addActionListener(_ => {
    if (hand.size < deck.current.total) {
      hand.draw()
      val panel = CardImagePanel()
      panel.setBackground(SettingsDialog.settings.editor.hand.background)
      imagePanel.add(panel)
      panel.setCard(hand.last)
      imagePanel.add(Box.createHorizontalStrut(10))
      imagePanel.validate()
      update()
    }
  })
  handModPanel.add(drawCardButton)
  private val handWidth = Seq(newHandButton.getPreferredSize, mulliganButton.getPreferredSize, drawCardButton.getPreferredSize).map(_.width).max
  newHandButton.setPreferredSize(Dimension(handWidth, newHandButton.getPreferredSize.height))
  mulliganButton.setPreferredSize(Dimension(handWidth, mulliganButton.getPreferredSize.height))
  drawCardButton.setPreferredSize(Dimension(handWidth, drawCardButton.getPreferredSize.height))

  private val handCalculations: CalculateHandPanel = CalculateHandPanel(deck.current, _ => updateStats())

  private val handSplit = JSplitPane(JSplitPane.VERTICAL_SPLIT, imagePane, handCalculations)
  handSplit.setOneTouchExpandable(true)
  handSplit.setContinuousLayout(true)
  SwingUtilities.invokeLater(() => handSplit.setDividerLocation(0.5))
  handSplit.setResizeWeight(0.5)
  handPanel.add(handModPanel, BorderLayout.NORTH)
  handPanel.add(handSplit, BorderLayout.CENTER)
  listTabs.addTab(SampleHand.title, handPanel)
  hand.refresh()

  /* NOTES TAB */
  private val notesArea = JTextArea(manager.notes)
  private val notes = collection.mutable.Stack[String]()
  notes.push(notesArea.getText)
  notesArea.getDocument.addDocumentListener(new DocumentListener {
    private var undoing = false
    private val timer = Timer(500, _ => {
      val text = notesArea.getText
      if (!undoing && text != notes.top) {
        performAction(() => {
          notes.push(text)
          if (notesArea.getText != notes.top) {
            undoing = true
            notesArea.setText(text)
            listTabs.setSelectedIndex(Notes.ordinal)
            undoing = false
          }
          true
        }, () => {
          notes.pop
          undoing = true
          notesArea.setText(notes.top)
          listTabs.setSelectedIndex(Notes.ordinal)
          undoing = false
          true
        })
      }
    })
    timer.setRepeats(false)

    def performNotesAction(text: String) = {
      if (timer.isRunning)
        timer.restart()
      else
        timer.start()
    }

    override def insertUpdate(e: DocumentEvent) = performNotesAction(notesArea.getText)
    override def removeUpdate(e: DocumentEvent) = performNotesAction(notesArea.getText)
    override def changedUpdate(e: DocumentEvent) = performNotesAction(notesArea.getText)
  })
  private val notesCCP = CCPItems(notesArea, true)
  private val notesMenu = JPopupMenu()
  notesMenu.add(notesCCP.cut)
  notesMenu.add(notesCCP.copy)
  notesMenu.add(notesCCP.paste)
  notesMenu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
    val text = Option(notesArea.getSelectedText)
    notesCCP.cut.setEnabled(text.map(!_.isEmpty).getOrElse(false))
    notesCCP.copy.setEnabled(text.map(!_.isEmpty).getOrElse(false))
    notesCCP.paste.setEnabled(Toolkit.getDefaultToolkit.getSystemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
  }))
  notesArea.setComponentPopupMenu(notesMenu)
  listTabs.addTab(Notes.title, JScrollPane(notesArea))

  // Panel to show the stats of the deck
  private val bottomPanel = JPanel(BorderLayout())
  getContentPane.add(bottomPanel, BorderLayout.SOUTH)

  // Labels to counts for total cards, lands, and nonlands
  private val statsPanel = Box(BoxLayout.X_AXIS)
  statsPanel.add(Box.createHorizontalStrut(10))
  private val countLabel = JLabel()
  statsPanel.add(countLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TextSize))
  private val landLabel = JLabel()
  statsPanel.add(landLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TextSize))
  private val nonlandLabel = JLabel()
  statsPanel.add(nonlandLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TextSize))
  private val avgManaValueLabel = JLabel()
  statsPanel.add(avgManaValueLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TextSize))
  private val medManaValueLabel = JLabel()
  statsPanel.add(medManaValueLabel)
  statsPanel.add(Box.createHorizontalGlue())
  updateStats()
  private val statsConstraints = GridBagConstraints()
  statsConstraints.anchor = GridBagConstraints.WEST
  bottomPanel.add(statsPanel, BorderLayout.WEST)

  // Check legality button
  private val legalityPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 10, 5))
  private val legalityButton = JButton("Show Legality")
  legalityButton.addActionListener(_ => JOptionPane.showMessageDialog(this, LegalityPanel(this), s"Legality of ${deck.name}", JOptionPane.PLAIN_MESSAGE))
  legalityPanel.add(legalityButton)
  private val legalityConstraints = GridBagConstraints()
  legalityConstraints.anchor = GridBagConstraints.EAST
  bottomPanel.add(legalityPanel, BorderLayout.EAST)

  /* CHANGELOG TAB */
  private val changelogPanel = JPanel(BorderLayout())
  private val changelogArea = JTextArea(manager.changelog)
  changelogArea.setEditable(false)
  changelogPanel.add(JScrollPane(changelogArea), BorderLayout.CENTER)
  private val clearLogPanel = JPanel(FlowLayout(FlowLayout.CENTER))
  private val clearLogButton = JButton("Clear Change Log")
  clearLogButton.addActionListener(_ => {
    if (!changelogArea.getText.isEmpty && JOptionPane.showConfirmDialog(this, "This change is permanent.  Clear change log?", "Clear Change Log?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      changelogArea.setText("")
      unsaved = true
    }
  })
  clearLogPanel.add(clearLogButton)
  changelogPanel.add(clearLogPanel, BorderLayout.SOUTH)
  listTabs.addTab(Changelog.title, changelogPanel)

  changelogArea.setText(manager.changelog)

  setTransferHandler(EditorFrameTransferHandler(this, MainDeck))

  deck.current.categories.foreach((c) => categoryPanels += createCategoryPanel(c.categorization))
  updateCategoryPanel()
  handCalculations.update()

  // Initialize extra lists
  private val addSideboard = (e: MouseEvent) => {
    val index = if (extrasPane.getTabCount > 1) extrasPane.indexAtLocation(e.getX, e.getY) else 0
    val last = extrasPane.getTabCount - 1
    if (index == last) {
      val id = _lists.size
      performAction(() => createExtra(s"Sideboard $id", id, last), () => deleteExtra(id, last))
    }
  }
  extrasPane.addMouseListener(MouseListenerFactory.createMouseListener(pressed = (e) => addSideboard(e)))
  extrasPane.addChangeListener(_ => sideboard = extras.find(_.name == extrasPane.getTitleAt(extrasPane.getSelectedIndex)))
  emptyPanel.addMouseListener(MouseListenerFactory.createMouseListener(clicked = (e) => addSideboard(e)))

  extrasPane.addTab("+", null)
  for ((name, list) <- manager.sideboards) {
    val id = _lists.size
    createExtra(name, id, extrasPane.getTabCount - 1)
    // Intentionally throw exception here if missing, as it shouldn't be missing
    _lists(id).get.current.addAll(list)
    _lists(id).get.original.addAll(list)
  }
  extrasPane.setSelectedIndex(0)

  // Handle various frame events, including selecting and closing
  addInternalFrameListener(new InternalFrameAdapter {
    override def internalFrameActivated(e: InternalFrameEvent) = parent.selectFrame(EditorFrame.this)
    override def internalFrameClosing(e: InternalFrameEvent) = {
      stopObserving()
      parent.close(EditorFrame.this)
    }
  })

  listTabs.setSelectedIndex(MainDeck)

  startObserving()

  require(EditorTab.values.forall(t => listTabs.getTitleAt(t.ordinal) == t.title))

  override def applySettings(oldSettings: Settings, newSettings: Settings) = {
    applyChanges(oldSettings, newSettings)(_.editor.columns)(deck.model.columns = _)
                                          (_.editor.stripe)(deck.table.stripe = _)
                                          (_.editor.manaAnalysis.line)(landRenderer.setSeriesPaint(0, _))

    for (i <- 0 until deck.table.getColumnCount)
      deck.model.columns(i).cellEditor(this).foreach(deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor)
    updateStats()
    update()
  }

  /**
   * Clear the selection in each table except for the given one.
   * @param except table to not clear
   */
  def clearTableSelections(except: CardTable) = {
    _lists.flatten.filter(_.table != except).foreach(_.table.clearSelection())
    for (c <- categoryPanels)
      if (c.table != except)
        c.table.clearSelection()
  }

  /**
   * If the deck has unsaved changes, allow the user to choose to save it or keep the
   * frame open.  If the user saves or declines to save, close the frame.
   *
   * @return true if the frame was closed and false otherwise.
   */
  def close() =  {
    if (unsaved) {
      val msg = s"""Deck "${getTitle.substring(0, getTitle.length - 2)}" has unsaved changes.  Save?"""
      JOptionPane.showConfirmDialog(this, msg, "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION) match {
        case JOptionPane.YES_OPTION => parent.save(this); true
        case JOptionPane.NO_OPTION => dispose(); true
        case JOptionPane.CANCEL_OPTION => false
        case JOptionPane.CLOSED_OPTION => false
        case _ => false
      }
    } else {
      dispose()
      true
    }
  }

  /**
   * Create a new extra, uncategorized, untracked list, which usually will be used for a sideboard.
   * 
   * @param name name of the extra list, i.e. "Sideboard"; should be unique
   * @param id ID of the extra to create
   * @param index index of the tab to insert the new list at
   * @return true if the list was created, and false otherwise.
   */
  @throws[IllegalArgumentException]("if a list with the given name or ID already exists")
  private def createExtra(name: String, id: Int, index: Int): Boolean = {
    if (id == MainDeck)
      throw IllegalArgumentException(s"only the main deck can have ID $MainDeck")
    else if (_lists.size > id && _lists(id).isDefined)
      throw IllegalArgumentException(s"extra already exists at ID $id")
    else {
      if (extras.exists(_.name == name))
        throw IllegalArgumentException(s"""sideboard "$name" already exists""")

      val newExtra = DeckData(id = id, name = name)
      if (id >= _lists.size)
        _lists ++= Seq.fill(id - _lists.size + 1)(None)
      _lists(id) = Some(newExtra)

      newExtra.table.setPreferredScrollableViewportSize(Dimension(newExtra.table.getPreferredScrollableViewportSize.width, 5*newExtra.table.getRowHeight))
      val panel = EditablePanel(name, Some(extrasPane))
      val sideboardPane = JScrollPane(newExtra.table)
      sideboardPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED))

      // Move cards to main deck
      val moveToMainItem = JMenuItem("Move to Main Deck")
      moveToMainItem.addActionListener(_ => _lists(id).map(_.move(parent.getSelectedCards.map(_.card -> 1))(deck)).getOrElse(throw NoSuchElementException(id.toString)))
      newExtra.popup.add(moveToMainItem)
      val moveAllToMainItem = JMenuItem("Move All to Main Deck")
      moveAllToMainItem.addActionListener(_ => _lists(id).map(_.move(parent.getSelectedCards.map((e) => e.card -> newExtra.current.find(_.card == e.card).map(_.count).getOrElse(0)))(deck)).getOrElse(throw NoSuchElementException(id.toString)))
      newExtra.popup.add(moveAllToMainItem)
      newExtra.popup.add(JSeparator())

      // Edit card tags item in sideboard
      newExtra.popup.add(newExtra.editTags)

      // Item enables as menu becomes visible
      newExtra.popup.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
        moveToMainItem.setEnabled(!parent.getSelectedCards.isEmpty)
        moveAllToMainItem.setEnabled(!parent.getSelectedCards.isEmpty)
      }))

      extrasPane.insertTab(name, null, sideboardPane, null, index)
      extrasPane.setTabComponentAt(index, panel)
      extrasPane.setSelectedIndex(index)
      extrasPane.getTabComponentAt(extrasPane.getSelectedIndex).requestFocus()
      southLayout.show(southPanel, "extras")
      listTabs.setSelectedIndex(MainDeck)

      panel.listeners += ((e) => e.getActionCommand match {
        case EditablePanel.Close =>
          val n = panel.title
          val extra = _lists(id).map(_.copy()).getOrElse(throw NoSuchElementException(id.toString))
          val i = extrasPane.indexOfTab(n)
          performAction(() => deleteExtra(id, i), () => {
            val created = createExtra(n, id, i)
            _lists(id).get.current ++= extra.current
            _lists(id).get.original ++= extra.original
            created || extra.current.exists(_.count > 0) || extra.original.exists(_.count > 0)
          })
        case EditablePanel.Edit =>
          val current = panel.title
          panel.previousTitle.map{ old =>
            if (current.isEmpty)
              panel.title = old
            else if (extras.exists(_.name == current)) {
              panel.title = old
              JOptionPane.showMessageDialog(this, s"""Sideboard "$current" already exists.""", "Error", JOptionPane.ERROR_MESSAGE)
            } else if (!current.equals(old)) {
              val j = extrasPane.indexOfTab(old)
              performAction(() => {
                _lists(id).get.name = current
                extrasPane.getTabComponentAt(j) match {
                  case ep: EditablePanel => ep.title = current
                  case _ =>
                }
                extrasPane.setTitleAt(j, current)
                listTabs.setSelectedIndex(MainDeck)
                true
              }, () => {
                _lists(id).get.name = old
                extrasPane.getTabComponentAt(j) match {
                  case ep: EditablePanel => ep.title = old
                  case _ =>
                }
                extrasPane.setTitleAt(j, old)
                listTabs.setSelectedIndex(MainDeck)
                true
              })
            }
          }.getOrElse(throw NoSuchElementException("previous title"))
        case EditablePanel.Cancel =>
      })

      true
    }
  }

  /**
   * Delete an extra list. This just sets its index in the list of card lists to None, so it can be reused later if this is undone.
   * 
   * @param id ID of the list to delete
   * @param index index of the tab containing the list
   * @return true if the list was successfully removed, and false otherwise
   */
  @throws[IllegalArgumentException]("if the list with the given ID doesn't exist")
  private def deleteExtra(id: Int, index: Int) = {
    if (_lists(id).isEmpty)
      throw IllegalArgumentException(s"missing sideboard with ID $id")

    _lists(id) = None
    extrasPane.remove(index)
    if (index > 0) {
      extrasPane.setSelectedIndex(index - 1)
      extrasPane.getTabComponentAt(extrasPane.getSelectedIndex).requestFocus()
    }
    southLayout.show(southPanel, if (extras.isEmpty) "empty" else "extras")
    listTabs.setSelectedIndex(MainDeck)

    true
  }

  /**
   * Open the dialog to create a new specification for a deck category.
   *
   * @return the {@link Categorization} created by the dialog, or null if it was
   * canceled.
   */
  def createCategory = {
    var spec: Option[Categorization] = None
    while {{
      spec = CategoryEditorPanel.showCategoryEditor(this, spec)
      spec.foreach((s) => {
      if (categories.contains(s.name))
        JOptionPane.showMessageDialog(this, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE)
      })
    }; spec.exists((s) => categories.contains(s.name)) } do ()
    spec
  }

  /**
   * Create a new {@link CategoryPanel} out of the given specification.
   *
   * @param spec specification for the category of the new {@link CategoryPanel}
   * @return the new {@link CategoryPanel}.
   */
  private def createCategoryPanel(spec: Categorization) = {
    val newCategory = CategoryPanel(deck.current, spec.name, this)
    // When a card is selected in a category, the others should deselect
    val listener = TableSelectionListener(parent, newCategory.table, deck.current.categories(newCategory.name).list)
    newCategory.table.addMouseListener(listener)
    newCategory.table.getSelectionModel.addListSelectionListener(listener)
    // Add the behavior for the edit category button
    newCategory.editButton.addActionListener(_ => editCategory(newCategory.name))
    // Add the behavior for the remove category button
    newCategory.removeButton.addActionListener(_ => categories -= newCategory.name)
    // Add the behavior for the color edit button
    newCategory.colorButton.addActionListener(_ => {
      Option(JColorChooser.showDialog(this, "Choose a Color", newCategory.colorButton.color)).foreach{ newColor =>
        val oldColor = deck.current.categories(newCategory.name).categorization.color
        val name = newCategory.name
        performAction(() => {
          deck.current.categories(newCategory.name) = deck.current.categories(name).categorization.copy(color = newColor)
          listTabs.setSelectedIndex(Categories.ordinal)
          true
        }, () => {
          deck.current.categories(newCategory.name) = deck.current.categories(name).categorization.copy(color = oldColor)
          listTabs.setSelectedIndex(Categories.ordinal)
          true
        })
      }
    })
    // Add the behavior for double-clicking the category title
    newCategory.addMouseListener(ChangeTitleListener(newCategory, (title) => categories(newCategory.name) = categories(newCategory.name).copy(name = title)))
    // Add behavior for the rank box
    newCategory.rankBox.addActionListener(_ => {
      if (newCategory.rankBox.isPopupVisible) {
        val name = newCategory.name
        val old = deck.current.categories(newCategory.name).rank
        val target = newCategory.rankBox.getSelectedIndex
        performAction(() => {
          deck.current.categories(name).rank = target
          for (panel <- categoryPanels)
            panel.rankBox.setSelectedIndex(deck.current.categories(panel.name).rank)
          listTabs.setSelectedIndex(Categories.ordinal)
          updateCategoryPanel()
          true
        }, () => {
          deck.current.categories(name).rank = old
          for (panel <- categoryPanels)
            panel.rankBox.setSelectedIndex(deck.current.categories(panel.name).rank)
          listTabs.setSelectedIndex(Categories.ordinal)
          updateCategoryPanel()
          true
        })
      }
    })

    newCategory.table.setTransferHandler(EditorTableTransferHandler(this, MainDeck))
    newCategory.table.setDragEnabled(true)
    newCategory.table.setDropMode(DropMode.ON)

    // Add the behavior for clicking on the category's table
    // Table popup menu
    val tableMenu = JPopupMenu()
    newCategory.table.addMouseListener(TableMouseAdapter(newCategory.table, tableMenu))

    // Cut, copy, paste
    val cardCCP = CCPItems(deck.table, true)
    tableMenu.add(cardCCP.cut)
    tableMenu.add(cardCCP.copy)
    tableMenu.add(cardCCP.paste)
    tableMenu.add(JSeparator())

    val tableMenuCardItems = CardMenuItems(Some(this), parent.getSelectedCards.map(_.card), true)
    tableMenuCardItems.addAddItems(tableMenu)
    tableMenu.add(JSeparator())
    tableMenuCardItems.addRemoveItems(tableMenu)

    val categoriesSeparator = JSeparator()
    tableMenu.add(categoriesSeparator)

    // Quick edit categories
    val addToCategoryMenu = JMenu("Include in")
    tableMenu.add(addToCategoryMenu)
    val removeFromCategoryItem = JMenuItem(s"Exclude from ${spec.name}")
    removeFromCategoryItem.addActionListener(_ => categories(newCategory.name) = {
      val category = deck.current.categories(newCategory.name).categorization
      newCategory.selectedCards.filter((e) => category(e.card)).foldLeft(category)((cat, e) => cat - e.card)
    })
    tableMenu.add(removeFromCategoryItem)
    val removeFromCategoryMenu = JMenu("Exclude from")
    tableMenu.add(removeFromCategoryMenu)

    // Edit categories item
    val editCategoriesItem = JMenuItem("Edit Categories...")
    editCategoriesItem.addActionListener(_ => {
      val iePanel = IncludeExcludePanel(deck.current.categories.map(_.categorization).toSeq.sortBy(_.name.toLowerCase), parent.getSelectedCards.map(_.card))
      if (JOptionPane.showConfirmDialog(this, JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
        categories.update(iePanel.updates.map((c) => c.name -> c).toMap)
    })
    tableMenu.add(editCategoriesItem)

    tableMenu.add(JSeparator())

    // Edit tags item
    val editTagsItem = JMenuItem("Edit Tags...")
    editTagsItem.addActionListener(_ => CardTagPanel.editTags(parent.getSelectedCards.map(_.card), parent))
    tableMenu.add(editTagsItem)

    // Table menu popup listeners
    tableMenu.addPopupMenuListener(TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu, editCategoriesItem, categoriesSeparator, newCategory.table))
    tableMenu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
      cardCCP.cut.setEnabled(!parent.getSelectedCards.isEmpty)
      cardCCP.copy.setEnabled(!parent.getSelectedCards.isEmpty)
      val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
      cardCCP.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor))

      removeFromCategoryItem.setText(s"Exclude from ${newCategory.name}")
      tableMenuCardItems.setEnabled(!parent.getSelectedCards.isEmpty)
      editTagsItem.setEnabled(!parent.getSelectedCards.isEmpty)
    }))

    newCategory.setTransferHandler(CategoryTransferHandler(
      () => categories(newCategory.name),
      (c) => categories.contains(c.name),
      categories += _,
      (c) => categories -= c.name
    ))

    // Categorization popup menu
    val categoryMenu = JPopupMenu()
    newCategory.setComponentPopupMenu(categoryMenu)

    // Cut, copy, paste
    val categoryCCP = CCPItems(newCategory, false)
    categoryCCP.cut.setText("Cut Categorization")
    categoryMenu.add(categoryCCP.cut)
    categoryCCP.copy.setText("Copy Categorization")
    categoryMenu.add(categoryCCP.copy)
    categoryCCP.paste.setText("Paste Categorization")
    categoryMenu.add(categoryCCP.paste)
    categoryMenu.add(JSeparator())

    // Edit item
    val editItem = JMenuItem("Edit...")
    editItem.addActionListener(_ => editCategory(newCategory.name))
    categoryMenu.add(editItem)

    // Delete item
    val deleteItem = JMenuItem("Delete")
    deleteItem.addActionListener(_ => categories -= newCategory.name)
    categoryMenu.add(deleteItem)

    // Add to presets item
    val addPresetItem = JMenuItem("Add to presets")
    addPresetItem.addActionListener(_ => parent.addPreset(deck.current.categories(newCategory.name).categorization))
    categoryMenu.add(addPresetItem)

    categoryMenu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
      val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
      try {
        categoryCCP.paste.setEnabled(!categories.contains(clipboard.getData(DataFlavors.categoryFlavor).asInstanceOf[CategoryTransferData].spec.name))
      } catch {
        case _ @ (_: UnsupportedFlavorException | _: IOException) =>
          // Technically using exceptions as control flow (as with unsupported flavors here) is bad
          // programming practice, but since the exception has to be caught here anyway it reduces
          // code size
          categoryCCP.paste.setEnabled(false)
      }
    }))

    newCategory.table.addMouseListener(TableMouseAdapter(newCategory.table, tableMenu))

    newCategory
  }

  /**
   * Open the category dialog to edit the category with the given name, if there is one, and then update the undo buffer.
   *
   * @param name name of the category to edit
   * @return true if the category was edited, and false otherwise
   */
  @throws[RuntimeException]("if an unexpected category was edited")
  def editCategory(name: String) = categories.get(name).map((toEdit) => {
    CategoryEditorPanel.showCategoryEditor(this, Some(toEdit)).map(categories(name) = _)
  }).getOrElse{ JOptionPane.showMessageDialog(this, s"Deck ${deck.name} has no category named $name.", "Error", JOptionPane.ERROR_MESSAGE); None }

  /**
   * Helper method for adding a category.
   * 
   * @param spec specification of the new category
   * @return true if the category was successfully added, and false otherwise
   */
  private def do_addCategory(spec: Categorization): Boolean = if (deck.current.categories.contains(spec.name)) false else {
    deck.current.categories += spec
    val category = createCategoryPanel(spec)
    category.startObserving()
    categoryPanels += category

    for (c <- categoryPanels)
      if (c != category)
        c.rankBox.addItem(deck.current.categories.size - 1)

    listTabs.setSelectedIndex(Categories.ordinal)
    updateCategoryPanel()
    SwingUtilities.invokeLater(() => {
      switchCategoryBox.setSelectedItem(category.name)
      category.scrollRectToVisible(Rectangle(category.getSize()))
      category.flash()
    })
    handCalculations.update()
    true
  }

  /**
   * Helper method for removing a category.
   * 
   * @param spec specification of the category to remove
   * @return true if the category was removed, and false otherwise.
   */
  private def do_removeCategory(spec: Categorization) = if (deck.current.categories.contains(spec.name)) {
    deck.current.categories -= spec.name
    val category = getCategoryPanel(spec.name).get
    categoryPanels -= category
    category.stopObserving()
    categoryPanels.foreach(_.rankBox.removeItemAt(categoryPanels.size))
    listTabs.setSelectedIndex(Categories.ordinal)
    updateCategoryPanel()
    handCalculations.update()
    true
  } else false

  /**
   * Export the deck to a different format.
   *
   * @param format formatter to use for export
   * @param comp sort ordering of cards in the exported list
   * @param extraNames names of extra lists to include in the export
   * @param file file to export to
   * @return a [[Try]] containing an exception if the file couldn't be written or any of the named extra lists
   * doesn't exist, or nothing if the file was written successfully
   */
  def exportList(format: CardListFormat, comp: Ordering[? >: CardListEntry], extraNames: Seq[String], file: File) = {
    Using(PrintWriter(OutputStreamWriter(FileOutputStream(file, false), "UTF8")))(wr => {
      def write(d: CardList, n: Option[String] = None) = {
        n.foreach(wr.println(_))
        val copy = Deck()
        copy ++= d.sorted(comp)
        wr.print(format.format(copy))
      }

      format.header.foreach(wr.println)
      if (!deck.current.isEmpty)
        write(deck.current)
      extraNames.foreach((name) => {
        extras.find(_.name == name).map((list) => if (!list.current.isEmpty) {
          wr.println()
          write(list.current, Some(name))
        }).getOrElse(throw NoSuchElementException(s"No extra list named $name"))
      })
    })
  }

  /**
   * Get the card at the given index in the given table.
   *
   * @param t table to get the card from
   * @param index index into the given table to get a card from
   * @return the card in the deck at the given index in the given table, if the table is in this EditorFrame
   */
  @throws[IllegalArgumentException]("if the desired table isn't in this deck")
  def getCardAt(t: CardTable, index: Int) = {
    if (t == deck.table)
      deck(deck.table.convertRowIndexToModel(index))
    else {
      categoryPanels.find(_.table == t) match {
        case Some(panel) => deck.current.categories(panel.name).list(panel.table.convertRowIndexToModel(index))
        case None => throw IllegalArgumentException(s"Table not in deck ${deck.name}")
      }
    }
  }

  /**
   * Get the panel for the category with the specified name in the deck.
   *
   * @param name name of the category to search for
   * @return the panel for the category with the specified name, if there is one, or None otherwise
   */
  private def getCategoryPanel(name: String) = categoryPanels.find(_.name == name)

  /** @return a list of cards representing the current table selection */
  def getSelectedCards = parent.getSelectedCards

  /** @return true if this editor has the table with the current selection and false otherwise */
  def hasSelectedCards = parent.getSelectedTable.exists((t) => _lists.exists((l) => l.isDefined && l.get.table == t) || categoryPanels.exists(_.table == t))

  /**
   * Save the deck to the current file.
   * @return true if the file was successfully saved, and false otherwise
   */
  def save(): Boolean = file.map(save).getOrElse(false)

  /**
   * Save the deck to the given file (like Save As).
   *
   * @param f file to save to
   * @return true if the file was successfully saved, and false otherwise.
   */
  def save(f: File): Boolean = {
    val changes = deck.changes
    if (!changes.isEmpty) {
      changelogArea.append(s"""|~~~~~${DesignSerializer.ChangelogDateFormat.format(Date())}~~~~~)
                               |$changes
                               |""".stripMargin)
    }

    val sideboards = extras.map((l) => l.name -> l.current).toMap
    val manager = DesignSerializer(deck.current, sideboards, notesArea.getText, changelogArea.getText, Some(f))
    try {
      manager.save()
      deck.original.clear()
      deck.original.addAll(deck.current)
      unsaved = false
      file = manager.file
      true
    }
    catch case e: IOException =>
      JOptionPane.showMessageDialog(parent, s"Error saving ${f.getName}: ${e.getMessage}.", "Error", JOptionPane.ERROR_MESSAGE)
      false
  }

  /**
   * Set the background color for the sample hand panel.
   * @param col new background color for the sample hand panel
   */
  def setHandBackground(col: Color) = {
    imagePanel.setBackground(col)
    imagePanel.getComponents.foreach(_.setBackground(col))
    imagePane.getViewport.setBackground(col)
  }

  /**
   * Update the GUI to show the latest state of the deck.
   * XXX: Graphical errors could be attributed to this function
   */
  def update() = {
    revalidate()
    repaint()
    categoryPanels.foreach(_.update())
  }

  /** Update the categories combo box with all of the current categories. */
  def updateCategoryPanel() = {
    categoriesContainer.removeAll()
    switchCategoryModel.removeAllElements()

    if (deck.current.categories.isEmpty)
      switchCategoryBox.setEnabled(false)
    else {
      switchCategoryBox.setEnabled(true)
      deck.current.categories.map(_.categorization).toSeq.sorted(sortCategoriesBox.getCurrentItem(deck.current)).foreach((c) => {
        categoriesContainer.add(getCategoryPanel(c.name).get)
        switchCategoryModel.addElement(c.name)
      })
    }

    analyzeCategoryBox.setVisible(!deck.current.categories.isEmpty)
    analyzeCategoryCombo.setVisible(!deck.current.categories.isEmpty)
    if (deck.current.categories.isEmpty)
      analyzeCategoryBox.setSelected(false)
    else {
      val selectedForAnalysis = analyzeCategoryCombo.getCurrentItem
      analyzeCategoryCombo.removeAllItems()
      deck.current.categories.foreach((c) => analyzeCategoryCombo.addItem(c.categorization.name))
      analyzeCategoryCombo.setMaximumSize(analyzeCategoryCombo.getPreferredSize())
      val indexForAnalysis = analyzeCategoryCombo.getModel.asInstanceOf[DefaultComboBoxModel[String]].getIndexOf(selectedForAnalysis)
      if (indexForAnalysis < 0) {
        analyzeCategoryCombo.setSelectedIndex(0)
        analyzeCategoryBox.setSelected(false)
      } else analyzeCategoryCombo.setSelectedIndex(indexForAnalysis)
    }

    categoriesContainer.revalidate()
    categoriesContainer.repaint()

    listTabs.setSelectedIndex(Categories.ordinal)
  }

  /** Update the card statistics to reflect the cards in the deck. */
  @throws[IllegalStateException]("if there are cards but no upper bound on mana values")
  def updateStats() = {
    val lands = deck.current.collect{ case e if SettingsDialog.settings.editor.isLand(e.card) => e.count }.sum
    countLabel.setText(s"Total cards: ${deck.current.total}")
    landLabel.setText(s"Lands: $lands")
    nonlandLabel.setText(s"Nonlands: ${(deck.current.total - lands)}")

    val manaValue = deck.current
        .filterNot(_.card.types.exists(_.equalsIgnoreCase("land")))
        .flatMap((e) => Seq.tabulate(e.count)(_ => SettingsDialog.settings.editor.getManaValue(e.card)))
        .toSeq.sorted
    val avgManaValue = if (manaValue.isEmpty) 0 else manaValue.sum/manaValue.size
    avgManaValueLabel.setText(s"Average Mana Value: ${StringUtils.formatDouble(avgManaValue, 2)}")

    val medManaValue = if (!manaValue.isEmpty) {
      if (manaValue.length % 2 == 0)
        (manaValue(manaValue.length/2 - 1) + manaValue(manaValue.length/2))/2
      else
        manaValue(manaValue.length/2)
    } else 0
    medManaValueLabel.setText(s"Median Mana Value: ${StringUtils.formatDouble(medManaValue, 1)}")

    manaCurve.clear()
    landDrops.clear()
    val colorSets = ListMap((1 to ManaType.colors.size).flatMap(ManaType.colors.combinations(_).map((c) => ManaType.sorted(c).map(_.toString) -> c.toSet)):_*)
    val sections = sectionsBox.getCurrentItem match {
      case ByNothing    => Seq(Seq(if (analyzeCategoryBox.isSelected) analyzeCategoryCombo.getCurrentItem else "Main Deck"))
      case ByColorGroup => Seq(Seq("Colorless")) ++ ManaType.colors.map((m) => Seq(m.toString)) ++ Seq(Seq("Multicolored"))
      case ByColors     => Seq(Seq("Colorless")) ++ colorSets.keys
      case ByType       => Seq(Seq("Creature"), Seq("Artifact"), Seq("Enchantment"), Seq("Planeswalker"), Seq("Instant"), Seq("Sorcery")); // Land is omitted because we don't count them here
    }
    val analyte = if (analyzeCategoryBox.isSelected) deck.current.categories(analyzeCategoryCombo.getCurrentItem).list else deck.current
    val analyteLands = analyte.collect{ case e if SettingsDialog.settings.editor.isLand(e.card) => e.count }.sum
    if (analyte.total - analyteLands > 0) {
      var sectionManaValues = sections.zipWithIndex.map{ case (s, i) => s -> analyte
        .filter((e) => !SettingsDialog.settings.editor.isLand(e.card))
        .filter((e) => sectionsBox.getCurrentItem match {
          case ByNothing => true
          case ByColorGroup => s match {
            case Seq("Colorless") => e.card.colors.isEmpty
            case Seq(color) if e.card.colors.size == 1 => ManaType.parse(color.toLowerCase).filter(e.card.colors.contains).isDefined
            case Seq("Multicolored") => e.card.colors.size > 1
            case _ => false
          }
          case ByColors => s match {
            case Seq("Colorless") => e.card.colors.isEmpty
            case _ => e.card.colors == colorSets(s)
          }
          case ByType => e.card.typeLine.containsIgnoreCase(s(0)) && !sections.slice(0, i).exists((s) => e.card.typeLine.containsIgnoreCase(s(0)))
        })
        .flatMap((e) => Seq.fill(e.count)(SettingsDialog.settings.editor.getManaValue(e.card)))
        .toSeq.sorted
        .map(math.ceil)
      }.toMap
      val minMV = math.ceil(manaValue.head).toInt
      val maxMV = math.ceil(manaValue.last).toInt
      sections.filter(!sectionManaValues(_).isEmpty).zipWithIndex.foreach{ case (s, i) =>
        val paint = s match {
          case Nil => SettingsDialog.settings.editor.manaAnalysis.none
          case Seq(c) => SettingsDialog.settings.editor.manaAnalysis.getOrElse(c)
          case _ => new Paint {
            override def createContext(cm: ColorModel, deviceBounds: Rectangle, userBounds: Rectangle2D, xform: AffineTransform, hints: RenderingHints) = new PaintContext {
              val bounds = (1 to s.size - 1).map((i) => (x: Int) => -1*(x - (deviceBounds.x + deviceBounds.width*i/s.size)) + (deviceBounds.y + deviceBounds.height*i/s.size)).toIndexedSeq

              override def getColorModel = Option(cm).getOrElse(ColorModel.getRGBdefault)

              override def getRaster(x: Int, y: Int, w: Int, h: Int) = {
                val raster = getColorModel.createCompatibleWritableRaster(w, h)
                for (j <- 0 until h) {
                  for (i <- 0 until w) {
                    if (deviceBounds.contains(x + i, y + j)) {
                      val k = bounds.indexWhere((b) => b(x + i) > y + j)
                      val color = SettingsDialog.settings.editor.manaAnalysis(if (k >= 0) s(k) else s.last)
                      raster.setPixel(i, j, Array(color.getRed, color.getGreen, color.getBlue, color.getAlpha))
                    }
                  }
                }
                raster
              }

              override def dispose = {}
            }

            override def getTransparency = Transparency.OPAQUE;
          }
        }
        manaCurveRenderer.setSeriesPaint(i, paint)
        for (j <- minMV to maxMV)
          manaCurve.addValue(sectionManaValues(s).count(_ == j), s.mkString("-"), j.toString)
      }

      if (minMV >= 0) {
        if (maxMV < 0)
          throw IllegalStateException("min mana value but no max mana value")
        val choice = landsBox.getCurrentItem
        landAxis.setLabel(choice.toString)
        for (i <- minMV to maxMV) {
          val v = choice match {
            case Played =>
              var e = 0.0
              var q = 0.0
              for (j <- 0 until math.min(i, lands)) {
                val p = stats.hypergeometric(j, math.min(handCalculations.handSize + i - 1, deck.current.size), lands, deck.current.total)
                q += p
                e += j*p
              }
              e + i*(1 - q)
            case Drawn => (lands.toDouble/deck.current.total.toDouble)*math.min(handCalculations.handSize + i - 1, deck.current.total)
            case Probability =>
              var q = 0.0
              for (j <- 0 until i)
                q += stats.hypergeometric(j, math.min(handCalculations.handSize + i - 1, deck.current.size), lands, deck.current.total)
              1 - q
          }
          landDrops.addValue(v, choice.toString, i.toString)
        }
      }
    }
  }

  /**
   * Update all of the tables and components with the contents of the cards in the
   * deck.
   * 
   * @param selected list of selected cards from <b>before</b> the change to the deck was made
   */
  private def updateTables(selected: Iterable[Card]) =  {
    updateStats()
    parent.updateCardsInDeck()
    _lists.flatten.foreach(_.model.fireTableDataChanged())
    categoryPanels.foreach(_.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged())
    parent.getSelectedTable.foreach((t) => {
      t.getSelectionModel().setValueIsAdjusting(true);
      parent.getSelectedList.foreach((l) => {
        for (c <- selected) {
          if (l.contains(c)) {
            val row = t.convertRowIndexToView(l.indexOf(c))
            t.addRowSelectionInterval(row, row)
          }
        }
        if (t.isEditing)
          t.getCellEditor.cancelCellEditing()
      })
      t.getSelectionModel().setValueIsAdjusting(false);
    })
    hand.refresh()
    handCalculations.update()

    if (listTabs.getSelectedIndex > Categories.ordinal)
      listTabs.setSelectedIndex(MainTable.ordinal)
  }
}