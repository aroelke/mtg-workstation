package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaCost
import editor.database.card.Card
import editor.util.Containment

import java.util.Objects

/**
 * Filter that groups cards by mana cost.
 * @author Alec Roelke
 */
class ManaCostFilter extends FilterLeaf(CardAttribute.ManaCost, false) {
  import Containment._

  /** Function used for comparing costs. */
  var contain = AnyOf
  /** Mana cost to compare cards with. */
  var cost = ManaCost()

  override def testFace(c: Card) = contain match {
    case AnyOf      => AnyOf(c.manaCost, cost)
    case NoneOf     => NoneOf(c.manaCost, cost)
    case AllOf      => c.manaCost.isSuperset(cost)
    case SomeOf     => !c.manaCost.isSuperset(cost)
    case Exactly    => c.manaCost == cost
    case NotExactly => c.manaCost != cost
  }

  override def copyLeaf = {
    val filter = CardAttribute.ManaCost.filter.get.asInstanceOf[ManaCostFilter]
    filter.contain = contain
    filter.cost = cost
    filter
  }

  override def leafEquals(other: Any) = other match {
    case o: ManaCostFilter => o.contain == contain && o.cost == cost
    case _ => false
  }

  override def hashCode = Objects.hash(attribute, contain, cost)
}