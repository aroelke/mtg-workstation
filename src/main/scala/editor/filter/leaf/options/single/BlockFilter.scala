package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute

/**
 * A filter that groups cards by the block they belong in.
 * @author Alec Roelke
 */
class BlockFilter extends SingletonOptionsFilter[String](CardAttribute.BLOCK, true, _.expansion.block) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.BLOCK).asInstanceOf[BlockFilter]
    filter.contain = contain
    filter.selected = selected
    filter
  }
}