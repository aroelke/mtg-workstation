package editor.database.attributes

/**
 * Functions for parsing [[Rarity]] from strings and characters.
 * @author Alec Roelke
 */
object Rarity {
  /** @return the [[Rarity]] whose shorthand matches the given character, or None if there isn't one. */
  def parse(rarity: Char) = values.find(_.shorthand == rarity.toUpper)

  /** @return the [[Rarity]] that matches the given string, or None if there isn't one. */
  def parse(rarity: String) = {
    val lower = rarity.toLowerCase
    val names = values.map((r) => r -> r.unique.toLowerCase)
    names.reverse.collect{ case (r, n) if lower == r.shorthand.toString || lower.contains(n) => r }.headOption
  }
}

/**
 * A possible rarity that a card can have, which approximately represents its frequency in appearing in booster packs.
 * 
 * @constructor create a new rarity
 * @param toString displayable string representation
 * @param unique internal representation to use for parsing
 * @param shorthand single-character representation
 * 
 * @author Alec Roelke
 */
enum Rarity(override val toString: String, val unique: String, val shorthand: Char) extends Ordered[Rarity] {
  override def compare(that: Rarity) = ordinal - that.ordinal

  /** Formerly used for basic lands, which are now [[COMMON]]. */
  @deprecated case BASIC_LAND extends Rarity("Basic Land", "basic", 'B')
  /** Common rarity. Appears 10-11 times per normal booster pack, including the basic land. */
  case COMMON extends Rarity("Common", "common", 'C')
  /** Uncommon rarity. Appears 3-4 times per normal booster pack. */
  case UNCOMMON extends Rarity("Uncommon", "uncommon", 'U')
  /** Rare rarity. Appears once per normal booster pack. */
  case RARE extends Rarity("Rare", "rare", 'R')
  /** Mythic rare rarity. Has a 1/8 chnce of replacing the rare in a normal booster pack. */
  case MYTHIC_RARE extends Rarity("Mythic Rare", "mythic", 'M')
  /** Special rarity. Some expansions have additional cards that might replace a common in a booster pack. */
  case SPECIAL extends Rarity("Special", "special", 'S')
  /** Bonus rarity. Used for extremely rare cards in certain online booster packs. */
  case BONUS extends Rarity("Bonus", "bonus", 'O')
  /** Rarity couldn't be determined. */
  case UNKNOWN extends Rarity("Unknown", "unknown", 0)
}