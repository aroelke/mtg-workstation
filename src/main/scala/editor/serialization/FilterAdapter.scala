package editor.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.database.attributes.CardAttribute
import editor.database.attributes.Expansion
import editor.database.attributes.ManaCost
import editor.database.attributes.ManaType
import editor.database.attributes.Rarity
import editor.database.card.CardLayout
import editor.filter.FaceSearchOptions
import editor.filter.Filter
import editor.filter.FilterGroup
import editor.filter.leaf._
import editor.util.Comparison
import editor.util.Containment

import java.lang.reflect.Type
import scala.jdk.CollectionConverters._
import org.json4s._
import org.json4s.native._

/**
 * JSON serializer/deserializer for [[Filter]]s using their methods for converting to/from JSON objects.
 * @author Alec Roelke
 */
class FilterAdapter extends CustomSerializer[Filter](implicit format => (
  { case v =>
    val faces = (v \ "faces").extract[Option[String]].map(FaceSearchOptions.valueOf).getOrElse(FaceSearchOptions.ANY)
    val selected = (v \ "selected").extract[Option[Set[String]]]
    val contain = (v \ "contains").extract[Option[String]].flatMap(Containment.parse)
    (v \ "type").extract[CardAttribute[?, ?]] match {
      case CardAttribute.Name => CardAttribute.Name.filter.copy(faces = faces)
      case CardAttribute.RulesText => CardAttribute.RulesText.filter.copy(faces = faces)
      case CardAttribute.FlavorText => CardAttribute.FlavorText.filter.copy(faces = faces)
      case CardAttribute.ManaCost => CardAttribute.ManaCost.filter.copy(faces = faces)
      case CardAttribute.RealManaValue => CardAttribute.RealManaValue.filter
      case CardAttribute.EffManaValue => CardAttribute.EffManaValue.filter.copy(faces = faces)
      case CardAttribute.Colors => CardAttribute.Colors.filter
      case CardAttribute.ColorIdentity => CardAttribute.ColorIdentity.filter
      case CardAttribute.TypeLine => CardAttribute.TypeLine.filter.copy(faces = faces)
      case CardAttribute.PrintedTypes => CardAttribute.PrintedTypes.filter.copy(faces = faces)
      case CardAttribute.CardType => CardAttribute.CardType.filter.copy(selected = selected.get)
      case CardAttribute.Subtype => CardAttribute.Subtype.filter.copy(selected = selected.get)
      case CardAttribute.Supertype => CardAttribute.Supertype.filter.copy(selected = selected.get)
      case CardAttribute.Power => CardAttribute.Power.filter.copy(faces = faces)
      case CardAttribute.Toughness => CardAttribute.Toughness.filter.copy(faces = faces)
      case CardAttribute.Loyalty => CardAttribute.Loyalty.filter.copy(faces = faces)
      case CardAttribute.Layout => CardAttribute.Layout.filter.copy(selected = selected.get.map((v) => CardLayout.valueOf(v.replace(' ', '_').toUpperCase)))
      case CardAttribute.Expansion => CardAttribute.Expansion.filter.copy(selected = selected.get.map((v) => Expansion.expansions.find(_.name == v).getOrElse(throw MatchError(v))).toSet)
      case CardAttribute.Block => CardAttribute.Block.filter.copy(selected = selected.get)
      case CardAttribute.Rarity => CardAttribute.Rarity.filter.copy(selected = selected.get.map((v) => Rarity.parse(v).getOrElse(Rarity.Unknown)))
      case CardAttribute.Artist => CardAttribute.Artist.filter.copy(faces = faces)
      case CardAttribute.CardNumber => CardAttribute.CardNumber.filter.copy(faces = faces)
      case CardAttribute.LegalIn => CardAttribute.LegalIn.filter.copy(selected = selected.get, restricted = (v \ "restricted").extract[Boolean])
      case CardAttribute.Tags => CardAttribute.Tags.filter.copy(selected = selected.get)
      case CardAttribute.AnyCard => CardAttribute.AnyCard.filter
      case CardAttribute.NoCard => CardAttribute.NoCard.filter
      case CardAttribute.Group => FilterGroup(
        (v \ "children").extract[Seq[Filter]],
        FilterGroup.Mode.values.find(_.toString.equalsIgnoreCase((v \ "mode").extract[String])).getOrElse(FilterGroup.Mode.And),
        (v \ "comment").extract[Option[String]].getOrElse("")
      )
      case x => throw IllegalArgumentException(s"$x is not a filterable attribute")
    } match {
      case t: TextFilter => t.copy(contain = contain.get, regex = (v \ "regex").extract[Boolean], text = (v \ "pattern").extract[String])
      case t: TypeLineFilter => t.copy(contain = contain.get, line = (v \ "pattern").extract[String])
      case m: ManaCostFilter => m.copy(contain = contain.get, cost = ManaCost.parse((v \ "cost").extract[String]).get)
      case c: ColorFilter =>
        val colors = (v \ "colors") match {
          case JArray(colors) => colors.map((s) => ManaType.parse(s.extract[String]).get).toSet
          case x => throw MatchError(x)
        }
        c.copy(contain = contain.get, colors = colors, multicolored = (v \ "multicolored").extract[Boolean])
      case n: NumberFilter => n.copy(operation = Comparison.valueOf((v \ "operation").extract[String].apply(0)), operand = (v \ "operand").extract[Double], varies = n.variable.isDefined && (v \ "varies").extract[Boolean])
      case s: SingletonOptionsFilter[?] => s.copy(contain = contain.get)
      case m: MultiOptionsFilter[?] => m.copy(contain = contain.get)
      case f => f
    }},
  { case filter: Filter =>
    var fields = List(JField("type", JString(filter.attribute.toString)))
    filter match {
      case l: FilterLeaf => fields = fields :+ JField("faces", JString(l.faces.toString))
      case g: FilterGroup =>
        fields = fields ++ List(
          JField("mode", JString(g.mode.toString)),
          JField("comment", JString(g.comment)),
          JField("children", JArray(g.map(Extraction.decompose).toList))
        )
    }
    JObject(fields) }
)) with JsonSerializer[Filter] {
  override def serialize(src: Filter, typeOfSrc: Type, context: JsonSerializationContext) = {
    val json = JsonObject()
    json.addProperty("type", src.attribute.toString)
    src match {
      case l: FilterLeaf => json.addProperty("faces", l.faces.toString)
      case g: FilterGroup =>
        json.addProperty("mode", g.mode.toString)
        json.addProperty("comment", g.comment)
        val array = JsonArray()
        g.foreach((f) => array.add(context.serialize(f)))
        json.add("children", array)
    }
    src match {
      case o: OptionsFilter[?, ?] => json.addProperty("contains", o.contain.toString)
      case _ =>
    }
    src match {
      case t: TypeLineFilter =>
        json.addProperty("contains", t.contain.toString)
        json.addProperty("pattern", t.line)
      case t: TextFilter =>
        json.addProperty("contains", t.contain.toString)
        json.addProperty("regex", t.regex)
        json.addProperty("pattern", t.text)
      case n: NumberFilter =>
        json.addProperty("operation", n.operation.toString)
        json.addProperty("operand", n.operand)
        if (n.variable.isDefined)
          json.addProperty("varies", n.varies)
      case m: ManaCostFilter =>
        json.addProperty("contains", m.contain.toString)
        json.addProperty("cost", m.cost.toString)
      case c: ColorFilter =>
        json.addProperty("contains", c.contain.toString)
        val array = JsonArray()
        c.colors.foreach((t) => array.add(t.toString))
        json.add("colors", array)
        json.addProperty("multicolored", c.multicolored)
      case _: BinaryFilter => // Nothing additional actually needs to be serialized
      case o: OptionsFilter[?, ?] =>
        val array = JsonArray()
        o.selected.foreach((i) => array.add(i match {
          case e @ (_: Rarity | _: CardLayout) => e.toString
          case e: Expansion => e.toString
          case s: String => s
        }))
        json.add("selected", array)
      case _ =>
    }
    json
  }
}