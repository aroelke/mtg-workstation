package editor.collection.`export`

import editor.collection.CardList
import editor.gui.deck.DesignSerializer

import java.io.InputStream

/**
 * Generic formatter for converting a [[CardList]] into a string or parsing a file into a deck.
 * @author Alec Roelke
 */
trait CardListFormat {
  /**
   * Convert a [[CardList]] into a string. Which cards, and which attributes of and other information about the cards,
   * that are included and how that information is arranged is implementation-dependent.
   * 
   * @param list list to convert
   * @return the formatted string representing the card list
   */
  def format(list: CardList): String

  /** The header of the string version of the list, or the None string if there isn't one. */
  def header: Option[String]

  /**
   * Parse a stream into a new deck. The format of the file is implementation-dependent.
   * 
   * @param source stream to convert to a deck
   * @return a [[DeckSerializer]] containing all of the information about the deck
   */
  def parse(source: InputStream): DesignSerializer
}
