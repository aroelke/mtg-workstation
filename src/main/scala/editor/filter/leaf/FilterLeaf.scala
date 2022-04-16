package editor.filter.leaf

import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.card.MultiCard
import editor.database.card.SingleCard
import editor.filter.FaceSearchOptions
import editor.filter.Filter

abstract class FilterLeaf(t: CardAttribute, unified: Boolean) extends Filter(t) {
  var faces = FaceSearchOptions.ANY

  protected def testFace(c: Card): Boolean

  final override def apply(c: Card) = if (unified) testFace(c) else c match {
    case s: SingleCard => testFace(c)
    case m: MultiCard => faces match {
      case FaceSearchOptions.ANY   => c.faces.exists(testFace)
      case FaceSearchOptions.ALL   => c.faces.forall(testFace)
      case FaceSearchOptions.FRONT => testFace(c.faces.head)
      case FaceSearchOptions.BACK  => testFace(c.faces.last)
    }
  }

  protected def copyLeaf: FilterLeaf

  final override def copy = {
    val filter = copyLeaf
    filter.faces = faces
    filter
  }

  protected def serializeLeaf(fields: JsonObject): Unit

  final override def serializeFields(fields: JsonObject) = {
    serializeLeaf(fields)
    fields.addProperty("faces", faces.toString)
  }

  protected def deserializeLeaf(fields: JsonObject): Unit

  final override def deserializeFields(fields: JsonObject) = {
    deserializeLeaf(fields)
    faces = if (fields.has("faces")) FaceSearchOptions.valueOf(fields.get("faces").getAsString) else FaceSearchOptions.ANY
  }

  protected def leafEquals(other: Any): Boolean

  override def equals(other: Any) = other match {
    case o: FilterLeaf => leafEquals(o) && o.faces == faces
    case _ => false
  }
}