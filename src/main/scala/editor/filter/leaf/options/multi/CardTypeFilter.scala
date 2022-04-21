package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute

import scala.jdk.CollectionConverters._

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
class CardTypeFilter extends MultiOptionsFilter[String](CardAttribute.CARD_TYPE, false, _.types)
