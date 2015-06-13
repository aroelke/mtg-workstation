package gui.filter;

import gui.filter.colors.CardColorFilterPanel;
import gui.filter.colors.ColorIdentityFilterPanel;
import gui.filter.number.CMCFilterPanel;
import gui.filter.number.CardNumberFilterPanel;
import gui.filter.number.LoyaltyFilterPanel;
import gui.filter.number.PowerFilterPanel;
import gui.filter.number.ToughnessFilterPanel;
import gui.filter.options.multi.LegalFilterPanel;
import gui.filter.options.singleton.BlockFilterPanel;
import gui.filter.options.singleton.ExpansionFilterPanel;
import gui.filter.options.singleton.RarityFilterPanel;
import gui.filter.text.ArtistFilterPanel;
import gui.filter.text.FlavorFilterPanel;
import gui.filter.text.NameFilterPanel;
import gui.filter.text.OracleTextFilterPanel;

/**
 * This enum represents a type of filter that can be used to filter Cards.
 * 
 * @author Alec Roelke
 */
public enum FilterType
{
	NAME("Name", "n", NameFilterPanel.class),
	MANA_COST("Mana Cost", "m", ManaCostFilterPanel.class),
	CMC("CMC", "cmc", CMCFilterPanel.class),
	COLOR("Color", "c", CardColorFilterPanel.class),
	COLOR_IDENTITY("Color Identity", "ci", ColorIdentityFilterPanel.class),
	TYPE_LINE("Type Line", "type", TypeLineFilterPanel.class),
	EXPANSION("Expansion", "x", ExpansionFilterPanel.class),
	BLOCK("Block", "b", BlockFilterPanel.class),
	RARITY("Rarity", "r", RarityFilterPanel.class),
	RULES_TEXT("Rules Text", "o", OracleTextFilterPanel.class),
	FLAVOR_TEXT("Flavor Text", "f", FlavorFilterPanel.class),
	POWER("Power", "p", PowerFilterPanel.class),
	TOUGHNESS("Toughness", "t", ToughnessFilterPanel.class),
	LOYALTY("Loyalty", "l", LoyaltyFilterPanel.class),
	ARTIST("Artist", "a", ArtistFilterPanel.class),
	CARD_NUMBER("Card Number", "#", CardNumberFilterPanel.class),
	FORMAT_LEGALITY("Format Legality", "legal", LegalFilterPanel.class);
	
	/**
	 * Get a FilterType from a String.
	 * 
	 * @param c String to parse
	 * @return The FilterType corresponding to the String.
	 */
	public static FilterType fromCode(String c)
	{
		for (FilterType filterType: FilterType.values())
			if (c.equalsIgnoreCase(filterType.code))
				return filterType;
		throw new IllegalArgumentException("Illegal filter type string");
	}
	
	/**
	 * Name of this FilterType.
	 */
	private final String name;
	/**
	 * Panel to create for this FilterType.
	 */
	private final Class<? extends FilterPanel> panel;
	/**
	 * Code for this FilterType to figure out which panel to set content for.
	 */
	public final String code;
	
	/**
	 * Create a new FilterType.
	 * 
	 * @param n Name of the new FilterType.
	 * @param c Code of the new FilterType.
	 * @param f Filter panel class to create.
	 */
	private FilterType(String n, String c, Class<? extends FilterPanel> f)
	{
		name = n;
		code = c;
		panel = f;
	}
	
	/**
	 * @return A new instance of this FilterType's panel.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public FilterPanel newInstance() throws InstantiationException, IllegalAccessException
	{
		return panel.newInstance();
	}
	
	/**
	 * @return A String representation of this FilterType, which is it's name.
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
