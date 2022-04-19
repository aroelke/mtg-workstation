package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.attributes.Expansion

/**
 * Filter that groups cards by expansion.
 * @author Alec Roelke
 */
class ExpansionFilter extends SingletonOptionsFilter[Expansion](CardAttribute.EXPANSION, true, _.expansion) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.EXPANSION).asInstanceOf[ExpansionFilter]
    filter.contain = contain
    filter.selected = selected
    filter
  }
}
