package editor.database

import javax.xml.crypto.Data

/**
 * Deck building constraints for a particular format.
 * 
 * @constructor create a new set of format constraints
 * @param deckSize minimum number of cards in a deck
 * @param isExact whether or not [[deckSize]] also represents a maximum size (i.e. is an exact count)
 * @param maxCopies maximum number of copies of cards that aren't basic lands allowed in a deck
 * @param sideboardSize maximum number of cards allowed in a deck's sideboard
 * @param hasCommander whether or not the deck has a commander
 * 
 * @author Alec Roelke
 */
case class FormatConstraints(deckSize: Int = 60, isExact: Boolean = false, maxCopies: Int = 4, sideboardSize: Int = 15, hasCommander: Boolean = false) {
  def this() = this(60, false, 4, 15, false)

  /**
   * @param name name of the format
   * @return An array containig the elements of this set of deckbuilding constraints
   * with the name of the format prepended, in the order specified by {@link #DATA_NAMES}.
   */
  def toArray(name: String): Array[Any] = Array(name, deckSize, isExact, maxCopies, sideboardSize, hasCommander)
}

object FormatConstraints {
  /** Mapping of format names onto their deckbulding constraints. */
  val Constraints = Map(
    "brawl" -> FormatConstraints(60, true, 1, 0, true),
    "commander" -> FormatConstraints(100, true, 1, 0, true),
    "duel" -> FormatConstraints(100, true, 1, 0, true),
    "future" -> FormatConstraints(),
    "historic" -> FormatConstraints(),
    "legacy" -> FormatConstraints(),
    "modern" -> FormatConstraints(),
    "oldschool" -> FormatConstraints(),
    "pauper" -> FormatConstraints(),
    "penny" -> FormatConstraints(),
    "pioneer" -> FormatConstraints(),
    "standard" -> FormatConstraints(),
    "vintage" -> FormatConstraints()
  )
  @deprecated def CONSTRAINTS() = Constraints

  /** List of supported format names, in alphabetical order. */
  val FormatNames = Constraints.map{ case (name, _) => name }.toSeq.sorted
  @deprecated def FORMAT_NAMES() = FormatNames

  /** List of types of each of the deckbuilding constraints. */
  val Classes = Seq(classOf[String], classOf[Integer], classOf[java.lang.Boolean], classOf[Integer], classOf[Integer], classOf[java.lang.Boolean])
  @deprecated def CLASSES() = Classes

  /** The name of each type of deckbuilding constraint. */
  val DataNames = Seq("Name", "Deck Size", "Exact?", "Max Card Count", "Sideboard size", "Has Commander?")
  @deprecated def DATA_NAMES() = DataNames
}