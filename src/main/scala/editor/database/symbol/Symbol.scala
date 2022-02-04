package editor.database.symbol

import javax.swing.ImageIcon
import javax.imageio.ImageIO
import java.io.IOException
import java.awt.Image
import java.util.regex.Pattern

import scala.jdk.OptionConverters._

object Symbol {
  val Regex = Pattern.compile(raw"\{([^}]+)\}")

  val Unknown = "unknown.png"

  def parse(s: String) = {
    ManaSymbol.parse(s).orElse(
    FunctionalSymbol.parse(s)
    )
  }

  @deprecated val SYMBOL_PATTERN = Regex
  @deprecated val UNKNOWN = Unknown
  @deprecated def tryParseSymbol(s: String) = parse(s).toJava
  @deprecated def parseSymbol(s: String) = parse(s).getOrElse(throw IllegalArgumentException(s"$s is not a symbol"))
}

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