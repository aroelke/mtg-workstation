package gui.filter.editor.text;

import gui.filter.editor.FilterEditorPanel;

import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import util.Containment;
import database.Card;

/**
 * This class represents a FilterPanel that can filter a Card based on a characteristic
 * containing arbitrary text.  This can be done using a simple search like exists in a search
 * engine, where * is a wild card, or using a regular expression (in which case the containment
 * options disappear).
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class TextFilterPanel extends FilterEditorPanel
{
	/**
	 * Regex pattern for extracting words or phrases between quotes from a String.
	 */
	public static final Pattern WORD_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|[^\\s]+"); 
	
	/**
	 * Function representing the Card characteristic to filter.
	 */
	private Function<Card, String> text;
	/**
	 * Combo box showing the containment options available, if regex matching is disabled.
	 */
	private JComboBox<Containment> contain;
	/**
	 * Text value to search for in the Card characteristic.
	 */
	private JTextField filterValue;
	/**
	 * Check box enabling regex matching.
	 */
	private JCheckBox regex;
	/**
	 * Code for determining what type of filter this is from a String.
	 * @see gui.filter.editor.FilterEditorPanel#setContents(String)
	 */
	private String code;
	
	/**
	 * Create a regex pattern matcher that searches a string for a set of words and quote-enclosed phrases
	 * separated by spaces, where * is a wild card. 
	 * 
	 * @param pattern String pattern to create a regex matcher out of
	 * @param f Function returning a String whose return value will be searched by the matcher
	 * @return A Matcher that searches the given function's output for the words and phrases in the given
	 * String.
	 */
	public static <T> Predicate<T> createSimpleMatcher(String pattern, Function<T, String> f)
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
		return (a) -> p.matcher(f.apply(a)).find();
	}
	
	/**
	 * Create a new TextFilterPanel.
	 * 
	 * @param t Function representing the characteristic to search through
	 * @param c This filter's code.
	 */
	public TextFilterPanel(Function<Card, String> t, String c)
	{
		super();
		text = t;
		code = c;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		// Set containment combo box
		add(contain = new JComboBox<Containment>(Containment.values()));
		
		// Text box for entering the filter string
		add(filterValue = new JTextField());
		
		// Check box for matching via regular expression rather than search-engine-like
		// functionality
		add(regex = new JCheckBox("regex"));
		regex.addActionListener((e) -> contain.setVisible(!regex.isSelected()));
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns <code>true</code> if the Card's
	 * characteristic matches the filter expression, and <code>false</code> otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		String filterText = filterValue.getText().toLowerCase();
		// If the filter is a regex, then just match it
		if (regex.isSelected())
		{
			Pattern p = Pattern.compile(filterText);
			return (c) -> p.matcher(text.apply(c).toLowerCase()).find();
		}
		else
		{
			// If the filter is a "simple" string, then the characteristic matches if it matches the
			// filter text in any order with the specified set containment
			switch (contain.getItemAt(contain.getSelectedIndex()))
			{
			case CONTAINS_ALL_OF:
				return createSimpleMatcher(filterText, (Card c) -> text.apply(c).toLowerCase());
			case CONTAINS_ANY_OF: case CONTAINS_NONE_OF:
				Matcher m = WORD_PATTERN.matcher(filterText);
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
				Predicate<Card> filter = (c) -> p.matcher(text.apply(c)).find();
				if (contain.getItemAt(contain.getSelectedIndex()).equals(Containment.CONTAINS_NONE_OF))
					return filter.negate();
				else
					return filter;
			case CONTAINS_NOT_ALL_OF:
				return createSimpleMatcher(filterText, (Card c) -> text.apply(c).toLowerCase()).negate();
			case CONTAINS_NOT_EXACTLY:
				return (c) -> !text.apply(c).equalsIgnoreCase(filterText);
			case CONTAINS_EXACTLY:
				return (c) -> text.apply(c).equalsIgnoreCase(filterText);
			default:
				return (c) -> false;
			}
		}
	}

	/**
	 * @return <code>true</code> if the filter text box is empty and <code>false</code>
	 * otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return filterValue.getText().isEmpty();
	}
	
	/**
	 * @return A String representation of this TextFilterPanel, which is its code followed by
	 * the set containment type followed by the filter expression surrounded by quotes if it is
	 * a simple filter and // if it is a regex.
	 */
	@Override
	public String toString()
	{
		boolean r = regex != null && regex.isSelected();
		return code + ":" + contain.getSelectedItem().toString() + (r ? "/" : "\"") + filterValue.getText() + (r ? "/" : "\"");
	}

	/**
	 * Set the type of containment, the filter expression, and whether or not the filter is
	 * a regex.  The containment type should appear even if the filter is a regex even though
	 * it won't be used.
	 * 
	 * @param content String to parse for settings
	 */
	@Override
	public void setContents(String content)
	{
		Matcher m = Pattern.compile("^([^\"'\\/]+)[\"'\\/]").matcher(content);
		if (m.find())
		{
			contain.setSelectedItem(Containment.get(m.group(1)));
			filterValue.setText(content.substring(m.end(), content.length() - 1));
			regex.setSelected(content.endsWith("/"));
			contain.setVisible(!regex.isSelected());
		}
		else
			throw new IllegalArgumentException("Illegal content string: " + content);
	}
}
