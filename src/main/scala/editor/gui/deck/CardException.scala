package editor.gui.deck

import editor.database.card.Card

/**
 * Exception indicating something unexpected went wrong with processing cards.
 * 
 * @constructor create a new exception
 * @param msg string indicating what went wrong
 * @param cards cards involved in the exception
 */
final case class CardException(msg: String = "", cards: Card*)
  extends RuntimeException(s"""${cards.mkString("[", ",", "]")}${if (msg.isEmpty) "" else s": $msg"}""")