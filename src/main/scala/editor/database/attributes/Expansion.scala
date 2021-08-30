package editor.database.attributes

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Structure containing information about an expansion of Magic: the Gathering.
 * 
 * @constructor create a new expansion structure
 * @param name name of the expansion
 * @param block block to which the expansion belongs (if any; use [[NO_BLOCK]] otherwise)
 * @param code the expansion's code
 * @param count number of cards in the expansion
 * @param released release date of the expansion
 * 
 * @author Alec Roelke
 */
case class Expansion(name: String, block: String, code: String, count: Int, released: LocalDate) extends Ordered[Expansion] {
  override def compare(that: Expansion) = name.compare(that.name)
  override val toString = name
}

object Expansion {
  /** Array of expansions. */
  private var _expansions = Array.empty[Expansion]
  @deprecated def expansions() = _expansions
  @deprecated def set_expansions(e: Array[Expansion]): Unit = { _expansions = e }

  /** Array containing block names. */
  private var _blocks = Array.empty[String]
  @deprecated def blocks() = _blocks
  @deprecated def set_blocks(b: Array[String]): Unit = { _blocks = b }

  /** Text to show when an expansion isn't part of a block. */
  val NoBlock = "<No Block>"
  @deprecated def NO_BLOCK() = NoBlock

  /** Date format used to decode expansion release dates. */
  val DateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  @deprecated def DATE_FORMATTER() = DateFormatter
}