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
import org.json4s.CustomSerializer
import org.json4s.JObject
import org.json4s.JString
import org.json4s.Extraction
import org.json4s.JArray
import org.json4s.JBool
import org.json4s.JField
import org.json4s.JDouble

/**
 * JSON serializer/deserializer for [[Filter]]s using their methods for converting to/from JSON objects.
 * @author Alec Roelke
 */
class FilterAdapter extends CustomSerializer[Filter](implicit format => (
  { case JObject(obj) =>
    val faces = obj.collect{ case ("faces", JString(faces)) => FaceSearchOptions.valueOf(faces) }.headOption.getOrElse(FaceSearchOptions.ANY)
    val selected = obj.collect{ case ("selected", JArray(selected)) => selected.collect{ case JString(item) => item }.toSet}.headOption
    val contain = obj.collect{ case ("contains", JString(contain)) => Containment.parse(contain) }.flatten.headOption
    obj.collect{ case ("type", json) => Extraction.extract[CardAttribute[?, ?]](json) }.head match {
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
      case CardAttribute.Expansion => CardAttribute.Expansion.filter.copy(selected = selected.get.map((v) => Expansion.expansions.find(_.name == v).getOrElse(throw IllegalArgumentException(s"unknown expansion \"$v\""))).toSet)
      case CardAttribute.Block => CardAttribute.Block.filter.copy(selected = selected.get)
      case CardAttribute.Rarity => CardAttribute.Rarity.filter.copy(selected = selected.get.map((v) => Rarity.parse(v).getOrElse(Rarity.Unknown)))
      case CardAttribute.Artist => CardAttribute.Artist.filter.copy(faces = faces)
      case CardAttribute.CardNumber => CardAttribute.CardNumber.filter.copy(faces = faces)
      case CardAttribute.LegalIn => CardAttribute.LegalIn.filter.copy(selected = selected.get, restricted = obj.collect{ case ("restricted", JBool(restricted)) => restricted }.head)
      case CardAttribute.Tags => CardAttribute.Tags.filter.copy(selected = selected.get)
      case CardAttribute.AnyCard => CardAttribute.AnyCard.filter
      case CardAttribute.NoCard => CardAttribute.NoCard.filter
      case CardAttribute.Group => FilterGroup(
        obj.collect{ case ("children", JArray(children)) => children.map(Extraction.extract[Filter]) }.head,
        obj.collect{ case ("mode", JString(mode)) => FilterGroup.Mode.values.find(_.toString == mode) }.flatten.headOption.getOrElse(FilterGroup.Mode.And),
        obj.collect{ case ("comment", JString(comment)) => comment }.headOption.getOrElse("")
      )
      case x => throw IllegalArgumentException(s"$x is not a filterable attribute")
    } match {
      case t: TextFilter => t.copy(contain = contain.get, regex = obj.collect{ case ("regex", JBool(regex)) => regex }.head, text = obj.collect{ case ("pattern", JString(pattern)) => pattern }.head)
      case t: TypeLineFilter => t.copy(contain = contain.get, line = obj.collect{ case ("pattern", JString(pattern)) => pattern }.head)
      case m: ManaCostFilter => m.copy(contain = contain.get, cost = obj.collect{ case ("cost", JString(cost)) => ManaCost.parse(cost).get }.head)
      case c: ColorFilter =>
        val colors = obj.collect{ case ("colors", JArray(colors)) => colors.collect{ case JString(color) => ManaType.parse(color).get }}.flatten.toSet
        c.copy(contain = contain.get, colors = colors, multicolored = obj.collect{ case ("multicolored", JBool(multi)) => multi }.head)
      case n: NumberFilter => n.copy(operation = obj.collect{ case ("operation", JString(op)) => Comparison.valueOf(op(0)) }.head, operand = obj.collect{ case ("operand", JDouble(op)) => op }.headOption.getOrElse(throw IllegalArgumentException(obj.mkString("\n", "\n", "\n"))), varies = n.variable.isDefined && obj.collect{ case ("varies", JBool(varies)) => varies }.head)
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
)) with JsonSerializer[Filter] with JsonDeserializer[Filter] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val obj = json.getAsJsonObject
    val faces = Option(obj.get("faces")).map((f) => FaceSearchOptions.valueOf(f.getAsString)).getOrElse(FaceSearchOptions.ANY)
    context.deserialize[CardAttribute[?, ?]](obj.get("type"), classOf[CardAttribute[?, ?]]) match {
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
      case CardAttribute.CardType => CardAttribute.CardType.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.Subtype => CardAttribute.Subtype.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.Supertype => CardAttribute.Supertype.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.Power => CardAttribute.Power.filter.copy(faces = faces)
      case CardAttribute.Toughness => CardAttribute.Toughness.filter.copy(faces = faces)
      case CardAttribute.Loyalty => CardAttribute.Loyalty.filter.copy(faces = faces)
      case CardAttribute.Layout => CardAttribute.Layout.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map((v) => CardLayout.valueOf(v.getAsString.replace(' ', '_').toUpperCase)).toSet)
      case CardAttribute.Expansion => CardAttribute.Expansion.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map((v) => Expansion.expansions.find(_.name == v.getAsString).getOrElse(throw JsonParseException(s"unknown expansion \"${v.getAsString}\""))).toSet)
      case CardAttribute.Block => CardAttribute.Block.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.Rarity => CardAttribute.Rarity.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map((v) => Rarity.parse(v.getAsString).getOrElse(Rarity.Unknown)).toSet)
      case CardAttribute.Artist => CardAttribute.Artist.filter.copy(faces = faces)
      case CardAttribute.CardNumber => CardAttribute.CardNumber.filter.copy(faces = faces)
      case CardAttribute.LegalIn => CardAttribute.LegalIn.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet, restricted = obj.get("restricted").getAsBoolean)
      case CardAttribute.Tags => CardAttribute.Tags.filter.copy(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.AnyCard => CardAttribute.AnyCard.filter
      case CardAttribute.NoCard => CardAttribute.NoCard.filter
      case CardAttribute.Group => FilterGroup(
        obj.get("children").getAsJsonArray.asScala.map((element) => context.deserialize[Filter](element, classOf[Filter])),
        FilterGroup.Mode.values.find(_.toString == obj.get("mode").getAsString).getOrElse(FilterGroup.Mode.And),
        Option(obj.get("comment")).map(_.getAsString).getOrElse("")
      )
      case x => throw JsonParseException(s"attribute $x can't be used to filter")
    } match {
      case t: TextFilter => t.copy(contain = Containment.parse(obj.get("contains").getAsString).get, regex = obj.get("regex").getAsBoolean, text = obj.get("pattern").getAsString)
      case t: TypeLineFilter => t.copy(contain = Containment.parse(obj.get("contains").getAsString).get, line = obj.get("pattern").getAsString)
      case m: ManaCostFilter => m.copy(contain = Containment.parse(obj.get("contains").getAsString).get, cost = ManaCost.parse(obj.get("cost").getAsString).get)
      case c: ColorFilter => c.copy(contain = Containment.parse(obj.get("contains").getAsString).get, colors = obj.get("colors").getAsJsonArray.asScala.map((e) => ManaType.parse(e.getAsString).get).toSet, multicolored = obj.get("multicolored").getAsBoolean)
      case n: NumberFilter => n.copy(operation = Comparison.valueOf(obj.get("operation").getAsString.apply(0)), operand = obj.get("operand").getAsDouble, varies = n.variable.isDefined && obj.get("varies").getAsBoolean)
      case s: SingletonOptionsFilter[?] => s.copy(contain = Containment.parse(obj.get("contains").getAsString).get)
      case m: MultiOptionsFilter[?] => m.copy(contain = Containment.parse(obj.get("contains").getAsString).get)
      case f => f
    }
  }

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