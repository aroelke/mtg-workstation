package editor.gui.filter

import _root_.editor.database.attributes.CardAttribute
import _root_.editor.filter.FaceSearchOptions
import _root_.editor.filter.leaf.FilterLeaf
import _root_.editor.gui.filter.editor.FilterEditorPanel
import _root_.editor.gui.generic.ComboBoxPanel
import _root_.editor.util.MouseListenerFactory
import _root_.editor.util.UnicodeSymbols

import java.awt.CardLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

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
  def apply(filter: FilterLeaf[?]) = {
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
class FilterSelectorPanel extends FilterPanel[FilterLeaf[?]] {
  setLayout(BoxLayout(this, BoxLayout.X_AXIS))

  // Filter type selector
  private val filterTypes = ComboBoxPanel(CardAttribute.filterableValues)
  add(filterTypes)

  // Panel containing each editor panel
  private val filterPanels = collection.mutable.Map[CardAttribute, FilterEditorPanel[?]]()
  private val filtersPanel = JPanel(CardLayout())
  add(filtersPanel)
  CardAttribute.filterableValues.foreach((attribute) => {
    val panel = FilterPanelFactory.createFilterPanel(attribute)
    filterPanels.put(attribute, panel)
    filtersPanel.add(panel, attribute.toString)
  })
  filterTypes.addItemListener(_ => filtersPanel.getLayout match {
    case cards: CardLayout => cards.show(filtersPanel, filterTypes.getSelectedItem.toString)
  })

  // Small button to choose which faces to look at when filtering
  private var faces = FaceSearchOptions.ANY
  private val facesLabel = JLabel()
  facesLabel.addMouseListener(MouseListenerFactory.createMouseListener(released = _ => {
    faces = FaceSearchOptions.values.apply((faces.ordinal + 1) % FaceSearchOptions.values.size)
    facesLabel.setIcon(faces.getIcon(getHeight/2))
    facesLabel.setToolTipText(faces.tooltip)
  }))
  add(Box.createHorizontalStrut(1))
  add(facesLabel)
  add(Box.createHorizontalStrut(1))

  // Button to remove this from the form
  private val removeButton = JButton(UnicodeSymbols.MINUS.toString)
  removeButton.addActionListener(_ => group.foreach((g) => {
    g -= this
    firePanelsChanged()
  }))
  add(removeButton)

  // Button to create a new group with this in it
  private val groupButton = JButton(UnicodeSymbols.ELLIPSIS.toString)
  groupButton.addActionListener(_ => group.foreach((g) => {
    g.engroup(this)
    firePanelsChanged()
  }))
  add(groupButton)

  facesLabel.setIcon(faces.getIcon(getPreferredSize.height/2))
  facesLabel.setToolTipText(faces.tooltip)

  override def filter = filterPanels(filterTypes.getSelectedItem).filter match {
    case l: FilterLeaf[?] => l.faces = faces; l
    case f => f
  }

  override def setContents(filter: FilterLeaf[?]) = {
    filterTypes.setSelectedItem(filter.`type`)
    filterPanels(filter.`type`).setContents(filter)
    faces = filter.faces
    facesLabel.setIcon(faces.getIcon(getPreferredSize.height/2))
    filtersPanel.getLayout match {
      case card: CardLayout => card.show(filtersPanel, filter.`type`.toString)
    }
  }
}