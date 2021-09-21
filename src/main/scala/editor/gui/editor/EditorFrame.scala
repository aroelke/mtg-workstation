package editor.gui.editor

import editor.collection.CardList
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
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

private object DeckData {
  def apply(name: Option[String] = None, deck: Deck = Deck()): DeckData = {
    val original = Deck()
    original.addAll(deck)
    DeckData(name, deck, original, null, null)
  }
}

private case class DeckData(var name: Option[String], current: Deck, var original: Deck, var model: CardTableModel, var table: CardTable) {
  def getChanges = {
    val changes: StringBuilder = StringBuilder()
    original.stream.forEach((c) => {
      val had = if (original.contains(c)) original.getEntry(c).count else 0
      val has = if (current.contains(c)) current.getEntry(c).count else 0
      if (has < had)
        changes ++= s"-${had - has}x ${c.unifiedName} (${c.expansion.name})\n"
    })
    current.stream.forEach((c) => {
      val had = if (original.contains(c)) original.getEntry(c).count else 0
      val has = if (current.contains(c)) current.getEntry(c).count else 0
      if (had < has)
        changes ++= s"+${has - had}x ${c.unifiedName} (${c.expansion.name})\n"
    })
    changes.result
  }
}

object EditorFrame {
  val MainDeck = 0

  /** Tab number containing the main list of cards. */
  val MainTable = 0
  /** Tab number containing categories. */
  val Categories = 1
  /** Tab number containing sample hands. */
  val SampleHands = 2
  /** Tab number containing user-defined notes. */
  val Notes = 3
  /** Tab number containing the changelog. */
  val Changelog = 4
}

class EditorFrame(parent: MainFrame, u: Int, manager: DeckSerializer = DeckSerializer()) extends JInternalFrame(if (manager.canSaveFile) manager.file.getName else s"Untitled $u", true, true, true, true)
  with SettingsObserver
{
  import EditorFrame._

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

          for (category <- deck.current.categories.asScala) {
            if (!category.includes(card)) {
              val categoryItem = JMenuItem(category.getName)
              categoryItem.addActionListener(_ => includeIn(card, category))
              addToCategoryMenu.add(categoryItem)
            }
          }
          addToCategoryMenu.setVisible(addToCategoryMenu.getItemCount > 0)

          for (category <- deck.current.categories.asScala) {
            if (category.includes(card)) {
              val categoryItem = JMenuItem(category.getName)
              categoryItem.addActionListener(_ => excludeFrom(card, category))
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

  private enum CategoryOrder(override val toString: String, order: (Deck) => Ordering[Category]) {
    def apply(d: Deck) = order(d)

    case AtoZ       extends CategoryOrder("A-Z", (d) => (a, b) => a.getName.compare(b.getName))
    case ZtoA       extends CategoryOrder("Z-A", (d) => (a, b) => -a.getName.compare(b.getName))
    case Ascending  extends CategoryOrder("Ascending Size", (d) => (a, b) => d.getCategoryList(a.getName).total.compare(d.getCategoryList(b.getName).total))
    case Descending extends CategoryOrder("Descending Size", (d) => (a, b) => -d.getCategoryList(a.getName).total.compare(d.getCategoryList(b.getName).total))
    case Priority   extends CategoryOrder("Increasing Rank", (d) => (a, b) => d.getCategoryRank(a.getName).compare(d.getCategoryRank(b.getName)))
    case Reverse    extends CategoryOrder("Decreasing Rank", (d) => (a, b) => -d.getCategoryRank(a.getName).compare(d.getCategoryRank(b.getName)))
  }
  import CategoryOrder._

  private enum ManaCurveSection(override val toString: String) {
    case ByNothing extends ManaCurveSection("Nothing")
    case ByColor   extends ManaCurveSection("Color")
    case ByType    extends ManaCurveSection("Card Type")    
  }
  import ManaCurveSection._

  private enum LandAnalysisChoice(override val toString: String) {
    case Played      extends LandAnalysisChoice("Expected Lands Played")
    case Drawn       extends LandAnalysisChoice("Expected Lands Drawn")
    case Probability extends LandAnalysisChoice("Probability of Drawing Lands")
  }
  import LandAnalysisChoice._

  setBounds(((u - 1) % 5)*30, ((u - 1) % 5)*30, 600, 600)
  setLayout(BorderLayout(0, 0))
  setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  private val lists = collection.mutable.ArrayBuffer[DeckData]()
  lists += DeckData(deck = manager.deck)
  private def deck = lists.head
  private def extras = lists.tail.filter(_ != null).toSeq

  private var _file: File = null
  private var _unsaved = false
  private val undoBuffer = collection.mutable.Stack[UndoableAction[Boolean, Boolean]]()
  private val redoBuffer = collection.mutable.Stack[UndoableAction[Boolean, Boolean]]()
  private var startingHandSize = SettingsDialog.settings.editor.hand.size
  if (manager.canSaveFile)
    file = manager.file
  else
    unsaved = true

  private val listTabs = JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
  add(listTabs, BorderLayout.CENTER)

  /* MAIN DECK TAB */
  private val mainPanel = JPanel(BorderLayout())

  deck.model = CardTableModel(this, deck.current, SettingsDialog.settings.editor.columns.asJava)
  deck.table = CardTable(deck.model)
  deck.table.setStripeColor(SettingsDialog.settings.editor.stripe)

  private val listener = TableSelectionListener(parent, deck.table, deck.current)
  deck.table.addMouseListener(listener)
  deck.table.getSelectionModel().addListSelectionListener(listener)
  for (i <- 0 until deck.table.getColumnCount)
    if (deck.model.isCellEditable(0, i))
      deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck.model.getColumnData(i)))
  deck.table.setTransferHandler(EditorTableTransferHandler(this, MainDeck))
  deck.table.setDragEnabled(true)
  deck.table.setDropMode(DropMode.ON)

  private val mainDeckPane = JScrollPane(deck.table)
  mainDeckPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED))
  mainPanel.add(mainDeckPane, BorderLayout.CENTER)

  private val deckButtons = VerticalButtonList("+", UnicodeSymbols.MINUS.toString, "X")
  deckButtons.get("+").addActionListener(_ => addCards(MainDeck, parent.getSelectedCards, 1))
  deckButtons.get(UnicodeSymbols.MINUS.toString).addActionListener(_ => removeCards(MainDeck,  parent.getSelectedCards, 1))
  deckButtons.get("X").addActionListener(_ => removeCards(MainDeck, parent.getSelectedCards, parent.getSelectedCards.map(deck.current.getEntry(_).count).max))
  mainPanel.add(deckButtons, BorderLayout.WEST)

  private val southLayout = CardLayout()
  private val southPanel = JPanel(southLayout)
  mainPanel.add(southPanel, BorderLayout.SOUTH)

  private val extrasPanel = JPanel()
  extrasPanel.setLayout(BorderLayout())
  southPanel.add(extrasPanel, "extras")

  private val extrasButtons = VerticalButtonList("+", UnicodeSymbols.MINUS.toString, "X")
  extrasButtons.get("+").addActionListener(_ => getSelectedExtraID.foreach(addCards(_, parent.getSelectedCards, 1)))
  extrasButtons.get(UnicodeSymbols.MINUS.toString).addActionListener(_ => getSelectedExtraID.foreach(removeCards(_, parent.getSelectedCards, 1)))
  extrasButtons.get("X").addActionListener(_ => {
    getSelectedExtraID.foreach(removeCards(_, parent.getSelectedCards, parent.getSelectedCards.map(sideboard.getEntry(_).count).max))
  })
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

  listTabs.addTab("Cards", mainPanel)

  // Main table popup menu
  private val tableMenu = JPopupMenu()
  deck.table.addMouseListener(TableMouseAdapter(deck.table, tableMenu))

  // Cut, copy, paste
  private val ccp = CCPItems(deck.table, true)
  tableMenu.add(ccp.cut)
  tableMenu.add(ccp.copy)
  tableMenu.add(ccp.paste)
  tableMenu.add(JSeparator())

  // Add/remove cards
  private val tableMenuCardItems = CardMenuItems(() => Some(this).toJava, () => parent.getSelectedCards.asJava, true)
  tableMenuCardItems.addAddItems(tableMenu)
  tableMenu.add(JSeparator())
  tableMenuCardItems.addRemoveItems(tableMenu)
  tableMenu.add(JSeparator())

  // Move cards to sideboard
  private val moveToMenu = JMenu("Move to")
  tableMenu.add(moveToMenu)
  private val moveAllToMenu = JMenu("Move all to")
  tableMenu.add(moveAllToMenu)
  private val moveSeparator = JSeparator()
  tableMenu.add(moveSeparator)

  // Quick edit categories
  private val addToCategoryMenu = JMenu("Include in")
  tableMenu.add(addToCategoryMenu)
  private val removeFromCategoryMenu = JMenu("Exclude from")
  tableMenu.add(removeFromCategoryMenu)

  // Edit categories item
  private val editCategoriesItem = JMenuItem("Edit Categories...")
  editCategoriesItem.addActionListener(_ => {
    val iePanel = IncludeExcludePanel(
      deck.current.categories.stream.sorted((a, b) => a.getName.compareToIgnoreCase(b.getName)).collect(Collectors.toList),
      parent.getSelectedCards.asJava
    )
    if (JOptionPane.showConfirmDialog(this, JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
      editInclusion(iePanel.getIncluded.asScala.map(_ -> _.asScala.toSet).toMap, iePanel.getExcluded.asScala.map(_ -> _.asScala.toSet).toMap)
  })
  tableMenu.add(editCategoriesItem)

  private val categoriesSeparator = JSeparator()
  tableMenu.add(categoriesSeparator)

  // Edit card tags item
  private val editTagsItem = JMenuItem("Edit Tags...")
  editTagsItem.addActionListener(_ => CardTagPanel.editTags(parent.getSelectedCards.asJava, parent))
  tableMenu.add(editTagsItem)

  // Table memu popup listeners
  tableMenu.addPopupMenuListener(TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu, editCategoriesItem, categoriesSeparator, deck.table))
  tableMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener(_ => {
    ccp.cut.setEnabled(!parent.getSelectedCards.isEmpty)
    ccp.copy.setEnabled(!parent.getSelectedCards.isEmpty)
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    ccp.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor))
    tableMenuCardItems.setEnabled(!parent.getSelectedCards.isEmpty)
    moveToMenu.setVisible(!extras.isEmpty)
    moveAllToMenu.setVisible(!extras.isEmpty)
    moveSeparator.setVisible(!extras.isEmpty)
    addToCategoryMenu.setEnabled(!categoryPanels.isEmpty)
    removeFromCategoryMenu.setEnabled(!categoryPanels.isEmpty)
    editCategoriesItem.setEnabled(!categoryPanels.isEmpty)
    editTagsItem.setEnabled(!parent.getSelectedCards.isEmpty)

    moveToMenu.removeAll()
    moveAllToMenu.removeAll()
    for (i <- 1 until lists.size) {
      if (lists(i) != null) {
        val id = i
        val moveToItem = JMenuItem(lists(i).name.get)
        moveToItem.addActionListener(_ => moveCards(MainDeck, id, parent.getSelectedCards.map((_ -> 1)).toMap))
        moveToMenu.add(moveToItem)
        val moveAllToItem = JMenuItem(lists(i).name.get)
        moveAllToItem.addActionListener(_ => moveCards(MainDeck, id, parent.getSelectedCards.map(c => (c -> deck.current.getEntry(c).count)).toMap))
        moveAllToMenu.add(moveAllToItem)
      }
    }
  }))

  /* CATEGORIES TAB */
  private val categoriesPanel = JPanel(BorderLayout())
  private val categoriesMainPanel = JPanel(BorderLayout())
  categoriesPanel.add(categoriesMainPanel, BorderLayout.CENTER)
  listTabs.addTab("Categories", categoriesPanel)

  // Panel containing components above the category panel
  private val categoryHeaderPanel = Box(BoxLayout.X_AXIS)
  categoriesMainPanel.add(categoryHeaderPanel, BorderLayout.NORTH)

  // Button to add a new category
  private val addCategoryPanel = JPanel(FlowLayout(FlowLayout.LEFT))
  private val addCategoryButton = JButton("Add")
  addCategoryButton.addActionListener(_ => createCategory.foreach(addCategory(_)))
  addCategoryPanel.add(addCategoryButton)
  categoryHeaderPanel.add(addCategoryPanel)

  // Combo box to change category sort order
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
  switchCategoryBox.addActionListener(_ => {
    if (switchCategoryBox.isPopupVisible) {
      getCategoryPanel(switchCategoryBox.getItemAt(switchCategoryBox.getSelectedIndex)).foreach((c) => {
        c.scrollRectToVisible(Rectangle(c.getSize()))
        c.flash()
      })
    }
  })
  switchCategoryPanel.add(JLabel("Go to category:"))
  switchCategoryPanel.add(switchCategoryBox)
  categoryHeaderPanel.add(switchCategoryPanel)

  // Make sure all parts of the category panel fit inside the window (this is necessary because
  // JScrollPanes do weird things with non-scroll-savvy components)
  private val categoriesSuperContainer = ScrollablePanel(BorderLayout(), ScrollablePanel.TRACK_WIDTH)
  private val categoriesContainer = Box(BoxLayout.Y_AXIS)
  private val categoryPanels = collection.mutable.ArrayBuffer[CategoryPanel]()

  // The category panel is a vertically-scrollable panel that contains all categories stacked vertically
  // The categories should have a constant height, but fit the container horizontally
  categoriesSuperContainer.add(categoriesContainer, BorderLayout.NORTH)
  private val categoriesPane = JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  categoriesMainPanel.add(categoriesPane, BorderLayout.CENTER)

  // Transfer handler for the category box
  // We explicitly use null here to cause exceptions if cutting or copying, as that should never happen
  categoriesPane.setTransferHandler(CategoryTransferHandler(null, (c) => containsCategory(c.getName), addCategory(_), null))

  // Popup menu for category container
  private val categoriesMenu = JPopupMenu()
  private val categoriesCCP = CCPItems(categoriesPane, false)
  categoriesCCP.paste.setText("Paste Category")
  categoriesMenu.add(categoriesCCP.paste)
  categoriesMenu.add(JSeparator())
  private val categoriesCreateItem = JMenuItem("Add Category...")
  categoriesCreateItem.addActionListener(_ => createCategory.foreach(addCategory(_)))
  categoriesMenu.add(categoriesCreateItem)
  categoriesMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener(_ => {
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    try {
        categoriesCCP.paste.setEnabled(!containsCategory((clipboard.getData(DataFlavors.categoryFlavor)).asInstanceOf[CategoryTransferData].spec.getName))
    } catch {
      case _ @ (_: UnsupportedFlavorException | _: IOException) => categoriesCCP.paste.setEnabled(false)
    }
  }))
  categoriesPane.setComponentPopupMenu(categoriesMenu)

  private val categoryButtons = VerticalButtonList("+", UnicodeSymbols.MINUS.toString, "X")
  categoryButtons.get("+").addActionListener(_ => addCards(MainDeck, parent.getSelectedCards, 1))
  categoryButtons.get(UnicodeSymbols.MINUS.toString).addActionListener(_ => removeCards(MainDeck, parent.getSelectedCards, 1))
  categoryButtons.get("X").addActionListener(_ => removeCards(MainDeck, parent.getSelectedCards, parent.getSelectedCards.map(deck.current.getEntry(_).count).max))
  categoriesPanel.add(categoryButtons, BorderLayout.WEST)

  /* MANA ANALYSIS TAB */
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

  listTabs.addTab("Mana Analysis", manaAnalysisPanel)

  /* SAMPLE HAND TAB */
  private val handPanel = JPanel(BorderLayout())

  // Table showing the cards in hand
  private val hand = Hand(deck.current)

  private val imagePanel = ScrollablePanel(ScrollablePanel.TRACK_HEIGHT)
  imagePanel.setLayout(BoxLayout(imagePanel, BoxLayout.X_AXIS))
  private val imagePane = JScrollPane(imagePanel)
  imagePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
  setHandBackground(SettingsDialog.settings.editor.hand.background)

  // Control panel for manipulating the sample hand
  private val handModPanel = JPanel(FlowLayout(FlowLayout.CENTER, 5, 5))
  private val newHandButton = JButton("New Hand")
  newHandButton.addActionListener(_ => {
    hand.newHand(startingHandSize)

    imagePanel.removeAll()
    hand.stream.forEach(c => {
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
    hand.stream.forEach((c) => {
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
  listTabs.addTab("Sample Hand", handPanel)
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
            listTabs.setSelectedIndex(Notes)
            undoing = false
          }
          true
        }, () => {
          notes.pop
          undoing = true
          notesArea.setText(notes.top)
          listTabs.setSelectedIndex(Notes)
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
  private val notesCCP = CCPItems(() => notesArea, true)
  private val notesMenu = JPopupMenu()
  notesMenu.add(notesCCP.cut)
  notesMenu.add(notesCCP.copy)
  notesMenu.add(notesCCP.paste)
  notesMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener(_ => {
    val text = notesArea.getSelectedText
    notesCCP.cut.setEnabled(text != null && !text.isEmpty)
    notesCCP.copy.setEnabled(text != null && !text.isEmpty)
    notesCCP.paste.setEnabled(Toolkit.getDefaultToolkit.getSystemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
  }))
  notesArea.setComponentPopupMenu(notesMenu)
  listTabs.addTab("Notes", JScrollPane(notesArea))

  // Panel to show the stats of the deck
  private val bottomPanel = JPanel(BorderLayout())
  getContentPane.add(bottomPanel, BorderLayout.SOUTH)

  // Labels to counts for total cards, lands, and nonlands
  private val statsPanel = Box(BoxLayout.X_AXIS)
  statsPanel.add(Box.createHorizontalStrut(10))
  private val countLabel = JLabel()
  statsPanel.add(countLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE))
  private val landLabel = JLabel()
  statsPanel.add(landLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE))
  private val nonlandLabel = JLabel()
  statsPanel.add(nonlandLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE))
  private val avgManaValueLabel = JLabel()
  statsPanel.add(avgManaValueLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE))
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
  legalityButton.addActionListener(_ => JOptionPane.showMessageDialog(this, LegalityPanel(this), s"Legality of $deckName", JOptionPane.PLAIN_MESSAGE))
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
    if (!changelogArea.getText.isEmpty && JOptionPane.showConfirmDialog(EditorFrame.this, "This change is permanent.  Clear change log?", "Clear Change Log?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      changelogArea.setText("")
      unsaved = true
    }
  })
  clearLogPanel.add(clearLogButton)
  changelogPanel.add(clearLogPanel, BorderLayout.SOUTH)
  listTabs.addTab("Change Log", changelogPanel)

  changelogArea.setText(manager.changelog)

  setTransferHandler(EditorFrameTransferHandler(this, MainDeck))

  for (spec <- deck.current.categories.asScala)
    categoryPanels += createCategoryPanel(spec)
  updateCategoryPanel()
  handCalculations.update()

  // Initialize extra lists
  extrasPane.addTab("+", null)
  for ((name, list) <- manager.sideboards.asScala) {
    val id = lists.size
    createExtra(name, id, extrasPane.getTabCount - 1)
    lists(id).current.addAll(list)
    lists(id).original.addAll(list)
  }
  extrasPane.setSelectedIndex(0)
  private val addSideboard = (e: MouseEvent) => {
    val index = if (extrasPane.getTabCount > 1) extrasPane.indexAtLocation(e.getX, e.getY) else 0
    val last = extrasPane.getTabCount - 1
    if (index == last) {
      val id = lists.size
      performAction(() => createExtra(s"Sideboard $id", id, last), () => deleteExtra(id, last))
    }
  }
  extrasPane.addMouseListener(MouseListenerFactory.createPressListener((e) => addSideboard(e)))
  emptyPanel.addMouseListener(MouseListenerFactory.createClickListener((e) => addSideboard(e)))

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

  /**
   * Add copies of a collection of cards to the specified list.
   * 
   * @param id ID of the list to add to
   * @param cards cards to add
   * @param n number of copies of each card to add
   * @return <code>true</code> if the cards were added, and <code>false</code> otherwise.
   */
  def addCards(id: Int, cards: Iterable[Card], n: Int) =  modifyCards(id, cards.map(_ -> n).toMap)

  /**
   * Add a new category to the main deck.
   * 
   * @param spec specification for the new category
   * @return <code>true</code> if adding the category was successful, and <code>false</code>
   * otherwise.
   */
  def addCategory(spec: Category) = if (deck.current.containsCategory(spec.getName)) false else {
    performAction(() => {
      if (deck.current.containsCategory(spec.getName))
        throw RuntimeException(s"attempting to add duplicate category ${spec.getName}")
      else
        do_addCategory(spec)
    }, () => do_removeCategory(spec))
  }

  override def applySettings(oldSettings: Settings, newSettings: Settings) = {
    applyChanges(oldSettings, newSettings)(_.editor.columns)(columns => deck.model.setColumns(columns.asJava))
                                          (_.editor.stripe)(deck.table.setStripeColor(_))
                                          (_.editor.hand.size)(startingHandSize = _)
                                          (_.editor.manaAnalysis.line)(landRenderer.setSeriesPaint(0, _))

    for (i <- 0 until deck.table.getColumnCount)
      if (deck.model.isCellEditable(0, i))
        deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck.model.getColumnData(i)))
    categoryPanels.foreach(_.applySettings(this))
    updateStats()
    update()
  }

  /**
   * Clear the selection in all of the tables in this EditorFrame except
   * for the given one.
   *
   * @param except table to not clear
   */
  def clearTableSelections(except: CardTable) = {
    lists.filter((l) => l != null && l.table != except).foreach(_.table.clearSelection())
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
   * Open the dialog to create a new specification for a deck category.
   *
   * @return the {@link Category} created by the dialog, or null if it was
   * canceled.
   */
  def createCategory = {
    var spec: Option[Category] = None
    while {{
      spec = CategoryEditorPanel.showCategoryEditor(this, spec.toJava).toScala
      spec.foreach((s) => {
      if (deck.current.containsCategory(s.getName))
        JOptionPane.showMessageDialog(this, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE)
      })
    }; spec.isDefined && deck.current.containsCategory(spec.get.getName) } do ()
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
    val listener = TableSelectionListener(parent, newCategory.table, deck.current.getCategoryList(newCategory.getCategoryName))
    newCategory.table.addMouseListener(listener)
    newCategory.table.getSelectionModel.addListSelectionListener(listener)
    // Add the behavior for the edit category button
    newCategory.editButton.addActionListener(_ => editCategory(newCategory.getCategoryName))
    // Add the behavior for the remove category button
    newCategory.removeButton.addActionListener(_ => removeCategory(newCategory.getCategoryName))
    // Add the behavior for the color edit button
    newCategory.colorButton.addActionListener(_ => {
      val newColor = JColorChooser.showDialog(this, "Choose a Color", newCategory.colorButton.color)
      if (newColor != null) {
        val oldColor = deck.current.getCategorySpec(newCategory.getCategoryName).getColor
        val name = newCategory.getCategoryName
        performAction(() => {
          val mod = deck.current.getCategorySpec(name)
          mod.setColor(newColor)
          deck.current.updateCategory(newCategory.getCategoryName, mod)
          listTabs.setSelectedIndex(Categories)
          true
        }, () => {
          val mod = deck.current.getCategorySpec(name)
          mod.setColor(oldColor)
          deck.current.updateCategory(newCategory.getCategoryName, mod)
          listTabs.setSelectedIndex(Categories)
          true
        })
      }
    })
    // Add the behavior for double-clicking the category title
    newCategory.addMouseListener(ChangeTitleListener(newCategory, (title) => {
      val oldName = newCategory.getCategoryName()
      if (!title.equals(oldName)) {
        performAction(() => {
          val mod = deck.current.getCategorySpec(oldName)
          mod.setName(title)
          deck.current.updateCategory(oldName, mod)
          newCategory.setCategoryName(title)
          updateCategoryPanel()
          true
        }, () => {
          val mod = deck.current.getCategorySpec(title)
          mod.setName(oldName)
          deck.current.updateCategory(title, mod)
          newCategory.setCategoryName(oldName)
          updateCategoryPanel()
          true
        })
      }
    }))
    // Add behavior for the rank box
    newCategory.rankBox.addActionListener(_ => {
      if (newCategory.rankBox.isPopupVisible) {
        val name = newCategory.getCategoryName
        val old = deck.current.getCategoryRank(newCategory.getCategoryName)
        val target = newCategory.rankBox.getSelectedIndex
        performAction(() => {
          deck.current.swapCategoryRanks(name, target)
          for (panel <- categoryPanels)
            panel.rankBox.setSelectedIndex(deck.current.getCategoryRank(panel.getCategoryName))
          listTabs.setSelectedIndex(Categories)
          updateCategoryPanel()
          true
        }, () => {
          deck.current.swapCategoryRanks(name, old)
          for (panel <- categoryPanels)
            panel.rankBox.setSelectedIndex(deck.current.getCategoryRank(panel.getCategoryName))
          listTabs.setSelectedIndex(Categories)
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

    val tableMenuCardItems = CardMenuItems(() => Some(this).toJava, () => parent.getSelectedCards.asJava, true)
    tableMenuCardItems.addAddItems(tableMenu)
    tableMenu.add(JSeparator())
    tableMenuCardItems.addRemoveItems(tableMenu)

    val categoriesSeparator = JSeparator()
    tableMenu.add(categoriesSeparator)

    // Quick edit categories
    val addToCategoryMenu = JMenu("Include in")
    tableMenu.add(addToCategoryMenu)
    val removeFromCategoryItem = JMenuItem(s"Exclude from ${spec.getName}")
    removeFromCategoryItem.addActionListener(_ => modifyInclusion(Seq.empty, newCategory.getSelectedCards.asScala, deck.current.getCategorySpec(newCategory.getCategoryName)))
    tableMenu.add(removeFromCategoryItem)
    val removeFromCategoryMenu = JMenu("Exclude from")
    tableMenu.add(removeFromCategoryMenu)

    // Edit categories item
    val editCategoriesItem = JMenuItem("Edit Categories...")
    editCategoriesItem.addActionListener(_ => {
      val iePanel = IncludeExcludePanel(
        deck.current.categories.stream.sorted((a, b) => a.getName.compareToIgnoreCase(b.getName)).collect(Collectors.toList),
        parent.getSelectedCards.asJava
      )
      if (JOptionPane.showConfirmDialog(this, JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
        editInclusion(iePanel.getIncluded.asScala.map(_ -> _.asScala.toSet).toMap, iePanel.getExcluded.asScala.map(_ -> _.asScala.toSet).toMap)
    })
    tableMenu.add(editCategoriesItem)

    tableMenu.add(JSeparator())

    // Edit tags item
    val editTagsItem = JMenuItem("Edit Tags...")
    editTagsItem.addActionListener(_ => CardTagPanel.editTags(parent.getSelectedCards.asJava, parent))
    tableMenu.add(editTagsItem)

    // Table menu popup listeners
    tableMenu.addPopupMenuListener(TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu, editCategoriesItem, categoriesSeparator, newCategory.table))
    tableMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener(_ => {
      cardCCP.cut.setEnabled(!parent.getSelectedCards.isEmpty)
      cardCCP.copy.setEnabled(!parent.getSelectedCards.isEmpty)
      val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
      cardCCP.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor))

      removeFromCategoryItem.setText(s"Exclude from ${newCategory.getCategoryName}")
      tableMenuCardItems.setEnabled(!parent.getSelectedCards.isEmpty)
      editTagsItem.setEnabled(!parent.getSelectedCards.isEmpty)
    }))

    newCategory.setTransferHandler(CategoryTransferHandler(
      () => getCategory(newCategory.getCategoryName),
      (c) => containsCategory(c.getName),
      addCategory(_),
      (c) => removeCategory(c.getName)
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
    editItem.addActionListener(_ => editCategory(newCategory.getCategoryName))
    categoryMenu.add(editItem)

    // Delete item
    val deleteItem = JMenuItem("Delete")
    deleteItem.addActionListener(_ => deck.current.removeCategory(newCategory.getCategoryName))
    categoryMenu.add(deleteItem)

    // Add to presets item
    val addPresetItem = JMenuItem("Add to presets")
    addPresetItem.addActionListener(_ => parent.addPreset(deck.current.getCategorySpec(newCategory.getCategoryName)))
    categoryMenu.add(addPresetItem)

    categoryMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener(_ => {
      val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
      try {
        categoryCCP.paste.setEnabled(!containsCategory(clipboard.getData(DataFlavors.categoryFlavor).asInstanceOf[CategoryTransferData].spec.getName))
      } catch {
        case _ @ (_: UnsupportedFlavorException | _: IOException) =>
          // Technically using exceptions as control flow (as with unsupported flavors here) is bad
          // programming practice, but since the exception has to be caught here anyway it reduces
          // code size
          categoryCCP.paste().setEnabled(false)
      }
    }))

    newCategory.table.addMouseListener(TableMouseAdapter(newCategory.table, tableMenu))

    newCategory
  }

        /**
   * Create a new extra, uncategorized, untracked list, which usually will be used for a
   * sideboard.
   * 
   * @param name name of the extra list, i.e. "Sideboard"; should be unique
   * @param id ID of the extra to create
   * @param index index of the tab to insert the new list at
   * @return <code>true</code> if the list was created, and <code>false</code> otherwise.
   * @throws IllegalArgumentException if a list with the given name already exists
   */
  private def createExtra(name: String, id: Int, index: Int): Boolean = {
    if (id == 0)
      throw new IllegalArgumentException("only the main deck can have ID 0")
    else if (lists.size > id && lists(id) != null)
      throw new IllegalArgumentException(s"extra already exists at ID $id")
    else {
      if (extras.exists(_.name.get == name))
        throw new IllegalArgumentException(s"""sideboard "$name" already exists""")

      val newExtra = DeckData(Some(name))
      while { lists.size <= id } do lists += null
      lists(id) = newExtra

      val panel = EditablePanel(name, extrasPane)
      extrasPane.insertTab(name, null, initExtraList(id), null, index)
      extrasPane.setTabComponentAt(index, panel)
      extrasPane.setSelectedIndex(index)
      extrasPane.getTabComponentAt(extrasPane.getSelectedIndex).requestFocus()
      southLayout.show(southPanel, "extras")
      listTabs.setSelectedIndex(MainDeck)

      panel.addActionListener((e) => e.getActionCommand() match {
        case EditablePanel.CLOSE =>
          val n = panel.getTitle
          val extra = lists(id).copy()
          val i = extrasPane.indexOfTab(n)
          performAction(() => deleteExtra(id, i), () => createExtra(n, id, i) | lists(id).current.addAll(extra.current) | lists(id).original.addAll(extra.original))
        case EditablePanel.EDIT =>
          val current = panel.getTitle
          val old = panel.getOldTitle
          if (current.isEmpty)
            panel.setTitle(old)
          else if (extras.exists(_.name.get == current)) {
            panel.setTitle(old)
            JOptionPane.showMessageDialog(EditorFrame.this, s"""Sideboard "$current" already exists.""", "Error", JOptionPane.ERROR_MESSAGE)
          } else if (!current.equals(old)) {
            val j = extrasPane.indexOfTab(old)
            performAction(() => {
              newExtra.name = Some(current)
              extrasPane.getTabComponentAt(j).asInstanceOf[EditablePanel].setTitle(current)
              extrasPane.setTitleAt(j, current)
              listTabs.setSelectedIndex(MainDeck)
              true
            }, () => {
              newExtra.name = Some(old)
              extrasPane.getTabComponentAt(j).asInstanceOf[EditablePanel].setTitle(old)
              extrasPane.setTitleAt(j, old)
              listTabs.setSelectedIndex(MainDeck)
              true
            })
          }
        case EditablePanel.CANCEL =>
      })

      true
    }
  }

  /**
   * Get the file name of the deck.
   *
   * @return the name of the deck being edited (its file name).
   */
  def deckName = if (unsaved) getTitle.substring(0, getTitle.length - 2) else getTitle

  /**
   * @return The names of the extra lists.
   */
  def getExtraNames = extras.flatMap(_.name)

  /**
   * Delete an extra list. This just sets its index in the list of card lists to null,
   * so it can be reused later if this is undone.
   * 
   * @param id ID of the list to delete
   * @param index index of the tab containing the list
   * @return <code>true</code> if the list was successfully removed, and <code>false</code>
   * otherwise.
   * @throws IllegalArgumentException if the list with the given ID doesn't exist
   */
  private def deleteExtra(id: Int, index: Int) = {
    if (lists(id) == null)
      throw new IllegalArgumentException("missing sideboard with ID " + id)

    lists(id) = null
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
   * Helper method for adding a category.
   * 
   * @param spec specification of the new category
   * @return <code>true</code> if the category was successfully added, and <code>false</code>
   * otherwise
   */
  private def do_addCategory(spec: Category): Boolean = {
    deck.current.addCategory(spec)

    val category = createCategoryPanel(spec)
    categoryPanels += category

    for (c <- categoryPanels)
      if (c != category)
        c.rankBox.addItem(deck.current.categories.size - 1)

    listTabs.setSelectedIndex(Categories)
    updateCategoryPanel()
    SwingUtilities.invokeLater(() => {
      switchCategoryBox.setSelectedItem(category.getCategoryName)
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
   * @return <code>true</code> if the category was removed, and <code>false</code>
   * otherwise.
   */
  private def do_removeCategory(spec: Category) = {
    deck.current.removeCategory(spec)

    categoryPanels -= getCategoryPanel(spec.getName).get
    for (panel <- categoryPanels)
      panel.rankBox.removeItemAt(categoryPanels.size)

    listTabs.setSelectedIndex(Categories)
    updateCategoryPanel()
    handCalculations.update()

    true
  }

  /**
   * Open the category dialog to edit the category with the given
   * name, if there is one, and then update the undo buffer.
   *
   * @param name name of the category to edit
   * @return <code>true</code> if the category was edited, and <code>false</code>
   * otherwise.
   */
  def editCategory(name: String) = {
    val toEdit = deck.current.getCategorySpec(name)
    if (toEdit == null)
      JOptionPane.showMessageDialog(this, s"Deck $deckName has no category named $name.", "Error", JOptionPane.ERROR_MESSAGE)
    CategoryEditorPanel.showCategoryEditor(this, Some(toEdit).toJava).map((s) => {
      val old = deck.current.getCategorySpec(name)
      performAction(() => {
        if (deck.current.updateCategory(old.getName, s) != old)
          throw RuntimeException("edited unexpected category")
        val panel = getCategoryPanel(old.getName).get
        panel.setCategoryName(s.getName)
        panel.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged()
        updateCategoryPanel()
        true
      }, () => {
        if (deck.current.updateCategory(s.getName(), old) != s)
          throw RuntimeException("restored from unexpected category")
        val panel = getCategoryPanel(s.getName).get
        panel.setCategoryName(old.getName)
        panel.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged()
        updateCategoryPanel()
        true
      })
    }).toScala.getOrElse(false)
  }

  /**
   * Change inclusion of cards in categories according to the given maps.
   *
   * @param included map of cards onto the set of categories they should become included in
   * @param excluded map of cards onto the set of categories they should become excluded from
   * @return <code>true</code> if any categories were modified, and <code>false</code>
   * otherwise.
   */
  def editInclusion(included: Map[Card, Set[Category]], excluded: Map[Card, Set[Category]]): Boolean = {
    val include = included.map{ case (card, in) => card -> in.filter(!_.includes(card)) }.filter{ case (_, in) => !in.isEmpty }
    val exclude = excluded.map{ case (card, out) => card -> out.filter(_.includes(card)) }.filter{ case (_, out) => !out.isEmpty }
    if (included.isEmpty && excluded.isEmpty) false else {
      performAction(() => {
        val mods = collection.mutable.HashMap[String, Category]()
        for ((card, in) <- include) {
          for (category <- in) {
            if (deck.current.getCategorySpec(category.getName).includes(card))
              throw IllegalArgumentException(s"$card is already in ${category.getName}")
            if (!mods.contains(category.getName))
              mods(category.getName) = deck.current.getCategorySpec(category.getName)
            mods(category.getName).include(card)
          }
        }
        for ((card, out) <- exclude) {
          for (category <- out) {
            if (!deck.current.getCategorySpec(category.getName).includes(card))
              throw IllegalArgumentException(s"$card is already not in ${category.getName}")
            if (!mods.contains(category.getName))
              mods(category.getName) = deck.current.getCategorySpec(category.getName)
            mods(category.getName).exclude(card)
          }
        }
        for ((card, mod) <- mods)
          deck.current.updateCategory(card, mod)
        for (panel <- categoryPanels)
          panel.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged()
        updateCategoryPanel()
        true
      }, () => {
        val mods = collection.mutable.HashMap[String, Category]()
        for ((card, in) <- include) {
          for (category <- in) {
            if (!deck.current.getCategorySpec(category.getName).includes(card))
              throw IllegalArgumentException(s"error undoing category edit: $card is already not in ${category.getName}")
            if (!mods.contains(category.getName))
              mods(category.getName) = deck.current.getCategorySpec(category.getName)
            mods(category.getName).exclude(card)
          }
        }
        for ((card, out) <- exclude) {
          for (category <- out) {
            if (deck.current.getCategorySpec(category.getName).includes(card))
              throw IllegalArgumentException(s"error undoing category edit: $card is already in ${category.getName}")
            if (!mods.contains(category.getName))
              mods(category.getName) = deck.current.getCategorySpec(category.getName)
            mods(category.getName).include(card)
          }
        }
        for ((card, mod) <- mods)
          deck.current.updateCategory(card, mod)
        for (panel <- categoryPanels)
          panel.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged()
        updateCategoryPanel()
        true
      })
    }
  }

  @deprecated
  def editInclusion(included: java.util.Map[Card, java.util.Set[Category]], excluded: java.util.Map[Card, java.util.Set[Category]]): Boolean = editInclusion(
    included.asScala.map{ case (card, categories) => card -> categories.asScala.toSet }.toMap,
    excluded.asScala.map{ case (card, categories) => card -> categories.asScala.toSet }.toMap
  )

  /**
   * Exclude a card from a category.
   * 
   * @param card card to exclude
   * @param spec specification for the category to exclude it from; must be a category in the
   * main deck
   * @return <code>true</code> if the card was successfully excluded from the category, and
   * <code>false</code> otherwise (such as if the card already wasn't in the category).
   */
  def excludeFrom(card: Card, spec: Category) = modifyInclusion(Seq.empty, Array(card), spec)

  /**
   * Export the deck to a different format.
   *
   * @param format formatter to use for export
   * @param file file to export to
   * @param extraNames names of extra lists to include in the export
   * @throws UnsupportedEncodingException
   * @throws FileNotFoundException if the file can't be opened
   * @throws NoSuchElementException if any of the named extra lists aren't in the deck
   */
  @throws[UnsupportedEncodingException]
  @throws[FileNotFoundException]
  @throws[NoSuchElementException]
  def exportList(format: CardListFormat, comp: Ordering[? >: CardList.Entry], extraNames: Seq[String], file: File) = {
    val wr = PrintWriter(OutputStreamWriter(FileOutputStream(file, false), "UTF8"))
    try {
      var copy: Deck = null

      if (format.hasHeader)
        wr.println(format.header)
      if (!deck.current.isEmpty) {
        copy = Deck(deck.current)
        copy.sort(comp)
        wr.print(format.format(copy))
      }
      for (extra <- extraNames) {
        val list = extras.find(_.name.get == extra)
        if (!list.isDefined)
          throw new NoSuchElementException(s"No extra list named $extra")
        if (!list.get.current.isEmpty) {
          copy = Deck(list.get.current)
          copy.sort(comp)
          wr.println()
          wr.println(extra)
          wr.print(format.format(copy))
        }
      }
    }
    finally {
      wr.close()
    }
  }

  def file = _file

  /**
   * Change the file this EditorFrame is associated with.  If the file has
   * not been saved, an error will be thrown instead.
   *
   * @param f file to associate with
   */
  @throws[RuntimeException]
  def file_=(f: File) = {
    if (unsaved)
      throw RuntimeException("Can't change the file of an unsaved deck")
    _file = f
    setTitle(f.getName)
  }

  /**
   * @return a copy of the extra list corresponding to the selected tab.
   */
  def getSelectedExtra = {
    val copy = Deck()
    copy.addAll(sideboard)
    copy
  }

  /**
   * Get the IDs of lists in the deck. ID 0 will always contain the main deck, and
   * IDs starting from 1 will contain extra lists. IDs do not have to be sequential,
   * but they will never be reused (unless list deletion is undone).
   * 
   * @return The list of IDs of card lists in the deck.
   */
  def getListIDs = lists.zipWithIndex.collect{ case (l, i) if l != null => i }.toSeq

  /**
   * @return The ID of the extra list corresponding to the selected tab.
   */
  def getSelectedExtraID = lists.zipWithIndex.find{ case (l, _) => l != null && l.name == getSelectedExtraName }.map(_._2)

  /**
   * @return the name of the extra list corresponding to the selected tab.
   */
  def getSelectedExtraName = if (extras.isEmpty) None else Some(extrasPane.getTitleAt(extrasPane.getSelectedIndex))

  /**
   * Get the card at the given index in the given table.
   *
   * @param t table to get the card from
   * @param index index into the given table to get a card from
   * @return the card in the deck at the given index in the given table, if the table is in this EditorFrame.
   */
  @throws[IllegalArgumentException]
  def getCardAt(t: CardTable, index: Int) = {
    if (t == deck.table)
      deck.current.get(deck.table.convertRowIndexToModel(index))
    else {
      categoryPanels.find(_.table == t) match {
        case Some(panel) => deck.current.getCategoryList(panel.getCategoryName).get(panel.table.convertRowIndexToModel(index))
        case None => throw IllegalArgumentException(s"Table not in deck $deckName")
      }
    }
  }

  /**
   * @return The categories in the main deck.
   */
  def getCategories = deck.current.categories

  /**
   * @param name name of the category to check
   * @return <code>true</code> if the deck has a category of that name, and <code>false</code> otherwise.
   */
  def containsCategory(name: String) = deck.current.containsCategory(name)
  
  /**
   * @param name name of the category to get
   * @return The specification for the chosen category
   * @throws IllegalArgumentException if no category of that name exists
   */
  @throws[IllegalArgumentException]
  def getCategory(name: String) = deck.current.getCategorySpec(name)

  /**
   * Get the panel for the category with the specified name in the deck.
   *
   * @param name name of the category to search for
   * @return the panel for the category with the specified name, if there is none.
   */
  private def getCategoryPanel(name: String) = categoryPanels.find(_.getCategoryName == name)

  /**
   * @return a copy of the main deck.
   */
  def getDeck = Deck(deck.current)

  /**
   * Get the cards in one of the deck lists. ID 0 corresponds to the main deck.
   *
   * @param id ID of the list to get
   * @return a copy of the list.
   * @throws ArrayIndexOutOfBoundsException if there is no list with the given ID.
   */
  @throws[ArrayIndexOutOfBoundsException]
  def getList(id: Int) = {
    if (lists(id) == null)
      throw ArrayIndexOutOfBoundsException(id)
    else
      Deck(lists(id).current)
  }

  @throws[ArrayIndexOutOfBoundsException]
  def getList(name: String) = lists.find((l) => l != null && l.name.exists(_ == name)).map(_.current).getOrElse(throw ArrayIndexOutOfBoundsException(name))

  /**
   * @return a {@link CardList} containing all of the cards in extra lists.
   */
  def getExtraCards = {
    val sideboard = Deck()
    extras.foreach(e => sideboard.addAll(e.current))
    sideboard
  }

  def unsaved = _unsaved
  /**
   * Change the frame title to reflect an unsaved state.
   */
  private def unsaved_=(u: Boolean) = {
    if (u && !unsaved)
      setTitle(s"$getTitle *")
    _unsaved = u
  }

  /**
   * Get the selected cards from the currently-selected list (even if it isn't this editor).
   *
   * @return a list of cards representing the current table selection
   */
  def getSelectedCards = parent.getSelectedCards

  /**
   * @param id ID of the list to search, with the empty string specifying the main deck
   * @param card card to search for
   * @return <code>true</code> if the specified list contains the specified card, and
   * <code>false</code> otherwise.
   * @see #getListIDs()
   * @throws ArrayIndexOutOfBoundsException if there is no list with the given ID.
   */
  @throws[ArrayIndexOutOfBoundsException]
  def hasCard(id: Int, card: Card) = if (lists(id) != null) lists(id).current.contains(card) else throw new ArrayIndexOutOfBoundsException(id)

  /**
   * Determine which lists contain the specified card.
   * 
   * @param card card to search for
   * @return A list of IDs corresponding to the lists that contain the given card.
   */
  def hasCard(card: Card) = lists.zipWithIndex.collect{ case (l, i) if l != null && l.current.contains(card) => i }.toSeq

  /**
   * Check whether or not this editor has the table with the current selection.
   *
   * @return true if this editor has the table with the current selection and false otherwise.
   */
  def hasSelectedCards = parent.getSelectedTable.exists((t) => lists.exists((l) => l != null && l.table == t) || categoryPanels.exists(_.table == t))

  /**
   * Include a card in a category.
   * 
   * @param card card to include
   * @param spec specification for the category to include the card in; must be a category in
   * the deck
   * @return <code>true</code> if the card was sucessfully included in the category, and
   * <code>false</code> otherwise (such as if the card was already in the category).
   */
  def includeIn(card: Card, spec: Category) = modifyInclusion(Seq(card), Seq.empty, spec)

  /**
   * Create and initialize the table, backing model, and menu items relating to a newly-created
   * extra list.
   * 
   * @param id ID of the new extra list
   * @return the pane that contains the table showing the extra list
   */
  def initExtraList(id: Int) =  {
    // Extra list's models
    lists(id).model = CardTableModel(this, lists(id).current, SettingsDialog.settings.editor.columns.asJava)
    lists(id).table = CardTable(lists(id).model)
    lists(id).table.setPreferredScrollableViewportSize(Dimension(lists(id).table.getPreferredScrollableViewportSize.width, 5*lists(id).table.getRowHeight))
    lists(id).table.setStripeColor(SettingsDialog.settings.editor.stripe)
    // When a card is selected in a sideboard table, select it for adding
    val listener = TableSelectionListener(parent, lists(id).table, lists(id).current)
    lists(id).table.addMouseListener(listener)
    lists(id).table.getSelectionModel.addListSelectionListener(listener)
    for (i <- 0 until lists(id).table.getColumnCount)
      if (lists(id).model.isCellEditable(0, i))
        lists(id).table.getColumn(lists(id).model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, lists(id).model.getColumnData(i)))
    lists(id).table.setTransferHandler(EditorTableTransferHandler(this, id))
    lists(id).table.setDragEnabled(true)
    lists(id).table.setDropMode(DropMode.ON)

    val sideboardPane = JScrollPane(lists(id).table)
    sideboardPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED))

    // Extra list's table menu
    val extraMenu = JPopupMenu()
    lists(id).table.addMouseListener(TableMouseAdapter(lists(id).table, extraMenu))

    // Cut, copy, paste
    val ccp = CCPItems(() => lists(id).table, true)
    extraMenu.add(ccp.cut)
    extraMenu.add(ccp.copy)
    extraMenu.add(ccp.paste)
    extraMenu.add(JSeparator())

    // Add/remove cards from sideboard
    val sideboardMenuCardItems = CardMenuItems(() => Some(this).toJava, () => parent.getSelectedCards.asJava, false)
    sideboardMenuCardItems.addAddItems(extraMenu)
    extraMenu.add(JSeparator())
    sideboardMenuCardItems.addRemoveItems(extraMenu)
    extraMenu.add(JSeparator())

    // Move cards to main deck
    val moveToMainItem = JMenuItem("Move to Main Deck")
    moveToMainItem.addActionListener(_ => moveCards(id, MainDeck, parent.getSelectedCards.map(_ -> 1).toMap))
    extraMenu.add(moveToMainItem)
    val moveAllToMainItem = JMenuItem("Move All to Main Deck")
    moveAllToMainItem.addActionListener(_ -> moveCards(
      id,
      MainDeck,
      parent.getSelectedCards.map((c) => c -> lists(id).current.getEntry(c).count).toMap
    ))
    extraMenu.add(moveAllToMainItem)
    extraMenu.add(JSeparator())

    // Edit card tags item in sideboard
    val sBeditTagsItem = JMenuItem("Edit Tags...")
    sBeditTagsItem.addActionListener(_ => CardTagPanel.editTags(parent.getSelectedCards.asJava, parent))
    extraMenu.add(sBeditTagsItem)

    extraMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener(_ => {
      ccp.cut.setEnabled(!parent.getSelectedCards.isEmpty)
      ccp.copy.setEnabled(!parent.getSelectedCards.isEmpty)
      val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
      ccp.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor))
    }))

    sideboardPane
  }

  /**
   * Change the number of copies of cards in the deck, adding and removing entries as
   * needed.
   * 
   * @param id ID of the list to add to
   * @param changes map of card onto integer representing the number of copies of each card to
   * add (positive number) or remove (negative number)
   * @return <code>true</code> if the list deck changed as a result, or <code>false</code>
   * otherwise
   */
  def modifyCards(id: Int, changes: Map[Card, Int]): Boolean = {
    if (changes.isEmpty || changes.forall{ case (_, n) => n == 0 })
      false
    else {
      val capped = changes.map{ case (card, n) => card -> Math.max(n, -lists(id).current.getEntry(card).count) }
      performAction(() => {
        val selected = parent.getSelectedCards
        val changed = capped.map{ case (card, n) =>
          if (n < 0)
            lists(id).current.remove(card, -n) > 0
          else if (n > 0)
            lists(id).current.add(card, n)
          else
            false
        }.fold(false)(_ || _)
        if (changed)
          updateTables(selected)
        changed
      }, () => {
        val selected = parent.getSelectedCards
        val changed = capped.map{ case (card, n) =>
          if (n < 0)
            lists(id).current.add(card, -n)
          else if (n > 0)
            lists(id).current.remove(card, n) > 0
          else
            false
        }.fold(false)(_ || _)
        if (changed)
          updateTables(selected)
        changed
      })
    }
  }

  @deprecated
  def modifyCards(id: Int, changes: java.util.Map[Card, Integer]): Boolean = modifyCards(id, changes.asScala.toMap.map{ case (c, n) => c -> n.toInt })

  /**
   * Modify the inclusion of cards in a category.
   * 
   * @param include cards to include in the category
   * @param exclude cards to exclude from the category
   * @param spec specification for the category to modify card inclusion for; must be part of
   * the deck
   * @return <code>true</code> if the category was modified, and <code>false</code>
   * otherwise (such as if the included cards already existed in the category and the
   * excluded cards didn't).
   */
  @throws[IllegalArgumentException]
  def modifyInclusion(include: Iterable[Card], exclude: Iterable[Card], spec: Category) = {
    if (!deck.current.containsCategory(spec.getName))
      throw IllegalArgumentException("can't include a card in a category that doesn't exist")
    if (deck.current.getCategorySpec(spec.getName) != spec)
      throw IllegalArgumentException("category name matches, but specification doesn't")

    val in = include.filter(spec.includes(_))
    val ex = exclude.filter(!spec.includes(_))
    if (in.isEmpty && ex.isEmpty) false else {
      val name = spec.getName
      performAction(() => {
        val mod = deck.current.getCategorySpec(name)
        for (c <- include) {
          if (mod.includes(c))
            throw IllegalArgumentException(s"${mod.getName} already includes $c")
          mod.include(c)
        }
        for (c <- exclude) {
          if (!mod.includes(c))
            throw IllegalArgumentException(s"${mod.getName} already doesn't include $c")
          mod.exclude(c)
        }
        deck.current.updateCategory(name, mod)
        for (panel <- categoryPanels)
          if (panel.getCategoryName == name)
            panel.table.getModel().asInstanceOf[AbstractTableModel].fireTableDataChanged()
        updateCategoryPanel()
        true
      }, () => {
        val mod = deck.current.getCategorySpec(name)
        for (c <- include) {
          if (!mod.includes(c))
            throw IllegalArgumentException(s"error undoing include: ${mod.getName} already doesn't include $c")
          mod.exclude(c)
        }
        for (c <- exclude) {
          if (mod.includes(c))
            throw IllegalArgumentException(s"error undoing exclude: ${mod.getName} already includes $c")
          mod.include(c)
        }
        deck.current.updateCategory(name, mod)
        for (panel <- categoryPanels)
          if (panel.getCategoryName.equals(name))
            panel.table.getModel().asInstanceOf[AbstractTableModel].fireTableDataChanged()
        updateCategoryPanel()
        true
      })
    }
  }

  /**
   * Move cards between lists.
   * 
   * @param from ID of the list to move from
   * @param to ID of the list to move to
   * @param moves Cards and amounts to move
   * @return <code>true</code> if the cards were successfully moved and <code>false</code> otherwise.
   */
  @throws[ArrayIndexOutOfBoundsException]
  def moveCards(from: Int, to: Int, moves: Map[Card, Int]): Boolean = {
    if (lists(from) == null)
      throw ArrayIndexOutOfBoundsException(from)
    if (lists(to) == null)
      throw ArrayIndexOutOfBoundsException(to)

    performAction(() => {
      val selected = parent.getSelectedCards
      val preserve = parent.getSelectedTable.exists((t) => t == lists(from).table) && moves.forall{ case (card, n) => lists(from).current.getEntry(card).count == n }
      if (lists(from).current.removeAll(moves.map{ case (c, n) => c -> Integer(n) }.asJava) != moves)
        throw CardException(moves.keySet.asJava, s"error moving cards from list $from")
      if (!lists(to).current.addAll(moves.map{ case (c, n) => c -> Integer(n) }.asJava))
        throw CardException(moves.keySet.asJava, s"could not move cards to list $to")
      if (preserve)
        parent.setSelectedComponents(lists(to).table, lists(to).current)
      updateTables(selected)
      if (preserve)
        lists(to).table.scrollRectToVisible(lists(to).table.getCellRect(lists(to).table.getSelectedRow, 0, true))
      true
    }, () => {
      val selected = parent.getSelectedCards
      val preserve = parent.getSelectedTable.exists((t) => t == lists(to).table) && moves.forall{ case (card, n) => lists(to).current.getEntry(card).count == n }
      if (!lists(from).current.addAll(moves.map{ case (c, n) => c -> Integer(n) }.asJava))
        throw CardException(moves.keySet.asJava, s"could not undo move from list $from")
      if (lists(to).current.removeAll(moves.map{ case (c, n) => c -> Integer(n) }.asJava) != moves)
        throw CardException(moves.keySet.asJava, s"error undoing move to list $to")
      if (preserve)
        parent.setSelectedComponents(lists(from).table, lists(from).current)
      updateTables(selected)
      if (preserve)
        lists(from).table.scrollRectToVisible(lists(from).table.getCellRect(lists(from).table.getSelectedRow, 0, true))
      true
    })
  }

  @deprecated
  def moveCards(from: Int, to: Int, moves: java.util.Map[Card, Integer]): Boolean = moveCards(from, to, moves.asScala.map{ case (c, n) => c -> n.toInt }.toMap)

  /**
   * Peform an action that can be undone.  Actions and their inverses should
   * return a boolean value indicating if they were successful.
   * 
   * @param action action to perform and its inverse
   * @return <code>true</code> if the action was successful, and <code>false</code>
   * otherwise.
   */
  private def performAction(action: UndoableAction[Boolean, Boolean]): Boolean = {
    redoBuffer.clear()
    undoBuffer.push(action)
    action.redo()
  }

  /**
   * Peform an action that can be undone.  Actions and their inverses should
   * return a boolean value indicating if they were successful.
   * 
   * @param redo action to perform; this gets performed upon calling this method
   * and stored for later in case it needs to be redone
   * @param undo action to perform when undoing the action
   * @return <code>true</code> if the action was successful, and <code>false</code>
   * otherwise.
   */
  private def performAction(redo: () => Boolean, undo: () => Boolean): Boolean = performAction(UndoableAction.createAction(() => {
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
   * Redo the last action that was undone, assuming nothing was done
   * between then and now.
   */
  @throws[RuntimeException]
  def redo() = if (!redoBuffer.isEmpty) {
    val action = redoBuffer.pop()
    if (action.redo()) {
      undoBuffer.push(action)
      true
    } else throw new RuntimeException("error redoing action")
  } else false

 /**
   * Remove some copies of each of a collection of cards from the specified list.
   * 
   * @param id ID of the list to remove cards from
   * @param cards cards to remove
   * @param n number of copies to remove
   * @return <code>true</code> if any copies were removed, and <code>false</code>
   * otherwise.
   */
  def removeCards(id: Int, cards: Iterable[Card], n: Int) = modifyCards(id, cards.map((c) => c -> -n).toMap)

  /**
   * Remove a category from the deck.
   * 
   * @param name name of the category to remove
   * @return <code>true</code> if the category was removed, and <code>false</code>
   * otherwise.
   */
  def removeCategory(name: String) = if (deck.current.containsCategory(name)) {
    val spec = deck.current.getCategorySpec(name)
    performAction(() => do_removeCategory(spec), () => {
      if (deck.current.containsCategory(name))
        throw RuntimeException(s"duplicate category $name found when attempting to undo removal")
      else
        do_addCategory(spec)
    })
  } else false

  /**
   * Save the deck to the current file.
   *
   * @return true if the file was successfully saved, and false otherwise.
   */
  def save(): Boolean = file != null && save(file)

        /**
   * Save the deck to the given file (like Save As).
   *
   * @param f file to save to
   * @return true if the file was successfully saved, and false otherwise.
   */
  def save(f: File): Boolean = {
    val changes = deck.getChanges
    if (!changes.isEmpty) {
      changelogArea.append(s"~~~~~${DeckSerializer.CHANGELOG_DATE.format(Date())}~~~~~\n")
      changelogArea.append(s"$changes\n")
    }

    val sideboards = lists.tail.collect{ case l if l != null => l.name.get -> l.current }.toMap
    val manager = DeckSerializer(deck.current, sideboards.asJava, notesArea.getText, changelogArea.getText)
    try {
      manager.save(f)
      deck.original = Deck()
      deck.original.addAll(deck.current)
      unsaved = false
      file = manager.file
      true
    }
    catch {
      case e: IOException =>
        JOptionPane.showMessageDialog(parent, s"Error saving ${f.getName}: ${e.getMessage}.", "Error", JOptionPane.ERROR_MESSAGE)
        return false
    }
  }

  /**
   * Set the background color for the sample hand panel.
   *
   * @param col new background color for the sample hand panel.
   */
  def setHandBackground(col: Color) = {
    imagePanel.setBackground(col)
    for (c <- imagePanel.getComponents)
      c.setBackground(col)
    imagePane.getViewport.setBackground(col)
  }

  /**
   * @return the Deck corresponding to the tab that's currently active in the sideboards
   * panel.
   */
  private def sideboard = getSelectedExtraID.map(lists(_).current).getOrElse(Deck())

  /**
   * Undo the last action that was performed on the deck.
   * 
   * @return <code>true</code> if the action was successfully undone, and
   * <code>false</code> otherwise.
   */
  def undo() = if (!undoBuffer.isEmpty) {
    var action = undoBuffer.pop()
    if (action.undo()) {
      redoBuffer.push(action)
      true
    }
    else throw new RuntimeException("error undoing action")
  } else false

  /**
   * Update the GUI to show the latest state of the deck.
   * XXX: Graphical errors could be attributed to this function
   */
  def update() = {
    revalidate()
    repaint()
    categoryPanels.foreach(_.update())
  }

  /**
   * Update the categories combo box with all of the current categories.
   */
  def updateCategoryPanel() = {
    categoriesContainer.removeAll()
    switchCategoryModel.removeAllElements()

    if (deck.current.categories.isEmpty)
      switchCategoryBox.setEnabled(false)
    else {
      switchCategoryBox.setEnabled(true)
      val categories = deck.current.categories.asScala.toSeq.sorted(sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex)(deck.current))

      categories.foreach((c) => categoriesContainer.add(getCategoryPanel(c.getName).get))
      categories.foreach((c) => switchCategoryModel.addElement(c.getName))
    }

    analyzeCategoryBox.setVisible(!deck.current.categories.isEmpty)
    analyzeCategoryCombo.setVisible(!deck.current.categories.isEmpty)
    if (deck.current.categories.isEmpty)
      analyzeCategoryBox.setSelected(false)
    else {
      val selectedForAnalysis = analyzeCategoryCombo.getItemAt(analyzeCategoryCombo.getSelectedIndex)
      analyzeCategoryCombo.removeAllItems()
      deck.current.categories.asScala.foreach((c) => analyzeCategoryCombo.addItem(c.getName))
      analyzeCategoryCombo.setMaximumSize(analyzeCategoryCombo.getPreferredSize())
      val indexForAnalysis = analyzeCategoryCombo.getModel.asInstanceOf[DefaultComboBoxModel[String]].getIndexOf(selectedForAnalysis)
      if (indexForAnalysis < 0) {
        analyzeCategoryCombo.setSelectedIndex(0)
        analyzeCategoryBox.setSelected(false)
      } else analyzeCategoryCombo.setSelectedIndex(indexForAnalysis)
    }

    categoriesContainer.revalidate()
    categoriesContainer.repaint()

    listTabs.setSelectedIndex(Categories)
  }

  /**
   * Update the card statistics to reflect the cards in the deck.
   */
  @throws[IllegalStateException]
  def updateStats() = {
    val lands = deck.current.stream.filter(SettingsDialog.settings.editor.isLand(_)).mapToInt((c) => deck.current.getEntry(c).count).sum
    countLabel.setText(s"Total cards: ${deck.current.total}")
    landLabel.setText(s"Lands: $lands")
    nonlandLabel.setText(s"Nonlands: ${(deck.current.total - lands)}")

    val manaValue = deck.current.asScala
        .filter((c) => !c.typeContains("land"))
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
    sectionsBox.getItemAt(sectionsBox.getSelectedIndex) match {
      case ByNothing => manaCurveRenderer.setSeriesPaint(0, SettingsDialog.settings.editor.manaAnalysis.none)
      case ByColor =>
        for (i <- 0 until SettingsDialog.settings.editor.manaAnalysis.colorColors.size)
          manaCurveRenderer.setSeriesPaint(i, SettingsDialog.settings.editor.manaAnalysis.colorColors(i))
      case ByType =>
        for (i <- 0 until SettingsDialog.settings.editor.manaAnalysis.typeColors.size)
          manaCurveRenderer.setSeriesPaint(i, SettingsDialog.settings.editor.manaAnalysis.typeColors(i))
    }
    val analyte = if (analyzeCategoryBox.isSelected) deck.current.getCategoryList(analyzeCategoryCombo.getItemAt(analyzeCategoryCombo.getSelectedIndex)) else deck.current
    val analyteLands = analyte.asScala.filter(SettingsDialog.settings.editor.isLand(_)).map((c) => deck.current.getEntry(c).count).sum
    if (analyte.total - analyteLands > 0) {
      var sections = sectionsBox.getItemAt(sectionsBox.getSelectedIndex()) match {
        case ByNothing => Seq(if (analyzeCategoryBox.isSelected) analyzeCategoryCombo.getItemAt(analyzeCategoryCombo.getSelectedIndex) else "Main Deck")
        case ByColor   => Seq("Colorless", "White", "Blue", "Black", "Red", "Green", "Multicolored")
        case ByType    => Seq("Creature", "Artifact", "Enchantment", "Planeswalker", "Instant", "Sorcery"); // Land is omitted because we don't count them here
      }
      var sectionManaValues = sections.map((s) => s -> {
        analyte.asScala
          .filter((c) => !SettingsDialog.settings.editor.isLand(c))
          .filter((c) =>sectionsBox.getItemAt(sectionsBox.getSelectedIndex) match {
            case ByNothing => true
            case ByColor => s match {
              case "Colorless"    => c.colors.size == 0
              case "White"        => c.colors.size == 1 && c.colors.get(0) == ManaType.WHITE
              case "Blue"         => c.colors.size == 1 && c.colors.get(0) == ManaType.BLUE
              case "Black"        => c.colors.size == 1 && c.colors.get(0) == ManaType.BLACK
              case "Red"          => c.colors.size == 1 && c.colors.get(0) == ManaType.RED
              case "Green"        => c.colors.size == 1 && c.colors.get(0) == ManaType.GREEN
              case "Multicolored" => c.colors.size > 1
              case _ => true
            }
            case ByType => c.typeContains(s) && !sections.slice(0, sections.indexOf(s)).exists(c.typeContains(_))
          })
          .flatMap((c) => Seq.tabulate(analyte.getEntry(c).count)(_ => SettingsDialog.settings.editor.getManaValue(c)))
          .toSeq.sorted
          .map(Math.ceil(_))
      }).toMap
      val minMV = Math.ceil(manaValue.head).toInt
      val maxMV = Math.ceil(manaValue.last).toInt
      for (i <- minMV to maxMV) {
        for (section <- sections) {
          val freq = sectionManaValues(section).count(_ == i)
          manaCurve.addValue(freq, section, i.toString)
        }
      }

      if (minMV >= 0) {
        if (maxMV < 0)
          throw new IllegalStateException("min mana value but no max mana value")
        val choice = landsBox.getItemAt(landsBox.getSelectedIndex)
        landAxis.setLabel(choice.toString)
        for (i <- minMV to maxMV) {
          val v = choice match {
            case Played =>
              var e = 0.0
              var q = 0.0
              for (j <- 0 until i) {
                val p = Stats.hypergeometric(j, Math.min(handCalculations.handSize + i - 1, deck.current.size), lands, deck.current.total)
                q += p
                e += j*p
              }
              e += i*(1 - q)
              e
            case Drawn => (lands.toDouble/deck.current.total.toDouble)*Math.min(handCalculations.handSize + i - 1, deck.current.total)
            case Probability =>
              var q = 0.0
              for (j <- 0 until i)
                q = q + Stats.hypergeometric(j, Math.min(handCalculations.handSize + i - 1, deck.current.size), lands, deck.current.total)
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
    lists.filter(_ != null).foreach(_.model.fireTableDataChanged())
    for (c <- categoryPanels)
      c.table.getModel.asInstanceOf[AbstractTableModel].fireTableDataChanged()
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

    if (listTabs.getSelectedIndex > Categories)
      listTabs.setSelectedIndex(MainDeck)
  }
}