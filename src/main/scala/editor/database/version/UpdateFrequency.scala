package editor.database.version

enum UpdateFrequency(override val toString: String, val name: String) {
  case Major    extends UpdateFrequency("Major version change", "major")
  case Minor    extends UpdateFrequency("Minor version change", "minor")
  case Revision extends UpdateFrequency("Revision change", "revision")
  case Daily    extends UpdateFrequency("Daily", "daily")
  case Never    extends UpdateFrequency("Never", "never")
}