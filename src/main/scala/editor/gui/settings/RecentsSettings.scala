package editor.gui.settings

/**
 * Settings structure containing information about recently-opened files.
 * 
 * @constructor create a new recent-files structure
 * @param count number of recently-opened files to track
 * @param files list of recently-opened files
 * 
 * @author Alec Roelke
 */
case class RecentsSettings(count: Int = 4, files: Seq[String] = Nil) {
  def this() = this(4, Nil)
}