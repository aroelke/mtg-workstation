package editor.gui.settings

/**
 * Settings structure containing information about how to determine deck legality.
 * 
 * @constructor create a new deck legality structure
 * @param searchForCommander whether or not to determine if a valid commander exists
 * @param main search in the main deck for a valid commander
 * @param all search all lists in a file for a valid commander
 * @param list search in list with a specific name (if it exists) for a valid commander
 * @param sideboard include the list with the given name as a sideboard
 * 
 * @author Alec Roelke
 */
case class LegalitySettings(searchForCommander: Boolean = true, main: Boolean = true, all: Boolean = false, list: String = "", sideboard: String = "") {
  def this() = this(true, true, false, "", "")
}