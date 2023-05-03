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
import editor.database.attributes.HasTextFilter
import editor.database.attributes.HasNumberFilter
import editor.database.attributes.HasColorFilter
import editor.database.attributes.HasSingletonOptionsFilter

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
      case text: HasTextFilter => text.filter.copy(faces = faces)
      case number: HasNumberFilter => number.filter.copy(faces = faces)
      case color: HasColorFilter => color.filter
      case single: HasSingletonOptionsFilter[_] => single.filter.copy(selected = selected.map(_.flatMap(single.parse)).get)
      case CardAttribute.ManaCost => CardAttribute.ManaCost.filter.copy(faces = faces)
      case CardAttribute.Devotion => CardAttribute.Devotion.filter.copy(faces = faces, types = (v \ "colors") match {
        case JArray(colors) => colors.map((s) => ManaType.parse(s.extract[String]).get).toSet
        case x => throw MatchError(x)
      }, operation = Comparison.valueOf((v \ "operation").extract[String].apply(0)), operand = (v \ "operand").extract[Int])
      case CardAttribute.TypeLine => CardAttribute.TypeLine.filter.copy(faces = faces)
      case CardAttribute.CardType => CardAttribute.CardType.filter.copy(selected = selected.get)
      case CardAttribute.Subtype => CardAttribute.Subtype.filter.copy(selected = selected.get)
      case CardAttribute.Supertype => CardAttribute.Supertype.filter.copy(selected = selected.get)
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
  { case filter: Filter => JObject(List(JField("type", JString(filter.attribute.toString))) ++ (filter match {
    case l: FilterLeaf => List(JField("faces", JString(l.faces.toString)))
    case g: FilterGroup => List(
      JField("mode", JString(g.mode.toString)),
      JField("comment", JString(g.comment)),
      JField("children", JArray(g.map(Extraction.decompose).toList))
    )
  }) ++ (filter match {
    case o: OptionsFilter[?, ?] => List(JField("contains", JString(o.contain.toString)))
    case _ => Nil
  }) ++ (filter match {
    case t: TypeLineFilter => List(
      JField("contains", JString(t.contain.toString)),
      JField("pattern", JString(t.line))
    )
    case t: TextFilter => List(
      JField("contains", JString(t.contain.toString)),
      JField("regex", JBool(t.regex)),
      JField("pattern", JString(t.text))
    )
    case n: NumberFilter => List(
      JField("operation", JString(n.operation.toString)),
      JField("operand", JDouble(n.operand))
    ) ++ n.variable.map(_ => JField("varies", JBool(n.varies))).toList
    case m: ManaCostFilter => List(
      JField("contains", JString(m.contain.toString)),
      JField("cost", JString(m.cost.toString))
    )
    case c: ColorFilter => List(
      JField("contains", JString(c.contain.toString)),
      JField("colors", JArray(c.colors.map((m) => JString(m.toString)).toList)),
      JField("multicolored", JBool(c.multicolored))
    )
    case d: DevotionFilter => List(
      JField("colors", JArray(d.types.map((m) => JString(m.toString)).toList)),
      JField("operation", JString(d.operation.toString)),
      JField("operand", JInt(d.operand))
    )
    case _: BinaryFilter => Nil // Nothing additional actually needs to be serialized
    case o: OptionsFilter[?, ?] => List(JField("selected", JArray(o.selected.map((e) => JString(e.toString)).toList)))
    case _ => Nil
  }))}
))