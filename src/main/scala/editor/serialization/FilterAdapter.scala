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
  import CardAttribute._

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val obj = json.getAsJsonObject
    context.deserialize[CardAttribute](obj.get("type"), classOf[CardAttribute]) match {
      case NAME => TextFilter(NAME, _.normalizedName)
      case RULES_TEXT => TextFilter(RULES_TEXT, _.normalizedOracle)
      case FLAVOR_TEXT => TextFilter(FLAVOR_TEXT, _.normalizedFlavor)
      case MANA_COST => ManaCostFilter()
      case REAL_MANA_VALUE => NumberFilter(REAL_MANA_VALUE, true, _.manaValue)
      case EFF_MANA_VALUE => NumberFilter(EFF_MANA_VALUE, false, _.manaValue)
      case COLORS => ColorFilter(COLORS, _.colors)
      case COLOR_IDENTITY => ColorFilter(COLOR_IDENTITY, _.colorIdentity)
      case TYPE_LINE => TypeLineFilter()
      case PRINTED_TYPES => TextFilter(PRINTED_TYPES, _.faces.map(_.printedTypes))
      case CARD_TYPE =>
        val filter = CardTypeFilter()
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet
        filter
      case SUBTYPE =>
        val filter = SubtypeFilter()
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet
        filter
      case SUPERTYPE =>
        val filter = SupertypeFilter()
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet
        filter
      case POWER => VariableNumberFilter(POWER, _.power.value, _.powerVariable)
      case TOUGHNESS => VariableNumberFilter(TOUGHNESS, _.toughness.value, _.toughnessVariable)
      case LOYALTY => VariableNumberFilter(LOYALTY, _.loyalty.value, _.loyaltyVariable)
      case LAYOUT =>
        val filter = LayoutFilter()
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map((v) => CardLayout.valueOf(v.getAsString.replace(' ', '_').toUpperCase)).toSet
        filter
      case EXPANSION =>
        val filter = ExpansionFilter()
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map((v) => Expansion.expansions.find(_.name == v.getAsString).getOrElse(throw JsonParseException(s"unknown expansion \"${v.getAsString}\""))).toSet
        filter
      case BLOCK =>
        val filter = BlockFilter()
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet
        filter
      case RARITY =>
        val filter = RarityFilter()
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map((v) => Rarity.parseRarity(v.getAsString)).toSet
        filter
      case ARTIST => TextFilter(ARTIST, _.faces.map(_.artist))
      case CARD_NUMBER => NumberFilter(CARD_NUMBER, false, (f) => try f.number.replace("--", "0").replaceAll("[\\D]", "").toInt catch case _: NumberFormatException => 0.0)
      case LEGAL_IN =>
        val filter = LegalityFilter()
        filter.restricted = obj.get("restricted").getAsBoolean
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet
        filter
      case TAGS =>
        val filter = TagsFilter()
        filter.selected = obj.get("selected").getAsJsonArray.asScala.map(_.getAsString).toSet
        filter
      case ANY => BinaryFilter(true)
      case NONE => BinaryFilter(false)
      case GROUP =>
        val group = FilterGroup(obj.get("children").getAsJsonArray.asScala.map((element) => context.deserialize[Filter](element, classOf[Filter])))
        group.mode = FilterGroup.Mode.values.find(_.toString == obj.get("mode").getAsString).getOrElse(FilterGroup.Mode.And)
        group.comment = Option(obj.get("comment")).map(_.getAsString).getOrElse("")
        group
      case x => throw JsonParseException(s"attribute $x can't be used to filter")
    } match {
      case t: TextFilter =>
        t.contain = Containment.parseContainment(obj.get("contains").getAsString)
        t.regex = obj.get("regex").getAsBoolean
        t.text = obj.get("pattern").getAsString
        t
      case t: TypeLineFilter =>
        t.contain = Containment.parseContainment(obj.get("contains").getAsString)
        t.line = obj.get("pattern").getAsString
        t
      case m: ManaCostFilter =>
        m.contain = Containment.parseContainment(obj.get("contains").getAsString)
        m.cost = ManaCost.parseManaCost(obj.get("cost").getAsString)
        m
      case c: ColorFilter =>
        c.contain = Containment.parseContainment(obj.get("contains").getAsString)
        c.colors = obj.get("colors").getAsJsonArray.asScala.map((e) => ManaType.parseManaType(e.getAsString)).toSet
        c.multicolored = obj.get("multicolored").getAsBoolean()
        c
      case v: VariableNumberFilter =>
        v.operation = Comparison.valueOf(obj.get("operation").getAsString)
        v.operand = obj.get("operand").getAsDouble
        v.varies = obj.get("varies").getAsBoolean
        v
      case n: NumberFilter =>
        n.operation = Comparison.valueOf(obj.get("operation").getAsString)
        n.operand = obj.get("operand").getAsDouble
        n
      case o: OptionsFilter[?] =>
        o.contain = Containment.parseContainment(obj.get("contains").getAsString)
        o
      case f => f
    } match {
      case l: FilterLeaf =>
        l.faces = Option(obj.get("faces")).map((f) => FaceSearchOptions.valueOf(f.getAsString)).getOrElse(FaceSearchOptions.ANY)
        l
      case g => g
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
      case o: OptionsFilter[?] => json.addProperty("contains", o.contain.toString)
      case _ =>
    }
    src match {
      case v: VariableNumberFilter =>
        json.addProperty("operation", v.operation.toString)
        json.addProperty("operand", v.operand)
        json.addProperty("varies", v.varies)
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
      case o: OptionsFilter[?] =>
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