package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute

/**
 * A filter that groups cards by the block they belong in.
 * @author Alec Roelke
 */
class BlockFilter extends SingletonOptionsFilter[String, BlockFilter](CardAttribute.Block, true, _.expansion.block)