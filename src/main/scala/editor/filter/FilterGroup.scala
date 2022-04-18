package editor.filter

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.card.Card

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * Object containing global information about filter groups.
 * @author Alec Roelke
 */
object FilterGroup {
  /**
   * A way in which filters that are part of a group can be combined to produce a new filter.
   * 
   * @constructor create a new filter combination mode
   * @param mode name of the mode
   * @param function function describing how to combine the filters
   * 
   * @author Alec Roelke
   */
  enum Mode(mode: String, function: (Iterable[? <: Filter], (? >: Filter) => Boolean) => Boolean) extends Function2[Iterable[? <: Filter], Card, Boolean] {
    override def apply(filters: Iterable[? <: Filter], card: Card) = function(filters, _(card))

    override val toString = mode
    
    /** All filters must pass a card for it to pass the group. */
    case And extends Mode("all of", _.forall(_))
    /** No filters must pass a card for it to pass the group. */
    case Nor extends Mode("none of", !_.exists(_))
    /** Any filter can pass a card for it to pass the group. */
    case Or  extends Mode("any of", _.exists(_))
  }
}

/**
 * A group of filters that can be combined together in various ways. Can contain other groups that combine their
 * filters in their own ways.
 * 
 * @constructor create a new filter group
 * @param filters initial filters that are part of the group
 * 
 * @author Alec Roelke
 */
class FilterGroup(filters: Iterable[Filter] = Seq.empty) extends Filter(CardAttribute.GROUP) with Iterable[Filter] {
  import FilterGroup._

  /** How filters in the group are combined. */
  var mode = Mode.And
  /** User-defined title or comment for the group. */
  var comment = ""

  private val children = collection.mutable.Buffer[Filter]()

  /**
   * Add a new filter to the group.
   * @param filter filter to add
   */
  def addChild(filter: Filter) = children += filter

  filters.foreach(addChild)

  override def apply(c: Card) = mode(children, c)

  override def copy = {
    val filter = FilterGroup()
    children.foreach((f) => filter.addChild(f.copy))
    filter.mode = mode
    filter.comment = comment
    filter
  }

  override def iterator = children.iterator

  override protected def serializeFields(fields: JsonObject) = {
    fields.addProperty("mode", mode.toString)
    fields.addProperty("comment", comment)
    val array = JsonArray()
    children.foreach((f) => array.add(f.toJsonObject))
    fields.add("children", array)
  }

  override def equals(other: Any) = other match {
    case o: FilterGroup => o.mode == mode && o.comment == comment && o.children == children
    case _ => false
  }

  override def hashCode = Objects.hash(mode, comment, children)
}