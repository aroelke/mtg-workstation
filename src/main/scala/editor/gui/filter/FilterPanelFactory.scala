package editor.gui.filter

import _root_.editor.database.attributes.CardAttribute
import _root_.editor.database.attributes.Expansion
import _root_.editor.database.attributes.Rarity
import _root_.editor.database.card.Card
import _root_.editor.database.card.CardLayout
import _root_.editor.filter.leaf.ColorFilter
import _root_.editor.filter.leaf.FilterLeaf
import _root_.editor.filter.leaf.ManaCostFilter
import _root_.editor.filter.leaf.NumberFilter
import _root_.editor.filter.leaf.TextFilter
import _root_.editor.filter.leaf.TypeLineFilter
import _root_.editor.filter.leaf.VariableNumberFilter
import _root_.editor.filter.leaf.options.multi.CardTypeFilter
import _root_.editor.filter.leaf.options.multi.LegalityFilter
import _root_.editor.filter.leaf.options.multi.SubtypeFilter
import _root_.editor.filter.leaf.options.multi.SupertypeFilter
import _root_.editor.filter.leaf.options.multi.TagsFilter
import _root_.editor.filter.leaf.options.single.BlockFilter
import _root_.editor.filter.leaf.options.single.ExpansionFilter
import _root_.editor.filter.leaf.options.single.LayoutFilter
import _root_.editor.filter.leaf.options.single.RarityFilter
import _root_.editor.gui.filter.editor.BinaryFilterPanel
import _root_.editor.gui.filter.editor.ColorFilterPanel
import _root_.editor.gui.filter.editor.DefaultsFilterPanel
import _root_.editor.gui.filter.editor.FilterEditorPanel
import _root_.editor.gui.filter.editor.LegalityFilterPanel
import _root_.editor.gui.filter.editor.ManaCostFilterPanel
import _root_.editor.gui.filter.editor.NumberFilterPanel
import _root_.editor.gui.filter.editor.OptionsFilterPanel
import _root_.editor.gui.filter.editor.TextFilterPanel
import _root_.editor.gui.filter.editor.TypeLineFilterPanel
import _root_.editor.gui.filter.editor.VariableNumberFilterPanel

import scala.jdk.CollectionConverters._

/**
 * Factory for creating new [[FilterEditorPanel]]s.
 * @author Alec Roelke
 */
object FilterPanelFactory {
  /**
   * Create a new [[FilterEditorPanel]] based on the type of filter given and pre-populated with its contents.
   * 
   * @param filter filter to use to create a new panel
   * @return a filter panel of the type corresponding to the filter and containing its contents
   */
  def createFilterPanel(filter: FilterLeaf[?]): FilterEditorPanel[?] = filter match {
    case text: TextFilter => TextFilterPanel(text)
    case variable: VariableNumberFilter => VariableNumberFilterPanel(variable)
    case number: NumberFilter => NumberFilterPanel(number)
    case color: ColorFilter => ColorFilterPanel(color)
    case layout: LayoutFilter => OptionsFilterPanel(layout, CardLayout.values)
    case cost: ManaCostFilter => ManaCostFilterPanel(cost)
    case line: TypeLineFilter => TypeLineFilterPanel(line)
    case supertype: SupertypeFilter => OptionsFilterPanel(supertype, SupertypeFilter.supertypeList)
    case cardtype: CardTypeFilter => OptionsFilterPanel(cardtype, CardTypeFilter.typeList)
    case subtype: SubtypeFilter => OptionsFilterPanel(subtype, SubtypeFilter.subtypeList)
    case expansion: ExpansionFilter => OptionsFilterPanel(expansion, Expansion.expansions)
    case block: BlockFilter => OptionsFilterPanel(block, Expansion.blocks)
    case rarity: RarityFilter => OptionsFilterPanel(rarity, Rarity.values)
    case legality: LegalityFilter => LegalityFilterPanel(legality)
    case tags: TagsFilter => OptionsFilterPanel(tags, Card.tags.toArray.sorted)
    case _ => filter.`type` match {
      case CardAttribute.DEFAULTS => DefaultsFilterPanel()
      case CardAttribute.NONE => BinaryFilterPanel(false)
      case CardAttribute.ANY => BinaryFilterPanel(true)
      case _ => throw IllegalArgumentException(s"no panel for fiters of type $filter")
    }
  }

  /**
   * Create a new, empty [[FilterEditorPanel]] for a particular [[CardAttribute]].
   * 
   * @param attribute attribute to create the filter panel for
   * @return a new filter panel for the given attribute
   */
  def createFilterPanel(attribute: CardAttribute): FilterEditorPanel[?] = if (attribute == CardAttribute.DEFAULTS) DefaultsFilterPanel() else createFilterPanel(attribute.get)
}