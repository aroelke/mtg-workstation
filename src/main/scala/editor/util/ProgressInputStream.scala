package editor.util

import java.io.FilterInputStream
import java.io.InputStream
import java.beans.PropertyChangeSupport
import java.beans.PropertyChangeListener

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

  def addPropertyChangeListener(listener: PropertyChangeListener) = propertySupport.addPropertyChangeListener(listener)

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
