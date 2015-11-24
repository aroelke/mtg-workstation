package editor.gui.filter.editor.text;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import editor.database.Card;
import editor.gui.filter.ComboBoxPanel;
import editor.gui.filter.FilterType;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.util.Containment;

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
	private Function<Card, List<String>> text;
	/**
	 * Combo box showing the containment options available, if regex matching is disabled.
	 */
	private ComboBoxPanel<Containment> contain;
	/**
	 * Text value to search for in the Card characteristic.
	 */
	private JTextField filterValue;
	/**
	 * Check box enabling regex matching.
	 */
	private JCheckBox regex;
	
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
	
	/**
	 * Create a new TextFilterPanel.
	 * 
	 * @param type Type of filter this TextFilterPanel edits
	 * @param t Function representing the characteristic to search through
	 * @param c This filter's code.
	 */
	public TextFilterPanel(FilterType type, Function<Card, List<String>> t, String c)
	{
		super(type);
		text = t;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		// Set containment combo box
		add(contain = new ComboBoxPanel<Containment>(Containment.values()));
		
		// Text box for entering the filter string
		add(filterValue = new JTextField());
		
		// Check box for matching via regular expression rather than search-engine-like
		// functionality
		add(regex = new JCheckBox("regex"));
		regex.addActionListener((e) -> contain.setVisible(!regex.isSelected()));
	}
	
	/**
	 * TODO: Comment this
	 * @param c
	 */
	public void setContainment(Containment c)
	{
		contain.setSelectedItem(c);
	}
	
	/**
	 * TODO: Comment this
	 * @param s
	 */
	public void setText(String s)
	{
		filterValue.setText(s);
	}
	
	/**
	 * TODO: Comment this
	 * @param r
	 */
	public void setRegex(boolean r)
	{
		regex.setSelected(r);
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
			Pattern p = Pattern.compile(filterText, Pattern.DOTALL);
			return (c) -> text.apply(c).stream().anyMatch((s) -> p.matcher(s.toLowerCase()).find());
		}
		else
		{
			// If the filter is a "simple" string, then the characteristic matches if it matches the
			// filter text in any order with the specified set containment
			switch (contain.getSelectedItem())
			{
			case CONTAINS_ALL_OF:
				return (c) -> text.apply(c).stream().map(String::toLowerCase).anyMatch(createSimpleMatcher(filterText));
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
				if (contain.getSelectedItem().equals(Containment.CONTAINS_NONE_OF))
					return (c) -> text.apply(c).stream().anyMatch((s) -> !p.matcher(s.toLowerCase()).find());
				else
					return (c) -> text.apply(c).stream().anyMatch((s) -> p.matcher(s.toLowerCase()).find());
			case CONTAINS_NOT_ALL_OF:
				return (c) -> text.apply(c).stream().map(String::toLowerCase).anyMatch(createSimpleMatcher(filterText).negate());
			case CONTAINS_NOT_EXACTLY:
				return (c) -> text.apply(c).stream().anyMatch((s) -> !s.equalsIgnoreCase(filterText));
			case CONTAINS_EXACTLY:
				return (c) -> text.apply(c).stream().anyMatch((s) -> s.equalsIgnoreCase(filterText));
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
	 * @return A String representation of the contents of this TextFilterPanel, which
	 * is either the containment type followed by the text box contents in quotes (if
	 * the regex box is not checked) or the text box contents contained in // (if it
	 * is).
	 */
	@Override
	protected String repr()
	{
		boolean r = regex != null && regex.isSelected();
		return contain.getSelectedItem().toString() + (r ? "/" : "\"") + filterValue.getText() + (r ? "/" : "\"");
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
