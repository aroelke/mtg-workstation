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
  private var line: Option[String] = None
  private var c = 0
  private def nextLine = {
    line = line.orElse{ Option.when(c >= 0){
      val builder = collection.mutable.StringBuilder()
      while (c >= 0 && c != '\n') {
        c = is.read()
        if (c >= 0 && c != '\r' && c != '\n')
          builder += c.toChar
      }
      if (c >= 0)
        c = 0
      builder.toString
    }}
    line
  }
  private def nextLine_=(s: Option[String]) = line = s

  override lazy val iterator = new collection.Iterator {
    override def hasNext = nextLine.isDefined

    override def next() = {
      val line = nextLine.getOrElse(throw NoSuchElementException())
      nextLine = None
      line
    }
  }
}