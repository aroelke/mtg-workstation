package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.util.Containment
import editor.util.Containment._

import scala.util.matching._

/**
 * Object containing global data and convenience functions for filtering text attributes.
 * @author Alec Roelke
 */
object TextFilter {
  /**
   * Generic tokens that can be used to represent a list of strings in a pattern.
   * 
   * @constructor create a new token
   * @param tokens strings that can be matched to multiple values
   * @param replacements list of values that can match the token
   * 
   * @author Alec Roelke
   */
  case class Token(tokens: Seq[String], replacements: () => Seq[String])

  /** Regex pattern matching a word. */
  val WordPattern = """"([^"]*)"|'([^']*)'|[^\s]+""".r

  /**
   * List of tokens that can be used to generically match card attributes in text. Currently supports supertypes ("\supertype"), card types ("\cardtype"),
   * and subtypes ("\subtype"). Be careful using these, as they can cause long delays in filtering lists of cards.
   */
  val Tokens = Seq(
    Token(Seq(raw"\supertype"), () => CardAttribute.Supertype.options),
    Token(Seq(raw"\cardtype"), () => CardAttribute.CardType.options),
    Token(Seq(raw"\subtype"), () => CardAttribute.Subtype.options)
  )

  /**
   * Convenience method for creating a filter that searches the attribute for the given string.
   * 
   * @param attribute attribute to filter by
   * @param s string to search for
   * @return a filter that filters by the attribute using the given string
   */
  def apply(attribute: CardAttribute[?, TextFilter], s: String) = attribute.filter.copy(text = Regex.quote(s), regex = true)

  /**
   * Transform a string into a regular expression that maches any instances of any [[Token]]s with the
   * replacement values of those tokens.
   * 
   * @param text string to replace
   * @param prefix prefix string to add before each list of matching values
   * @param suffix suffix string to add after each list of matching values
   * @return a string containing a regex with the replaced matching values
   */
  def replaceTokens(text: String, prefix: String = "", suffix: String = "") = if (!text.contains("\\")) text else {
    Tokens.foldLeft(text)((t, token) => token.tokens.foldLeft(t)(_.replace(_, token.replacements().mkString(s"$prefix(?:", "|", s")$suffix"))))
  }

  /**
   * Create a regex matcher that matches words and quote-enclosed strings separated by white space in a pattern.
   * 
   * @param pattern pattern containing words and phrases to match
   * @return a string function that returns true if the input string contains all of the words and phrases in the pattern, in
   * any order, and returns false otherwise
   */
  def createSimpleMatcher(pattern: String) = {
    val r = WordPattern.findAllMatchIn(pattern).map((m) => {
      replaceTokens(Option(m.group(1)).orElse(Option(m.group(2))).getOrElse(m.matched).replace("*", "\\E\\w*\\Q"), "\\E", "\\Q")
    }).mkString("(?mi)^(?=.*(?:^|$|\\W)\\Q", "\\E(?:^|$|\\W))(?=.*(?:^|$|\\W)\\Q", "\\E(?:^|$|\\W)).*$").r
    (s: String) => r.findFirstIn(s).isDefined
  }

  private case class Data(contain: Containment, regex: Boolean, text: String)
}

/**
 * Filter that groups cards based on matching patterns in a text-based attribute.
 * 
 * @constructor create a new text filter for an attribute
 * @param value function to use to get the value of the text attribute from a card
 * @param contain function to use to compare with the value from a card
 * @param regex whether or not the text string is a regular expression
 * @param text text to compare with the value from a card
 * 
 * @author Alec Roelke
 */
final case class TextFilter(attribute: CardAttribute[Seq[String], TextFilter], value: (Card) => Seq[String], faces: FaceSearchOptions = FaceSearchOptions.ANY, contain: Containment = Containment.AnyOf, regex: Boolean = false, text: String = "") extends FilterLeaf {
  import TextFilter._

  private lazy val matches: (String) => Boolean = if (regex) s"(?si)${replaceTokens(text)}".r.findFirstIn(_).isDefined else contain match {
    case AllOf => createSimpleMatcher(text)
    case AnyOf | NoneOf =>
      val r = WordPattern.findAllMatchIn(text).map((m) => {
        replaceTokens(Option(m.group(1)).orElse(Option(m.group(2))).getOrElse(m.matched).replace("*", "\\E\\w*\\Q"), "\\E", "\\Q")
      }).mkString("(?mi)((?:^|$|\\W)\\Q", "\\E(?:^|$|\\W))|((?:^|$|\\W)\\Q", "\\E(?:^|$|\\W))").replace("\\Q\\E", "").r
      if (contain == AnyOf)
        r.findFirstIn(_).isDefined
      else
        !r.findFirstIn(_).isDefined
    case SomeOf => !createSimpleMatcher(text)(_)
    case Exactly | NotExactly =>
      val r = s"(?i)${replaceTokens(text)}".r
      if (contain == Exactly)
        r.findFirstIn(_).isDefined
      else
        !r.findFirstIn(_).isDefined
  }

  override val unified = false
  override protected def testFace(c: Card) = value(c).exists(matches)
}