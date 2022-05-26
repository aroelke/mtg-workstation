package editor.gui.editor

import editor.collection.CardListEntry
import editor.collection.deck.Deck
import editor.database.card.Card
import editor.gui.display.CardTable
import editor.gui.display.CardTableModel
import editor.gui.generic.ColorButton
import editor.gui.generic.ComponentUtils
import editor.gui.settings.Settings
import editor.gui.settings.SettingsDialog
import editor.gui.settings.SettingsObserver
import editor.util.StringUtils
import editor.util.UnicodeSymbols

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.SystemColor
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.util.Collections
import java.util.NoSuchElementException
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.event.MouseInputAdapter
import scala.collection.immutable.ListMap
import scala.jdk.CollectionConverters._

/**
 * A panel displaying information about a deck category.  In addition to the cards contained by the category,
 * it displays the average mana value and number of cards in it and has controls for modifying the categorization.
 * The panel can "flash" a color to draw the user's eyes to it if changes are made.
 * 
 * @constructor create a new panel tracking a category
 * @param deck [[Deck]] containing the cards to display
 * @param _name name of the category
 * @param editor parent [[EditorFrame]] containing the category
 * @param flashDuration amount of time in milliseconds the "flash" should last
 * @param flashColor color the panel changes when it "flashes"
 * 
 * @author Alec Roelke
 */ 
class CategoryPanel(private val deck: Deck, private var _name: String, private val editor: EditorFrame, flashDuration: Int = 20, flashColor: Color = SystemColor.textHighlight) extends JPanel
  with SettingsObserver
{
  /** @return the name of the category corresponding to this CategoryPanel */
  def name = _name

  /**
   * Change the category this panel should display to a new one.
   * @param n name of the new category to display
   */
  @throws[NoSuchElementException]("if the deck does not have a category with that name")
  def name_=(n: String) = {
    if (!deck.categories.contains(n))
      throw NoSuchElementException(s"deck does not have a category named $n")
    _name = n
  }

  private val timer = new Timer(flashDuration, null) {
    val bg = getBackground
    var count = 0
    addActionListener(_ => {
      count += 1
      if (count > flashDuration) {
        stop()
      } else {
        val ratio = count.toDouble/flashDuration.toDouble
        val r = (flashColor.getRed + (bg.getRed - flashColor.getRed)*ratio).toInt
        val g = (flashColor.getGreen + (bg.getGreen - flashColor.getGreen)*ratio).toInt
        val b = (flashColor.getBlue + (bg.getBlue - flashColor.getBlue)*ratio).toInt
        setBackground(Color(r, g, b))
      }
    })

    override def restart() = {
      count = 0
      super.restart()
    }

    override def stop() = {
      super.stop()
      setBackground(bg)
      repaint()
    }
  }

  /** Briefly flash to draw attention to this CategoryPanel. */
  def flash() = timer.restart()

  private val border = BorderFactory.createTitledBorder(name)
  setBorder(border)
  setLayout(BorderLayout())

  private val topPanel = JPanel()
  topPanel.setLayout(BorderLayout())

  // Labels showing category stats
  private val statsPanel = Box(BoxLayout.X_AXIS)
  private val countLabel = JLabel(s"Cards: ${deck.categories(name).list.total}")
  statsPanel.add(countLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TextSize))
  private val avgManaValueLabel = JLabel("Average mana value: 0")
  statsPanel.add(avgManaValueLabel)
  statsPanel.add(Box.createHorizontalGlue)
  topPanel.add(statsPanel, BorderLayout.WEST)

  // Panel containing edit and remove buttons
  private val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
  val rankBox = JComboBox((0 until deck.categories.size).toArray.map(Integer.valueOf(_)))
  rankBox.setSelectedIndex(deck.categories(name).rank)
  buttonPanel.add(rankBox)
  val colorButton = ColorButton(deck.categories(name).categorization.getColor)
  buttonPanel.add(colorButton)
  val editButton = JButton(UnicodeSymbols.Ellipsis.toString)
  buttonPanel.add(editButton)
  val removeButton = JButton(UnicodeSymbols.Minus.toString)
  buttonPanel.add(removeButton)
  topPanel.add(buttonPanel, BorderLayout.EAST)

  add(topPanel, BorderLayout.NORTH)

  // Table showing the cards in the category
  private var tableRows = SettingsDialog.settings.editor.categories.rows
  private val model = CardTableModel(deck.categories(name).list, SettingsDialog.settings.editor.columns, Some(editor))
  val table = new CardTable(model) {
    override def getPreferredScrollableViewportSize = Dimension(getPreferredSize.width, tableRows*getRowHeight)
  }
  table.stripe = SettingsDialog.settings.editor.stripe
  for (i <- 0 until table.getColumnCount)
    if (model.isCellEditable(0, i))
      table.getColumn(model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(editor, model.columns(i)))
  private val tablePane = JScrollPane(table)
  tablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
  tablePane.addMouseWheelListener(new MouseWheelListener {
    private lazy val parentScrollPane = {
      var pane = getParent
      while (pane != null && !pane.isInstanceOf[JScrollPane])
        pane = pane.getParent
      Option(pane.asInstanceOf[JScrollPane])
    }

    private def cloneEvent(e: MouseWheelEvent, pane: Container) = MouseWheelEvent(
      pane,
      e.getID,
      e.getWhen,
      e.getModifiersEx,
      1,
      1,
      e.getClickCount,
      false,
      e.getScrollType,
      e.getScrollAmount,
      e.getWheelRotation
    )

    private def max = tablePane.getVerticalScrollBar.getMaximum - tablePane.getVerticalScrollBar.getVisibleAmount

    private var previous = 0
    override def mouseWheelMoved(e: MouseWheelEvent) = {
      parentScrollPane.map(pane => {
        if (e.getWheelRotation < 0) {
          if (tablePane.getVerticalScrollBar.getValue == 0 && previous == 0)
            pane.dispatchEvent(cloneEvent(e, pane))
        } else if (tablePane.getVerticalScrollBar.getValue == max && previous == max) {
          pane.dispatchEvent(cloneEvent(e, pane))
        }
        previous = tablePane.getVerticalScrollBar.getValue
      }).getOrElse(tablePane.removeMouseWheelListener(this))
    }
  })
  private var resizeAdapter = new MouseInputAdapter {
    private var resizing = false
    private var base = 0

    override def mousePressed(e: MouseEvent) = {
      val p = SwingUtilities.convertPoint(e.getSource.asInstanceOf[Component], e.getPoint, tablePane)
      resizing = p.y >= tablePane.getHeight - 2
      if (resizing) {
        base = SwingUtilities.convertPoint(e.getSource.asInstanceOf[Component], e.getPoint, table).y
      }
    }

    override def mouseReleased(e: MouseEvent) = {
      resizing = false
      val p = SwingUtilities.convertPoint(e.getSource.asInstanceOf[Component], e.getPoint, tablePane)
      if (p.y >= tablePane.getHeight - 2)
        setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR))
      else
        setCursor(Cursor.getDefaultCursor)
    }

    override def mouseMoved(e: MouseEvent) = {
      val p = SwingUtilities.convertPoint(e.getSource.asInstanceOf[Component], e.getPoint, tablePane)
      if (p.y >= tablePane.getHeight - 2)
        setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR))
      else
        setCursor(Cursor.getDefaultCursor)
    }

    override def mouseDragged(e: MouseEvent) = {
      if (resizing) {
        val p = SwingUtilities.convertPoint(e.getSource.asInstanceOf[Component], e.getPoint, table)
        setCursor(Cursor(Cursor.S_RESIZE_CURSOR))
        val minRows = 1
        val maxRows = math.max(deck.categories(name).list.total, tableRows)
        if (p.y <= base - table.getRowHeight/2 && tableRows > minRows) {
          val n = math.min(((base - p.y) + table.getRowHeight - 1)/table.getRowHeight, tableRows - minRows)
          tableRows -= n
          base -= n*table.getRowHeight
          table.revalidate()
          table.repaint()
          revalidate()
          repaint()
        } else if (p.y >= base + table.getRowHeight/2 && tableRows < maxRows) {
          val n = math.min(((p.y - base) + table.getRowHeight - 1)/table.getRowHeight, maxRows - tableRows)
          tableRows += n
          base += n*table.getRowHeight
          table.revalidate()
          table.repaint()
          revalidate()
          repaint()
        }
      }
    }
  }
  tablePane.addMouseMotionListener(resizeAdapter)
  tablePane.addMouseListener(resizeAdapter)
  tablePane.getViewport.addMouseMotionListener(resizeAdapter)
  tablePane.getViewport.addMouseListener(resizeAdapter)
  table.addMouseMotionListener(resizeAdapter)
  table.addMouseListener(resizeAdapter)
  tablePane.getHorizontalScrollBar.addMouseMotionListener(resizeAdapter)
  tablePane.getHorizontalScrollBar.addMouseListener(resizeAdapter)
  tablePane.getVerticalScrollBar.addMouseMotionListener(resizeAdapter)
  tablePane.getVerticalScrollBar.addMouseListener(resizeAdapter)
  addMouseMotionListener(resizeAdapter)
  addMouseListener(resizeAdapter)
  
  add(tablePane, BorderLayout.CENTER)

  update()

  /** Apply settings to this CategoryPanel. */
  def applySettings(oldSettings: Settings, newSettings: Settings) = {
    applyChanges(oldSettings, newSettings)(_.editor.columns)(model.columns = _)
                                          (_.editor.stripe)(table.stripe = _)
    for (i <- 0 until table.getColumnCount)
      if (model.isCellEditable(0, i))
        table.getColumn(model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(editor, model.columns(i)))
  }

  /** @return the list of cards corresponding to the selected rows in the category's table */
  def selectedCards = table.getSelectedRows.map(r => deck.categories(name).list(table.convertRowIndexToModel(r))).toSeq

  /** Update the GUI to reflect changes in a category. */
  def update() = {
    countLabel.setText(s"Cards: ${deck.categories(name).list.total}")

    val flatManaValues = deck.filter((e) => deck.categories(name).categorization.includes(e.card)).flatMap((e) => Seq.fill(e.count)(e.card.manaValue))
    avgManaValueLabel.setText(s"Average mana value: ${StringUtils.formatDouble(flatManaValues.sum/flatManaValues.size, 2)}")

    border.setTitle(name)
    table.revalidate()
    table.repaint()
    colorButton.color = deck.categories(name).categorization.getColor
    revalidate()
    repaint()
  }
}