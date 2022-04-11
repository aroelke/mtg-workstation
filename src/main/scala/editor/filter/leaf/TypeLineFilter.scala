package editor.filter.leaf

import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.util.Containment

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by all of their types.
 * @author Alec Roelke
 */
class TypeLineFilter extends FilterLeaf(CardAttribute.TYPE_LINE) {
  var contain = Containment.CONTAINS_ANY_OF
  var line = ""

  override protected def testFace(c: Card) = !line.isEmpty && contain.test(c.typeLine.toSet.map(_.toLowerCase).asJava, line.toLowerCase.split("\\s").toSeq.asJava)

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.TYPE_LINE).asInstanceOf[TypeLineFilter]
    filter.contain = contain
    filter.line = line
    filter
  }

  override protected def serializeLeaf(fields: JsonObject) = {
    fields.addProperty("contains", contain.toString)
    fields.addProperty("pattern", line)
  }

  override protected def deserializeLeaf(fields: JsonObject) = {
    contain = Containment.parseContainment(fields.get("contains").getAsString)
    line = fields.get("pattern").getAsString
  }

  override def leafEquals(other: Any) = other match {
    case o: TypeLineFilter => o.contain == contain && o.line == line
    case _ => false
  }

  override def hashCode = Objects.hash(`type`, contain, line)
}