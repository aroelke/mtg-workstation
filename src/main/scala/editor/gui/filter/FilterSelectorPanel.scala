package editor.gui.filter

import editor.database.attributes.CardAttribute
import editor.database.attributes.Expansion
import editor.database.attributes.Rarity
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.filter.leaf._
import editor.gui.ElementAttribute
import editor.gui.filter.leaf.FilterEditorPanel
import editor.gui.filter.leaf._
import editor.gui.generic.ComboBoxPanel
import editor.unicode.{_, given}
import editor.util.MouseListenerFactory

import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

/** Alternate constructors for [[FilterSelectorPanel]]. */
object FilterSelectorPanel {
  /** @return a new, default [[FilterSelectorPanel]] */
  def apply() = new FilterSelectorPanel

  /**
   * Create a new [[FilterSelectorPanel]] pre-populated with the contents of a filter.
   * 
   * @param filter filter to use for populating contents
   * @return a new [[FilterSelectorPanel]] showing the attribute from the filter and populated with its
   * contents
   */
  def apply(filter: FilterLeaf) = {
    val panel = new FilterSelectorPanel
    panel.setContents(filter)
    panel
  }
}

/**
 * A panel with a drop-down on the left allowing a user to pick an attribute to filter by and controls
 * on the right for adding, removing, and grouping filters, as well as an icon indicating how the filter
 * applies to multi-faced cards.
 * 
 * @author Alec Roelke
 */
class FilterSelectorPanel extends FilterPanel[FilterLeaf] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  // Filter type selector
  private val filterTypes = ComboBoxPanel(ElementAttribute.values.toArray)
  filterTypes.setToolTipText(filterTypes.getSelectedItem.attribute.description)
  private val renderer = filterTypes.getRenderer
  filterTypes.setRenderer(new ListCellRenderer[ElementAttribute[?, ?]] {
    override def getListCellRendererComponent(list: JList[? <: ElementAttribute[?, ?]], value: ElementAttribute[?, ?], index: Int, isSelected: Boolean, cellHasFocus: Boolean) = {
      val component = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
      component match { case j: JComponent => j.setToolTipText(value.attribute.description) }
      component
    }
  })
  add(filterTypes)

  // Panel containing each editor panel
  private val filterPanels = collection.mutable.Map[ElementAttribute[?, ?], FilterEditorPanel[?]]()
  private val filtersPanel = JPanel(java.awt.CardLayout())
  add(filtersPanel)
  ElementAttribute.filterableValues.foreach((a) => {
    val panel = a.filter(this)
    filterPanels(a) = panel
    filtersPanel.add(panel, a.attribute.toString)
  })

  // Small button to choose which faces to look at when filtering
  private var _faces = FaceSearchOptions.ANY
  private val facesLabel = JLabel()
  facesLabel.addMouseListener(MouseListenerFactory.createMouseListener(released = _ => faces = FaceSearchOptions.values.apply((faces.ordinal + 1) % FaceSearchOptions.values.size)))
  add(Box.createHorizontalStrut(1))
  add(facesLabel)
  add(Box.createHorizontalStrut(1))

  // Button to remove this from the form
  private val removeButton = JButton(Minus)
  removeButton.addActionListener(_ => group.foreach((g) => {
    g -= this
    firePanelsChanged()
  }))
  add(removeButton)

  // Button to create a new group with this in it
  private val groupButton = JButton(Ellipsis)
  groupButton.addActionListener(_ => group.foreach((g) => {
    g.engroup(this)
    firePanelsChanged()
  }))
  add(groupButton)

  facesLabel.setIcon(faces.scaled(getPreferredSize.height/2))
  facesLabel.setToolTipText(faces.tooltip)

  filterTypes.addItemListener(_ => filtersPanel.getLayout match {
    case cards: java.awt.CardLayout =>
      cards.show(filtersPanel, filterTypes.getSelectedItem.toString)
      filterTypes.setToolTipText(filterTypes.getSelectedItem.attribute.description)
      facesLabel.setVisible(!filterTypes.getSelectedItem.attribute.filter.unified)
  })

  /** @return the current selection for which faces the filter should search */
  def faces = _faces
  private def faces_=(f: FaceSearchOptions) = {
    _faces = f
    facesLabel.setIcon(f.scaled((if (getHeight == 0) getPreferredSize.height else getHeight)/2))
    facesLabel.setToolTipText(f.tooltip)
  }

  override def filter = filterPanels(filterTypes.getSelectedItem).filter

  override def setContents(filter: FilterLeaf) = {
    filterTypes.setSelectedItem(ElementAttribute.fromAttribute(filter.attribute))
    filterPanels(ElementAttribute.fromAttribute(filter.attribute)).setContents(filter)
    faces = filter.faces
    filtersPanel.getLayout match {
      case card: java.awt.CardLayout => card.show(filtersPanel, filter.attribute.toString)
    }
  }
}