package editor.database.version

/**
 * The frequency at which the inventory should be downloaded.
 * 
 * @constructor create a new update frequency
 * @param toString string to show in the dialog when choosing the frequency
 * @param name name of the frequency to store in settings
 * 
 * @author Alec Roelke
 */
enum UpdateFrequency(override val toString: String, val name: String) {
  case Major    extends UpdateFrequency("Major version change", "major")
  case Minor    extends UpdateFrequency("Minor version change", "minor")
  case Revision extends UpdateFrequency("Revision change", "revision")
  case Daily    extends UpdateFrequency("Daily", "daily")
  case Never    extends UpdateFrequency("Never", "never")
}