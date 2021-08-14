package editor.filter.leaf;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.CombatStat;
import editor.database.attributes.Loyalty;
import editor.database.card.Card;
import editor.filter.leaf.options.multi.CardTypeFilter;
import editor.filter.leaf.options.multi.SubtypeFilter;
import editor.filter.leaf.options.multi.SupertypeFilter;
import editor.util.Containment;

/**
 * This class represents a filter for a text characteristic of a card.
 *
 * @author Alec Roelke
 */
public class TextFilter extends FilterLeaf<Collection<String>>
{
    /**
     * Regex pattern for extracting words or phrases between quotes from a String.
     */
    public static final Pattern WORD_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|[^\\s]+");

    /**
     * Mapping of tokens onto the lists of words they can match.  Each token is a backslash
     * followed by a word describing what it is, e.g. "\supertype" to match any supertype.
     * They should be used with care, though, because they hurt performance.
     */
    public static final Map<String, Function<Card, Set<String>>> TOKENS = Map.of(
        "\\supertype", (c) -> Set.of(SupertypeFilter.supertypeList),
        "\\cardtype", (c) -> Set.of(CardTypeFilter.typeList),
        "\\subtype", (c) -> Set.of(SubtypeFilter.subtypeList),
        "\\power", (c) -> c.power().stream().map(CombatStat::toString).collect(Collectors.toSet()),
        "\\toughness", (c) -> c.toughness().stream().map(CombatStat::toString).collect(Collectors.toSet()),
        "\\loyalty", (c) -> c.loyalty().stream().map(Loyalty::toString).collect(Collectors.toSet())
    );

    /**
     * Create a new TextFilter that filters out cards whose characteristic
     * matches the given String.
     *
     * @param s string to match
     * @return the new TextFilter
     */
    public static TextFilter createQuickFilter(CardAttribute t, String s)
    {
        try
        {
            TextFilter filter = (TextFilter)CardAttribute.createFilter(t);
            filter.text = Pattern.quote(s);
            filter.regex = true;
            return filter;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Illegal text filter type " + t);
        }
    }

    /**
     * Replace all tokens in a string with regular expressions that look for the words they
     * can match.
     * 
     * @param text text to replace
     * @param c card to use for replacement, if the token is to be replaced with a card's value
     * for an attribute
     * @param prefix prefix placed just before the regular expression
     * @param suffix suffix placed just after the regular expression
     * @return A string computed by replacing all tokens with their lists of words
     * @see #TOKENS
     */
    public static String replaceTokens(String text, Card c, String prefix, String suffix)
    {
        return TOKENS.keySet().stream().reduce(text, (t, token) -> t.replace(token, TOKENS.get(token).apply(c).stream().collect(Collectors.joining("|", prefix + "(?:", ")" + suffix))));
    }

    /**
     * Replace all tokens in a string with regular expressions that look for the words they
     * can match, with no prefix or suffix.
     * 
     * @param text text to replace
     * @param c card to use for replacement, if the token is to be replaced with a card's value
     * for a attribute
     * @return A string computed by replacing all tokens with their lists of words
     * @see #TOKENS
     */
    public static String replaceTokens(String text, Card c)
    {
        return replaceTokens(text, c, "", "");
    }

    /**
     * Create a regex pattern matcher that searches a string for a set of words and quote-enclosed phrases
     * separated by spaces, where * is a wild card.
     *
     * @param pattern string pattern to create a regex matcher out of
     * @return a predicate that searches a string for the words and phrases in the given string.
     */
    public static Predicate<String> createSimpleMatcher(String pattern, Card c)
    {
        Matcher m = WORD_PATTERN.matcher(pattern);
        StringJoiner str = new StringJoiner("\\E(?:^|$|\\W))(?=.*(?:^|$|\\W)\\Q", "^(?=.*(?:^|$|\\W)\\Q", "\\E(?:^|$|\\W)).*$");
        while (m.find())
        {
            String toAdd;
            if (m.group(1) != null)
                toAdd = m.group(1);
            else if (m.group(2) != null)
                toAdd = m.group(2);
            else
                toAdd = m.group();
            str.add(replaceTokens(toAdd.replace("*", "\\E\\w*\\Q"), c, "\\E", "\\Q"));
        }
        Pattern p = Pattern.compile(str.toString().replace("\\Q\\E", ""), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        return (s) -> p.matcher(s).find();
    }

    /**
     * Containment type for this TextFilter.
     */
    public Containment contain;
    /**
     * Whether or not the text is a regular expression.
     */
    public boolean regex;
    /**
     * Text to filter.
     */
    public String text;

    /**
     * Create a new TextFilter without a type or function.  Should only be used for
     * deserialization.
     */
    public TextFilter()
    {
        this(null, null);
    }

    /**
     * Create a new TextFilter.
     *
     * @param t Type of the new TextFilter
     * @param f Function for the new TextFilter
     */
    public TextFilter(CardAttribute t, Function<Card, Collection<String>> f)
    {
        super(t, f);
        contain = Containment.CONTAINS_ANY_OF;
        text = "";
        regex = false;
    }

    @Override
    protected FilterLeaf<Collection<String>> copyLeaf()
    {
        TextFilter filter = (TextFilter)CardAttribute.createFilter(type());
        filter.contain = contain;
        filter.regex = regex;
        filter.text = text;
        return filter;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (other.getClass() != getClass())
            return false;
        TextFilter o = (TextFilter)other;
        return o.type().equals(type()) && o.contain == contain && o.regex == regex && o.text.equals(text);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), function(), contain, regex, text);
    }

    /**
     * {@inheritDoc}
     * Cards are filtered by a text attribute that matches this TextFilter's text.
     */
    @Override
    protected boolean testFace(Card c)
    {
        // If the filter is a regex, then just match it
        if (regex)
        {
            Pattern p = Pattern.compile(
                replaceTokens(text, c),
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
            );
            return function().apply(c).stream().anyMatch((s) -> p.matcher(s).find());
        }
        else
        {
            // If the filter is a "simple" string, then the characteristic matches if it matches the
            // filter text in any order with the specified set containment
            Predicate<String> matcher = switch (contain) {
                case CONTAINS_ALL_OF -> createSimpleMatcher(text, c);
                case CONTAINS_ANY_OF, CONTAINS_NONE_OF -> {
                    Matcher m = TextFilter.WORD_PATTERN.matcher(text);
                    StringJoiner str = new StringJoiner("\\E(?:^|$|\\W))|((?:^|$|\\W)\\Q", "((?:^|$|\\W)\\Q", "\\E(?:^|$|\\W))");
                    while (m.find())
                    {
                        String toAdd;
                        if (m.group(1) != null)
                            toAdd = m.group(1);
                        else if (m.group(2) != null)
                            toAdd = m.group(2);
                        else
                            toAdd = m.group();
                        str.add(replaceTokens(toAdd.replace("*", "\\E\\w*\\Q"), c, "\\E", "\\Q"));
                    }
                    Pattern p = Pattern.compile(str.toString().replace("\\Q\\E", ""), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                    if (contain == Containment.CONTAINS_NONE_OF)
                        yield (s) -> !p.matcher(s).find();
                    else
                        yield (s) -> p.matcher(s).find();
                }
                case CONTAINS_NOT_ALL_OF -> createSimpleMatcher(text, c).negate();
                case CONTAINS_NOT_EXACTLY -> {
                    Pattern p = Pattern.compile(replaceTokens(text, c), Pattern.CASE_INSENSITIVE);
                    yield (s) -> !p.matcher(s).matches();
                }
                case CONTAINS_EXACTLY -> {
                    Pattern p = Pattern.compile(replaceTokens(text, c), Pattern.CASE_INSENSITIVE);
                    yield (s) -> p.matcher(s).matches();
                }
            };
            return function().apply(c).stream().anyMatch(matcher);
        }
    }

    @Override
    protected void serializeLeaf(JsonObject fields)
    {
        fields.addProperty("contains", contain.toString());
        fields.addProperty("regex", regex);
        fields.addProperty("pattern", text);
    }

    @Override
    protected void deserializeLeaf(JsonObject fields)
    {
        contain = Containment.parseContainment(fields.get("contains").getAsString());
        regex = fields.get("regex").getAsBoolean();
        text = fields.get("pattern").getAsString();
    }
}