package editor.filter.leaf

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaType
import editor.database.card.Card
import editor.util.Containment

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by a color attribute.
 * 
 * @constructor create a new filter for a color attribute
 * @param t attribute to filter by
 * @param f function retrieving the value of the attribute from a card
 * 
 * @author Alec Roelke
 */
class ColorFilter(t: CardAttribute, f: (Card) => Seq[ManaType]) extends FilterLeaf[Seq[ManaType]](t, f(_)) {
  var contain = Containment.CONTAINS_ANY_OF
  var colors = Set[ManaType]()
  var multicolored = false

  override protected def testFace(c: Card) = contain.test(function.apply(c).asJava, colors.asJava) && (!multicolored || function.apply(c).size > 1)

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(`type`).asInstanceOf[ColorFilter]
    filter.contain = contain
    filter.colors = colors
    filter.multicolored = multicolored
    filter
  }

  override protected def serializeLeaf(fields: JsonObject) = {
    val array = JsonArray()
    colors.foreach((c) => array.add(c.toString))
    fields.addProperty("contains", contain.toString)
    fields.add("colors", array)
    fields.addProperty("multicolored", multicolored)
  }

  override protected def deserializeLeaf(fields: JsonObject) = {
    contain = Containment.parseContainment(fields.get("contains").getAsString)
    colors = fields.get("colors").getAsJsonArray.asScala.map((e) => ManaType.parseManaType(e.getAsString)).toSet
    multicolored = fields.get("multicolored").getAsBoolean()
  }

  override def leafEquals(other: Any) = other match {
    case o: ColorFilter if o.`type` == `type` => o.colors == colors && o.contain == contain && o.multicolored == multicolored
    case _ => false
  }

  override def hashCode = Objects.hash(`type`, function, colors, contain, multicolored)
}