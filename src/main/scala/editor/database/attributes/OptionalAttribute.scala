package editor.database.attributes

/**
 * Global data about optional attributes.
 * @author Alec Roelke
 */
object OptionalAttribute {
  /** @return an [[OptionalAttribute]] with no value that's always missing. */
  def empty = new OptionalAttribute { override val exists = false }
}

/**
 * A card attribute that may or may not appear on a card, such as power or toughness which only appears on creatures
 * and Vehicles.  Note that [[ManaCost]] is not optional, as an empty mana cost is still a mana cost.
 * 
 * @author Alec Roelke
 */
trait OptionalAttribute {
  /** True if the attribute is defined on a card, and false otherwise. */
  def exists: Boolean
}