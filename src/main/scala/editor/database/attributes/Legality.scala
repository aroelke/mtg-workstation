package editor.database.attributes

object Legality {
  def parse(s: String) = values.find(_.toString.equalsIgnoreCase(s)).headOption
}

/**
 * Legality of a card in a format. A legal card can be restricted to having only one copy in a deck, which
 * is mainly used for Vintage (singleton-only formats like Commander don't count). Illegal cards are broken
 * down into two groups: cards that are illegal (i.e. the expansion or printing is not legal) and cards that are
 * banned (they would be legal except they have been declared illegal for various reasons).
 * 
 * @constructor create a new legality type
 * @param toString string representation of the legality
 * @param isLegal whether or not it represents a legal card in a format
 * 
 * @author Alec Roelke
 */
enum Legality(override val toString: String, val isLegal: Boolean) {
  /** A card that has been banned from a format. */
  case BANNED extends Legality("Banned", false)
  /** A card that is legal in a format. */
  case LEGAL extends Legality("Legal", true)
  /** A card that is legal, but restricted in a format. */
  case RESTRICTED extends Legality("Restricted", true)
  /** A card that is illegal in a format. */
  case ILLEGAL extends Legality("Illegal", false)
}
