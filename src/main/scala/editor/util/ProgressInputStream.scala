package editor.util

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.FilterInputStream
import java.io.InputStream

/**
 * Input stream that notifies listeners about progress reading input. Changes to the number of bytes read are reported as a "bytesRead" event.
 * 
 * @constructor create a new progress-reporting input stream
 * @param in stream to read from
 * @param listener function operating on bytes read before and after an update
 * 
 * @author Alec Roelke
 */
class ProgressInputStream(in: InputStream, listener: (Long, Long) => Unit = (_, _) => {}) extends FilterInputStream(in) {
  private val propertySupport = PropertyChangeSupport(this)
  private var totalRead = 0L

  addPropertyChangeListener((e) => {
    if (e.getPropertyName == "bytesRead") {
      (e.getOldValue, e.getNewValue) match {
        case (o: Long, n: Long) => listener(o, n)
        case _ => throw IllegalStateException("bytes read not long")
      }
    }
  })

  /**
   * Add a listener to listen for updates to the number of bytes read.
   * @param listener listener to add
   */
  def addPropertyChangeListener(listener: PropertyChangeListener) = propertySupport.addPropertyChangeListener(listener)

  /**
   * Remove a listener so it no longer receives updates.
   * @param listener listener to remove
   */
  def removePropertyChangeListener(listener: PropertyChangeListener) = propertySupport.removePropertyChangeListener(listener)

  override def read() = {
    val r = super.read()
    update(1)
    r
  }

  override def read(b: Array[Byte], off: Int, len: Int) = {
    val r = super.read(b, off, len)
    update(r)
    r
  }

  override def mark(readLimit: Int) = throw UnsupportedOperationException()
  override def markSupported = false

  private def update(read: Int) = {
    if (read > 0) {
      val old = totalRead
      totalRead += read
      propertySupport.firePropertyChange("bytesRead", old, totalRead)
    }
  }
}
