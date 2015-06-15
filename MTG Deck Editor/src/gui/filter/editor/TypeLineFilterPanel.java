package gui.filter.editor;

import gui.filter.CardFilter;
import gui.filter.FilterType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import util.Containment;
import database.Card;

/**
 * This class represents a panel that filters according to the card's type line.
 * Because the type line consists of lists of distinct values, it is different than
 * a TextFilterPanel and regex matching is not allowed.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class TypeLineFilterPanel extends FilterEditorPanel
{
	/**
	 * Text box for editing the types that are to be filtered out.
	 */
	private JTextField filter;
	/**
	 * Combo box for changing containment type.
	 */
	private JComboBox<Containment> contain;
	
	public TypeLineFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		// Containment type combo box
		add(contain = new JComboBox<Containment>(Containment.values()));
		
		// Text field for editing the filter
		add(filter = new JTextField());
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns <code>true</code> if the Card's
	 * type line matches the filter with the specified containment type, and false otherwise.
	 */
	@Override
	public CardFilter getFilter()
	{
		Predicate<Card> f;
		if (filter.getText().length() > 0)
		{
			List<String> types = Arrays.asList(filter.getText().toLowerCase().split("\\s"));
			f= (c) -> contain.getItemAt(contain.getSelectedIndex()).test(c.allTypes.stream().map(String::toLowerCase).collect(Collectors.toList()), types);
		}
		else
			f= (c) -> true;
		return new CardFilter(f, toString());
	}

	/**
	 * @return <code>true</code> if the text box is empty, and <code>false</code
	 * otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return filter.getText().isEmpty();
	}
	
	/**
	 * @return A String representation of this TypeLineFilterPanel, which is its code
	 * followed by the selected containment type and then the text box's contents inside
	 * quotes.
	 */
	@Override
	public String repr()
	{
		return FilterType.TYPE_LINE.code + ":" + contain.getSelectedItem().toString() + "\"" + filter.getText() + "\"";
	}

	/**
	 * Set the set containment type combo box, and then set the text box's contents
	 * according to the given String.
	 * 
	 * @param content String to parse for content
	 */
	@Override
	public void setContent(String content)
	{
		Matcher m = Pattern.compile("^([^\"']+)[\"']").matcher(content);
		if (m.find())
		{
			contain.setSelectedItem(Containment.get(m.group(1)));
			filter.setText(content.substring(m.end(), content.length() - 1));
		}
		else
			throw new IllegalArgumentException("Illegal content string");
	}
}
