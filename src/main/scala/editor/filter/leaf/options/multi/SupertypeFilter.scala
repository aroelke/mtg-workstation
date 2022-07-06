package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute

import scala.jdk.CollectionConverters._

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
class SupertypeFilter extends MultiOptionsFilter[String](CardAttribute.Supertype, false, _.supertypes)