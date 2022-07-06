package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.util.Containment

import java.util.Objects

/**
 * Filter that groups cards by all of their types.
 * @author Alec Roelke
 */
class TypeLineFilter extends FilterLeaf(CardAttribute.TypeLine, false) {
  var contain = Containment.AnyOf
  var line = ""

  override protected def testFace(c: Card) = !line.isEmpty && contain(c.typeLine.toSet.map(_.toLowerCase), line.toLowerCase.split("\\s").toSeq)

  override protected def copyLeaf = {
    val filter = CardAttribute.TypeLine.filter.get.asInstanceOf[TypeLineFilter]
    filter.contain = contain
    filter.line = line
    filter
  }

  override def leafEquals(other: Any) = other match {
    case o: TypeLineFilter => o.contain == contain && o.line == line
    case _ => false
  }

  override def hashCode = Objects.hash(attribute, contain, line)
}