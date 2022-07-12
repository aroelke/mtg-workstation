package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * Filter that groups cards by user-defined tags.
 * @author Alec Roelke
 */
final case class TagsFilter(contain: Containment = Containment.AnyOf, selected: Set[String] = Set.empty) extends MultiOptionsFilter[String, TagsFilter] {
  override def faces = FaceSearchOptions.ANY
  override def attribute = CardAttribute.Tags
  override val unified = true
  override def values = Card.tags.get(_).map(_.toSet).getOrElse(Set.empty)
  override def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[String]) = copy(contain = contain, selected = selected)
  override def copyFaces(faces: FaceSearchOptions) = this
}