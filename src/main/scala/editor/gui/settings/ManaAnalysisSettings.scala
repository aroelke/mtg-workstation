package editor.gui.settings

import java.awt.Color

/**
 * Settings structure containing mana analysis settings.
 * 
 * @constructor create a new mana analysis structure
 * @param none color of uncategorized histogram bars
 * @param colorless color of histogram bar sections for colorless cards
 * @param white color of histogram bar sections for white cards
 * @param blue color of histogram bar sections for blue cards
 * @param black color of histogram bar sections for black cards
 * @param red color of histogram bar sections for red cards
 * @param green color of histogram bar sections for green cards
 * @param multi color of histogram bar sections for multicolored cards
 * @param creature color of histogram bar sections for creature cards
 * @param artifact color of histogram bar sections for artifact cards
 * @param enchantment color of histogram bar sections for enchantment cards
 * @param planeswalker color of histogram bar sections for planeswalker cards
 * @param instant color of histogram bar sections for instant cards
 * @param sorcery color of histogram bar sections for sorcery cards
 * 
 * @author Alec Roelke
 */
case class ManaAnalysisSettings(
  none: Color = Color(128, 128, 255),
  
  colorless: Color = Color(203, 198, 193),
  white: Color = Color(248, 246, 216),
  blue: Color = Color(193, 215, 233),
  black: Color = Color(186, 177, 171),
  red: Color = Color(228, 153, 119),
  green: Color = Color(163, 192, 149),
  multi: Color = Color(204, 166, 82),

  creature: Color = Color(163, 192, 149),
  artifact: Color = Color(203, 198, 193),
  enchantment: Color = Color(248, 246, 216),
  planeswalker: Color = Color(215, 181, 215),
  instant: Color = Color(193, 215, 233),
  sorcery: Color = Color(228, 153, 119),

  line: Color = Color.BLACK
) {
  def this() = this(
    Color(128, 128, 255),
    Color(203, 198, 193), Color(248, 246, 216), Color(193, 215, 233), Color(186, 177, 171), Color(228, 153, 119), Color(163, 192, 149), Color(204, 166, 82),
    Color(163, 192, 149), Color(203, 198, 193), Color(248, 246, 216), Color(215, 181, 215), Color(193, 215, 233), Color(228, 153, 119),
    Color.BLACK
  )

  /**
   * Get the color associated with a histogram bar section by name.
   * 
   * @param key name of the section
   * @return the color of the section
   */
  def apply(key: String) = key.toLowerCase match {
    case "none" | "nothing" => none

    case "colorless" | "c" => colorless
    case "white"     | "w" => white
    case "blue"      | "u" => blue
    case "black"     | "b" => black
    case "red"       | "r" => red
    case "green"     | "g" => green
    case "multicolored" | "multi" | "m" => multi

    case "creature"     => creature
    case "artifact"     => artifact
    case "enchantment"  => enchantment
    case "planeswalker" => planeswalker
    case "instant"      => instant
    case "sorcery"      => sorcery
  }

  /** @return the list of bar colors when dividing by color */
  def colorColors = Seq(colorless, white, blue, black, red, green, multi)
  
  /** @return the list of bar colors when dividing by card type */
  def typeColors = Seq(creature, artifact, enchantment, planeswalker, instant, sorcery)
}