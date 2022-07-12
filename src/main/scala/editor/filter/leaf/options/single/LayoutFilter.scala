package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.card.CardLayout
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * A filter that groups cards based on layout.
 * @author Alec Roelke
 */
final case class LayoutFilter(contain: Containment = Containment.AnyOf, selected: Set[CardLayout] = Set.empty) extends SingletonOptionsFilter[CardLayout, LayoutFilter] {
  override def faces = FaceSearchOptions.ANY
  override def attribute = CardAttribute.Layout
  override val unified = true
  override def value = _.layout
  override def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[CardLayout]) = copy(contain = contain, selected = selected)
  override def copyFaces(faces: FaceSearchOptions) = this
}