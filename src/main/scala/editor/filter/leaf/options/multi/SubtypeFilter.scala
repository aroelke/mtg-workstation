package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.filter.FaceSearchOptions
import editor.util.Containment

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
final case class SubtypeFilter(faces: FaceSearchOptions = FaceSearchOptions.ANY, contain: Containment = Containment.AnyOf, selected: Set[String] = Set.empty) extends MultiOptionsFilter[String, SubtypeFilter] {
  override def attribute = CardAttribute.Subtype
  override val unified = false
  override def values = _.subtypes
}