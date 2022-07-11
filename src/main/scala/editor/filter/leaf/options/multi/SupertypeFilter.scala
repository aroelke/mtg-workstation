package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute

/**
 * Object containing global information about supertypes.
 * @author Alec Roelke
 */
object SupertypeFilter {
  /** List of all supertypes. */
  var supertypeList = Seq[String]()
}

/**
 * Filter that groups cards by supertype.
 * @author Alec Roelke
 */
class SupertypeFilter extends MultiOptionsFilter[String, SupertypeFilter](CardAttribute.Supertype, false, _.supertypes)