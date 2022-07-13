package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * A filter that groups cards by the block they belong in.
 * @author Alec Roelke
 */
final case class BlockFilter(contain: Containment = Containment.AnyOf, selected: Set[String] = Set.empty) extends SingletonOptionsFilter[String, BlockFilter] {
  override def faces = FaceSearchOptions.ANY
  override def attribute = CardAttribute.Block
  override val unified = true
  override def value = _.expansion.block
  override def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[String]) = copy(contain = contain, selected = selected)
}