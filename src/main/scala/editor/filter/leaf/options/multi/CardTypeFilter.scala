package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute

/**
 * Object containing global information about card types.
 * @author Alec Roelke
 */
object CardTypeFilter {
  /** List of all card types. */
  var typeList = Seq[String]()
}

/**
 * Filter that groups cards based on card types.
 * @author Alec Roelke
 */
class CardTypeFilter extends MultiOptionsFilter[String, CardTypeFilter](CardAttribute.CardType, false, _.types)