package editor.gui.filter.editor

import editor.filter.leaf.options.OptionsFilter
import editor.database.attributes.CardAttribute
import java.awt.BorderLayout
import editor.gui.generic.ComboBoxPanel
import editor.util.Containment
import editor.gui.generic.ScrollablePanel
import javax.swing.BoxLayout
import editor.gui.generic.ButtonScrollPane
import javax.swing.JComboBox
import javax.swing.BorderFactory
import javax.swing.JPanel
import editor.util.PopupMenuListenerFactory
import javax.swing.plaf.basic.BasicComboPopup
import javax.swing.SwingUtilities
import javax.swing.JScrollPane
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.SwingConstants
import java.awt.Font
import java.awt.Component
import editor.util.MouseListenerFactory
import editor.util.UnicodeSymbols
import java.awt.Color
import scala.jdk.CollectionConverters._
import editor.filter.leaf.FilterLeaf

object OptionsFilterPanel {
  def apply[T <: AnyRef](attribute: CardAttribute, options: Array[T]) = new OptionsFilterPanel(attribute, options)

  def apply[T <: AnyRef](filter: OptionsFilter[T], options: Array[T]) = {
    val panel = new OptionsFilterPanel(filter.`type`, options)
    panel.setContents(filter)
    panel
  }
}

class OptionsFilterPanel[T <: AnyRef](attribute: CardAttribute, options: Array[T]) extends FilterEditorPanel[OptionsFilter[T]] {
  private val MaxComboWidth = 100

  setLayout(BorderLayout())

  private val boxes = collection.mutable.Buffer[JComboBox[T]]()

  private val contain = ComboBoxPanel(Containment.values)
  add(contain, BorderLayout.WEST)

  private val optionsPanel = ScrollablePanel(ScrollablePanel.TrackHeight)
  optionsPanel.setLayout(BoxLayout(optionsPanel, BoxLayout.X_AXIS))
  private val optionsPane = ButtonScrollPane(optionsPanel)
  optionsPane.setBorder(BorderFactory.createEmptyBorder)
  add(optionsPane, BorderLayout.CENTER)

  private def +=(value: T): Unit = {
    val boxPanel = JPanel(BorderLayout())
    val box = JComboBox(options)
    box.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => if (!options.isEmpty) {
      box.getAccessibleContext.getAccessibleChild(0) match {
        case popup: BasicComboPopup => SwingUtilities.invokeLater(() => SwingUtilities.getAncestorOfClass(classOf[JScrollPane], popup.getList) match {
          case scrollPane: JScrollPane =>
            val popupWidth = popup.getList.getPreferredSize.width + (if (options.size > box.getMaximumRowCount) scrollPane.getVerticalScrollBar.getPreferredSize.width else 0)
            scrollPane.setPreferredSize(Dimension(math.max(popupWidth, scrollPane.getPreferredSize.width), scrollPane.getPreferredSize.height))
            scrollPane.setMaximumSize(scrollPane.getPreferredSize)
            val location = box.getLocationOnScreen
            popup.setLocation(location.x, location.y + box.getHeight - 1)
            popup.setLocation(location.x, location.y + box.getHeight)
        })
      }
    }))
    box.setPreferredSize(Dimension(MaxComboWidth, box.getPreferredSize.height))

    boxPanel.add(box, BorderLayout.CENTER)
    boxes += box
    box.setSelectedItem(value)

    val buttonPanel = JPanel(GridLayout(2, 1, 0, 0))
    val addButton = JLabel("+", SwingConstants.CENTER)
    val buttonFont = Font(addButton.getFont.getFontName, Font.PLAIN, addButton.getFont.getSize - 2)
    addButton.setAlignmentX(Component.CENTER_ALIGNMENT)
    addButton.setFont(buttonFont)
    addButton.addMouseListener(MouseListenerFactory.createMouseListener(pressed = _ => {
      this += options(0)
      optionsPanel.revalidate()
    }))
    val removeButton = JLabel(UnicodeSymbols.MULTIPLY.toString, SwingConstants.CENTER)
    removeButton.setForeground(Color.RED)
    removeButton.setAlignmentX(Component.CENTER_ALIGNMENT)
    removeButton.setFont(buttonFont)
    removeButton.addMouseListener(MouseListenerFactory.createMouseListener(pressed = _ => if (!boxes.isEmpty) {
      optionsPanel.remove(boxPanel)
      boxes -= box
      optionsPanel.revalidate()
    }))
    buttonPanel.add(removeButton)
    buttonPanel.add(addButton)
    boxPanel.add(buttonPanel, BorderLayout.EAST)

    optionsPanel.add(boxPanel)
  }

  override def filter = CardAttribute.createFilter(attribute) match {
    case of: OptionsFilter[T] =>
      of.contain = contain.getSelectedItem
      of.selected = boxes.map((b) => b.getItemAt(b.getSelectedIndex)).toSet.asJava
      of
  }

  override def setContents(filter: OptionsFilter[T]) = if (filter.`type` == attribute) {
    contain.setSelectedItem(filter.contain)
    if (options.isEmpty)
      contain.setVisible(false)
    boxes.clear()
    optionsPanel.removeAll()
    if (filter.selected.isEmpty && !options.isEmpty)
      this += options(0)
    else
      filter.selected.asScala.foreach(this += _)
  } else throw IllegalArgumentException(s"${filter.`type`} is not a $attribute filter")

  override def setContents(filter: FilterLeaf[?]) = filter match {
    case of: OptionsFilter[T] if filter.`type` == attribute => setContents(of)
    case _ => throw IllegalArgumentException(s"${filter.`type`} is not a $attribute filter")
  }
}