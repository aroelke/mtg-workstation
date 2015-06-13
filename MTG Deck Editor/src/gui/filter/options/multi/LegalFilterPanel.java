package gui.filter.options.multi;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import javax.swing.JCheckBox;

import gui.filter.FilterType;
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
		super(Card.formatList, Card::legalIn, FilterType.FORMAT_LEGALITY.code);
		
		restrictedBox = new JCheckBox("Restricted");
		GridBagConstraints restrictedConstraints = new GridBagConstraints();
		restrictedConstraints.gridx = 2;
		restrictedConstraints.gridy = 2;
		restrictedConstraints.fill = GridBagConstraints.BOTH;
		add(restrictedBox, restrictedConstraints);
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
						if (c.legality.get(format) != Legality.RESTRICTED)
							return false;
					return true;
				}
			});
		else
			return super.getFilter();
	}
}
