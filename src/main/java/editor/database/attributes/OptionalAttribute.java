package editor.database.attributes;

/**
 * This class represents the value of a card attribute that might or might not
 * actually appear on a card, such as power, toughness, and loyalty. Mana cost
 * does not count, as an empty mana cost is still a valid mana cost.
 * 
 * @author Alec Roelke
 */
public interface OptionalAttribute
{
    /**
     * @return An unspecified attribute that is missing from a card.
     */
    public static OptionalAttribute empty()
    {
        // This isn't actually a functional interface, but this shorthand is more
        // convenient than fully defining an anonymous class
        return () -> false;
    }

    /**
     * @return <code>true</code> if the attribute is present, and <code>false</code>
     * otherwise.
     */
    boolean exists();    
}