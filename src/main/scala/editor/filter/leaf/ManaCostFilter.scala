package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaCost
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.util.Containment
import editor.util.Containment._

/**
 * Filter that groups cards by mana cost.
 * 
 * @param contain function to use to compare with the card's mana cost
 * @param cost list of mana symbols to use for comparison
 * 
 * @author Alec Roelke
 */
final case class ManaCostFilter(faces: FaceSearchOptions = FaceSearchOptions.ANY, contain: Containment = AnyOf, cost: ManaCost = ManaCost()) extends FilterLeaf[ManaCostFilter] {
  override def attribute = CardAttribute.ManaCost
  override val unified = false
  override def testFace(c: Card) = contain match {
    case AnyOf      => AnyOf(c.manaCost, cost)
    case NoneOf     => NoneOf(c.manaCost, cost)
    case AllOf      => c.manaCost.isSuperset(cost)
    case SomeOf     => !c.manaCost.isSuperset(cost)
    case Exactly    => c.manaCost == cost
    case NotExactly => c.manaCost != cost
  }
  override def copyFaces(faces: FaceSearchOptions) = copy(faces = faces)
}