package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.attributes.Rarity

/**
 * A filter that groups cards by rarity.
 * @author Alec Roelke
 */
class RarityFilter extends SingletonOptionsFilter[Rarity, RarityFilter](CardAttribute.Rarity, true, _.rarity)