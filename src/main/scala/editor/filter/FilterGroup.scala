package editor.filter

import editor.database.attributes.CardAttribute
import editor.database.card.Card

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
  enum Mode(mode: String, function: (Iterable[? <: Filter], (? >: Filter) => Boolean) => Boolean) extends ((Iterable[? <: Filter], Card) => Boolean) {
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
final case class FilterGroup(children: Iterable[Filter] = Seq.empty, mode: FilterGroup.Mode = FilterGroup.Mode.And, comment: String = "") extends Filter with Iterable[Filter] {
  import FilterGroup._

  /** How filters in the group are combined. */
//  var mode = Mode.And
  /** User-defined title or comment for the group. */
//  var comment = ""

//  private val children = collection.mutable.Buffer[Filter]()

  override def attribute = CardAttribute.Group
  override def apply(c: Card) = mode(children, c)
  override def iterator = children.iterator
}