package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.attributes.Expansion

/**
 * Filter that groups cards by expansion.
 * @author Alec Roelke
 */
class ExpansionFilter extends SingletonOptionsFilter[Expansion, ExpansionFilter](CardAttribute.Expansion, true, _.expansion)