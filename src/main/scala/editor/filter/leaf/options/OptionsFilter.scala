package editor.filter.leaf.options

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.FilterLeaf
import editor.util.Containment

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * A type of filter that groups cards by attributes that have values taken from a set of discreet possibiliies.
 * 
 * @constructor create a new options filter
 * @param t attribute to filter by
 * @param unified whether or not the value of an attribute is the same across all card faces
 * @tparam T type of data used for filtering
 * 
 * @author Alec Roelke
 */
abstract class OptionsFilter[T](t: CardAttribute, unified: Boolean) extends FilterLeaf(t, unified) {
  /** Function to use to compare card attributes. */
  var contain = Containment.CONTAINS_ANY_OF
  /** Set of items to look for in cards. */
  var selected = Set[T]() // Using an immutable var guarantees that changing this in a copy doesn't change this filter's version

  /**
   * Convert a string to a value of the type of data being used for filtering.
   * 
   * @param str string to convert
   * @return a value of the same type as the data of the filter
   */
  protected def convertFromString(str: String): T

  /**
   * Convert an attribute value to a JSON element.
   * 
   * @param item value to convert
   * @return a JSON element representing the value
   */
  protected def convertToJson(item: T): JsonElement

  /**
   * Get an attribute value from a JSON element.
   * 
   * @param item JSON element to convert
   * @return a value of the type of data used for filtering
   */
  protected def convertFromJson(item: JsonElement): T

  override protected def serializeLeaf(fields: JsonObject) = {
    fields.addProperty("contains", contain.toString)
    fields.add("selected", { val array = JsonArray(); selected.foreach((i) => array.add(convertToJson(i))); array })
  }

  override protected def deserializeLeaf(fields: JsonObject) = {
    contain = Containment.parseContainment(fields.get("contains").getAsString)
    fields.get("selected").getAsJsonArray.asScala.foreach(selected += convertFromJson(_))
  }

  override def leafEquals(other: Any) = other match {
    case o: OptionsFilter[?] if o.getClass == getClass => o.attribute == attribute && o.contain == contain && o.selected == selected
    case _ => false
  }

  override def hashCode = Objects.hash(attribute, contain, selected)
}