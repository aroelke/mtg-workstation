package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.card.CardLayout

/**
 * A filter that groups cards based on layout.
 * @author Alec Roelke
 */
class LayoutFilter extends SingletonOptionsFilter[CardLayout](CardAttribute.LAYOUT, true, _.layout) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.LAYOUT).asInstanceOf[LayoutFilter]
    filter.contain = contain
    filter.selected = selected
    filter
  }
}
