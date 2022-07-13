package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.attributes.Expansion
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * Filter that groups cards by expansion.
 * @author Alec Roelke
 */
final case class ExpansionFilter(contain: Containment = Containment.AnyOf, selected: Set[Expansion] = Set.empty) extends SingletonOptionsFilter[Expansion, ExpansionFilter] {
  override def faces = FaceSearchOptions.ANY
  override def attribute = CardAttribute.Expansion
  override val unified = true
  override def value = _.expansion
  override def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[Expansion]) = copy(contain = contain, selected = selected)
}