package editor.gui.filter.editor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JTextField;

import editor.database.Card;
import editor.gui.filter.ComboBoxPanel;
import editor.gui.filter.FilterType;
import editor.util.Containment;

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
	private ComboBoxPanel<Containment> contain;
	
	/**
	 * Create a new TypeLineFilterPanel.
	 */
	public TypeLineFilterPanel()
	{
		super(FilterType.TYPE_LINE);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		// Containment type combo box
		add(contain = new ComboBoxPanel<Containment>(Containment.values()));
		
		// Text field for editing the filter
		add(filter = new JTextField());
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns <code>true</code> if the Card's
	 * type line matches the filter with the specified containment type, and false otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		if (filter.getText().length() > 0)
		{
			List<String> types = Arrays.asList(filter.getText().toLowerCase().split("\\s"));
			return (c) -> c.allTypes().stream().anyMatch((f) -> contain.getSelectedItem().test(f.stream().map(String::toLowerCase).collect(Collectors.toList()), types));
		}
		else
			return (c) -> true;
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
	 * @return A String representation of this TypeLineFilterPanel's contents, which
	 * is the containment type and the String that was entered.
	 */
	@Override
	protected String repr()
	{
		return contain.getSelectedItem().toString() + "\"" + filter.getText() + "\"";
	}

	/**
	 * Set the set containment type combo box, and then set the text box's contents
	 * according to the given String.
	 * 
	 * @param content String to parse for content
	 */
	@Override
	public void setContents(String content)
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
