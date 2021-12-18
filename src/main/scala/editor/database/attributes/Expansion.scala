package editor.database.attributes

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Structure containing information about an expansion of Magic: the Gathering.
 * 
 * @constructor create a new expansion structure
 * @param name name of the expansion
 * @param block block to which the expansion belongs (if any; use [[NoBlock]] otherwise)
 * @param code the expansion's code
 * @param count number of cards in the expansion
 * @param released release date of the expansion
 * 
 * @author Alec Roelke
 */
case class Expansion(name: String, block: String, code: String, count: Int, released: LocalDate) extends Ordered[Expansion] {
  override def compare(that: Expansion) = name.compare(that.name)
  override def toString = name
}

object Expansion {
  var _expansions = Seq.empty[Expansion]
  var _blocks = Seq.empty[String]

  /** @return the list of expansions */
  def expansions = _expansions.toArray

  /**
   * Update the lists of expansions and the blocks they belong to.
   * @param e new list of expansions
   */
  def expansions_=(e: Seq[Expansion]) = {
    _expansions = e
    _blocks = e.map(_.block).distinct
  }

  /** @return the list of blocks */
  def blocks = _blocks.toArray

  /** Text to show when an expansion isn't part of a block. */
  val NoBlock = "<No Block>"

  /** Date format used to decode expansion release dates. */
  val DateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  @deprecated def DATE_FORMATTER() = DateFormatter
}