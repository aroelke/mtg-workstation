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
     *
     * @see SingleCard
     */
    NORMAL("Normal"),
    /**
     * Card with multiple "mini-cards" present on the front face.  Usually
     * there's only two.
     *
     * @see SplitCard
     */
    SPLIT("Split", true),
    /**
     * Special split card where one of the "mini-cards" can only be cast from
     * the graveyard.
     * 
     * @see SplitCard
     */
    AFTERMATH("Aftermath", true),
    /**
     * Card with one face on the top and another on the bottom that is
     * accessible by rotating it 180 degrees.
     *
     * @see FlipCard
     */
    FLIP("Flip", true),
    /**
     * Card with one face on the front and another on the back.
     *
     * @see DoubleFacedCard
     */
    TRANSFORM("Transform", true),
    /**
     * Card with one face on the front and half of another on the back.
     * Another meld card will have the other half of the back.
     *
     * @see MeldCard
     */
    MELD("Meld", true),
    /**
     * Single-faced card with a special frame that has three sets of abilities.
     *
     * @see SingleCard
     */
    LEVELER("Leveler"),
    /**
     * Single-faced card with a special frame that indicates several "chapters"
     * of a story.
     * 
     * @see SingleCard
     */
    SAGA("Saga"),
    /**
     * Single-faced card witha special frame that indicates several "levels"
     * of cumulative abilities.
     * 
     * @see SingleCard
     */
    CLASS("Class"),
    /**
     * Special split card with a "main" creature face and a special instant or
     * sorcery "Adventure" face.
     * 
     * @see SplitCard
     */
    ADVENTURE("Adventure", true),
    /**
     * Special double-faced card where either face can be played.
     */
    MODAL_DFC("Modal DFC", true),
    /**
     * Special double-faced card where both sides are the same, but one is full-art. Only
     * the full-art side is shown here, so these aren't listed as double-faced.
     */
    REVERSIBLE_CARD("Reversible"),
    /**
     * An extra-large card for use in the Planechase format.
     */
    PLANAR("Planar"),
    /**
     * An extra-large scheme card for use in the Archenemy format.
     */
    SCHEME("Scheme"),
    /**
     * An extra-large card for use in the Vanguard format.
     */
    VANGUARD("Vanguard"),
    /**
     * A single-faced card intended to host an #{@link #AUGMENT}.
     */
    HOST("Host"),
    /**
     * A single-faced card intended to attach to a {@link #HOST} and improve
     * it.
     */
    AUGMENT("Augment");

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
    CardLayout(String l)
    {
        this(l, false);
    }

    /**
     * Create a new CardLayout.
     *
     * @param l String representation of the layout.
     * @param m whether or not the layout is multi-faced.
     */
    CardLayout(String l, boolean m)
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
