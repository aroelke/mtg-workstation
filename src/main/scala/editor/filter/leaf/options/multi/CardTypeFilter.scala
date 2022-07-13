package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.filter.FaceSearchOptions
import editor.util.Containment

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
final case class CardTypeFilter(faces: FaceSearchOptions = FaceSearchOptions.ANY, contain: Containment = Containment.AnyOf, selected: Set[String] = Set.empty) extends MultiOptionsFilter[String, CardTypeFilter] {
  override def attribute = CardAttribute.CardType
  override val unified = false
  override val values = _.types
}