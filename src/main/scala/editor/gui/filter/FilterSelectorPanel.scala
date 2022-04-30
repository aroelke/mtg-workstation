package editor.gui.filter

import _root_.editor.database.attributes.CardAttribute
import _root_.editor.database.attributes.Expansion
import _root_.editor.database.attributes.Rarity
import _root_.editor.database.card.Card
import _root_.editor.filter.FaceSearchOptions
import _root_.editor.filter.leaf.FilterLeaf
import _root_.editor.filter.leaf._
import _root_.editor.filter.leaf.options.multi._
import _root_.editor.filter.leaf.options.single._
import _root_.editor.gui.filter.editor.FilterEditorPanel
import _root_.editor.gui.filter.editor._
import _root_.editor.gui.generic.ComboBoxPanel
import _root_.editor.util.MouseListenerFactory
import _root_.editor.util.UnicodeSymbols

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
  private val filterTypes = ComboBoxPanel(CardAttribute.filterableValues)
  filterTypes.setToolTipText(filterTypes.getSelectedItem.description)
  private val renderer = filterTypes.getRenderer
  filterTypes.setRenderer(new ListCellRenderer[CardAttribute] {
    override def getListCellRendererComponent(list: JList[? <: CardAttribute], value: CardAttribute, index: Int, isSelected: Boolean, cellHasFocus: Boolean) = {
      val component = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
      component match { case j: JComponent => j.setToolTipText(value.description) }
      component
    }
  })
  add(filterTypes)

  // Panel containing each editor panel
  private val filterPanels = collection.mutable.Map[CardAttribute, FilterEditorPanel[?]]()
  private val filtersPanel = JPanel(java.awt.CardLayout())
  add(filtersPanel)
  CardAttribute.filterableValues.foreach((attribute) => {
    val panel = attribute match {
      case CardAttribute.ANY => BinaryFilterPanel(true)
      case CardAttribute.NONE => BinaryFilterPanel(false)
      case CardAttribute.DEFAULTS => DefaultsFilterPanel()
      case _ => attribute.get match {
        case text: TextFilter => TextFilterPanel(text, this)
        case variable: VariableNumberFilter => VariableNumberFilterPanel(variable, this)
        case number: NumberFilter => NumberFilterPanel(number, this)
        case color: ColorFilter => ColorFilterPanel(color, this)
        case layout: LayoutFilter => OptionsFilterPanel(layout, _root_.editor.database.card.CardLayout.values.toSeq, this)
        case cost: ManaCostFilter => ManaCostFilterPanel(cost, this)
        case line: TypeLineFilter => TypeLineFilterPanel(line, this)
        case supertype: SupertypeFilter => OptionsFilterPanel(supertype, SupertypeFilter.supertypeList, this)
        case cardtype: CardTypeFilter => OptionsFilterPanel(cardtype, CardTypeFilter.typeList, this)
        case subtype: SubtypeFilter => OptionsFilterPanel(subtype, SubtypeFilter.subtypeList, this)
        case expansion: ExpansionFilter => OptionsFilterPanel(expansion, Expansion.expansions, this)
        case block: BlockFilter => OptionsFilterPanel(block, Expansion.blocks, this)
        case rarity: RarityFilter => OptionsFilterPanel(rarity, Rarity.values.toSeq, this)
        case legality: LegalityFilter => LegalityFilterPanel(legality, this)
        case tags: TagsFilter => OptionsFilterPanel(tags, Card.tags.flatMap{ case (_, s) => s }.toArray.sorted, this)
        case _ => throw IllegalArgumentException(s"no panel for fiters of type $filter")
      }
    }
    filterPanels.put(attribute, panel)
    filtersPanel.add(panel, attribute.toString)
  })

  // Small button to choose which faces to look at when filtering
  private var _faces = FaceSearchOptions.ANY
  private val facesLabel = JLabel()
  facesLabel.addMouseListener(MouseListenerFactory.createMouseListener(released = _ => faces = FaceSearchOptions.values.apply((faces.ordinal + 1) % FaceSearchOptions.values.size)))
  add(Box.createHorizontalStrut(1))
  add(facesLabel)
  add(Box.createHorizontalStrut(1))

  // Button to remove this from the form
  private val removeButton = JButton(UnicodeSymbols.Minus.toString)
  removeButton.addActionListener(_ => group.foreach((g) => {
    g -= this
    firePanelsChanged()
  }))
  add(removeButton)

  // Button to create a new group with this in it
  private val groupButton = JButton(UnicodeSymbols.Ellipsis.toString)
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
      filterTypes.setToolTipText(filterTypes.getSelectedItem.description)
      facesLabel.setVisible(CardAttribute.createFilter(filterTypes.getSelectedItem) match {
        case l: FilterLeaf => !l.unified
        case _ => false
      })
  })

  /** @return the current selection for which faces the filter should search */
  def faces = _faces
  private def faces_=(f: FaceSearchOptions) = {
    _faces = FaceSearchOptions.values.apply((f.ordinal + 1) % FaceSearchOptions.values.size)
    facesLabel.setIcon(f.scaled((if (getHeight == 0) getPreferredSize.height else getHeight)/2))
    facesLabel.setToolTipText(f.tooltip)
  }

  override def filter = filterPanels(filterTypes.getSelectedItem).filter

  override def setContents(filter: FilterLeaf) = {
    filterTypes.setSelectedItem(filter.attribute)
    filterPanels(filter.attribute).setContents(filter)
    faces = filter.faces
    filtersPanel.getLayout match {
      case card: java.awt.CardLayout => card.show(filtersPanel, filter.attribute.toString)
    }
  }
}