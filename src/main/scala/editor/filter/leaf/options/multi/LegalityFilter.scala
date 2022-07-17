package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.attributes.Legality
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.util.Containment
import editor.filter.leaf.options.OptionsFilter

/**
 * Filter that groups cards by format legality and, optionally, by whether or not they are restricted in those formats (if applicable).
 * 
 * @constructor create a new legality filter
 * @param restricted whether or not to only include cards that are restricted in the formats
 * 
 * @author Alec Roelke
 */
final case class LegalityFilter(contain: Containment = Containment.AnyOf, selected: Set[String] = Set.empty, restricted: Boolean = false) extends OptionsFilter[String, LegalityFilter] {
  override def faces = FaceSearchOptions.ANY
  override def attribute = CardAttribute.LegalIn
  override val unified = true
  override def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[String]) = copy(contain = contain, selected = selected)

  override protected def testFace(c: Card) = {
    if (!contain(c.legalIn, selected))
      false
    else if (restricted)
      c.legalIn.filter(selected.contains).forall(c.legality(_) == Legality.Restricted)
    else
      true
  }
}