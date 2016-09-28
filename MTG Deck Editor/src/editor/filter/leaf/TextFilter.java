package editor.filter.leaf;

import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

/**
 * This class represents a filter for a text characteristic of a Card.
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
	 * Create a new TextFilter that filters out Cards whose characteristic
	 * matches the given String.
	 * 
	 * @param s String to match
	 * @return The new TextFilter.
	 */
	public static TextFilter createQuickFilter(String t, String s)
	{
		try
		{
			TextFilter filter = (TextFilter)FilterFactory.createFilter(t);
			filter.text = Pattern.quote(s);
			filter.regex = true;
			return filter;
		}
		catch (ClassCastException e)
		{
			throw new IllegalArgumentException("Illegal text filter type " + t.toString());
		}
	}
	
	/**
	 * Create a regex pattern matcher that searches a string for a set of words and quote-enclosed phrases
	 * separated by spaces, where * is a wild card.
	 * 
	 * @param pattern String pattern to create a regex matcher out of
	 * @return A Predicate that searches a String for the words and phrases in the given String.
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
			else if (m.group(1) != null)
				toAdd = m.group(2);
			else
				toAdd = m.group();
			str.add(toAdd.replace("*", "\\E\\w*\\Q"));
		}
		Pattern p = Pattern.compile(str.toString(), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
		return (s) -> p.matcher(s).find();
	}
	
	/**
	 * Containment type for this TextFilter.
	 */
	public Containment contain;
	/**
	 * Text to filter.
	 */
	public String text;
	/**
	 * Whether or not the text is a regular expression.
	 */
	public boolean regex;
	
	/**
	 * Create a new TextFilter.
	 * 
	 * @param t Type of the new TextFilter
	 * @param f Function for the new TextFilter
	 */
	public TextFilter(String t, Function<Card, Collection<String>> f)
	{
		super(t, f);
		contain = Containment.CONTAINS_ANY_OF;
		text = "";
		regex = false;
	}

	/**
	 * @param c Card to test
	 * @return <code>true</code> if the Card's text characteristic matches this
	 * TextFilter's containment and text, and <code>false</code> otherwise.
	 */
	@Override
	public boolean test(Card c)
	{
		// If the filter is a regex, then just match it
		if (regex)
		{
			Pattern p = Pattern.compile(text, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			return function.apply(c).stream().anyMatch((s) -> p.matcher(s).find());
		}
		else
		{
			// If the filter is a "simple" string, then the characteristic matches if it matches the
			// filter text in any order with the specified set containment
			Predicate<String> matcher;
			switch (contain)
			{
			case CONTAINS_ALL_OF:
				matcher = createSimpleMatcher(text);
				break;
			case CONTAINS_ANY_OF: case CONTAINS_NONE_OF:
				Matcher m = TextFilter.WORD_PATTERN.matcher(text);
				StringJoiner str = new StringJoiner("\\E(?:^|$|\\W))|((?:^|$|\\W)\\Q", "((?:^|$|\\W)\\Q", "\\E(?:^|$|\\W))");
				while (m.find())
				{
					String toAdd;
					if (m.group(1) != null)
						toAdd = m.group(1);
					else if (m.group(1) != null)
						toAdd = m.group(2);
					else
						toAdd = m.group();
					str.add(toAdd.replace("*", "\\E\\w*\\Q"));
				}
				Pattern p = Pattern.compile(str.toString(), Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
				if (contain.equals(Containment.CONTAINS_NONE_OF))
					matcher = (s) -> !p.matcher(s).find();
				else
					matcher = (s) -> p.matcher(s).find();
				break;
			case CONTAINS_NOT_ALL_OF:
				matcher = createSimpleMatcher(text).negate();
				break;
			case CONTAINS_NOT_EXACTLY:
				matcher = (s) -> !s.equalsIgnoreCase(text);
				break;
			case CONTAINS_EXACTLY:
				matcher = (s) -> s.equalsIgnoreCase(text);
				break;
			default:
				matcher = (s) -> false;
				break;
			}
			return function.apply(c).stream().anyMatch(matcher);
		}
	}

	/**
	 * @return The String representation of this TextFilterPanel's content, which is
	 * the String representation of its containment followed by its text in either
	 * quotes if it's not a regex or forward slashes if it is.
	 * 
	 * @see FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		return contain.toString() + (regex ? "/" : "\"") + text + (regex ? "/" : "\"");
	}
	
	/**
	 * Parse a String to determine the containment, text, and regex status of
	 * this TextFilter.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, type);
		int delim = content.indexOf('"');
		if (delim > -1)
		{
			contain = Containment.get(content.substring(0, delim));
			text = content.substring(delim + 1, content.lastIndexOf('"'));
			regex = false;
		}
		else
		{
			delim = content.indexOf('/');
			contain = Containment.get(content.substring(0, delim));
			text = content.substring(delim + 1, content.lastIndexOf('/'));
			regex = true;
		}
	}
	
	/**
	 * @return A new TextFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		TextFilter filter = (TextFilter)FilterFactory.createFilter(type);
		filter.contain = contain;
		filter.regex = regex;
		filter.text = text;
		return filter;
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a TextFilter and its
	 * type, containment, regex flag, and text are all the same.
	 */
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
		return o.type.equals(type) && o.contain == contain && o.regex == regex && o.text.equals(text);
	}
	
	/**
	 * @return The hash code of this TextFilter, which is composed of its containment,
	 * regex flag, and text.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(type, function, contain, regex, text);
	}
}