package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.attributes.Rarity
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * A filter that groups cards by rarity.
 * @author Alec Roelke
 */
final case class RarityFilter(contain: Containment = Containment.AnyOf, selected: Set[Rarity] = Set.empty) extends SingletonOptionsFilter[Rarity, RarityFilter] {
  override def faces = FaceSearchOptions.ANY
  override def attribute = CardAttribute.Rarity
  override val unified = true
  override def value = _.rarity
  override def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[Rarity]) = copy(contain = contain, selected = selected)
}