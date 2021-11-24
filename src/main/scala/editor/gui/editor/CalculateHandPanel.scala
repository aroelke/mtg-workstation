package editor.gui.editor

import javax.swing.JPanel
import javax.swing.event.ChangeListener
import editor.collection.deck.Deck
import java.awt.BorderLayout
import javax.swing.JComboBox
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import scala.jdk.CollectionConverters._
import javax.swing.JTable
import javax.swing.DefaultCellEditor
import javax.swing.table.AbstractTableModel
import editor.gui.settings.SettingsDialog
import javax.swing.table.TableCellRenderer
import java.awt.Color
import java.awt.Font
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.SwingConstants
import javax.swing.JScrollPane
import editor.util.Stats

object CalculateHandPanel {
  val RoundMode = Map(
    "No Rounding" -> ((x: Double) => f"$x%.2f"),
    "Round to nearest" -> ((x: Double) => f"${math.round(x)}%d"),
    "Truncate" -> ((x: Double) => f"${x.toInt}%d")
  )
}

class CalculateHandPanel(deck: Deck, recalculateFunction: ChangeListener) extends JPanel(BorderLayout()) {
  import CalculateHandPanel._
  import DisplayMode._
  import RelationChoice._

  private val Category = 0
  private val Count = 1
  private val Relation = 2
  private val Desired = 3
  private val PInitial = 4
  private val PInfoCols = 5
  private val EInitial = 1
  private val EInfoCols = 2

  private val desiredBoxes = collection.mutable.Map[String, JComboBox[Integer]]()
  private val relationBoxes = collection.mutable.Map[String, JComboBox[RelationChoice]]()
  private val probabilities = collection.mutable.Map[String, Array[Double]]()
  private val expectedCounts = collection.mutable.Map[String, Array[Double]]()

  // Right panel containing table and settings
  private val tablePanel = JPanel(BorderLayout());
  add(tablePanel, BorderLayout.CENTER);

  private val northPanel = JPanel(BorderLayout());
  tablePanel.add(northPanel, BorderLayout.NORTH);

  // Spinners controlling draws to show and initial hand size
  private val leftControlPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0));
  northPanel.add(leftControlPanel, BorderLayout.WEST);
  leftControlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
  leftControlPanel.add(JLabel("Hand Size: "));
  private val handSpinner = JSpinner(SpinnerNumberModel(7, 0, Int.MaxValue, 1))
  leftControlPanel.add(handSpinner);
  leftControlPanel.add(Box.createHorizontalStrut(15));
  leftControlPanel.add(JLabel("Show Draws: "));
  private val drawsSpinner = JSpinner(SpinnerNumberModel(0, 0, Int.MaxValue, 1))
  leftControlPanel.add(drawsSpinner);

  // Combo box controlling what numbers to show in the table
  private val rightControlPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0));
  rightControlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
  northPanel.add(rightControlPanel);
  private val modeBox = JComboBox(DisplayMode.values);
  modeBox.addActionListener(_ => {
    recalculate();
    model.fireTableStructureChanged();
  });
  rightControlPanel.add(modeBox);

  private object model extends AbstractTableModel {
    override def getColumnClass(column: Int) = modeBox.getItemAt(modeBox.getSelectedIndex) match {
      case DesiredProbability => column match {
        case Category => classOf[String]
        case Count | Desired => classOf[Integer]
        case Relation => classOf[RelationChoice]
        case _ => classOf[String]
      }
      case ExpectedCount => classOf[String]
    }

    override def getColumnCount = drawsSpinner.getValue match {
      case n: Int => n + (modeBox.getItemAt(modeBox.getSelectedIndex) match {
        case DesiredProbability => PInfoCols
        case ExpectedCount => EInfoCols
      })
      case _ => throw IllegalStateException(s"unexpected value of type ${drawsSpinner.getClass}")
    }

    override def getColumnName(column: Int) = modeBox.getItemAt(modeBox.getSelectedIndex) match {
      case DesiredProbability => column match {
        case Category => "Kind of Card"
        case Count    => "Count"
        case Desired  => "Desired"
        case Relation => "Relation"
        case PInitial => "Initial Hand"
        case _ => s"Draw ${column - (PInfoCols - 1)}"
      }
      case ExpectedCount => column match {
        case Category => "Kind of Card"
        case EInitial => "Initial Hand"
        case _ => s"Draw ${column - (EInfoCols - 1)}"
      }
    }

    override def getRowCount = deck.numCategories

    override def getValueAt(row: Int, column: Int) = {
      val category = deck.categories.asScala.map(_.getName).toSeq.sorted.apply(row)
      modeBox.getItemAt(modeBox.getSelectedIndex) match {
        case DesiredProbability => column match {
          case Category => category
          case Count => deck.getCategoryList(category).total
          case Desired => desiredBoxes(category).getSelectedItem
          case Relation => relationBoxes(category).getSelectedItem
          case _ => f"${probabilities(category)(column - (PInfoCols - 1))*100}%.2f%%"
        }
        case ExpectedCount =>
          if (column == Category)
            category
          else if (column - (EInfoCols - 1) < expectedCounts(category).size)
            RoundMode(SettingsDialog.settings.editor.hand.rounding)(expectedCounts(category)(column - (EInfoCols -1 )))
          else
            ""
      }
    }
  }
  private val table = new JTable(model) {
    override def getCellEditor(row: Int, column: Int) = {
      val category = deck.categories.asScala.map(_.getName).toSeq.sorted.apply(row)
      column match {
        case Desired  => DefaultCellEditor(desiredBoxes(category));
        case Relation => DefaultCellEditor(relationBoxes(category));
        case _ => super.getCellEditor(row, column);
      };
    }

    override def getScrollableTracksViewportWidth = getPreferredSize.width < getParent.getWidth

    override def isCellEditable(row: Int, column: Int) = column == Desired || column == Relation

    override def prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int) = {
      val c = super.prepareRenderer(renderer, row, column);
      if (!isRowSelected(row) || !getRowSelectionAllowed())
        c.setBackground(if (row % 2 == 0) Color(getBackground.getRGB) else SettingsDialog.settings.editor.stripe);
      if (model.getValueAt(row, Relation) == AtLeast && model.getValueAt(row, Desired) == 0) {
        c.setForeground(c.getBackground.darker);
        c.setFont(Font(c.getFont.getFontName, Font.ITALIC, c.getFont.getSize));
      } else
          c.setForeground(Color.BLACK);
      c;
    }
  };
  table.setFillsViewportHeight(true);
  table.setShowGrid(false);
  table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  private val intRenderer = DefaultTableCellRenderer();
  intRenderer.setHorizontalAlignment(SwingConstants.LEFT);
  table.setDefaultRenderer(classOf[Integer], intRenderer);
  tablePanel.add(JScrollPane(table), BorderLayout.CENTER);

  def handSize = handSpinner.getValue match {
    case n: Int => n
    case _ => throw IllegalStateException(s"unexpected value of type ${handSpinner.getValue.getClass}")
  }

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
            var p = 0.0
            r match {
              case AtLeast =>
                for (k <- 0 until desiredBoxes(category).getSelectedIndex)
                  p += Stats.hypergeometric(k, handSize + j, deck.getCategoryList(category).total, deck.total)
                p = 1 - p
              case Exactly =>
                p = Stats.hypergeometric(desiredBoxes(category).getSelectedIndex, handSize + j, deck.getCategoryList(category).total, deck.total)
              case AtMost =>
                for (k <- 0 to desiredBoxes(category).getSelectedIndex)
                  p += Stats.hypergeometric(k, handSize + j, deck.getCategoryList(category).total, deck.total)
            }
            probabilities(category)(j) = p
            expectedCounts(category)(j) = deck.getCategoryList(category).total.toDouble/deck.total.toDouble*(handSize + j).toDouble
          }
        }
      case _ => throw IllegalStateException(s"unexpected value of type ${handSpinner.getValue.getClass}")
    }
    model.fireTableDataChanged()
  }

  def update() = {
    val categories = deck.categories.asScala.map(_.getName).toSeq.sorted

    val oldDesired = desiredBoxes.map{ case (c, b) => c -> b.getSelectedIndex }
    val oldRelations = relationBoxes.map{ case (c, b) => c -> b.getItemAt(b.getSelectedIndex) }

    desiredBoxes.clear()
    relationBoxes.clear()
    probabilities.clear()

    categories.foreach{ category =>
      val desiredBox = JComboBox[Integer]()
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

private enum DisplayMode(override val toString: String) {
  case DesiredProbability extends DisplayMode("Probabilities")
  case ExpectedCount      extends DisplayMode("Expected Counts")
}

private enum RelationChoice(override val toString: String) {
  case AtLeast extends RelationChoice("At least")
  case AtMost  extends RelationChoice("At most")
  case Exactly extends RelationChoice("Exactly")
}