package editor.database.card;

/**
 * This enum enumerates all of the different types of possible card layouts.
 * Some of them indicate that a card has multiple faces.
 * 
 * @author Alec Roelke
 */
public enum CardLayout
{
	/**
	 * Normal, single-faced card.
	 * @see SingleCard
	 */
	NORMAL("Normal"),
	/**
	 * Single-faced card with a special frame that has three sets of abilities.
	 * @see SingleCard
	 */
	LEVELER("Leveler"),
	/**
	 * Card with one face on the front and another on the back.
	 * @see DoubleFacedCard
	 */
	DOUBLE_FACED("Double-faced", true),
	/**
	 * Card with one face on the top and another on the bottom that is
	 * accessible by rotating it 180 degrees.
	 * @see FlipCard
	 */
	FLIP("Flip", true),
	/**
	 * Card with multiple "mini-cards" present on the front face.  Usually
	 * there's only two.
	 * @see SplitCard
	 */
	SPLIT("Split", true),
	/**
	 * Card with one face on the front and half of another on the back.
	 * Another meld card will have the other half of the back.
	 * @see MeldCard
	 */
	MELD("Meld", true),
	/**
	 * Card with two "mini-cards" present on the front face, with one sideways.
	 */
	AFTERMATH("Aftermath", true),
	/**
	 * An extra-large phenomenon card for use in the Planechase format.
	 */
	PHENOMENON("Phenomenon"),
	/**
	 * An extra-large card for use in the Planechase format.
	 */
	PLANE("Plane"),
	/**
	 * An extra-large scheme card for use in the Archenemy format.
	 */
	SCHEME("Scheme"),
	/**
	 * A token card.  Not supported by this program.
	 */
	TOKEN("Token"),
	/**
	 * An extra-large card for use in the Vanguard format.
	 */
	VANGUARD("Vanguard");
	
	/**
	 * Whether or not the card is multi-faced.
	 */
	public final boolean isMultiFaced;
	/**
	 * String representation of the card's layout.
	 */
	private final String layout;
	
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
	 * Create a new CardLayout.
	 * 
	 * @param l String representation of the layout.
	 * @param m whether or not the layout is multi-faced.
	 */
	private CardLayout(String l, boolean m)
	{
		layout = l;
		isMultiFaced = m;
	}

	@Override
	public String toString()
	{
		return layout;
	}
}
