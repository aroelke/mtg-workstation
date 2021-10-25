package editor.gui.editor

import editor.collection.deck.Deck
import editor.gui.display.CardTable
import editor.gui.display.CardTableModel
import editor.gui.generic.ColorButton
import editor.gui.generic.ComponentUtils
import editor.gui.settings.SettingsDialog
import editor.util.StringUtils
import editor.util.UnicodeSymbols

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.SystemColor
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.util.Collections
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
import scala.jdk.CollectionConverters._
import java.{util => ju}
import java.awt.Container

class CategoryPanel(private val deck: Deck, private var _name: String, private val editor: EditorFrame) extends JPanel {

  /** @return the name of the category corresponding to this CategoryPanel */
  def name = _name

  /**
   * Change the category this panel should display to a new one.
   * @param n name of the new category to display
   */
  @throws[NoSuchElementException]("if the deck does not have a category with that name")
  def name_=(n: String) = {
    if (!deck.containsCategory(n))
      throw ju.NoSuchElementException(s"deck does not have a category named $n")
    _name = n
  }
  private class FlashTimer(bg: Color, end: Int = 20, flash: Color = SystemColor.textHighlight) extends Timer(end, null) {
    private var count = 0
    addActionListener(_ => {
      count += 1
      if (count > end) {
        stop()
      } else {
        val ratio = count.toDouble/end.toDouble
        val r = (flash.getRed + (bg.getRed - flash.getRed)*ratio).toInt
        val g = (flash.getGreen + (bg.getGreen - flash.getGreen)*ratio).toInt
        val b = (flash.getBlue + (bg.getBlue - flash.getBlue)*ratio).toInt
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
  private val timer = FlashTimer(getBackground)

  /** Briefly flash to draw attention to this CategoryPanel. */
  def flash() = timer.restart()

  private val border = BorderFactory.createTitledBorder(name)
  setBorder(border)
  setLayout(BorderLayout())

  private val topPanel = JPanel()
  topPanel.setLayout(BorderLayout())

  // Labels showing category stats
  private val statsPanel = Box(BoxLayout.X_AXIS)
  private val countLabel = JLabel(s"Cards: ${deck.getCategoryList(name).total}")
  statsPanel.add(countLabel)
  statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE))
  private val avgManaValueLabel = JLabel("Average mana value: 0")
  statsPanel.add(avgManaValueLabel)
  statsPanel.add(Box.createHorizontalGlue)
  topPanel.add(statsPanel, BorderLayout.WEST)

  // Panel containing edit and remove buttons
  private val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
  val rankBox = JComboBox((0 until deck.categories.size).toArray.map(Integer.valueOf(_)))
  rankBox.setSelectedIndex(deck.getCategoryRank(name))
  buttonPanel.add(rankBox)
  val colorButton = ColorButton(deck.getCategorySpec(name).getColor())
  buttonPanel.add(colorButton)
  val editButton = JButton(UnicodeSymbols.ELLIPSIS.toString)
  buttonPanel.add(editButton)
  val removeButton = JButton(UnicodeSymbols.MINUS.toString)
  buttonPanel.add(removeButton)
  topPanel.add(buttonPanel, BorderLayout.EAST)

  add(topPanel, BorderLayout.NORTH)

  // Table showing the cards in the category
  private var tableRows = SettingsDialog.settings.editor.categories.rows
  private val model = CardTableModel(editor, deck.getCategoryList(name), SettingsDialog.settings.editor.columns.asJava)
  val table = new CardTable(model) {
    override def getPreferredScrollableViewportSize = Dimension(getPreferredSize.width, tableRows*getRowHeight)
  }
  table.setStripeColor(SettingsDialog.settings.editor.stripe)
  for (i <- 0 until table.getColumnCount)
    if (model.isCellEditable(0, i))
      table.getColumn(model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(editor, model.getColumnData(i)))
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
  var resizeAdapter = new MouseInputAdapter {
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
        val maxRows = Math.max(deck.getCategoryList(name).total, tableRows)
        if (p.y <= base - table.getRowHeight/2 && tableRows > minRows) {
          val n = Math.min(((base - p.y) + table.getRowHeight - 1)/table.getRowHeight, tableRows - minRows)
          tableRows -= n
          base -= n*table.getRowHeight
          table.revalidate()
          table.repaint()
          revalidate()
          repaint()
        } else if (p.y >= base + table.getRowHeight/2 && tableRows < maxRows) {
          val n = Math.min(((p.y - base) + table.getRowHeight - 1)/table.getRowHeight, maxRows - tableRows)
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

  /**
   * Apply settings to this CategoryPanel.
   * @param editor [[EditorFrame]] containing this CategoryPanel
   */
  def applySettings(editor: EditorFrame) = {
    model.setColumns(SettingsDialog.settings.editor.columns.asJava)
    table.setStripeColor(SettingsDialog.settings.editor.stripe)
    for (i <- 0 until table.getColumnCount)
      if (model.isCellEditable(0, i))
        table.getColumn(model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(editor, model.getColumnData(i)))
  }

  /** @return the list of cards corresponding to the selected rows in the category's table */
  def getSelectedCards = table.getSelectedRows.map(r => deck.getCategoryList(name).get(table.convertRowIndexToModel(r))).toSeq

  /** Update the GUI to reflect changes in a category. */
  def update() = {
    countLabel.setText(s"Cards: ${deck.getCategoryList(name).total}")

    val avgManaValue = deck.stream
      .filter(deck.getCategorySpec(name).includes(_))
      .flatMap((c) => Collections.nCopies(deck.getEntry(c).count, c.manaValue).stream)
      .mapToDouble(_.toDouble)
      .average.orElse(0)
    avgManaValueLabel.setText(s"Average mana value: ${StringUtils.formatDouble(avgManaValue, 2)}")

    border.setTitle(name)
    table.revalidate()
    table.repaint()
    colorButton.setColor(deck.getCategorySpec(name).getColor())
    colorButton.repaint()
    revalidate()
    repaint()
  }
}