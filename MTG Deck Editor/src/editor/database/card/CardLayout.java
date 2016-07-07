package editor.database.card;

/**
 * This enum enumerates all of the different types of possible card layouts.
 * Some of them indicate that a card has multiple faces.
 * 
 * @author Alec Roelke
 */
public enum CardLayout
{
	NORMAL("Normal"),
	SPLIT("Split", true),
	FLIP("Flip", true),
	DOUBLE_FACED("Double-faced", true),
	TOKEN("Token"),
	PLANE("Plane"),
	SCHEME("Scheme"),
	PHENOMENON("Phenomenon"),
	LEVELER("Leveler"),
	VANGUARD("Vanguard");
	
	/**
	 * String representation of the card's layout.
	 */
	private final String layout;
	/**
	 * Whether or not the card is multi-faced.
	 */
	public final boolean isMultiFaced;
	
	/**
	 * Create a new CardLayout.
	 * 
	 * @param l String representation of the layout.
	 * @param m Whether or not the layout is multi-faced.
	 */
	private CardLayout(String l, boolean m)
	{
		layout = l;
		isMultiFaced = m;
	}
	
	/**
	 * Create a new CardLayout that is not multi-faced.
	 * 
	 * @param l String representation of the layout.
	 */
	private CardLayout(String l)
	{
		this(l, false);
	}
	
	/**
	 * @return The String representation of this CardLayout.
	 */
	@Override
	public String toString()
	{
		return layout;
	}
}
