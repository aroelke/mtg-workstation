package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.util.Containment

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by all of their types.
 * @author Alec Roelke
 */
class TypeLineFilter extends FilterLeaf(CardAttribute.TYPE_LINE, false) {
  var contain = Containment.CONTAINS_ANY_OF
  var line = ""

  override protected def testFace(c: Card) = !line.isEmpty && contain.test(c.typeLine.toSet.map(_.toLowerCase).asJava, line.toLowerCase.split("\\s").toSeq.asJava)

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.TYPE_LINE).asInstanceOf[TypeLineFilter]
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