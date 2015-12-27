package editor.filter.leaf;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import editor.database.Card;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class TextFilter extends FilterLeaf<Collection<String>>
{
	/**
	 * Regex pattern for extracting words or phrases between quotes from a String.
	 */
	public static final Pattern WORD_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|[^\\s]+");
	
	/**
	 * Create a regex pattern matcher that searches a string for a set of words and quote-enclosed phrases
	 * separated by spaces, where * is a wild card.
	 * 
	 * @param pattern String pattern to create a regex matcher out of
	 * @return A Matcher that searches a String for the words and phrases in the given String.
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
		Pattern p = Pattern.compile(str.toString(), Pattern.MULTILINE);
		return (s) -> p.matcher(s).find();
	}
	
	public Containment contain;
	public String text;
	public boolean regex;
	
	public TextFilter(FilterType t, Function<Card, Collection<String>> f)
	{
		super(t, f);
		contain = Containment.CONTAINS_ANY_OF;
		text = "";
		regex = false;
	}

	@Override
	public boolean test(Card c)
	{
		// If the filter is a regex, then just match it
		if (regex)
		{
			Pattern p = Pattern.compile(text, Pattern.DOTALL);
			return function.apply(c).stream().anyMatch((s) -> p.matcher(s.toLowerCase()).find());
		}
		else
		{
			// If the filter is a "simple" string, then the characteristic matches if it matches the
			// filter text in any order with the specified set containment
			switch (contain)
			{
			case CONTAINS_ALL_OF:
				return function.apply(c).stream().map(String::toLowerCase).anyMatch(TextFilter.createSimpleMatcher(text));
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
				Pattern p = Pattern.compile(str.toString(), Pattern.MULTILINE);
				if (contain.equals(Containment.CONTAINS_NONE_OF))
					return function.apply(c).stream().anyMatch((s) -> !p.matcher(s.toLowerCase()).find());
				else
					return function.apply(c).stream().anyMatch((s) -> p.matcher(s.toLowerCase()).find());
			case CONTAINS_NOT_ALL_OF:
				return function.apply(c).stream().map(String::toLowerCase).anyMatch(TextFilter.createSimpleMatcher(text).negate());
			case CONTAINS_NOT_EXACTLY:
				return function.apply(c).stream().anyMatch((s) -> !s.equalsIgnoreCase(text));
			case CONTAINS_EXACTLY:
				return function.apply(c).stream().anyMatch((s) -> s.equalsIgnoreCase(text));
			default:
				return false;
			}
		}
	}

	@Override
	public String content()
	{
		return contain.toString() + (regex ? "/" : "\"") + text + (regex ? "/" : "\"");
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.ARTIST, FilterType.FLAVOR_TEXT, FilterType.NAME, FilterType.RULES_TEXT);
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
}