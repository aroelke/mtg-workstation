package editor.database.version

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.regex.Pattern

import UpdateFrequency._

/**
 * Version structure.  Versions are expected to conform to major.minor.rev-YYYYMMDD, where the date represents daily minor updates (i.e. prices)
 * and is optional.
 * 
 * @constructor create a new version structure
 * @param major major version number
 * @param minor minor version number
 * @param revision revision number
 * @param date optional latest update date
 * 
 * @author Alec Roelke
 */
case class DatabaseVersion(major: Int = 0, minor: Int = 0, revision: Int = 0, date: Option[Date] = None) extends Ordered[DatabaseVersion] {
  def this(major: Int, minor: Int, revision: Int, date: Date) = this(major, minor, revision, Some(date))
  def this(major: Int, minor: Int, revision: Int) = this(major, minor, revision, None)
  def this() = this(0, 0, 0, None)

  /**
   * Check if another version needs an update compared to this one based on the desired update frequency.
   * 
   * @param other version to check
   * @param freq frequency of desired update
   * @return true if an update is needed, and false otherwise.
   */
  def needsUpdate(other: DatabaseVersion, freq: UpdateFrequency) = freq match {
    case Never    => false
    case Daily    => this != other
    case Revision => major != other.major || minor != other.minor || revision != other.revision
    case Minor    => major != other.major || minor != other.minor
    case Major    => major != other.major
  }

  override def compare(that: DatabaseVersion) = {
    if (major != that.major)
      major - that.major;
    else if (minor != that.minor)
      minor - that.minor;
    else if (revision != that.revision)
      revision - that.revision;
    else (date, that.date) match {
      case (None, None) => 0
      case (Some(_), None) => 1
      case (None, Some(_)) => -1
      case (Some(d1), Some(d2)) => d1.compareTo(d2)
    }
  }

  override def toString = Seq(major, minor, revision).mkString(".") + date.map(d => s"-${DatabaseVersion.VersionDate.format(d)}").getOrElse("")
}

object DatabaseVersion {
  /** Regular expression pattern used to match version info. */
  lazy val VersionPattern = Pattern.compile("""^(\d+)\.(\d+)\.(\d+)(?:(?:\+|-)(\d{4}\d{2}\d{2}))?$""")

  /** Date formatter for parsing and formatting dates to strings. */
  lazy val VersionDate = new SimpleDateFormat("yyyyMMdd")

  /** @return a [[DatabaseVersion]] with the given version info and date. */
  def apply(major: Int, minor: Int, revision: Int, date: Date) = new DatabaseVersion(major, minor, revision, Some(date))

  /**
    * Create a new database version from the string of the form major.minor.rev-YYYYMMDD, with the date being optional.
    * Anything else throws an exception.
    * 
    * @param s string to parse
    * @return a [[DatabaseVersion]] parsed from the string
    */
  @throws[ParseException]("if the version isn't major.minor.rev[-YYYYMMDD]")
  def parseVersion(s: String) = {
    val m = VersionPattern.matcher(s)
    if (m.matches)
      DatabaseVersion(m.group(1).toInt, m.group(2).toInt, m.group(3).toInt, Option.unless(m.group(4) == null)(VersionDate.parse(m.group(4))))
    else
      throw new ParseException(s, 0)
  }
}