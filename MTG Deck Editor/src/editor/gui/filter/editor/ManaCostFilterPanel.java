package editor.gui.filter.editor;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JTextField;

import editor.database.Card;
import editor.database.characteristics.ManaCost;
import editor.gui.filter.ComboBoxPanel;
import editor.gui.filter.FilterType;
import editor.util.Containment;

/**
 * This class represents a panel that filters Cards by mana cost.  When entering
 * a mana cost, each symbol should be the text of the symbol surrounded by {}.
 * 
 * @author Alec Roelke
 * @see editor.database.symbol.Symbol
 */
@SuppressWarnings("serial")
public class ManaCostFilterPanel extends FilterEditorPanel
{
	/**
	 * Combo box for specifying set containment type.
	 */
	private ComboBoxPanel<Containment> contain;
	/**
	 * Text field for specifying the mana cost to filter.
	 */
	private JTextField filterValue;

	/**
	 * Create a new ManaCostFilterPanel.
	 */
	public ManaCostFilterPanel()
	{
		super(FilterType.MANA_COST);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		// The panel only contains a combo box for defining set containment
		// and a text box.
		add(contain = new ComboBoxPanel<Containment>(Containment.values()));
		add(filterValue = new JTextField());
	}

	/**
	 * @return A <code>Predicate<Card></code> that returns <code>true</code> if the Card's
	 * mana cost matches the filter's mana cost with the specified set containment type.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		ManaCost cost = ManaCost.valueOf(filterValue.getText());
		switch (contain.getSelectedItem())
		{
		case CONTAINS_ANY_OF:
			return (c) -> c.mana().stream().anyMatch((m) -> Containment.CONTAINS_ANY_OF.test(m.symbols(), cost.symbols()));
		case CONTAINS_NONE_OF:
			return (c) -> c.mana().stream().anyMatch((m) -> Containment.CONTAINS_NONE_OF.test(m.symbols(), cost.symbols()));
		case CONTAINS_ALL_OF:
			return (c) -> c.mana().stream().anyMatch((m) -> m.isSuperset(cost));
		case CONTAINS_NOT_ALL_OF:
			return (c) -> c.mana().stream().anyMatch((m) -> !m.isSuperset(cost));
		case CONTAINS_EXACTLY:
			return (c) -> c.mana().stream().anyMatch((m) -> m.equals(cost));
		case CONTAINS_NOT_EXACTLY:
			return (c) -> c.mana().stream().anyMatch((m) -> !m.equals(cost));
		default:
			return (c) -> false;
		}
	}

	/**
	 * @return <code>true</code> if the text box is empty, and <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return filterValue.getText().isEmpty();
	}
	
	/**
	 * @return A String representation of this ManaCostFilterPanel's contents, which
	 * is its containment type followed by the String representation of the equivalent
	 * mana cost entered.
	 */
	@Override
	protected String repr()
	{
		return contain.getSelectedItem().toString() + "\"" + ManaCost.valueOf(filterValue.getText()).toString() + "\"";
	}

	/**
	 * Change the value of the set containment combo box and the value of the filter text box
	 * according to the specified String.
	 * 
	 * @param content String to parse for content.
	 */
	@Override
	public void setContents(String content)
	{
		Matcher m = Pattern.compile("^([^\"']+)[\"']").matcher(content);
		if (m.find())
		{
			contain.setSelectedItem(Containment.get(m.group(1)));
			ManaCost cost = ManaCost.valueOf(content.substring(m.end(), content.length() - 1));
			filterValue.setText(cost.toString());
		}
		else
			throw new IllegalArgumentException("Illegal content string: " + content);
	}
}
