package editor.gui.settings

import editor.collection.Categorization
import editor.database.FormatConstraints
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.version.DatabaseVersion
import editor.database.version.UpdateFrequency
import editor.gui.MainFrame
import editor.gui.deck.CalculateHandPanel
import editor.gui.deck.CategoryEditorPanel
import editor.gui.display.CardTable
import editor.gui.display.CategoryList
import editor.gui.generic.ComponentUtils
import editor.gui.generic.ScrollablePanel
import editor.gui.generic.VerticalButtonList
import editor.util.UnicodeSymbols
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.chart.renderer.category.StackedBarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.data.category.DefaultCategoryDataset

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dialog
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Frame
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.text.ParseException
import java.util.Optional
import java.util.StringJoiner
import java.util.regex.Pattern
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JColorChooser
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JSpinner
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.SpinnerNumberModel
import javax.swing.SwingConstants
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableModel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

/**
 * Application-modal dialog that allows the user to make global changes to UI elements, including various colors,
 * defaults for options, and behaviors for multi-faced cards.  It takes a while to construct, so one should be
 * constructed on program start and then saved for later.  The dialog will automatically update when it's made visible.
 * 
 * @constructor create a new settings dialog
 * @param parent owner of the dialog, used for location and any live updates
 * 
 * @author Alec Roelke
 */
class SettingsDialog(parent: MainFrame) extends JDialog(parent, "Preferences", Dialog.ModalityType.APPLICATION_MODAL) {
  import SettingsDialog._

  setResizable(false)

  // Tree
  private val root = DefaultMutableTreeNode("Preferences")
  private val inventoryNode = DefaultMutableTreeNode("Inventory")
  root.add(inventoryNode)
  private val inventoryAppearanceNode = DefaultMutableTreeNode("Appearance")
  inventoryNode.add(inventoryAppearanceNode)
  private val editorNode = DefaultMutableTreeNode("Editor")
  private val editorCategoriesNode = DefaultMutableTreeNode("Preset Categories")
  editorNode.add(editorCategoriesNode)
  private val editorAppearanceNode = DefaultMutableTreeNode("Appearance")
  editorNode.add(editorAppearanceNode)
  private val handAppearanceNode = DefaultMutableTreeNode("Sample Hand")
  editorNode.add(handAppearanceNode)
  private val formatsNode = DefaultMutableTreeNode("Formats")
  editorNode.add(formatsNode)
  private val manaAnalysisNode = DefaultMutableTreeNode("Mana Analysis")
  editorNode.add(manaAnalysisNode)
  root.add(editorNode)

  // Settings panels
  private val settingsPanel = JPanel()
  settingsPanel.setLayout(CardLayout())
  add(settingsPanel, BorderLayout.CENTER)

  // Inventory paths
  private val inventoryPanel = Box(BoxLayout.Y_AXIS)
  inventoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
  settingsPanel.add(inventoryPanel, TreePath(inventoryNode.getPath.map(_.asInstanceOf[Object])).toString)

  // Inventory site
  private val inventorySitePanel = Box(BoxLayout.X_AXIS)
  inventorySitePanel.add(new JLabel("Inventory Site:"))
  private val siteStarLabel = JLabel("*")
  siteStarLabel.setForeground(Color.RED)
  inventorySitePanel.add(siteStarLabel)
  inventorySitePanel.add(Box.createHorizontalStrut(5))
  private val inventorySiteField = JTextField(15)
  inventorySitePanel.add(inventorySiteField)
  inventorySitePanel.setMaximumSize(new Dimension(Int.MaxValue, inventorySitePanel.getPreferredSize().height))
  inventoryPanel.add(inventorySitePanel)
  inventoryPanel.add(Box.createVerticalStrut(5))

  // Inventory file name
  private val inventoryFilePanel = Box(BoxLayout.X_AXIS)
  inventoryFilePanel.add(new JLabel("Inventory File:"))
  private val fileStarLabel = JLabel("*")
  fileStarLabel.setForeground(Color.RED)
  inventoryFilePanel.add(fileStarLabel)
  inventoryFilePanel.add(Box.createHorizontalStrut(5))
  private val inventoryFileField = JTextField(10)
  inventoryFilePanel.add(inventoryFileField)
  inventoryFilePanel.add(Box.createHorizontalStrut(5))
  private val currentVersionLabel = JLabel()
  currentVersionLabel.setFont(Font(currentVersionLabel.getFont.getFontName, Font.ITALIC, currentVersionLabel.getFont.getSize))
  inventoryFilePanel.add(currentVersionLabel)
  inventoryFilePanel.setMaximumSize(Dimension(Int.MaxValue, inventoryFilePanel.getPreferredSize().height))
  inventoryPanel.add(inventoryFilePanel)
  inventoryPanel.add(Box.createVerticalStrut(5))

  // Inventory file directory
  private val inventoryDirPanel = Box(BoxLayout.X_AXIS)
  inventoryDirPanel.add(JLabel("Inventory File Location:"))
  inventoryDirPanel.add(Box.createHorizontalStrut(5))
  private val inventoryDirField = JTextField(25)
  private val  inventoryChooser = JFileChooser()
  inventoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
  inventoryChooser.setAcceptAllFileFilterUsed(false)
  inventoryDirPanel.add(inventoryDirField)
  inventoryDirPanel.add(Box.createHorizontalStrut(5))
  private val inventoryDirButton = JButton(UnicodeSymbols.Ellipsis.toString)
  inventoryDirButton.addActionListener((e) => {
    if (inventoryChooser.showDialog(null, "Select Folder") == JFileChooser.APPROVE_OPTION) {
      val f = relativize(inventoryChooser.getSelectedFile)
      inventoryDirField.setText(f.getPath)
      inventoryChooser.setCurrentDirectory(f)
    }
  })
  inventoryDirPanel.add(inventoryDirButton)
  inventoryDirPanel.setMaximumSize(new Dimension(Int.MaxValue, inventoryDirPanel.getPreferredSize.height))
  inventoryPanel.add(inventoryDirPanel)
  inventoryPanel.add(Box.createVerticalStrut(5))

  // Card scans directory
  private val scansDirPanel = Box(BoxLayout.X_AXIS)
  scansDirPanel.add(JLabel("Card Images Location:"))
  scansDirPanel.add(Box.createHorizontalStrut(5))
  private val scansDirField = JTextField(25)
  private val scansChooser = JFileChooser()
  scansChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
  scansChooser.setAcceptAllFileFilterUsed(false)
  scansDirPanel.add(scansDirField)
  scansDirPanel.add(Box.createHorizontalStrut(5))
  private val scansDirButton = JButton(UnicodeSymbols.Ellipsis.toString)
  scansDirButton.addActionListener((e) => {
    if (scansChooser.showDialog(null, "Select Folder") == JFileChooser.APPROVE_OPTION) {
      val f = relativize(scansChooser.getSelectedFile)
      scansDirField.setText(f.getPath())
      scansChooser.setCurrentDirectory(f)
    }
  })
  scansDirPanel.add(scansDirButton)
  scansDirPanel.setMaximumSize(Dimension(Int.MaxValue, scansDirPanel.getPreferredSize.height))
  inventoryPanel.add(scansDirPanel)
  inventoryPanel.add(Box.createVerticalStrut(5))

  // Card images source
  private val imgSourcePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
  imgSourcePanel.add(JLabel("Download card images from:"))
  imgSourcePanel.add(Box.createHorizontalStrut(5))
  private val imgSourceBox = JComboBox(ImageSources.toArray)
  imgSourcePanel.add(imgSourceBox)
  imgSourcePanel.setMaximumSize(Dimension(Int.MaxValue, imgSourcePanel.getPreferredSize.height))
  inventoryPanel.add(imgSourcePanel)
  inventoryPanel.add(Box.createVerticalStrut(5))

  // Number of card images to keep
  private val imgLimitPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
  private val limitImageBox = JCheckBox("Limit downloaded card images to:")
  imgLimitPanel.add(limitImageBox)
  private val limitImageSpinner = JSpinner(SpinnerNumberModel(1, 1, Int.MaxValue, 1))
  imgLimitPanel.add(limitImageSpinner)
  limitImageBox.addActionListener((e) => limitImageSpinner.setEnabled(limitImageBox.isSelected))
  imgLimitPanel.setMaximumSize(Dimension(Int.MaxValue, imgLimitPanel.getPreferredSize.height))
  inventoryPanel.add(imgLimitPanel)
  inventoryPanel.add(Box.createVerticalStrut(5))

  // Check for update on startup
  private val updatePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
  updatePanel.add(JLabel("Update inventory on:"))
  updatePanel.add(Box.createHorizontalStrut(5))
  private val updateBox = JComboBox(UpdateFrequency.values)
  updatePanel.add(updateBox)
  updatePanel.setMaximumSize(Dimension(Int.MaxValue, updatePanel.getPreferredSize.height))
  inventoryPanel.add(updatePanel)
  inventoryPanel.add(Box.createVerticalStrut(5))

  // Show warnings from loading inventory
  private val suppressPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
  private val suppressCheckBox = JCheckBox("Show warnings after loading inventory")
  suppressPanel.add(suppressCheckBox)
  suppressPanel.add(Box.createHorizontalStrut(5))
  private val viewWarningsButton = JButton("View Warnings")
  viewWarningsButton.addActionListener(_ => showWarnings(parent, "Warnings from last inventory load:", inventoryWarnings, false))
  suppressPanel.add(viewWarningsButton)
  suppressPanel.setMaximumSize(Dimension(Int.MaxValue, suppressPanel.getPreferredSize.height))
  inventoryPanel.add(suppressPanel)

  inventoryPanel.add(Box.createVerticalGlue)

  // Warning panel
  private val pathWarningPanel = JPanel(new BorderLayout())
  private val pathWarningLabel = JLabel("*Warning:  Changing these settings may break functionality")
  pathWarningLabel.setFont(Font(pathWarningLabel.getFont().getFontName(), Font.ITALIC, pathWarningLabel.getFont().getSize()))
  pathWarningLabel.setForeground(Color.RED)
  pathWarningPanel.add(pathWarningLabel)
  pathWarningPanel.setMaximumSize(Dimension(Int.MaxValue, pathWarningLabel.getPreferredSize().height))
  inventoryPanel.add(pathWarningPanel)

  // Inventory appearance
  private val inventoryAppearancePanel = ScrollablePanel(ScrollablePanel.TrackWidth)
  inventoryAppearancePanel.setLayout(BoxLayout(inventoryAppearancePanel, BoxLayout.Y_AXIS))
  private val inventoryAppearanceScroll = JScrollPane(inventoryAppearancePanel)
  inventoryAppearanceScroll.setBorder(BorderFactory.createEmptyBorder())
  settingsPanel.add(inventoryAppearanceScroll, TreePath(inventoryAppearanceNode.getPath.map(_.asInstanceOf[Object])).toString)

  // Columns
  private val inventoryColumnsPanel = JPanel(GridLayout(0, 5))
  inventoryColumnsPanel.setBorder(BorderFactory.createTitledBorder("Columns"))
  private val inventoryColumnCheckBoxes = collection.mutable.HashMap[CardAttribute[?, ?], JCheckBox]()
  private val inventoryAttributes = CardAttribute.inventoryValues.sortBy(_.toString).toSeq
  for (characteristic <- inventoryAttributes) {
    val checkBox = JCheckBox(characteristic.toString())
    inventoryColumnCheckBoxes.put(characteristic, checkBox)
    inventoryColumnsPanel.add(checkBox)
  }
  inventoryAppearancePanel.add(inventoryColumnsPanel)

  // Stripe color
  private val inventoryColorPanel = JPanel(BorderLayout())
  inventoryColorPanel.setBorder(BorderFactory.createTitledBorder("Stripe Color"))
  private val inventoryStripeColor = JColorChooser()
  createStripeChooserPreview(inventoryStripeColor)
  inventoryColorPanel.add(inventoryStripeColor)
  inventoryAppearancePanel.add(inventoryColorPanel)

  // Card image background color
  private val scanBGPanel = JPanel(BorderLayout())
  scanBGPanel.setBorder(BorderFactory.createTitledBorder("Image Background Color"))
  private val scanBGChooser = JColorChooser()
  scanBGChooser.getSelectionModel.addChangeListener((e) => parent.setImageBackground(scanBGChooser.getColor))
  scanBGPanel.add(scanBGChooser)
  inventoryAppearancePanel.add(scanBGPanel)

  // Editor
  private val editorPanel = Box(BoxLayout.Y_AXIS)
  editorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
  settingsPanel.add(editorPanel, TreePath(editorNode.getPath.map(_.asInstanceOf[Object])).toString)

  // Recent count
  private val recentPanel = Box(BoxLayout.X_AXIS)
  recentPanel.add(JLabel("Recent file count:"))
  recentPanel.add(Box.createHorizontalStrut(5))
  private val recentSpinner = JSpinner(SpinnerNumberModel(1, 1, Int.MaxValue, 1))
  recentPanel.add(recentSpinner)
  recentPanel.add(Box.createHorizontalStrut(5))
  private val recentInfoLabel = JLabel("(Changes will not be visible until program restart)")
  recentInfoLabel.setFont(Font(recentInfoLabel.getFont.getFontName, Font.ITALIC, recentInfoLabel.getFont.getSize))
  recentPanel.add(recentInfoLabel)
  recentPanel.setMaximumSize(Dimension(recentPanel.getPreferredSize.width + 10, recentPanel.getPreferredSize.height))
  recentPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  editorPanel.add(recentPanel)
  editorPanel.add(Box.createVerticalStrut(5))

  // Whitelist and blacklist rows to show
  private val explicitsPanel = Box(BoxLayout.X_AXIS)
  explicitsPanel.add(JLabel("Blacklist/Whitelist rows to display:"))
  explicitsPanel.add(Box.createHorizontalStrut(5))
  private val explicitsSpinner = JSpinner(SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1))
  explicitsPanel.add(explicitsSpinner)
  explicitsPanel.setMaximumSize(Dimension(explicitsPanel.getPreferredSize().width + 5, explicitsPanel.getPreferredSize().height))
  explicitsPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  editorPanel.add(explicitsPanel)
  editorPanel.add(Box.createVerticalStrut(5))

  // Mana value choice for analysis
  private val manaValuePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
  manaValuePanel.add(JLabel("Mana value to analyze:"))
  manaValuePanel.add(Box.createHorizontalStrut(5))
  private val manaValueBox = JComboBox(ManaValueOptions.toArray)
  manaValuePanel.add(manaValueBox)
  manaValuePanel.setMaximumSize(Dimension(Int.MaxValue, manaValuePanel.getPreferredSize().height))
  manaValuePanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  editorPanel.add(manaValuePanel)
  editorPanel.add(Box.createVerticalStrut(5))

  // Which layouts to count lands on back sides
  private val landsPanel = JPanel(FlowLayout(0, 0, FlowLayout.LEADING))
  landsPanel.setBorder(BorderFactory.createTitledBorder("Count lands on back sides for layouts:"))
  private val landsCheckBoxes = editor.database.card.CardLayout.values.filter(_.isMultiFaced).map((l) => JCheckBox(l.toString, settings.editor.backFaceLands.contains(l)))
  landsCheckBoxes.foreach(landsPanel.add(_))
  landsPanel.setMaximumSize(Dimension(Int.MaxValue, landsPanel.getPreferredSize.height))
  landsPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  editorPanel.add(landsPanel)
  editorPanel.add(Box.createVerticalStrut(5))

  editorPanel.add(Box.createVerticalGlue)

  // Editor categories
  private val categoriesPanel = JPanel()
  categoriesPanel.setLayout(BorderLayout(5, 0))
  categoriesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
  settingsPanel.add(categoriesPanel, TreePath(editorCategoriesNode.getPath.map(_.asInstanceOf[Object])).toString)
  private val categoriesList = CategoryList("<html><i>&lt;Double-click to add or edit&gt;</i></html>")
  categoriesPanel.add(JScrollPane(categoriesList), BorderLayout.CENTER)

  // Categorization modification buttons
  private val categoryModPanel = VerticalButtonList(Seq("+", UnicodeSymbols.Ellipsis.toString, UnicodeSymbols.Minus.toString))
  categoryModPanel("+").addActionListener((e) => CategoryEditorPanel.showCategoryEditor(this).foreach(categoriesList.addCategory(_)))
  categoryModPanel(UnicodeSymbols.Ellipsis.toString).addActionListener((e) => {
    if (categoriesList.getSelectedIndex >= 0) {
      CategoryEditorPanel.showCategoryEditor(this, Option(categoriesList.categories(categoriesList.getSelectedIndex))).foreach((s) => {
        categoriesList.setCategoryAt(categoriesList.getSelectedIndex, s)
      })
    }
  })
  categoryModPanel(UnicodeSymbols.Minus.toString).addActionListener((e) => {
    if (categoriesList.getSelectedIndex >= 0)
      categoriesList.removeCategoryAt(categoriesList.getSelectedIndex)
  })
  categoriesPanel.add(categoryModPanel, BorderLayout.EAST)

  // Editor appearance
  private val editorAppearancePanel = Box(BoxLayout.Y_AXIS)
  settingsPanel.add(editorAppearancePanel, TreePath(editorAppearanceNode.getPath.map(_.asInstanceOf[Object])).toString)

  // Editor category rows
  private val rowsPanel = Box(BoxLayout.X_AXIS)
  rowsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0))
  rowsPanel.add(JLabel("Initial displayed rows in categories:"))
  rowsPanel.add(Box.createHorizontalStrut(5))
  private val rowsSpinner = JSpinner(SpinnerNumberModel(1, 1, Int.MaxValue, 1))
  rowsPanel.add(rowsSpinner)
  rowsPanel.setMaximumSize(Dimension(rowsPanel.getPreferredSize().width + 5, rowsPanel.getPreferredSize().height))
  rowsPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  editorAppearancePanel.add(rowsPanel)

  // Editor table columns
  private val editorColumnsPanel = JPanel(GridLayout(0, 5))
  editorColumnsPanel.setBorder(BorderFactory.createTitledBorder("Columns"))
  private val editorColumnCheckBoxes = collection.mutable.HashMap[CardAttribute[?, ?], JCheckBox]()
  private val editorAttributes = CardAttribute.displayableValues.sortBy(_.toString)
  for (characteristic <- editorAttributes) {
    val checkBox = JCheckBox(characteristic.toString())
    editorColumnCheckBoxes.put(characteristic, checkBox)
    editorColumnsPanel.add(checkBox)
  }
  editorColumnsPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  editorAppearancePanel.add(editorColumnsPanel)

  // Editor table stripe color
  private val editorColorPanel = JPanel(BorderLayout())
  editorColorPanel.setBorder(BorderFactory.createTitledBorder("Stripe Color"))
  private val editorStripeColor = JColorChooser()
  createStripeChooserPreview(editorStripeColor)
  editorColorPanel.add(editorStripeColor)
  editorColorPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  editorAppearancePanel.add(editorColorPanel)

  editorAppearancePanel.add(Box.createVerticalGlue)

  // Sample hand
  private val sampleHandPanel = ScrollablePanel(ScrollablePanel.TrackWidth)
  sampleHandPanel.setLayout(BoxLayout(sampleHandPanel, BoxLayout.Y_AXIS))
  private val sampleHandScroll = JScrollPane(sampleHandPanel)
  sampleHandScroll.setBorder(BorderFactory.createEmptyBorder())
  settingsPanel.add(sampleHandScroll, TreePath(handAppearanceNode.getPath.map(_.asInstanceOf[Object])).toString)

  sampleHandPanel.add(Box.createVerticalStrut(5))

  // Starting Size
  private val startingSizePanel = Box(BoxLayout.X_AXIS)
  startingSizePanel.add(Box.createHorizontalStrut(5))
  startingSizePanel.add(JLabel("Starting Size:"))
  startingSizePanel.add(Box.createHorizontalStrut(5))
  private val startingSizeSpinner = JSpinner(SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1))
  startingSizePanel.add(startingSizeSpinner)
  startingSizePanel.add(Box.createHorizontalGlue)
  startingSizePanel.setMaximumSize(Dimension(startingSizePanel.getPreferredSize.width + 5, startingSizePanel.getPreferredSize.height))
  startingSizePanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  sampleHandPanel.add(startingSizePanel)

  sampleHandPanel.add(Box.createVerticalStrut(5))

  // Expected counts round mode
  private val expectedRoundPanel = Box(BoxLayout.X_AXIS)
  expectedRoundPanel.add(Box.createHorizontalStrut(5))
  expectedRoundPanel.add(JLabel("Expected Categorization Count Round Mode:"))
  expectedRoundPanel.add(Box.createHorizontalStrut(5))
  private val roundGroup = ButtonGroup()
  private val modeButtons = collection.mutable.ArrayBuffer[JRadioButton]()
  for (mode <- CalculateHandPanel.RoundMode.keySet.toSeq.sorted) {
    val modeButton = JRadioButton(mode)
    roundGroup.add(modeButton)
    expectedRoundPanel.add(modeButton)
    expectedRoundPanel.add(Box.createHorizontalStrut(5))
    modeButtons += modeButton
  }
  expectedRoundPanel.setMaximumSize(expectedRoundPanel.getPreferredSize)
  expectedRoundPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  sampleHandPanel.add(expectedRoundPanel)

  sampleHandPanel.add(Box.createVerticalStrut(5))

  // Sample hand background color
  private val handBGColorPanel = JPanel(BorderLayout())
  handBGColorPanel.setBorder(BorderFactory.createTitledBorder("Background Color"))
  private val handBGColor = JColorChooser()
  handBGColor.getSelectionModel().addChangeListener((e) => parent.setHandBackground(handBGColor.getColor))
  handBGColorPanel.add(handBGColor)
  handBGColorPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
  sampleHandPanel.add(handBGColorPanel)

  // Format constraints
  private val formatsPanel = Box.createVerticalBox
  settingsPanel.add(formatsPanel, TreePath(formatsNode.getPath.map(_.asInstanceOf[Object])).toString)

  private val formatsTable = JTable(new DefaultTableModel(
    FormatConstraints.FormatNames.map((f) => FormatConstraints.Constraints(f).toArray(f).map(_.asInstanceOf[Object])).toArray,
    FormatConstraints.DataNames.map(_.asInstanceOf[Object]).toArray
  ) {
    override def getColumnClass(column: Int) = FormatConstraints.Classes(column)
    override def isCellEditable(rowIndex: Int, columnIndex: Int) = false
  })
  formatsTable.setFillsViewportHeight(true)
  private val formatsPane = JScrollPane(formatsTable)
  formatsPane.setAlignmentX(Component.LEFT_ALIGNMENT)
  formatsPanel.add(formatsPane)

  // Mana analysis
  private val manaAnalysisPanel = ScrollablePanel(ScrollablePanel.TrackWidth)
  manaAnalysisPanel.setLayout(BoxLayout(manaAnalysisPanel, BoxLayout.Y_AXIS))
  private val manaAnalysisScroll = new JScrollPane(manaAnalysisPanel)
  manaAnalysisScroll.setBorder(BorderFactory.createEmptyBorder())
  settingsPanel.add(manaAnalysisScroll, TreePath(manaAnalysisNode.getPath.map(_.asInstanceOf[Object])).toString)

  // Mana analysis section color selector
  private val manaAnalysisColorPanel = Box.createVerticalBox()
  manaAnalysisColorPanel.setBorder(BorderFactory.createTitledBorder("Plot Sections"))
  private val sections = Array(
    "Nothing",
    "Colorless", "White", "Blue", "Black", "Red", "Green", "Multicolored",
    "Creature", "Artifact", "Enchantment", "Planeswalker", "Instant", "Sorcery"
  )
  private val sectionChoosers = sections.map(_ -> JColorChooser()).toMap
  for ((_, chooser) <- sectionChoosers) {
    val preview = JPanel(GridLayout(1, 3))

    val nothingDataset = DefaultCategoryDataset()
    nothingDataset.addValue(3, "Nothing", "2")
    nothingDataset.addValue(6, "Nothing", "3")
    nothingDataset.addValue(4, "Nothing", "4")
    val nothingRenderer = BarRenderer()
    nothingRenderer.setBarPainter(StandardBarPainter())
    nothingRenderer.setDrawBarOutline(true)
    nothingRenderer.setDefaultOutlinePaint(Color.BLACK)
    nothingRenderer.setShadowVisible(false)
    sectionChoosers("Nothing").getSelectionModel.addChangeListener((e) => nothingRenderer.setSeriesPaint(0, sectionChoosers("Nothing").getColor))
    val nothingX = CategoryAxis()
    val nothingY = NumberAxis()
    val nothingPlot = CategoryPlot(nothingDataset, nothingX, nothingY, nothingRenderer)
    nothingPlot.setRangeGridlinesVisible(false)
    val nothingChart = JFreeChart("Nothing", JFreeChart.DEFAULT_TITLE_FONT, nothingPlot, true)
    val nothingPanel = ChartPanel(nothingChart)
    preview.add(nothingPanel)

    val colorsDataset = DefaultCategoryDataset()
    colorsDataset.addValue(1, "Colorless", "2")
    colorsDataset.addValue(2, "White", "2")
    colorsDataset.addValue(2, "Blue", "3")
    colorsDataset.addValue(2, "Black", "3")
    colorsDataset.addValue(2, "Red", "3")
    colorsDataset.addValue(3, "Green", "4")
    colorsDataset.addValue(1, "Multicolored", "4")
    val colorsRenderer = StackedBarRenderer()
    colorsRenderer.setBarPainter(StandardBarPainter())
    colorsRenderer.setDrawBarOutline(true)
    colorsRenderer.setDefaultOutlinePaint(Color.BLACK)
    colorsRenderer.setShadowVisible(false)
    sectionChoosers("Colorless").getSelectionModel.addChangeListener((e) => colorsRenderer.setSeriesPaint(0, sectionChoosers("Colorless").getColor))
    sectionChoosers("White").getSelectionModel.addChangeListener((e) => colorsRenderer.setSeriesPaint(1, sectionChoosers("White").getColor))
    sectionChoosers("Blue").getSelectionModel.addChangeListener((e) => colorsRenderer.setSeriesPaint(2, sectionChoosers("Blue").getColor))
    sectionChoosers("Black").getSelectionModel.addChangeListener((e) => colorsRenderer.setSeriesPaint(3, sectionChoosers("Black").getColor))
    sectionChoosers("Red").getSelectionModel.addChangeListener((e) => colorsRenderer.setSeriesPaint(4, sectionChoosers("Red").getColor))
    sectionChoosers("Green").getSelectionModel.addChangeListener((e) => colorsRenderer.setSeriesPaint(5, sectionChoosers("Green").getColor))
    sectionChoosers("Multicolored").getSelectionModel.addChangeListener((e) => colorsRenderer.setSeriesPaint(6, sectionChoosers("Multicolored").getColor))
    val colorsX = CategoryAxis()
    val colorsY = NumberAxis()
    val colorsPlot = CategoryPlot(colorsDataset, colorsX, colorsY, colorsRenderer)
    colorsPlot.setRangeGridlinesVisible(false)
    val colorsChart = JFreeChart("Colors", JFreeChart.DEFAULT_TITLE_FONT, colorsPlot, true)
    val colorsPanel = ChartPanel(colorsChart)
    preview.add(colorsPanel)

    val typesDataset = DefaultCategoryDataset()
    typesDataset.addValue(3, "Creature", "2")
    typesDataset.addValue(2, "Artifact", "3")
    typesDataset.addValue(2, "Enchantment", "3")
    typesDataset.addValue(2, "Planeswalker", "3")
    typesDataset.addValue(2, "Instant", "4")
    typesDataset.addValue(2, "Sorcery", "4")
    val typesRenderer = StackedBarRenderer()
    typesRenderer.setBarPainter(StandardBarPainter())
    typesRenderer.setDrawBarOutline(true)
    typesRenderer.setDefaultOutlinePaint(Color.BLACK)
    typesRenderer.setShadowVisible(false)
    sectionChoosers("Creature").getSelectionModel.addChangeListener((e) => typesRenderer.setSeriesPaint(0, sectionChoosers("Creature").getColor))
    sectionChoosers("Artifact").getSelectionModel.addChangeListener((e) => typesRenderer.setSeriesPaint(1, sectionChoosers("Artifact").getColor))
    sectionChoosers("Enchantment").getSelectionModel.addChangeListener((e) => typesRenderer.setSeriesPaint(2, sectionChoosers("Enchantment").getColor))
    sectionChoosers("Planeswalker").getSelectionModel.addChangeListener((e) => typesRenderer.setSeriesPaint(3, sectionChoosers("Planeswalker").getColor))
    sectionChoosers("Instant").getSelectionModel.addChangeListener((e) => typesRenderer.setSeriesPaint(4, sectionChoosers("Instant").getColor))
    sectionChoosers("Sorcery").getSelectionModel.addChangeListener((e) => typesRenderer.setSeriesPaint(5, sectionChoosers("Sorcery").getColor))
    val typesX = CategoryAxis()
    val typesY = NumberAxis()
    val typesPlot = CategoryPlot(typesDataset, typesX, typesY, typesRenderer)
    typesPlot.setRangeGridlinesVisible(false)
    val typesChart = JFreeChart("Types", JFreeChart.DEFAULT_TITLE_FONT, typesPlot, true)
    val typesPanel = new ChartPanel(typesChart)
    preview.add(typesPanel)

    preview.setPreferredSize(Dimension(chooser.getPreferredSize.width - 3*ComponentUtils.TextSize, chooser.getPreviewPanel.getPreferredSize().height*5/2))
    chooser.setPreviewPanel(preview)
  }

  private val manaAnalysisSectionPanel = Box.createHorizontalBox
  manaAnalysisSectionPanel.add(JLabel("Color for:"))
  manaAnalysisSectionPanel.add(Box.createHorizontalStrut(2))
  private val sectionsBox = JComboBox(sections)
  private val original = sectionsBox.getRenderer
  sectionsBox.setRenderer((list, value, index, isSelected, cellHasFocus) => {
    val label = original.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
    val panel = JPanel(new BorderLayout())
    panel.setForeground(label.getForeground())
    panel.setBackground(label.getBackground())
    panel.add(label, BorderLayout.CENTER)
    val color = new JPanel {
      override def paintComponent(g: Graphics): Unit = {
        super.paintComponent(g)
        g.setColor(sectionChoosers(value).getColor)
        g.fillRect(1, 1, getWidth - 3, getHeight - 3)
        g.setColor(Color.BLACK)
        g.drawRect(1, 1, getWidth - 3, getHeight - 3)
      }
    }
    color.setPreferredSize(Dimension(label.getPreferredSize.height, label.getPreferredSize.height))
    color.setForeground(label.getForeground)
    color.setBackground(label.getBackground)
    panel.add(color, BorderLayout.EAST)
    panel
  })
  sectionsBox.setMaximumSize(sectionsBox.getPreferredSize())
  manaAnalysisSectionPanel.add(sectionsBox)
  manaAnalysisSectionPanel.add(Box.createHorizontalGlue())
  manaAnalysisColorPanel.add(manaAnalysisSectionPanel)

  private val chooserLayout = CardLayout()
  private val sectionChooserPanel = JPanel(chooserLayout)
  sections.foreach(s => sectionChooserPanel.add(sectionChoosers(s), s))
  sectionsBox.addItemListener((e) => chooserLayout.show(sectionChooserPanel, sectionsBox.getItemAt(sectionsBox.getSelectedIndex)))
  manaAnalysisColorPanel.add(sectionChooserPanel)
  manaAnalysisPanel.add(manaAnalysisColorPanel)

  // Land analysis line color
  private val landAnalysisLinePanel = JPanel(BorderLayout())
  landAnalysisLinePanel.setBorder(BorderFactory.createTitledBorder("Land Analysis Line"))
  private val landLineChooser = JColorChooser()
  landAnalysisLinePanel.add(landLineChooser, BorderLayout.CENTER)
  manaAnalysisPanel.add(landAnalysisLinePanel)

  private val lineDataset = new DefaultCategoryDataset()
  lineDataset.addValue(0.15, "Lands", "1")
  lineDataset.addValue(0.95, "Lands", "2")
  lineDataset.addValue(1.35, "Lands", "3")
  private val lineRenderer = LineAndShapeRenderer()
  lineRenderer.setDefaultItemLabelGenerator(StandardCategoryItemLabelGenerator())
  lineRenderer.setDefaultItemLabelsVisible(true)
  landLineChooser.getSelectionModel().addChangeListener((e) => lineRenderer.setSeriesPaint(0, landLineChooser.getColor))
  private val lineX = CategoryAxis()
  private val lineY = NumberAxis()
  private val linePlot = CategoryPlot(lineDataset, lineX, lineY, lineRenderer)
  linePlot.setRangeGridlinesVisible(false)
  private val nothingChart = JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, linePlot, true)
  private val linePanel = ChartPanel(nothingChart)
  linePanel.setPreferredSize(Dimension(landLineChooser.getPreviewPanel.getPreferredSize.width, landLineChooser.getPreviewPanel.getPreferredSize.height*5/2))
  landLineChooser.setPreviewPanel(linePanel)

  // Default options for legality panel
  private val legalityDefaultsBox = Box.createHorizontalBox
  legalityDefaultsBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2))
  private val cmdrCheck = JCheckBox("")
  legalityDefaultsBox.add(cmdrCheck)
  private val cmdrGroup = ButtonGroup()
  private val cmdrMainDeck = JRadioButton("Main Deck")
  cmdrGroup.add(cmdrMainDeck)
  legalityDefaultsBox.add(cmdrMainDeck)
  private val cmdrAllLists = JRadioButton("All Lists")
  cmdrGroup.add(cmdrAllLists)
  legalityDefaultsBox.add(cmdrAllLists)
  private val cmdrList = JRadioButton()
  cmdrGroup.add(cmdrList)
  legalityDefaultsBox.add(cmdrList)
  private val cmdrListName = JTextField()
  legalityDefaultsBox.add(cmdrListName)
  legalityDefaultsBox.setMaximumSize(Dimension(Int.MaxValue, legalityDefaultsBox.getPreferredSize.height))
  legalityDefaultsBox.setAlignmentX(Component.LEFT_ALIGNMENT)
  formatsPanel.add(legalityDefaultsBox)
  private val sideboardBox = Box.createHorizontalBox
  sideboardBox.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2))
  private val sideCheck = JCheckBox("")
  sideboardBox.add(sideCheck)
  private val sideField = JTextField()
  sideboardBox.add(sideField)
  sideboardBox.setAlignmentX(Component.LEFT_ALIGNMENT)
  formatsPanel.add(sideboardBox)
  formatsPanel.add(Box.createVerticalGlue)

  cmdrCheck.addActionListener((e) => {
    cmdrCheck.setText(if (cmdrCheck.isSelected) "Search for commander in:" else "Search for commander")
    cmdrMainDeck.setVisible(cmdrCheck.isSelected)
    cmdrAllLists.setVisible(cmdrCheck.isSelected)
    cmdrList.setVisible(cmdrCheck.isSelected)
    cmdrListName.setVisible(cmdrCheck.isSelected)
  })
  private val cmdrListener: ActionListener = (e) => cmdrListName.setEnabled(cmdrList.isSelected)
  cmdrMainDeck.addActionListener(cmdrListener)
  cmdrAllLists.addActionListener(cmdrListener)
  cmdrList.addActionListener(cmdrListener)
  sideCheck.addActionListener((e) => {
    sideCheck.setText(if (sideCheck.isSelected) "Default sideboard name:" else "Include sideboard")
    sideField.setVisible(sideCheck.isSelected)
  })

  // Tree panel
  private val treePanel = JPanel(BorderLayout())
  private val tree = JTree(root)
  tree.getSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
  tree.getCellRenderer.asInstanceOf[DefaultTreeCellRenderer].setLeafIcon(null)
  tree.addTreeSelectionListener((e) => settingsPanel.getLayout.asInstanceOf[CardLayout].show(settingsPanel, e.getPath.toString))
  treePanel.add(tree, BorderLayout.CENTER)
  treePanel.add(JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST)
  treePanel.setPreferredSize(Dimension(130, 0))
  add(treePanel, BorderLayout.WEST)

  // Button panel
  private val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))

  private val applyButton = JButton("Apply")
  applyButton.addActionListener((e) => confirmSettings())
  buttonPanel.add(applyButton)

  private val okButton = JButton("OK")
  okButton.addActionListener((e) => {
    confirmSettings()
    setVisible(false)
  })
  buttonPanel.add(okButton)

  private val cancelButton = JButton("Cancel")
  cancelButton.addActionListener((e) => {
    rejectSettings()
    setVisible(false)
  })
  buttonPanel.add(cancelButton)

  private val bottomPanel = JPanel(BorderLayout())
  bottomPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH)
  bottomPanel.add(buttonPanel, BorderLayout.CENTER)
  add(bottomPanel, BorderLayout.SOUTH)

  pack()
  setLocationRelativeTo(parent)

  /** Confirm settings made while using the settings dialog and make corresponding changes to the UI. */
  def confirmSettings(): Unit = {
    var newSettings = try {
      recentSpinner.commitEdit()
      explicitsSpinner.commitEdit()
      rowsSpinner.commitEdit()
      startingSizeSpinner.commitEdit()

      val presets = new ArrayBuffer[Categorization](categoriesList.getCount)
      for (i <- 0 until categoriesList.getCount)
        presets += categoriesList.categories(i)
      
      settings.copy(
        inventory = settings.inventory.copy(
          source = inventorySiteField.getText,
          file = inventoryFileField.getText,
          location = inventoryDirField.getText,
          scans = scansDirField.getText,
          imageSource = imgSourceBox.getItemAt(imgSourceBox.getSelectedIndex),
          imageLimitEnable = limitImageBox.isSelected,
          imageLimit = limitImageSpinner.getValue.asInstanceOf[Int],
          update = updateBox.getItemAt(updateBox.getSelectedIndex),
          warn = suppressCheckBox.isSelected,
          columns = inventoryColumnCheckBoxes.collect{ case (a, b) if b.isSelected => a }.toIndexedSeq.sortBy(_.ordinal),
          background = scanBGChooser.getColor,
          stripe = inventoryStripeColor.getColor
        ),
        editor = settings.editor.copy(
          recents = settings.editor.recents.copy(count = recentSpinner.getValue.asInstanceOf[Int]),
          categories = settings.editor.categories.copy(
            presets = presets.toSeq,
            explicits = explicitsSpinner.getValue.asInstanceOf[Int]
          ),
          hand = settings.editor.hand.copy(
            size = startingSizeSpinner.getValue.asInstanceOf[Int],
            rounding = modeButtons.find(_.isSelected).map(_.getText).getOrElse("No Rounding"),
            background = handBGColor.getColor
          ),
          legality = settings.editor.legality.copy(
            searchForCommander = cmdrCheck.isSelected,
            main = cmdrMainDeck.isSelected || (cmdrCheck.isSelected && cmdrList.isSelected && cmdrListName.getText.isEmpty),
            all = cmdrAllLists.isSelected,
            list = cmdrListName.getText,
            sideboard = if (sideCheck.isSelected) sideField.getText else ""
          ),
          manaAnalysis = ManaAnalysisSettings(sectionChoosers.map{ case (s, c) => s -> c.getColor }.toMap).copy(line = landLineChooser.getColor),
          columns = editorColumnCheckBoxes.collect{ case (a, b) if b.isSelected => a }.toIndexedSeq.sortBy(_.ordinal),
          stripe = editorStripeColor.getColor,
          manaValue = manaValueBox.getItemAt(manaValueBox.getSelectedIndex),
          backFaceLands = landsCheckBoxes.filter(_.isSelected).map(b => editor.database.card.CardLayout.values.find(_.toString == b.getText).get).toSet
        )
      )
    } catch {
      case e: ParseException =>
        e.printStackTrace
        Settings()
      case e: MalformedURLException =>
        JOptionPane.showMessageDialog(this, s"Bad file URL: ${SettingsDialog.settings.inventory.url}", "Warning", JOptionPane.WARNING_MESSAGE)
        Settings()
    }
    if (newSettings.inventory.columns.isEmpty)
      newSettings = newSettings.copy(inventory = newSettings.inventory.copy(columns = InventorySettings().columns))
    if (newSettings.editor.columns.isEmpty)
      newSettings = newSettings.copy(editor = newSettings.editor.copy(columns = EditorSettings().columns))
    settings = newSettings
  }

  /** Reject any changes that were made while using the settings dialog. */
  def rejectSettings(): Unit = {
    parent.setImageBackground(settings.inventory.background)
    parent.setHandBackground(settings.editor.hand.background)
  }

  /**
   * Set the UI elements in the settings dialog to contain values based on the global settings, and then show
   * the dialog.
   */
  def updateAndShow() = {
    inventorySiteField.setText(settings.inventory.source)
    inventoryFileField.setText(settings.inventory.file)
    currentVersionLabel.setText("(Current version: " + settings.inventory.version + ")")
    inventoryDirField.setText(settings.inventory.location)
    inventoryChooser.setCurrentDirectory(File(inventoryDirField.getText).getAbsoluteFile)
    scansDirField.setText(settings.inventory.scans)
    scansChooser.setCurrentDirectory(File(scansDirField.getText).getAbsoluteFile)
    imgSourceBox.setSelectedIndex(Math.max(ImageSources.indexOf(settings.inventory.imageSource), 0))
    limitImageBox.setSelected(settings.inventory.imageLimitEnable)
    limitImageSpinner.setEnabled(settings.inventory.imageLimitEnable)
    limitImageSpinner.setValue(settings.inventory.imageLimit)
    updateBox.setSelectedIndex(settings.inventory.update.ordinal)
    suppressCheckBox.setSelected(settings.inventory.warn)
    viewWarningsButton.setEnabled(!inventoryWarnings.isEmpty)
    for ((a, b) <- inventoryColumnCheckBoxes)
      b.setSelected(settings.inventory.columns.contains(a))
    inventoryStripeColor.setColor(settings.inventory.stripe)
    scanBGChooser.setColor(settings.inventory.background)
    recentSpinner.getModel.setValue(settings.editor.recents.count)
    explicitsSpinner.getModel.setValue(settings.editor.categories.explicits)
    manaValueBox.setSelectedIndex(Math.max(ManaValueOptions.indexOf(settings.editor.manaValue), 0))
    categoriesList.clear()
    settings.editor.categories.presets.foreach(categoriesList.addCategory)
    rowsSpinner.getModel().setValue(settings.editor.categories.rows)
    for ((a, b) <- editorColumnCheckBoxes)
      b.setSelected(settings.editor.columns.contains(a))
    editorStripeColor.setColor(settings.editor.stripe)
    startingSizeSpinner.getModel.setValue(settings.editor.hand.size)
    for (mode <- modeButtons)
      mode.setSelected(mode.getText == settings.editor.hand.rounding)
    handBGColor.setColor(settings.editor.hand.background)
    cmdrCheck.setSelected(settings.editor.legality.searchForCommander)
    sideCheck.setSelected(!settings.editor.legality.sideboard.isEmpty)
    sideCheck.setText(if (sideCheck.isSelected) "Default sideboard name:" else "Include sideboard")
    sideField.setText(settings.editor.legality.sideboard)
    sideField.setVisible(sideCheck.isSelected)
    if (settings.editor.legality.searchForCommander) {
      cmdrCheck.setText("Search for commander in:")
      if (settings.editor.legality.main || (!settings.editor.legality.all && settings.editor.legality.list.isEmpty)) {
        cmdrMainDeck.setSelected(true)
      } else if (settings.editor.legality.all) {
        cmdrAllLists.setSelected(true)
      } else {
        cmdrList.setSelected(true)
      }
      cmdrListName.setEnabled(cmdrList.isSelected)
      cmdrListName.setText(settings.editor.legality.list)
    } else {
      cmdrCheck.setText("Search for commander")
      cmdrMainDeck.setVisible(false)
      cmdrAllLists.setVisible(false)
      cmdrList.setVisible(false)
      cmdrListName.setVisible(false)
    }
    sections.foreach(s => sectionChoosers(s).setColor(settings.editor.manaAnalysis(s)))
    landLineChooser.setColor(settings.editor.manaAnalysis.line)

    setVisible(true)
  }
}

object SettingsDialog {
  /** Warnings generated from the last time the inventory was loaded. */
  var inventoryWarnings = Seq.empty[String]

  /** Location to store settings and default location for other data. */
  val EditorHome = Path.of(System.getProperty("user.home"), ".mtgworkstation")

  /** Pattern to use for parsing an ARGB [[java.awt.Color]] from a string. */
  val ColorPattern = Pattern.compile("^#([0-9a-fA-F]{2})?([0-9a-fA-F]{6})$")

  /** Number of cards that's considered a "playset," that is, the max allowed in a normal constructed deck. */
  val PlaysetSize = 4

  /** Full path of the file storing settings. */
  val PropertiesFile = EditorHome.resolve("settings.json")

  /** Possible locations to download card images from. */
  val ImageSources = Seq("Scryfall", "Gatherer")

  /** Possible options for how to count the mana values of multi-faced cards. */
  val ManaValueOptions = Seq("Minimum", "Maximum", "Average", "Real")

  private[settings] val observers = collection.mutable.ArrayBuffer[SettingsObserver]()
  private var _settings = Settings()

  /** @return the global application settings. */
  def settings = _settings

  /**
   * Update settings, notifying any observers of the change.
   * @param s new settings
   */
  def settings_=(s: Settings) = {
    val old = _settings
    _settings = s
    observers.foreach(_.applySettings(old, s))
  }

  /** Create a preview panel containing a striped [[javax.swing.JTable]] to show the selected color for table stripes. */
  private[settings] def createStripeChooserPreview(chooser: JColorChooser): Unit = {
    val preview = Box(BoxLayout.X_AXIS)
    val table = CardTable(new AbstractTableModel {
      override def getColumnCount = 4
      override def getRowCount = 4
      override def getValueAt(rowIndex: Int, colIndex: Int) = "Sample Text"
    })
    table.stripe = chooser.getColor
    preview.add(table)
    chooser.getSelectionModel().addChangeListener((e) => table.stripe = chooser.getColor)
    chooser.setPreviewPanel(preview)
  }

  /** Find the path to a file relative to the current directory */
  private[settings] def relativize(f: File) = {
    val p = File(".").getAbsoluteFile.getParentFile.toPath
    var fp = f.getAbsoluteFile.toPath
    if (fp.startsWith(p)) {
      val rp = p.relativize(fp)
      if (rp.toString.isEmpty) File(".") else rp.toFile
    } else f
  }

  /**
   * Add a new preset category.  Make sure its whitelist and blacklist are empty.
   * @param category specification for the category to add
   */
  def addPresetCategory(category: Categorization): Unit = {
    settings = settings.copy(editor = settings.editor.copy(categories = settings.editor.categories.copy(presets = settings.editor.categories.presets :+ category)))
  }

  /** Load settings from [[PropertiesFile]]. */
  @throws[IOException]("if an error occurred while loading the file")
  @throws[MalformedURLException]("if the URL for the inventory source or version is invalid")
  def load(): Unit = {
    if (Files.exists(PropertiesFile))
      settings = MainFrame.Serializer.fromJson(Files.readAllLines(PropertiesFile).asScala.mkString("\n"), classOf[Settings])
    else
      settings = Settings()
  }

  /** Save settings in JSON format to [[PropertiesFile]]. */
  @throws[IOException]
  def save(): Unit = {
    if (!CardAttribute.Tags.tags.flatMap{ case (_, s) => s }.isEmpty) {
      Files.createDirectories(Path.of(settings.inventory.tags).getParent)
      Files.writeString(Path.of(settings.inventory.tags), MainFrame.Serializer.toJson(CardAttribute.Tags.tags.collect{ case (card, tags) if !tags.isEmpty => card.faces(0).scryfallid -> tags.asJava }.toMap.asJava))
    } else
      Files.deleteIfExists(Path.of(settings.inventory.tags))
    Files.writeString(PropertiesFile, MainFrame.Serializer.toJson(settings))
  }

  /**
   * Display warnings in a dialog box. The box's height will be constrained to half the height of its parent. It will also optionally
   * contain a check box allowing the user to suppress automatic display in the future. The callee is responsible for determining what
   * that means.
   * 
   * @param owner parent of the dialog
   * @param header header string to display at the top of the list of warnings
   * @param warnings list of strings to display, bulleted
   * @param canSuppress whether or not this dialog can be suppressed in the future
   * @return true if the dialog should be suppressed in the future, and false otherwise
   */
  def showWarnings(owner: Frame, header: String, warnings: Seq[String], canSuppress: Boolean) = {
    val str = (s"""$header<ul style="margin-top:0;margin-left:20pt">""" +: warnings).mkString("<html>", "<li>", "</ul></html>")
    val dialogPanel = JPanel(BorderLayout())
    val warningPanel = ScrollablePanel(ScrollablePanel.TrackWidth)
    warningPanel.add(JLabel(str))
    if (warningPanel.getPreferredSize.height < owner.getHeight/2)
      warningPanel.setPreferredScrollableViewportSize(warningPanel.getPreferredSize)
    else
      warningPanel.setPreferredScrollableViewportSize(Dimension(warningPanel.getPreferredSize.width, owner.getHeight/2))
    val warningScroll = JScrollPane(warningPanel)
    warningScroll.setBorder(BorderFactory.createEmptyBorder)
    dialogPanel.add(warningScroll, BorderLayout.CENTER)
    val suppressBox = JCheckBox("Don't show this warning in the future", !SettingsDialog.settings.inventory.warn)
    if (canSuppress)
      dialogPanel.add(suppressBox, BorderLayout.SOUTH)
    JOptionPane.showMessageDialog(owner, dialogPanel, "Warning", JOptionPane.WARNING_MESSAGE)
    canSuppress && suppressBox.isSelected
  }
}