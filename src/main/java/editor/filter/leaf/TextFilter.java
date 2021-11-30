package editor.filter.leaf;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
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
     * List of tokens that can be used to generically refer to different card attributes, e.g.
     * "\cardtype" can be used to refer to any card type. This should be used with care, though,
     * as it slows down filtering.
     */
    public static final List<Token> TOKENS = List.of(
        new Token(List.of("\\supertype"), () -> Arrays.asList(SupertypeFilter.supertypeList)),
        new Token(List.of("\\cardtype"), () -> Arrays.asList(CardTypeFilter.typeList)),
        new Token(List.of("\\subtype"), () -> Arrays.asList(SubtypeFilter.subtypeList))
    );

    /**
     * A token is a string that can match a list of options, like card types or subtypes.
     * 
     * @param tokens list of strings that can be replaced
     * @param replacements generator of a list of strings to replace the token with
     * @author Alec Roelke
     * 
     * ~~RECORD~~
     */
    private static class Token/*(List<String> tokens, Supplier<List<String>> replacements)*/
    {
        private final List<String> tokens;
        private final Supplier<List<String>> replacements;

        public Token(List<String> t, Supplier<List<String>> r)
        {
            tokens = t;
            replacements = r;
        }
    }

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
     * @param prefix prefix placed just before the regular expression
     * @param suffix suffix placed just after the regular expression
     * @return A string computed by replacing all tokens with their lists of words
     * @see #TOKENS
     */
    public static String replaceTokens(String text, String prefix, String suffix)
    {
        if (!text.contains("\\"))
            return text;
        else
            return TOKENS.stream().reduce(
                text,
                (t, token) -> token.tokens.stream().reduce(
                    t,
                    (s, element) -> s.replace(element, token.replacements.get().stream().collect(Collectors.joining("|", prefix + "(?:", ")" + suffix)))
                ),
                String::concat
        );
    }

    /**
     * Replace all tokens in a string with regular expressions that look for the words they
     * can match, with no prefix or suffix.
     * 
     * @param text text to replace
     * @return A string computed by replacing all tokens with their lists of words
     * @see #TOKENS
     */
    public static String replaceTokens(String text)
    {
        return replaceTokens(text, "", "");
    }

    /**
     * Create a regex pattern matcher that searches a string for a set of words and quote-enclosed phrases
     * separated by spaces, where * is a wild card.
     *
     * @param pattern string pattern to create a regex matcher out of
     * @return a predicate that searches a string for the words and phrases in the given string.
     */
    public static Predicate<String> createSimpleMatcher(String pattern)
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
            str.add(replaceTokens(toAdd.replace("*", "\\E\\w*\\Q"), "\\E", "\\Q"));
        }
        Pattern p = Pattern.compile(str.toString().replace("\\Q\\E", ""), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        return (s) -> p.matcher(s).find();
    }

    /** Containment type for this TextFilter. */
    public Containment contain;
    /** Whether or not the text is a regular expression. */
    public boolean regex;
    /** Text to filter. */
    public String text;
    private Containment prevContain;
    private Boolean prevRegex;
    private String prevText;
    private Predicate<String> patternCache;

    /**
     * Create a new TextFilter without a type or function.  Should only be used for
     * deserialization.
     */
    public TextFilter()
    {
        this(null, null);
        prevContain = null;
        prevRegex = null;
        prevText = null;
        patternCache = null;
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
    public boolean leafEquals(Object other)
    {
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

    private boolean compilePattern()
    {
        return (patternCache == null) || (prevContain != contain || !prevRegex.equals(regex) || !prevText.equals(text));
    }

    /**
     * {@inheritDoc}
     * Cards are filtered by a text attribute that matches this TextFilter's text.
     */
    @Override
    protected boolean testFace(Card c)
    {
        // If the filter is a regex, then just match it
        if (compilePattern())
        {
            prevContain = contain;
            prevRegex = regex;
            prevText = text;
            if (regex)
            {
                Pattern p = Pattern.compile(replaceTokens(text), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                patternCache = (s) -> p.matcher(s).find();
            }
            else
            {
                // If the filter is a "simple" string, then the characteristic matches if it matches the
                // filter text in any order with the specified set containment
                patternCache = switch (contain) {
                    case CONTAINS_ALL_OF -> createSimpleMatcher(text);
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
                            str.add(replaceTokens(toAdd.replace("*", "\\E\\w*\\Q"), "\\E", "\\Q"));
                        }
                        Pattern p = Pattern.compile(str.toString().replace("\\Q\\E", ""), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                        if (contain == Containment.CONTAINS_NONE_OF)
                            yield (s) -> !p.matcher(s).find();
                        else
                            yield (s) -> p.matcher(s).find();
                    }
                    case CONTAINS_NOT_ALL_OF -> createSimpleMatcher(text).negate();
                    case CONTAINS_NOT_EXACTLY -> {
                        Pattern p = Pattern.compile(replaceTokens(text), Pattern.CASE_INSENSITIVE);
                        yield (s) -> !p.matcher(s).matches();
                    }
                    case CONTAINS_EXACTLY -> {
                        Pattern p = Pattern.compile(replaceTokens(text), Pattern.CASE_INSENSITIVE);
                        yield (s) -> p.matcher(s).matches();
                    }
                };
            }
        }
        return function().apply(c).stream().anyMatch(patternCache);
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