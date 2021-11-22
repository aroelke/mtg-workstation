package editor.gui.editor

import java.io.File

/**
 * An exception that might occur while loading a deck.
 * 
 * @constructor create a new deck load exception
 * @param file file that was being loaded when the exception ocurred
 * @param message additional information about the exception
 * @param cause an optional throwable cause of the exception
 * 
 * @author Alec Roelke
 */
case class DeckLoadException(file: File, message: String = "", cause: Option[Throwable] = None) extends Exception(message, cause.getOrElse(null))