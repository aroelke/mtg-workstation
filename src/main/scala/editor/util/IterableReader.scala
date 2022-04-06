package editor.util

import java.io.InputStream
import java.util.NoSuchElementException
import java.io.InputStreamReader

class IterableReader(is: InputStream) extends IterableOnce[String] {
  private var line: Option[collection.mutable.StringBuilder] = None
  private var c = 0

  override lazy val iterator = new collection.Iterator {
    override def hasNext = {
      if (line.isDefined)
        true
      else if (c < 0)
        false
      else {
        line = Some(collection.mutable.StringBuilder())
        while (c >= 0 && c != '\n') {
          c = is.read()
          if (c >= 0 && c != '\r' && c != '\n')
            line.foreach(_ += c.toChar)
        }
        if (c >= 0)
          c = 0
        true
      }
    }

    override def next() = {
      if (hasNext) {
        val l = line.get.toString // intentional Option.get as it will always be defined here
        line = None
        l
      } else throw NoSuchElementException()
    }
  }
}