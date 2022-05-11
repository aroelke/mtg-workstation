package editor.gui.editor

import editor.collection.CardList
import editor.collection.CardListEntry
import editor.collection.deck.Category
import editor.collection.deck.Deck
import editor.collection.deck.Hand
import editor.collection.`export`.CardListFormat
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
import editor.util.MouseListenerFactory
import editor.util.PopupMenuListenerFactory
import editor.util.Stats
import editor.util.StringUtils
import editor.util.UndoableAction
import editor.util.UnicodeSymbols
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
import java.awt.PopupMenu
import java.awt.Rectangle
import java.awt.ScrollPane
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.event.MouseEvent
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat
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
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import scala.util.Using

object EditorFrame {
  val MainDeck = 0
}

class EditorFrame(parent: MainFrame, u: Int, manager: DeckSerializer = DeckSerializer()) extends JInternalFrame(manager.file.map(_.getName).getOrElse(s"Untitled $u"), true, true, true, true)
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

  /**
   * Auxiliary class for controlling the cards in a [[Deck]] and storing related information.
   * @author Alec Roelke
   */
  case class DeckData private[EditorFrame](
    private[EditorFrame] val id: Int,
    private var _name: String,
    private[EditorFrame] val current: Deck,
    private[EditorFrame] val original: Deck
  ) extends CardList {
    /** @return the name of the list */
    def name = _name
    private[EditorFrame] def name_=(n: String) = _name = n

    /**
     * Modify the cards and number of copies of cards in the deck.
     * 
     * @param changes mapping of card onto the number of copies to change (positive numbers add cards, negative numbers remove cards)
     * @return true if the deck was modified, and false otherwise
     */
    def %%=(changes: ListMap[Card, Int]) = if (changes.isEmpty || changes.forall{ case (_, n) => n == 0 }) false else {
      val capped = changes.map{ case (card, n) => card -> math.max(n, -current.getEntry(card).count) }
      performAction(() => _lists(id).map(l => { // can't use "this" here because after redoing, reference is different
        val selected = parent.getSelectedCards
        val changed = capped.map{ case (card, n) =>
          if (n < 0)
            l.current.remove(card, -n) > 0
          else if (n > 0)
            l.current.add(card, n)
          else
            false
        }.fold(false)(_ || _)
        if (changed)
          updateTables(selected)
        changed
      }).getOrElse(throw NoSuchElementException(id.toString)), () => _lists(id).map((l) => { // see above
        val selected = parent.getSelectedCards
        val changed = capped.map{ case (card, n) =>
          if (n < 0)
            l.current.add(card, -n)
          else if (n > 0)
            l.current.remove(card, n) > 0
          else
            false
        }.fold(false)(_ || _)
        if (changed)
          updateTables(selected)
        changed
      }).getOrElse(throw NoSuchElementException(id.toString)))
    }

    /**
     * Add copies of cards to the deck.
     * 
     * @param changes cards to add and number of copies of each one to add (the same number of copies of all cards are added)
     * @return true if the deck was modified, and false otherwise
     */
    def ++=(changes: (Iterable[Card], Int)) = changes match { case (cards, n) => this %%= ListMap.from(cards.map(_ -> n)) }

    /**
     * Remove copies of cards from the deck.
     * 
     * @param changes cards to remove and number of copies of each one to remove (the same number of copies of all cards are removed)
     * @return true if the deck was modified, and false otherwise
     */
    def --=(changes: (Iterable[Card], Int)) = changes match { case (cards, n) => this %%= ListMap.from(cards.map((c) => c -> -n)) }

    /**
     * Set the number of copies of a card in the deck to a specific amount.
     * 
     * @param card card to update
     * @param count new number of copies in the deck
     * @return the old amount of cards in the deck
     */
    def update(card: Card, count: Int) = {
      val old = current.getEntry(card).count
      if (old != count)
        this %%= ListMap(card -> (count - old))
      old
    }

    /**
     * Move cards from this list to another in the same editor.
     * 
     * @param moves mapping of cards onto counts of cards to move to the other list
     * @param target list to move to
     * @return true if the lists changed as a result, and false otherwise
     */
    def move(moves: Map[Card, Int])(target: DeckData) = {
      val t = target.id
      performAction(() => (_lists(id), _lists(t)) match {
        case (Some(from), Some(to)) =>
          val selected = parent.getSelectedCards
          val preserve = parent.getSelectedTable.contains(table) && moves.forall{ case (card, n) => from.current.getEntry(card).count == n }
          if (from.current.removeAll(moves) != moves)
            throw CardException(s"error moving cards from list $id", moves.keys.toSeq:_*)
          if (!to.current.addAll(moves))
            throw CardException(s"could not move cards to list $t}", moves.keys.toSeq:_*)
          if (preserve)
            parent.setSelectedComponents(to.table, to.current)
          updateTables(selected)
          if (preserve)
            to.table.scrollRectToVisible(to.table.getCellRect(to.table.getSelectedRow, 0, true))
          true
        case (Some(_), None) => throw NoSuchElementException(target.toString)
        case (None, Some(_)) => throw NoSuchElementException(id.toString)
        case (None, None) => throw NoSuchElementException(s"$id,$target")
      }, () => (_lists(id), _lists(t)) match {
        case (Some(from), Some(to)) =>
          val selected = parent.getSelectedCards
          val preserve = parent.getSelectedTable.contains(to.table) && moves.forall{ case (card, n) => to.current.getEntry(card).count == n }
          if (!from.current.addAll(moves))
            throw CardException(s"could not undo move from list $id", moves.keys.toSeq:_*)
          if (to.current.removeAll(moves) != moves)
            throw CardException(s"error undoing move to list $t", moves.keys.toSeq:_*)
          if (preserve)
            parent.setSelectedComponents(table, from.current)
          updateTables(selected)
          if (preserve)
            table.scrollRectToVisible(table.getCellRect(table.getSelectedRow, 0, true))
          true
        case (Some(_), None) => throw NoSuchElementException(target.toString)
        case (None, Some(_)) => throw NoSuchElementException(id.toString)
        case (None, None) => throw NoSuchElementException(s"$id,$target")
      })
    }

    /** @return a String detailing the numbers of copies of cards that have been added or removed since the last time the deck was saved */
    def changes = {
      val changes: StringBuilder = StringBuilder()
      original.foreach((c) => {
        val had = if (original.contains(c)) original.getEntry(c).count else 0
        val has = if (current.contains(c)) current.getEntry(c).count else 0
        if (has < had)
          changes ++= s"-${had - has}x ${c.name} (${c.expansion.name})\n"
      })
      current.foreach((c) => {
        val had = if (original.contains(c)) original.getEntry(c).count else 0
        val has = if (current.contains(c)) current.getEntry(c).count else 0
        if (had < has)
          changes ++= s"+${has - had}x ${c.name} (${c.expansion.name})\n"
      })
      changes.result
    }

    override def contains(c: Card) = current.contains(c)
    @deprecated override def containsAll(cards: Iterable[? <: Card]) = cards.forall(contains)
    @deprecated override def get(index: Int) = current.get(index)
    @deprecated override def set(card: Card, amount: Int): Boolean = update(card, amount) != amount
    override def getEntry(index: Int) = current.getEntry(index)
    override def getEntry(card: Card) = current.getEntry(card)
    override def indexOf(card: Card) = current.indexOf(card)
    override def isEmpty = current.isEmpty
    override def size = current.size
    override def total = current.total
    override def iterator = current.iterator

    private[EditorFrame] lazy val model = CardTableModel(this, SettingsDialog.settings.editor.columns, Some(EditorFrame.this))
    private[EditorFrame] lazy val table = {
      val table = CardTable(model)
      table.stripe = SettingsDialog.settings.editor.stripe
      // When a card is selected in a table, mark it for adding
      val listener = TableSelectionListener(parent, table, current)
      table.addMouseListener(listener)
      table.getSelectionModel.addListSelectionListener(listener)
      // Create cell editors for applicable table columns
      for (i <- 0 until table.getColumnCount)
        if (model.isCellEditable(0, i))
          table.getColumn(model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(EditorFrame.this, model.columns(i)))
      // Set up drag-and-drop for the table
      table.setTransferHandler(EditorTableTransferHandler(EditorFrame.this, id))
      table.setDragEnabled(true)
      table.setDropMode(DropMode.ON)

      table
    }

    private[EditorFrame] lazy val ccp = CCPItems(table, true)
    private[EditorFrame] lazy val cards = CardMenuItems(Some(EditorFrame.this), parent.getSelectedCards, id == MainDeck)
    private[EditorFrame] lazy val editTags = {
      val item = JMenuItem("Edit Tags...")
      item.addActionListener(_ => CardTagPanel.editTags(parent.getSelectedCards, parent))
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

    @deprecated override def add(card: Card): Boolean = throw UnsupportedOperationException()
    @deprecated override def add(card: Card, amount: Int): Boolean = throw UnsupportedOperationException()
    @deprecated override def addAll(cards: CardList): Boolean = throw UnsupportedOperationException()
    @deprecated override def addAll(amounts: Map[? <: Card, Int]): Boolean = throw UnsupportedOperationException()
    @deprecated override def addAll(cards: Set[? <: Card]): Boolean = throw UnsupportedOperationException()
    @deprecated override def remove(card: Card): Boolean = throw UnsupportedOperationException()
    @deprecated override def remove(card: Card, amount: Int): Int = throw UnsupportedOperationException()
    @deprecated override def removeAll(cards: CardList): Map[Card, Int] = throw UnsupportedOperationException()
    @deprecated override def removeAll(cards: Map[? <: Card, Int]) = throw UnsupportedOperationException()
    @deprecated override def removeAll(cards: Set[? <: Card]) = throw UnsupportedOperationException()
    @deprecated override def set(index: Int, amount: Int): Boolean = throw UnsupportedOperationException()
    override def clear() = throw UnsupportedOperationException()
    override def sort(comp: Ordering[? >: CardListEntry]) = throw UnsupportedOperationException()
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
  @deprecated def getSelectedExtraID = Option.when(sideboard.id >= 0)(sideboard.id)
  @deprecated def getExtraNames = extras.map(_.name)

  /** @return a [[CardList]] containing all of the cards in extra lists */
  def allExtras = {
    val sideboard = Deck()
    extras.foreach(e => sideboard.addAll(e.current))
    sideboard
  }

  /**
   * Auxiliary object for controlling the categories of the deck.
   * @author Alec Roelke
   */
  object categories extends Iterable[Category] {
    /**
     * Add a new category to the main deck.
     * 
     * @param spec specification for the new category
     * @return true if adding the category was successful, and false otherwise
     */
    def +=(spec: Category) = if (contains(spec.getName)) false else {
      performAction(() => {
        if (contains(spec.getName))
          throw RuntimeException(s"attempting to add duplicate category ${spec.getName}")
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
      val spec = deck.current.getCategorySpec(name)
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
    def update(name: String, spec: Category): Option[Category] = update(Map(name -> spec)).get(name)

    /**
     * Change several categories in the deck at once, updating UI elements as necessary. All changes
     * are made in the same action, so all will be undone/redone with one command.
     * 
     * @param specs mapping of old category names onto their new specifications (which can have new names)
     * @return a mapping of the old category names onto their old specifications before the change
     */
    @throws[NoSuchElementException]("if any names don't have corresponding categories in the deck")
    def update(specs: Map[String, Category]): Map[String, Category] = if (specs.forall{ case (name, _) => contains(name) }) {
      val changes = specs.filter{ case (name, spec) => apply(name) != spec }
      val old = specs.map{ case (name, _) => name -> apply(name) }
      if (!changes.isEmpty) {
        performAction(() => {
          changes.foreach{ case (name, spec) => deck.current.updateCategory(name, spec) }
          for (panel <- categoryPanels) {
            if (changes.contains(panel.name)) {
              panel.name = changes(panel.name).getName
              panel.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged()
            }
          }
          updateCategoryPanel()
          true
        }, () => {
          changes.foreach{ case (name, spec) => deck.current.updateCategory(spec.getName, old(name)) }
          for (panel <- categoryPanels) {
            if (changes.map{ case (_, spec) => spec.getName }.toSet.contains(panel.name)) {
              specs.find{ case (_, spec) => spec.getName == panel.name }.foreach{ case (name, _) => panel.name = name }
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
    def apply(name: String) = deck.current.getCategorySpec(name)

    /**
     * @param name name of the category to look for
     * @return an [[Option]] containing the category with the given name, or None if there isn't one
     */
    def get(name: String) = Option(deck.current.getCategorySpec(name))

    /**
     * @param name name of the category to check
     * @return true if the deck contains a category with the given name, and false otherwise
     */
    def contains(name: String) = deck.current.containsCategory(name)

    def iterator = deck.current.categories.iterator
  }

  /**
   * Change inclusion of cards in categories according to the given maps.
   *
   * @param included map of cards onto the set of categories they should become included in
   * @param excluded map of cards onto the set of categories they should become excluded from
   * @return true if any categories were modified, and false otherwise
   */
  @deprecated def editInclusion(included: Map[Card, Set[Category]], excluded: Map[Card, Set[Category]]): Boolean = {
    val include = included.map{ case (card, in) => card -> in.filter(!_.includes(card)) }.filter{ case (_, in) => !in.isEmpty }
    val exclude = excluded.map{ case (card, out) => card -> out.filter(_.includes(card)) }.filter{ case (_, out) => !out.isEmpty }
    if (included.isEmpty && excluded.isEmpty) false else {
      val mods = collection.mutable.HashMap[String, Category]()
      for ((card, in) <- include) {
        for (category <- in) {
          if (!mods.contains(category.getName))
            mods(category.getName) = deck.current.getCategorySpec(category.getName)
          mods(category.getName).include(card)
        }
      }
      for ((card, out) <- exclude) {
        for (category <- out) {
          if (!mods.contains(category.getName))
            mods(category.getName) = deck.current.getCategorySpec(category.getName)
          mods(category.getName).exclude(card)
        }
      }
      categories.update(mods.toMap)
      true
    }
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

          for (category <- deck.current.categories) {
            if (!category.includes(card)) {
              val categoryItem = JMenuItem(category.getName)
              categoryItem.addActionListener(_ => categories(category.getName) = {
                val mod = Category(category)
                mod.include(card)
                mod
              })
              addToCategoryMenu.add(categoryItem)
            }
          }
          addToCategoryMenu.setVisible(addToCategoryMenu.getItemCount > 0)

          for (category <- deck.current.categories) {
            if (category.includes(card)) {
              val categoryItem = JMenuItem(category.getName)
              categoryItem.addActionListener(_ => categories(category.getName) = {
                val mod = Category(category)
                mod.exclude(card)
                mod
              })
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

  private val deckButtons = VerticalButtonList(Seq("+", UnicodeSymbols.Minus.toString, "X"))
  deckButtons("+").addActionListener(_ => deck ++= parent.getSelectedCards -> 1)
  deckButtons(UnicodeSymbols.Minus.toString).addActionListener(_ => deck --= parent.getSelectedCards -> 1)
  deckButtons("X").addActionListener(_ => deck --= parent.getSelectedCards -> parent.getSelectedCards.map(deck.current.getEntry(_).count).max)
  mainPanel.add(deckButtons, BorderLayout.WEST)

  private val southLayout = CardLayout()
  private val southPanel = JPanel(southLayout)
  mainPanel.add(southPanel, BorderLayout.SOUTH)

  private val extrasPanel = JPanel()
  extrasPanel.setLayout(BorderLayout())
  southPanel.add(extrasPanel, "extras")

  private val extrasButtons = VerticalButtonList(Seq("+", UnicodeSymbols.Minus.toString, "X"))
  extrasButtons("+").addActionListener(_ => sideboard ++= parent.getSelectedCards -> 1)
  extrasButtons(UnicodeSymbols.Minus.toString).addActionListener(_ => sideboard --= parent.getSelectedCards -> 1)
  extrasButtons("X").addActionListener(_ => sideboard --= parent.getSelectedCards -> parent.getSelectedCards.map(sideboard.current.getEntry(_).count).max)
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
    val iePanel = IncludeExcludePanel(deck.current.categories.toSeq.sortBy(_.getName.toLowerCase), parent.getSelectedCards)
    if (JOptionPane.showConfirmDialog(this, JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
      editInclusion(iePanel.included, iePanel.excluded)
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
        moveToItem.addActionListener(_ => _lists(id).map(deck.move(parent.getSelectedCards.map((_ -> 1)).toMap)(_)).getOrElse(throw NoSuchElementException(id.toString)))
        moveToItem.setEnabled(!parent.getSelectedCards.isEmpty)
        moveToMenu.add(moveToItem)
        val moveAllToItem = JMenuItem(l.name)
        moveAllToItem.addActionListener(_ => _lists(id).map(deck.move(parent.getSelectedCards.map(c => (c -> deck.current.getEntry(c).count)).toMap)(_)).getOrElse(throw NoSuchElementException(id.toString)))
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
  private enum CategoryOrder(override val toString: String, order: (Deck) => Ordering[Category]) {
    def apply(d: Deck) = order(d)

    case AtoZ       extends CategoryOrder("A-Z", (d) => (a, b) => a.getName.compare(b.getName))
    case ZtoA       extends CategoryOrder("Z-A", (d) => (a, b) => -a.getName.compare(b.getName))
    case Ascending  extends CategoryOrder("Ascending Size", (d) => (a, b) => d.getCategoryList(a.getName).total.compare(d.getCategoryList(b.getName).total))
    case Descending extends CategoryOrder("Descending Size", (d) => (a, b) => -d.getCategoryList(a.getName).total.compare(d.getCategoryList(b.getName).total))
    case Priority   extends CategoryOrder("Increasing Rank", (d) => (a, b) => d.getCategoryRank(a.getName).compare(d.getCategoryRank(b.getName)))
    case Reverse    extends CategoryOrder("Decreasing Rank", (d) => (a, b) => -d.getCategoryRank(a.getName).compare(d.getCategoryRank(b.getName)))
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
    getCategoryPanel(switchCategoryBox.getItemAt(switchCategoryBox.getSelectedIndex)).foreach((c) => {
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
  categoriesPane.setTransferHandler(CategoryTransferHandler(null, (c) => categories.contains(c.getName), categories += _, null))

  // Popup menu for category container
  private val categoriesMenu = JPopupMenu()
  private val categoriesCCP = CCPItems(categoriesPane, false)
  categoriesCCP.paste.setText("Paste Category")
  categoriesMenu.add(categoriesCCP.paste)
  categoriesMenu.add(JSeparator())
  private val categoriesCreateItem = JMenuItem("Add Category...")
  categoriesCreateItem.addActionListener(_ => createCategory.foreach(categories += _))
  categoriesMenu.add(categoriesCreateItem)
  categoriesMenu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    try {
      categoriesCCP.paste.setEnabled(!categories.contains((clipboard.getData(DataFlavors.categoryFlavor)).asInstanceOf[CategoryTransferData].spec.getName))
    } catch {
      case _ @ (_: UnsupportedFlavorException | _: IOException) => categoriesCCP.paste.setEnabled(false)
    }
  }))
  categoriesPane.setComponentPopupMenu(categoriesMenu)

  private val categoryButtons = VerticalButtonList(Seq("+", UnicodeSymbols.Minus.toString, "X"))
  categoryButtons("+").addActionListener(_ => deck ++= parent.getSelectedCards -> 1)
  categoryButtons(UnicodeSymbols.Minus.toString).addActionListener(_ => deck --= parent.getSelectedCards -> 1)
  categoryButtons("X").addActionListener(_ => deck --= parent.getSelectedCards -> parent.getSelectedCards.map(deck.current.getEntry(_).count).max)
  categoriesPanel.add(categoryButtons, BorderLayout.WEST)

  /* MANA ANALYSIS TAB */
  private enum ManaCurveSection(override val toString: String) {
    case ByNothing extends ManaCurveSection("Nothing")
    case ByColor   extends ManaCurveSection("Color")
    case ByType    extends ManaCurveSection("Card Type")    
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
  analyzeCategoryCombo.addActionListener(_ => updateStats())
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
      panel.setCard(hand.get(hand.size - 1))
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

  for (spec <- deck.current.categories)
    categoryPanels += createCategoryPanel(spec)
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
      if (deck.model.isCellEditable(0, i))
        deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck.model.columns(i)))
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
      moveToMainItem.addActionListener(_ => _lists(id).map(_.move(parent.getSelectedCards.map(_ -> 1).toMap)(deck)).getOrElse(throw NoSuchElementException(id.toString)))
      newExtra.popup.add(moveToMainItem)
      val moveAllToMainItem = JMenuItem("Move All to Main Deck")
      moveAllToMainItem.addActionListener(_ => _lists(id).map(_.move(parent.getSelectedCards.map((c) => c -> newExtra.current.getEntry(c).count).toMap)(deck)).getOrElse(throw NoSuchElementException(id.toString)))
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
            val currented = _lists(id).get.current.addAll(extra.current)
            val originaled = _lists(id).get.original.addAll(extra.original)
            created || currented || originaled
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
   * @return the {@link Category} created by the dialog, or null if it was
   * canceled.
   */
  def createCategory = {
    var spec: Option[Category] = None
    while {{
      spec = CategoryEditorPanel.showCategoryEditor(this, spec)
      spec.foreach((s) => {
      if (categories.contains(s.getName))
        JOptionPane.showMessageDialog(this, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE)
      })
    }; spec.exists((s) => categories.contains(s.getName)) } do ()
    spec
  }

  /**
   * Create a new {@link CategoryPanel} out of the given specification.
   *
   * @param spec specification for the category of the new {@link CategoryPanel}
   * @return the new {@link CategoryPanel}.
   */
  private def createCategoryPanel(spec: Category) = {
    val newCategory = CategoryPanel(deck.current, spec.getName, this)
    // When a card is selected in a category, the others should deselect
    val listener = TableSelectionListener(parent, newCategory.table, deck.current.getCategoryList(newCategory.name))
    newCategory.table.addMouseListener(listener)
    newCategory.table.getSelectionModel.addListSelectionListener(listener)
    // Add the behavior for the edit category button
    newCategory.editButton.addActionListener(_ => editCategory(newCategory.name))
    // Add the behavior for the remove category button
    newCategory.removeButton.addActionListener(_ => categories -= newCategory.name)
    // Add the behavior for the color edit button
    newCategory.colorButton.addActionListener(_ => {
      Option(JColorChooser.showDialog(this, "Choose a Color", newCategory.colorButton.color)).foreach{ newColor =>
        val oldColor = deck.current.getCategorySpec(newCategory.name).getColor
        val name = newCategory.name
        performAction(() => {
          val mod = deck.current.getCategorySpec(name)
          mod.setColor(newColor)
          deck.current.updateCategory(newCategory.name, mod)
          listTabs.setSelectedIndex(Categories.ordinal)
          true
        }, () => {
          val mod = deck.current.getCategorySpec(name)
          mod.setColor(oldColor)
          deck.current.updateCategory(newCategory.name, mod)
          listTabs.setSelectedIndex(Categories.ordinal)
          true
        })
      }
    })
    // Add the behavior for double-clicking the category title
    newCategory.addMouseListener(ChangeTitleListener(newCategory, (title) => categories(newCategory.name) = {
      val spec = Category(categories(newCategory.name))
      spec.setName(title)
      spec
    }))
    // Add behavior for the rank box
    newCategory.rankBox.addActionListener(_ => {
      if (newCategory.rankBox.isPopupVisible) {
        val name = newCategory.name
        val old = deck.current.getCategoryRank(newCategory.name)
        val target = newCategory.rankBox.getSelectedIndex
        performAction(() => {
          deck.current.swapCategoryRanks(name, target)
          for (panel <- categoryPanels)
            panel.rankBox.setSelectedIndex(deck.current.getCategoryRank(panel.name))
          listTabs.setSelectedIndex(Categories.ordinal)
          updateCategoryPanel()
          true
        }, () => {
          deck.current.swapCategoryRanks(name, old)
          for (panel <- categoryPanels)
            panel.rankBox.setSelectedIndex(deck.current.getCategoryRank(panel.name))
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

    val tableMenuCardItems = CardMenuItems(Some(this), parent.getSelectedCards, true)
    tableMenuCardItems.addAddItems(tableMenu)
    tableMenu.add(JSeparator())
    tableMenuCardItems.addRemoveItems(tableMenu)

    val categoriesSeparator = JSeparator()
    tableMenu.add(categoriesSeparator)

    // Quick edit categories
    val addToCategoryMenu = JMenu("Include in")
    tableMenu.add(addToCategoryMenu)
    val removeFromCategoryItem = JMenuItem(s"Exclude from ${spec.getName}")
    removeFromCategoryItem.addActionListener(_ => categories(newCategory.name) = {
      val mod = Category(deck.current.getCategorySpec(newCategory.name))
      newCategory.selectedCards.filter(mod.includes).foreach(mod.exclude)
      mod
    })
    tableMenu.add(removeFromCategoryItem)
    val removeFromCategoryMenu = JMenu("Exclude from")
    tableMenu.add(removeFromCategoryMenu)

    // Edit categories item
    val editCategoriesItem = JMenuItem("Edit Categories...")
    editCategoriesItem.addActionListener(_ => {
      val iePanel = IncludeExcludePanel(deck.current.categories.toSeq.sortBy(_.getName.toLowerCase), parent.getSelectedCards)
      if (JOptionPane.showConfirmDialog(this, JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
        editInclusion(iePanel.included, iePanel.excluded)
    })
    tableMenu.add(editCategoriesItem)

    tableMenu.add(JSeparator())

    // Edit tags item
    val editTagsItem = JMenuItem("Edit Tags...")
    editTagsItem.addActionListener(_ => CardTagPanel.editTags(parent.getSelectedCards, parent))
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
      (c) => categories.contains(c.getName),
      categories += _,
      (c) => categories -= c.getName
    ))

    // Category popup menu
    val categoryMenu = JPopupMenu()
    newCategory.setComponentPopupMenu(categoryMenu)

    // Cut, copy, paste
    val categoryCCP = CCPItems(newCategory, false)
    categoryCCP.cut.setText("Cut Category")
    categoryMenu.add(categoryCCP.cut)
    categoryCCP.copy.setText("Copy Category")
    categoryMenu.add(categoryCCP.copy)
    categoryCCP.paste.setText("Paste Category")
    categoryMenu.add(categoryCCP.paste)
    categoryMenu.add(JSeparator())

    // Edit item
    val editItem = JMenuItem("Edit...")
    editItem.addActionListener(_ => editCategory(newCategory.name))
    categoryMenu.add(editItem)

    // Delete item
    val deleteItem = JMenuItem("Delete")
    deleteItem.addActionListener(_ => deck.current.removeCategory(newCategory.name))
    categoryMenu.add(deleteItem)

    // Add to presets item
    val addPresetItem = JMenuItem("Add to presets")
    addPresetItem.addActionListener(_ => parent.addPreset(deck.current.getCategorySpec(newCategory.name)))
    categoryMenu.add(addPresetItem)

    categoryMenu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
      val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
      try {
        categoryCCP.paste.setEnabled(!categories.contains(clipboard.getData(DataFlavors.categoryFlavor).asInstanceOf[CategoryTransferData].spec.getName))
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
  private def do_addCategory(spec: Category): Boolean = Option(deck.current.addCategory(spec)).map(_ => {
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
  }).isDefined

  /**
   * Helper method for removing a category.
   * 
   * @param spec specification of the category to remove
   * @return true if the category was removed, and false otherwise.
   */
  private def do_removeCategory(spec: Category) = {
    val success = deck.current.removeCategory(spec)
    if (success) {
      val category = getCategoryPanel(spec.getName).get
      categoryPanels -= category
      category.stopObserving()
      categoryPanels.foreach(_.rankBox.removeItemAt(categoryPanels.size))
      listTabs.setSelectedIndex(Categories.ordinal)
      updateCategoryPanel()
      handCalculations.update()
    }
    success
  }

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
      def write(d: Deck, n: Option[String] = None) = {
        val copy = Deck(d)
        copy.sort(comp)
        n.foreach(wr.println(_))
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
      deck.get(deck.table.convertRowIndexToModel(index))
    else {
      categoryPanels.find(_.table == t) match {
        case Some(panel) => deck.current.getCategoryList(panel.name).get(panel.table.convertRowIndexToModel(index))
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
  def save(): Boolean = file.map(save(_)).getOrElse(false)

  /**
   * Save the deck to the given file (like Save As).
   *
   * @param f file to save to
   * @return true if the file was successfully saved, and false otherwise.
   */
  def save(f: File): Boolean = {
    val changes = deck.changes
    if (!changes.isEmpty) {
      changelogArea.append(s"""|~~~~~${DeckSerializer.ChangelogDateFormat.format(Date())}~~~~~)
                               |$changes
                               |""".stripMargin)
    }

    val sideboards = extras.map((l) => l.name -> l.current).toMap
    val manager = DeckSerializer(deck.current, sideboards, notesArea.getText, changelogArea.getText, Some(f))
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
      deck.current.categories.toSeq.sorted(sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex)(deck.current)).foreach((c) => {
        categoriesContainer.add(getCategoryPanel(c.getName).get)
        switchCategoryModel.addElement(c.getName)
      })
    }

    analyzeCategoryBox.setVisible(!deck.current.categories.isEmpty)
    analyzeCategoryCombo.setVisible(!deck.current.categories.isEmpty)
    if (deck.current.categories.isEmpty)
      analyzeCategoryBox.setSelected(false)
    else {
      val selectedForAnalysis = analyzeCategoryCombo.getItemAt(analyzeCategoryCombo.getSelectedIndex)
      analyzeCategoryCombo.removeAllItems()
      deck.current.categories.foreach((c) => analyzeCategoryCombo.addItem(c.getName))
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
    val lands = deck.current.collect{ case c if SettingsDialog.settings.editor.isLand(c) => deck.current.getEntry(c).count }.sum
    countLabel.setText(s"Total cards: ${deck.current.total}")
    landLabel.setText(s"Lands: $lands")
    nonlandLabel.setText(s"Nonlands: ${(deck.current.total - lands)}")

    val manaValue = deck.current
        .filterNot(_.types.exists(_.equalsIgnoreCase("land")))
        .flatMap((c) => Seq.tabulate(deck.current.getEntry(c).count)(_ => SettingsDialog.settings.editor.getManaValue(c)))
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
    (sectionsBox.getItemAt(sectionsBox.getSelectedIndex) match {
      case ByNothing => Seq(SettingsDialog.settings.editor.manaAnalysis.none)
      case ByColor   => SettingsDialog.settings.editor.manaAnalysis.colorColors
      case ByType    => SettingsDialog.settings.editor.manaAnalysis.typeColors
    }).zipWithIndex.foreach{ case (color, i) => manaCurveRenderer.setSeriesPaint(i, color) }
    val analyte = if (analyzeCategoryBox.isSelected) deck.current.getCategoryList(analyzeCategoryCombo.getItemAt(analyzeCategoryCombo.getSelectedIndex)) else deck.current
    val analyteLands = analyte.filter(SettingsDialog.settings.editor.isLand).map((c) => deck.current.getEntry(c).count).sum
    if (analyte.total - analyteLands > 0) {
      var sections = sectionsBox.getItemAt(sectionsBox.getSelectedIndex()) match {
        case ByNothing => Seq(if (analyzeCategoryBox.isSelected) analyzeCategoryCombo.getItemAt(analyzeCategoryCombo.getSelectedIndex) else "Main Deck")
        case ByColor   => Seq("Colorless", "White", "Blue", "Black", "Red", "Green", "Multicolored")
        case ByType    => Seq("Creature", "Artifact", "Enchantment", "Planeswalker", "Instant", "Sorcery"); // Land is omitted because we don't count them here
      }
      var sectionManaValues = sections.map((s) => s -> analyte
          .filter((c) => !SettingsDialog.settings.editor.isLand(c))
          .filter((c) => sectionsBox.getItemAt(sectionsBox.getSelectedIndex) match {
            case ByNothing => true
            case ByColor => s match {
              case "Colorless"    => c.colors.size == 0
              case "White"        => c.colors.size == 1 && c.colors.apply(0) == ManaType.White
              case "Blue"         => c.colors.size == 1 && c.colors.apply(0) == ManaType.Blue
              case "Black"        => c.colors.size == 1 && c.colors.apply(0) == ManaType.Black
              case "Red"          => c.colors.size == 1 && c.colors.apply(0) == ManaType.Red
              case "Green"        => c.colors.size == 1 && c.colors.apply(0) == ManaType.Green
              case "Multicolored" => c.colors.size > 1
              case _ => true
            }
            case ByType => c.types.exists(_.equalsIgnoreCase(s)) && !sections.slice(0, sections.indexOf(s)).exists(s => c.types.exists(_.equalsIgnoreCase(s)))
          })
          .flatMap((c) => Seq.tabulate(analyte.getEntry(c).count)(_ => SettingsDialog.settings.editor.getManaValue(c)))
          .toSeq.sorted
          .map(math.ceil)
      ).toMap
      val minMV = math.ceil(manaValue.head).toInt
      val maxMV = math.ceil(manaValue.last).toInt
      for (i <- minMV to maxMV) {
        sections.foreach((s) => manaCurve.addValue(sectionManaValues(s).count(_ == i), s, i.toString))
      }

      if (minMV >= 0) {
        if (maxMV < 0)
          throw IllegalStateException("min mana value but no max mana value")
        val choice = landsBox.getItemAt(landsBox.getSelectedIndex)
        landAxis.setLabel(choice.toString)
        for (i <- minMV to maxMV) {
          val v = choice match {
            case Played =>
              var e = 0.0
              var q = 0.0
              for (j <- 0 until math.min(i, lands)) {
                val p = Stats.hypergeometric(j, math.min(handCalculations.handSize + i - 1, deck.current.size), lands, deck.current.total)
                q += p
                e += j*p
              }
              e + i*(1 - q)
            case Drawn => (lands.toDouble/deck.current.total.toDouble)*math.min(handCalculations.handSize + i - 1, deck.current.total)
            case Probability =>
              var q = 0.0
              for (j <- 0 until i)
                q += Stats.hypergeometric(j, math.min(handCalculations.handSize + i - 1, deck.current.size), lands, deck.current.total)
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
    })
    hand.refresh()
    handCalculations.update()

    if (listTabs.getSelectedIndex > Categories.ordinal)
      listTabs.setSelectedIndex(MainTable.ordinal)
  }
}