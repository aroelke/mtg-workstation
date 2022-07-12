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
import editor.filter.leaf.options.OptionsFilter
import editor.filter.leaf.options.multi._
import editor.filter.leaf.options.single._
import editor.util.Comparison
import editor.util.Containment

import java.lang.reflect.Type
import scala.jdk.CollectionConverters._

/**
 * JSON serializer/deserializer for [[Filter]]s using their methods for converting to/from JSON objects.
 * @author Alec Roelke
 */
class FilterAdapter extends JsonSerializer[Filter] with JsonDeserializer[Filter] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val obj = json.getAsJsonObject
    context.deserialize[CardAttribute[?, ?]](obj.get("type"), classOf[CardAttribute[?, ?]]) match {
      case CardAttribute.Name => TextFilter(CardAttribute.Name, _.normalizedName)
      case CardAttribute.RulesText => TextFilter(CardAttribute.RulesText, _.normalizedOracle)
      case CardAttribute.FlavorText => TextFilter(CardAttribute.FlavorText, _.normalizedFlavor)
      case CardAttribute.ManaCost => ManaCostFilter()
      case CardAttribute.RealManaValue => NumberFilter(CardAttribute.RealManaValue, true, _.manaValue)
      case CardAttribute.EffManaValue => NumberFilter(CardAttribute.EffManaValue, false, _.manaValue)
      case CardAttribute.Colors => ColorFilter(CardAttribute.Colors, _.colors)
      case CardAttribute.ColorIdentity => ColorFilter(CardAttribute.ColorIdentity, _.colorIdentity)
      case CardAttribute.TypeLine => TypeLineFilter()
      case CardAttribute.PrintedTypes => TextFilter(CardAttribute.PrintedTypes, _.faces.map(_.printedTypes))
      case CardAttribute.CardType => CardTypeFilter(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.Subtype => SubtypeFilter(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.Supertype => SupertypeFilter(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.Power => NumberFilter(CardAttribute.Power, false, _.power.value, Some(_.powerVariable))
      case CardAttribute.Toughness => NumberFilter(CardAttribute.Toughness, false, _.toughness.value, Some(_.toughnessVariable))
      case CardAttribute.Loyalty => NumberFilter(CardAttribute.Loyalty, false, _.loyalty.value, Some(_.loyaltyVariable))
      case CardAttribute.Layout => LayoutFilter(selected = obj.get("selected").getAsJsonArray.asScala.map((v) => CardLayout.valueOf(v.getAsString.replace(' ', '_').toUpperCase)).toSet)
      case CardAttribute.Expansion => ExpansionFilter(selected = obj.get("selected").getAsJsonArray.asScala.map((v) => Expansion.expansions.find(_.name == v.getAsString).getOrElse(throw JsonParseException(s"unknown expansion \"${v.getAsString}\""))).toSet)
      case CardAttribute.Block => BlockFilter(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.Rarity => RarityFilter(selected = obj.get("selected").getAsJsonArray.asScala.map((v) => Rarity.parse(v.getAsString).getOrElse(Rarity.Unknown)).toSet)
      case CardAttribute.Artist => TextFilter(CardAttribute.Artist, _.faces.map(_.artist))
      case CardAttribute.CardNumber => NumberFilter(CardAttribute.CardNumber, false, (f) => try f.number.replace("--", "0").replaceAll("[\\D]", "").toInt catch case _: NumberFormatException => 0.0)
      case CardAttribute.LegalIn => LegalityFilter(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet, restricted = obj.get("restricted").getAsBoolean)
      case CardAttribute.Tags => TagsFilter(selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet)
      case CardAttribute.AnyCard => BinaryFilter(true)
      case CardAttribute.NoCard => BinaryFilter(false)
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
      case o: OptionsFilter[?, ?] => o.copy(faces = o.faces, contain = Containment.parse(obj.get("contains").getAsString).get, selected = o.selected)
      case f => f
    } match {
      case l: FilterLeaf[?] => l.copyFaces(faces = Option(obj.get("faces")).map((f) => FaceSearchOptions.valueOf(f.getAsString)).getOrElse(FaceSearchOptions.ANY))
      case g => g
    }
  }

  override def serialize(src: Filter, typeOfSrc: Type, context: JsonSerializationContext) = {
    val json = JsonObject()
    json.addProperty("type", src.attribute.toString)
    src match {
      case l: FilterLeaf[?] => json.addProperty("faces", l.faces.toString)
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