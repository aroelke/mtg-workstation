package editor.database.symbol

import java.awt.Image
import java.io.IOException
import java.util.regex.Pattern
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import scala.jdk.OptionConverters._

trait SymbolParser[S <: Symbol] {
  def parse(s: String): Option[S]
}

trait HasDiscreteValues[K, S <: Symbol] {
  def values: Map[K, S]
  def apply(key: K) = values(key)
}

/**
 * Companion to [[Symbol]] containing global data and methods for parsing from strings.
 * @author Alec Roelke
 */
object Symbol extends SymbolParser[Symbol] {
  /** Regular expression for retrieving a [[Symbol]]'s text from its string representation. */
  val Regex = Pattern.compile(raw"\{([^}]+)\}")

  /** Icon name to use for [[Symbol]]s whose icons are unknown. */
  val Unknown = "unknown.png"

  /**
   * Parse a [[Symbol]] from a string. The string should be its text representation without braces.
   * 
   * @param s string to parse
   * @return the [[Symbol]] represented by the string, or None if there isn't one
   */
  override def parse(s: String) = ManaSymbol.parse(s) orElse FunctionalSymbol.parse(s)

  @deprecated val SYMBOL_PATTERN = Regex
  @deprecated val UNKNOWN = Unknown
  @deprecated def tryParseSymbol(s: String) = parse(s).toJava
  @deprecated def parseSymbol(s: String) = parse(s).getOrElse(throw IllegalArgumentException(s"$s is not a symbol"))
}

/**
 * A symbol that can appear on a card. Since there is a finite set of symbols, individual ones cannot be instantiated;
 * rather, they are accessed via static data structure or by parsing from string.
 * 
 * @constructor create a new symbol
 * @param name file name containing the icon to show for the symbol
 * @param text text representation of the symbol; when printed out, this will be displayed between braces
 * 
 * @author Alec Roelke
 */
abstract class Symbol(val name: String, private val text: String) {
  val icon = try {
    ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/icons/$name")))
  } catch case e: IOException => { e.printStackTrace(); ImageIcon() }

  def scaled(size: Int) = ImageIcon(icon.getImage.getScaledInstance(-1, size, Image.SCALE_SMOOTH))

  override def toString = s"{$text}"
  override def hashCode = toString.hashCode
  override def equals(other: Any) = other match {
    case null => false
    case ref: AnyRef if ref eq this => true
    case _ => other.getClass == getClass && other.toString == toString
  }

  @deprecated def getIcon = icon
  @deprecated def getIcon(size: Int) = scaled(size)
  @deprecated def getName = name
}