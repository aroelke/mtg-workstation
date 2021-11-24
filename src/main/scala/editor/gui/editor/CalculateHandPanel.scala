package editor.gui.editor

import editor.collection.deck.Deck
import editor.gui.settings.SettingsDialog
import editor.util.Stats

import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.DefaultCellEditor
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.SpinnerNumberModel
import javax.swing.SwingConstants
import javax.swing.event.ChangeListener
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import scala.jdk.CollectionConverters._

/**
 * Companion object containing global information about hand calculation.
 */
object CalculateHandPanel {
  /**
   * Possible ways to round numbers in the [[CalculateHandPanel]].  The options are to not round
   * (display numbers to the nearest hundredth), round to the nearest integer, and truncate.
   */
  val RoundMode = Map(
    "No rounding" -> ((x: Double) => f"$x%.2f"),
    "Round to nearest" -> ((x: Double) => f"${math.round(x)}%d"),
    "Truncate" -> ((x: Double) => f"${x.toInt}%d")
  )
}

/**
 * Panel showing a table of deck categories with their probabilities of being drawn in the opening hand
 * and subsequent card draws. The number of desired max, min, or exact cards in each category can be specified,
 * and the table can be reorganized to show expected counts instead.
 * 
 * @constructor create a new hand calculations panel for a deck
 * @param deck deck to show calculations for
 * @param recalculateFunction what to do when the opening hand size is changed
 * 
 * @author Alec Roelke
 */
class CalculateHandPanel(deck: Deck, recalculateFunction: ChangeListener) extends JPanel(BorderLayout()) {
  import CalculateHandPanel._
  import RelationChoice._

  private case class ColumnInfo(name: String, clazz: Class[?], value: (String) => Any, editor: Option[(String) => TableCellEditor] = None)

  private enum DisplayMode(override val toString: String, cols: Seq[ColumnInfo], default: (Int, Int) => ColumnInfo) {
    def columns = cols.size
    def title(i: Int) = cols.applyOrElse(i, default(_, columns)).name
    def clazz(i: Int) = cols.applyOrElse(i, default(_, columns)).clazz
    def value(i: Int) = cols.applyOrElse(i, default(_, columns)).value
    def editor(i: Int) = cols.applyOrElse(i, default(_, columns)).editor

    case DesiredProbability extends DisplayMode("Probabilities", Seq(
      ColumnInfo("Kind of Card", classOf[String], identity),
      ColumnInfo("Count", classOf[Int], deck.getCategoryList(_).total),
      ColumnInfo("Desired", classOf[Int], desiredBoxes(_).getSelectedItem, Some((c) => DefaultCellEditor(desiredBoxes(c)))),
      ColumnInfo("Relation", classOf[RelationChoice], relationBoxes(_).getSelectedItem, Some((c) => DefaultCellEditor(relationBoxes(c)))),
      ColumnInfo("Initial Hand", classOf[String], (c) => f"${probabilities(c)(0)*100}%.2f%%")
    ), (column, columns) => ColumnInfo(s"Draw ${column - (columns - 1)}", classOf[String], (category) => f"${probabilities(category)(column - (columns - 1))*100}%.2f%%"))

    case ExpectedCount extends DisplayMode("Expected Counts", Seq(
      ColumnInfo("Kind of Card", classOf[String], identity),
      ColumnInfo("Initial Hand", classOf[String], (c) => if (!expectedCounts(c).isEmpty) RoundMode(SettingsDialog.settings.editor.hand.rounding)(expectedCounts(c)(0)) else "")
    ), (column, columns) => ColumnInfo(s"Draw ${column - (columns - 1)}", classOf[String], (category) => {
      if (column - (columns - 1) < expectedCounts(category).size)
        RoundMode(SettingsDialog.settings.editor.hand.rounding)(expectedCounts(category)(column - (columns - 1)))
      else
        ""
    }))
  }
  import DisplayMode._
  lazy val Relation = (0 until DesiredProbability.columns).map(DesiredProbability.title(_)).indexOf("Relation")
  lazy val Desired = (0 until DesiredProbability.columns).map(DesiredProbability.title(_)).indexOf("Desired")

  private val desiredBoxes = collection.mutable.Map[String, JComboBox[Int]]()
  private val relationBoxes = collection.mutable.Map[String, JComboBox[RelationChoice]]()
  private val probabilities = collection.mutable.Map[String, Array[Double]]()
  private val expectedCounts = collection.mutable.Map[String, Array[Double]]()

  // Right panel containing table and settings
  private val tablePanel = JPanel(BorderLayout())
  add(tablePanel, BorderLayout.CENTER)

  private val northPanel = JPanel(BorderLayout())
  tablePanel.add(northPanel, BorderLayout.NORTH)

  // Spinners controlling draws to show and initial hand size
  private val leftControlPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
  northPanel.add(leftControlPanel, BorderLayout.WEST)
  leftControlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5))
  leftControlPanel.add(JLabel("Hand Size: "))
  private val handSpinner = JSpinner(SpinnerNumberModel(7, 0, Int.MaxValue, 1))
  leftControlPanel.add(handSpinner)
  leftControlPanel.add(Box.createHorizontalStrut(15))
  leftControlPanel.add(JLabel("Show Draws: "))
  private val drawsSpinner = JSpinner(SpinnerNumberModel(0, 0, Int.MaxValue, 1))
  leftControlPanel.add(drawsSpinner)

  // Combo box controlling what numbers to show in the table
  private val rightControlPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0))
  rightControlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0))
  northPanel.add(rightControlPanel)
  private val modeBox = JComboBox(DisplayMode.values)
  modeBox.addActionListener(_ => {
    recalculate()
    model.fireTableStructureChanged()
  })
  rightControlPanel.add(modeBox)

  private object model extends AbstractTableModel {
    override def getRowCount = deck.numCategories

    override def getColumnCount = drawsSpinner.getValue match {
      case n: Int => n + modeBox.getItemAt(modeBox.getSelectedIndex).columns
      case _ => throw IllegalStateException(s"unexpected value of type ${drawsSpinner.getClass}")
    }

    override def getColumnName(column: Int) = modeBox.getItemAt(modeBox.getSelectedIndex).title(column)

    override def getColumnClass(column: Int) = modeBox.getItemAt(modeBox.getSelectedIndex).clazz(column)

    override def getValueAt(row: Int, column: Int) = {
      val category = deck.categories.asScala.map(_.getName).toSeq.sorted.apply(row)
      modeBox.getItemAt(modeBox.getSelectedIndex).value(column)(category)
    }
  }
  private val table = new JTable(model) {
    override def getCellEditor(row: Int, column: Int) = {
      val category = deck.categories.asScala.map(_.getName).toSeq.sorted.apply(row)
      modeBox.getItemAt(modeBox.getSelectedIndex).editor(column).map(_(category)).getOrElse(super.getCellEditor(row, column))
    }

    override def getScrollableTracksViewportWidth = getPreferredSize.width < getParent.getWidth

    override def isCellEditable(row: Int, column: Int) = modeBox.getItemAt(modeBox.getSelectedIndex).editor(column).isDefined

    override def prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int) = {
      val c = super.prepareRenderer(renderer, row, column)
      if (!isRowSelected(row) || !getRowSelectionAllowed)
        c.setBackground(if (row % 2 == 0) Color(getBackground.getRGB) else SettingsDialog.settings.editor.stripe)
      if (model.getValueAt(row, Relation) == AtLeast && model.getValueAt(row, Desired) == 0) {
        c.setForeground(c.getBackground.darker)
        c.setFont(Font(c.getFont.getFontName, Font.ITALIC, c.getFont.getSize))
      } else
        c.setForeground(Color.BLACK)
      c
    }
  }
  table.setFillsViewportHeight(true)
  table.setShowGrid(false)
  table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  private val intRenderer = DefaultTableCellRenderer()
  intRenderer.setHorizontalAlignment(SwingConstants.LEFT)
  table.setDefaultRenderer(classOf[Int], intRenderer)
  tablePanel.add(JScrollPane(table), BorderLayout.CENTER)

  // Actions
  drawsSpinner.addChangeListener(_ => {
    recalculate()
    model.fireTableStructureChanged()
  })
  handSpinner.addChangeListener(_ -> recalculate())
  handSpinner.addChangeListener(recalculateFunction)

  def handSize = handSpinner.getValue match {
    case n: Int => n
    case _ => throw IllegalStateException(s"unexpected value of type ${handSpinner.getValue.getClass}")
  }

  /** Recalculate category probabilities and update the table accordingly. */
  def recalculate() = {
    val categories = deck.categories.asScala.map(_.getName).toSeq.sorted

    probabilities.clear()
    drawsSpinner.getValue match {
      case draws: Int =>
        categories.foreach{ category =>
          probabilities(category) = Array.fill(1 + draws)(0)
          expectedCounts(category) = Array.fill(1 + draws)(0)
          val box = relationBoxes(category)
          val r = box.getItemAt(box.getSelectedIndex)
          for (j <- 0 to draws) {
            probabilities(category)(j) = r match {
              case AtLeast => 1 - (0 until desiredBoxes(category).getSelectedIndex).map(Stats.hypergeometric(_, handSize + j, deck.getCategoryList(category).total, deck.total)).sum
              case Exactly => Stats.hypergeometric(desiredBoxes(category).getSelectedIndex, handSize + j, deck.getCategoryList(category).total, deck.total)
              case AtMost => (0 to desiredBoxes(category).getSelectedIndex).map(Stats.hypergeometric(_, handSize + j, deck.getCategoryList(category).total, deck.total)).sum
            }
            expectedCounts(category)(j) = deck.getCategoryList(category).total.toDouble/deck.total.toDouble*(handSize + j).toDouble
          }
        }
      case _ => throw IllegalStateException(s"unexpected value of type ${handSpinner.getValue.getClass}")
    }
    model.fireTableDataChanged()
  }

  /** Update the available categories, recalculate their probabilities,and then update the table. */
  def update() = {
    val categories = deck.categories.asScala.map(_.getName).toSeq.sorted

    val oldDesired = desiredBoxes.map{ case (c, b) => c -> b.getSelectedIndex }
    val oldRelations = relationBoxes.map{ case (c, b) => c -> b.getItemAt(b.getSelectedIndex) }

    desiredBoxes.clear()
    relationBoxes.clear()
    probabilities.clear()

    categories.foreach{ category =>
      val desiredBox = JComboBox[Int]()
      for (i <- 0 to deck.getCategoryList(category).total)
        desiredBox.addItem(i)
      if (oldDesired.contains(category) && oldDesired(category) < deck.getCategoryList(category).total)
        desiredBox.setSelectedIndex(oldDesired(category))
      desiredBox.addActionListener(_ => recalculate())
      desiredBoxes(category) = desiredBox

      val relationBox = JComboBox(RelationChoice.values)
      if (oldRelations.contains(category))
        relationBox.setSelectedItem(oldRelations(category))
      relationBox.addActionListener(_ => recalculate())
      relationBoxes(category) = relationBox
    }
    
    recalculate()
    model.fireTableStructureChanged()
  }
}

private enum RelationChoice(override val toString: String) {
  case AtLeast extends RelationChoice("At least")
  case AtMost  extends RelationChoice("At most")
  case Exactly extends RelationChoice("Exactly")
}