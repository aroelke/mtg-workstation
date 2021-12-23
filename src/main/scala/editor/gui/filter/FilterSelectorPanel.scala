package editor.gui.filter

import _root_.editor.filter.leaf.FilterLeaf
import javax.swing.BoxLayout
import _root_.editor.gui.generic.ComboBoxPanel
import _root_.editor.database.attributes.CardAttribute
import _root_.editor.gui.filter.editor.FilterEditorPanel
import javax.swing.JPanel
import java.awt.CardLayout
import _root_.editor.filter.FaceSearchOptions
import javax.swing.JLabel
import _root_.editor.util.MouseListenerFactory
import javax.swing.Box
import javax.swing.JButton
import _root_.editor.util.UnicodeSymbols

object FilterSelectorPanel {
  def apply() = new FilterSelectorPanel

  def apply(filter: FilterLeaf[?]) = {
    val panel = new FilterSelectorPanel
    panel.setContents(filter)
    panel
  }
}

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
    faces = faces match {
      case FaceSearchOptions.ANY   => FaceSearchOptions.ALL
      case FaceSearchOptions.ALL   => FaceSearchOptions.FRONT
      case FaceSearchOptions.FRONT => FaceSearchOptions.BACK
      case FaceSearchOptions.BACK  => FaceSearchOptions.ANY
    }
    facesLabel.setIcon(faces.getIcon(getHeight/2))
  }))
  add(Box.createHorizontalStrut(1))
  add(facesLabel)
  add(Box.createHorizontalStrut(1))

  // Button to remove this from the form
  private val removeButton = JButton(UnicodeSymbols.MINUS.toString)
  removeButton.addActionListener(_ => {
    group -= this
    firePanelsChanged()
  })
  add(removeButton)

  // Button to create a new group with this in it
  private val groupButton = JButton(UnicodeSymbols.ELLIPSIS.toString)
  groupButton.addActionListener(_ => {
    group.engroup(this)
    firePanelsChanged()
  })
  add(groupButton)

  facesLabel.setIcon(faces.getIcon(getPreferredSize.height/2))

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