package editor.filter

import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.card.Card

trait Filter(val attribute: CardAttribute) extends Function1[Card, Boolean] with java.util.function.Predicate[Card] {
  var parent: FilterGroup = null

  def copy: Filter

  protected def serializeFields(fields: JsonObject): Unit

  protected def deserializeFields(fields: JsonObject): Unit

  final def toJsonObject = {
    val fields = JsonObject()
    fields.addProperty("type", attribute.toString)
    serializeFields(fields)
    fields
  }

  final def fromJsonObject(obj: JsonObject) = deserializeFields(obj)

  @deprecated final override def test(c: Card) = apply(c)
}