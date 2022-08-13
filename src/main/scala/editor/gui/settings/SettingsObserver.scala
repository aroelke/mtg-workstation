package editor.gui.settings

/**
 * A class that can observe changes to the global [[Settings]] structure.
 * 
 * @author Alec Roelke
 */
trait SettingsObserver {
  /** Begin observing changes to [[Settings]] */
  def startObserving(): Unit = SettingsDialog.observers += this

  /** Stop observing changes to [[Settings]] */
  def stopObserving(): Unit = SettingsDialog.observers -= this
  
  /**
   * Make any updates necessary to reflect any changes to the global [[Settings]] structure.
   * 
   * @param oldSettings settings before changes were made
   * @param newSettings settings after changes were made
   */
  def applySettings(oldSettings: Settings, newSettings: Settings): Unit

  private[settings] class SettingsApplicator(private val oldSettings: Settings, private val newSettings: Settings) {
    def apply[T](settingsMapper: (Settings) => T)(changeFunction: (T) => Unit) = {
      val oldVal = settingsMapper(oldSettings)
      val newVal = settingsMapper(newSettings)
      if (oldVal != newVal)
        changeFunction(newVal)
      this
    }
  }

  /**
   * Convenience function for applying settings.  Conditionally applies individual settings if they have changed.
   * 
   * @param oldSettings settings before changes were made
   * @param newSettings settings after changes were made
   * @return an object whose apply((Settings) => T)((T) => Unit) function can be repeatedly called to extract a value from
   * the old and new settings structures and then apply a change if the value is different between the two of them for each
   * call.
   */
  def applyChanges(oldSettings: Settings, newSettings: Settings) = new SettingsApplicator(oldSettings, newSettings)
}
