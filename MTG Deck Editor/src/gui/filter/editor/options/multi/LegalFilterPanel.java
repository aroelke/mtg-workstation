package gui.filter.editor.options.multi;

import gui.filter.FilterType;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import javax.swing.JCheckBox;

import database.Card;
import database.characteristics.Legality;

/**
 * This class represents a FilterPanel that filters cards by the formats they are legal
 * in.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class LegalFilterPanel extends MultiOptionsFilterPanel<String>
{
	private JCheckBox restrictedBox;
	
	/**
	 * Create a new LegalFilterPanel
	 */
	public LegalFilterPanel()
	{
		super(FilterType.FORMAT_LEGALITY, Card.formatList, Card::legalIn);
		restrictedBox = new JCheckBox("Restricted");
		add(restrictedBox, BorderLayout.EAST);
	}
	
	/**
	 * @return A <code>Predicate<Card></code> that returns <code>true</code> if the Card is
	 * legal in the formats selected using the selected containment option and if the Card is
	 * restricted in those formats if the "Restricted" box is checked, and <code>false</code>
	 * otherwise.
	 */
	@Override
	public Predicate<Card> getFilter()
	{
		if (restrictedBox.isSelected())
			return super.getFilter().and(new Predicate<Card>()
			{
				@Override
				public boolean test(Card c)
				{
					Collection<String> formats = new ArrayList<String>(c.legalIn());
					formats.retainAll(optionsBox.getSelectedValuesList());
					for (String format: formats)
						if (c.legality().get(format) != Legality.RESTRICTED)
							return false;
					return true;
				}
			});
		else
			return super.getFilter();
	}
	
	/**
	 * @return A String representation of this LegalFilterPanel, which is the selected
	 * containment type followed by a list of selected formats, followed by an "r"
	 * to search for restricted cards and a "u" for any legal card.
	 */
	@Override
	public String toString()
	{
		return super.toString() + (restrictedBox.isSelected() ? "r" : "u");
	}
	
	/**
	 * Automatically select formats and select whether restricted cards should
	 * be shown or not by parsing the given String.
	 * 
	 * @param s
	 */
	@Override
	public void setContents(String s)
	{
		super.setContents(s.substring(0, s.length() - 1));
		restrictedBox.setSelected(Character.toLowerCase(s.charAt(s.length() - 1)) == 'r');
	}
}
