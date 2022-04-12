package editor.filter.leaf

import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaCost
import editor.database.card.Card
import editor.util.Containment

import java.util.Objects

/**
 * Filter that groups cards by mana cost.
 * @author Alec Roelke
 */
class ManaCostFilter extends FilterLeaf(CardAttribute.MANA_COST, false) {
  import Containment._

  /** Function used for comparing costs. */
  var contain = CONTAINS_ANY_OF
  /** Mana cost to compare cards with. */
  var cost = ManaCost()

  override def testFace(c: Card) = contain match {
    case CONTAINS_ANY_OF      => CONTAINS_ANY_OF.test(c.manaCost, cost)
    case CONTAINS_NONE_OF     => CONTAINS_NONE_OF.test(c.manaCost, cost)
    case CONTAINS_ALL_OF      => c.manaCost.isSuperset(cost)
    case CONTAINS_NOT_ALL_OF  => !c.manaCost.isSuperset(cost)
    case CONTAINS_EXACTLY     => c.manaCost == cost
    case CONTAINS_NOT_EXACTLY => c.manaCost != cost
  }

  override def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.MANA_COST).asInstanceOf[ManaCostFilter]
    filter.contain = contain
    filter.cost = cost
    filter
  }

  override protected def serializeLeaf(fields: JsonObject) = {
    fields.addProperty("contains", contain.toString)
    fields.addProperty("cost", cost.toString)
  }

  override protected def deserializeLeaf(fields: JsonObject) = {
    contain = Containment.parseContainment(fields.get("contains").getAsString)
    cost = ManaCost.parseManaCost(fields.get("cost").getAsString)
  }

  override def leafEquals(other: Any) = other match {
    case o: ManaCostFilter => o.contain == contain && o.cost == cost
    case _ => false
  }

  override def hashCode = Objects.hash(`type`, contain, cost)
}