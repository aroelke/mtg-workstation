package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.attributes.Legality
import editor.database.card.Card

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by format legality and, optionally, by whether or not they are restricted in those formats (if applicable).
 * @author Alec Roelke
 */
class LegalityFilter extends MultiOptionsFilter[String](CardAttribute.LEGAL_IN, true, _.legalIn) {
  /** Whether or not matching cards should be restricted in the formats they're legal in. */
  var restricted = false

  override protected def testFace(c: Card) = {
    if (!super.testFace(c))
      false
    else if (restricted)
      c.legalIn.filter(selected.contains).forall(c.legality(_) == Legality.RESTRICTED)
    else
      true
  }

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.LEGAL_IN).asInstanceOf[LegalityFilter]
    filter.contain = contain
    filter.selected = selected
    filter.restricted = restricted
    filter
  }

  override def leafEquals(other: Any) = other match {
    case o: LegalityFilter => o.contain == contain && o.selected == selected && o.restricted == restricted
    case _ => false
  }

  override def hashCode = Objects.hash(contain, selected, restricted)
}
