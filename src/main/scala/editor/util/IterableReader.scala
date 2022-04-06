package editor.util

import java.io.InputStream
import java.io.InputStreamReader
import java.util.NoSuchElementException

/**
 * Input stream reader that presents in iterable interface to a character stream, allowing for
 * iteration over lines and guaranteeing that no bytes are consumed beyond the current line.
 * Useful for iterating over a file's lines while tracking progress through the file. Once used,
 * the reader is no longer valid and attempts to get the next line will result in a
 * [[NoSuchElementException]]. Currently assumes Unicode character encoding.
 * 
 * @constructor create a new iterable stream reader for a character stream
 * @param is input stream to read from
 * 
 * @author Alec Roelke
 */
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