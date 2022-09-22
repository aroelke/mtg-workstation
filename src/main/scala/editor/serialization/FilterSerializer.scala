package editor.serialization

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
import org.json4s._
import org.json4s.native._

/**
 * JSON serializer/deserializer for [[Filter]]s using their methods for converting to/from JSON objects.
 * @author Alec Roelke
 */
object FilterSerializer extends CustomSerializer[Filter](implicit format => (
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
      case l: FilterLeaf => fields :+= JField("faces", JString(l.faces.toString))
      case g: FilterGroup =>
        fields = fields ++ List(
          JField("mode", JString(g.mode.toString)),
          JField("comment", JString(g.comment)),
          JField("children", JArray(g.map(Extraction.decompose).toList))
        )
    }
    filter match {
      case o: OptionsFilter[?, ?] => fields :+= JField("contains", JString(o.contain.toString))
      case _ =>
    }
    filter match {
      case t: TypeLineFilter => fields ++= List(
        JField("contains", JString(t.contain.toString)),
        JField("pattern", JString(t.line))
      )
      case t: TextFilter => fields ++= List(
        JField("contains", JString(t.contain.toString)),
        JField("regex", JBool(t.regex)),
        JField("pattern", JString(t.text))
      )
      case n: NumberFilter => fields ++= List(
        JField("operation", JString(n.operation.toString)),
        JField("operand", JDouble(n.operand))
      ) ++ n.variable.map(_ => JField("varies", JBool(n.varies))).toList
      case m: ManaCostFilter => fields ++= List(
        JField("contains", JString(m.contain.toString)),
        JField("cost", JString(m.cost.toString))
      )
      case c: ColorFilter => fields ++= List(
        JField("contains", JString(c.contain.toString)),
        JField("colors", JArray(c.colors.map(Extraction.decompose).toList)),
        JField("multicolored", JBool(c.multicolored))
      )
      case _: BinaryFilter => // Nothing additional actually needs to be serialized
      case o: OptionsFilter[?, ?] => fields :+= JField("selected", JArray(o.selected.map((e) => JString(e.toString)).toList))
      case _ =>
    }
    JObject(fields) }
))