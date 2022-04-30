package editor.gui.filter.editor

import editor.database.attributes.CardAttribute
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.options.OptionsFilter
import editor.gui.filter.FilterSelectorPanel
import editor.gui.generic.ButtonScrollPane
import editor.gui.generic.ComboBoxPanel
import editor.gui.generic.ScrollablePanel
import editor.util.Containment
import editor.util.MouseListenerFactory
import editor.util.PopupMenuListenerFactory
import editor.util.UnicodeSymbols

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.plaf.basic.BasicComboPopup
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

/**
 * Convenience constructors for [[OptionsFilterPanel]].
 * @author Alec Roelke
 */
object OptionsFilterPanel {
  /**
   * Create a new [[OptionsFilterPanel]] that filters the given attribute using the given options.
   * 
   * @param attribute card attribute to filter
   * @param options values of the attribute that can be chosen
   * @return an [[OptionsFilterPanel]] for a filter of the given attribute with the given possible values
   */
  def apply[T <: AnyRef : ClassTag](attribute: CardAttribute, options: Seq[T], selector: FilterSelectorPanel) = new OptionsFilterPanel(attribute, options, selector)

  /**
   * Create a new [[OptionsFilterPanel]] with the given filter and possible options to choose from. The attribute
   * used for filtering is inferred form the filter.
   * 
   * @param filter filter used to infer the attribute and pre-populate the panel
   * @param options values of the attribute that can be chosen
   * @return an [[OptionsFilterPanel]] populated with the contents of the filter
   */
  def apply[T <: AnyRef : ClassTag](filter: OptionsFilter[T], options: Seq[T], selector: FilterSelectorPanel) = {
    val panel = new OptionsFilterPanel(filter.attribute, options, selector)
    panel.setContents(filter)
    panel
  }
}

/**
 * A filter editor panel that is used to customize a filter that selects one or more items from a number of
 * predefined options. Each selected option is a combo box with all of the options and a pair of small buttons
 * that allows for adding another selection or removing an existing one. If enough boxes are added, arrows on
 * the left and right side will become enabled to allow for scrolling.
 * 
 * @constructor create a new options filter panel for filtering a particular attribute using a particular set of
 * options
 * @param attribute card attribute to filter for
   @param options options that can be chosen for the filter value
 * 
 * @author Alec Roelke
 */
class OptionsFilterPanel[T <: AnyRef : ClassTag](protected override val attribute: CardAttribute, options: Seq[T], selector: FilterSelectorPanel) extends FilterEditorPanel[OptionsFilter[T]] {
  private val MaxComboWidth = 100

  setLayout(BorderLayout())

  private val boxes = collection.mutable.Buffer[JComboBox[T]]()

  private val contain = ComboBoxPanel(Containment.values.toArray)
  add(contain, BorderLayout.WEST)

  private val optionsPanel = ScrollablePanel(ScrollablePanel.TrackHeight)
  optionsPanel.setLayout(BoxLayout(optionsPanel, BoxLayout.X_AXIS))
  private val optionsPane = ButtonScrollPane(optionsPanel)
  optionsPane.setBorder(BorderFactory.createEmptyBorder)
  add(optionsPane, BorderLayout.CENTER)

  private def +=(value: T): Unit = {
    val boxPanel = JPanel(BorderLayout())
    val box = JComboBox(options.toArray)
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
      of.faces = selector.faces
      of.contain = contain.getSelectedItem
      of.selected = boxes.map((b) => b.getItemAt(b.getSelectedIndex)).toSet
      of
  }

  override def setFields(filter: OptionsFilter[T]) = if (filter.attribute == attribute) {
    contain.setSelectedItem(filter.contain)
    if (options.isEmpty)
      contain.setVisible(false)
    boxes.clear()
    optionsPanel.removeAll()
    if (filter.selected.isEmpty && !options.isEmpty)
      this += options(0)
    else
      filter.selected.foreach(this += _)
  } else throw IllegalArgumentException(s"${filter.attribute} is not a $attribute filter")
}