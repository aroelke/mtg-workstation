package editor.filter.leaf

import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.multi.CardTypeFilter
import editor.filter.leaf.options.multi.SubtypeFilter
import editor.filter.leaf.options.multi.SupertypeFilter
import editor.util.Containment

import java.util.Objects
import java.util.regex.Pattern

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
  val WordPattern = Pattern.compile(raw""""([^"]*)"|'([^']*)'|[^\s]+""")

  /**
   * List of tokens that can be used to generically match card attributes in text. Currently supports supertypes ("\supertype"), card types ("\cardtype"),
   * and subtypes ("\subtype"). Be careful using these, as they can cause long delays in filtering lists of cards.
   */
  val Tokens = Seq(
    Token(Seq(raw"\supertype"), () => SupertypeFilter.supertypeList),
    Token(Seq(raw"\cardtype"), () => CardTypeFilter.typeList),
    Token(Seq(raw"\subtype"), () => SubtypeFilter.subtypeList)
  )

  /**
   * Convenience method for creating a filter that searches the attribute for the given string.
   * 
   * @param t attribute to filter by
   * @param s string to search for
   * @return a filter that filters by the attribute using the given string
   */
  def createQuickFilter(t: CardAttribute, s: String) = {
    try {
      val filter = CardAttribute.createFilter(t).asInstanceOf[TextFilter]
      filter.text = Pattern.quote(s)
      filter.regex = true
      filter
    } catch case _: ClassCastException => throw IllegalArgumentException(s"illegal text filter type $t")
  }

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
    val m = WordPattern.matcher(pattern)
    val strs = collection.mutable.Buffer[String]()
    while (m.find()) {
      strs += replaceTokens(Option(m.group(1)).orElse(Option(m.group(2))).getOrElse(m.group).replace("*", "\\E\\w*\\Q"), "\\E", "\\Q")
    }
    val p = Pattern.compile(
      strs.mkString("^(?=.*(?:^|$|\\W)\\Q", "\\E(?:^|$|\\W))(?=.*(?:^|$|\\W)\\Q", "\\E(?:^|$|\\W)).*$"),
      Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
    )
    (s: String) => p.matcher(s).find()
  }

  private case class Data(contain: Containment, regex: Boolean, text: String)
}

/**
 * Filter that groups cards based on matching patterns in a text-based attribute.
 * 
 * @constructor create a new text filter for an attribute
 * @param t attribute to filter by
 * @param f function to use to get the value of the attribute from a card
 * 
 * @author Alec Roelke
 */
class TextFilter(t: CardAttribute, f: (Card) => Iterable[String]) extends FilterLeaf[Iterable[String]](t, f(_)) {
  import Containment._
  import TextFilter._

  private var current = Data(CONTAINS_ANY_OF, false, "")
  /** @return the method of containment used to determine if a non-regex pattern matches */
  def contain = current.contain
  /** @param c new method of containment to use for matching text */
  def contain_=(c: Containment) = current = current.copy(contain = c)
  /** @return true if the pattern is a regex, and false otherwise */
  def regex = current.regex
  /** @param r whether or not the pattern should be considered a regex */
  def regex_=(r: Boolean) = current = current.copy(regex = r)
  /** @return the pattern used to match the attribute */
  def text = current.text
  /** @param t new pattern to match the attribute using */
  def text_=(t: String) = current = current.copy(text = t)

  private var prev: Option[Data] = None
  private var patternCache: (String) => Boolean = null
  private def compilePattern = patternCache == null || !prev.exists(_ == current)

  override protected def testFace(c: Card) = {
    if (compilePattern) {
      prev = Some(Data(contain, regex, text))
      if (regex) {
        val p = Pattern.compile(replaceTokens(text), Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
        patternCache = p.matcher(_).find()
      } else {
        // If the filter is a "simple" string, then the characteristic matches if it matches the
        // filter text in any order with the specified set containment
        patternCache = contain match {
          case CONTAINS_ALL_OF => createSimpleMatcher(text)
          case CONTAINS_ANY_OF | CONTAINS_NONE_OF =>
            val m = WordPattern.matcher(text)
            val str = java.util.StringJoiner("\\E(?:^|$|\\W))|((?:^|$|\\W)\\Q", "((?:^|$|\\W)\\Q", "\\E(?:^|$|\\W))")
            while (m.find()) {
              val toAdd = if (m.group(1) != null) m.group(1) else if (m.group(2) != null) m.group(2) else m.group
              str.add(replaceTokens(toAdd.replace("*", "\\E\\w*\\Q"), "\\E", "\\Q"))
            }
            val p = Pattern.compile(str.toString.replace("\\Q\\E", ""), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
            if (contain == Containment.CONTAINS_NONE_OF)
              !p.matcher(_).find()
            else
              p.matcher(_).find()
          case CONTAINS_NOT_ALL_OF => !createSimpleMatcher(text)(_)
          case CONTAINS_NOT_EXACTLY =>
            val p = Pattern.compile(replaceTokens(text), Pattern.CASE_INSENSITIVE)
            !p.matcher(_).matches
          case CONTAINS_EXACTLY =>
            val p = Pattern.compile(replaceTokens(text), Pattern.CASE_INSENSITIVE)
            p.matcher(_).matches
        }
      }
    }
    function.apply(c).exists(patternCache)
  }

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(`type`).asInstanceOf[TextFilter]
    filter.current = current.copy()
    filter
  }

  override protected def serializeLeaf(fields: JsonObject) = {
    fields.addProperty("contains", contain.toString)
    fields.addProperty("regex", regex)
    fields.addProperty("pattern", text)
  }

  override protected def deserializeLeaf(fields: JsonObject) = {
    current = Data(Containment.parseContainment(fields.get("contains").getAsString), fields.get("regex").getAsBoolean, fields.get("pattern").getAsString)
  }

  override def leafEquals(other: Any) = other match {
    case o: TextFilter => o.`type` == `type` && o.contain == contain && o.regex == regex && o.text == text
    case _ => false
  }

  override def hashCode = Objects.hash(`type`, function, current)
}