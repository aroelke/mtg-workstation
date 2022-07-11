package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute

/**
 * Object containing global information about subtypes.
 */
object SubtypeFilter {
  /** List of all of the subtypes across all card types. */
  var subtypeList = Seq[String]()
}

/**
 * Filter for grouping cards by subtype.
 * @author Alec Roelke
 */
class SubtypeFilter extends MultiOptionsFilter[String, SubtypeFilter](CardAttribute.Subtype, false, _.subtypes)