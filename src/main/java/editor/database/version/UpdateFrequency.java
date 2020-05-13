package editor.database.version;

/**
 * This enum specifies the frequency at which a database udpate is desired.
 * 
 * @author Alec Roelke
 */
public enum UpdateFrequency
{
    /** Only update on major version changes. */
    MAJOR("Major version change"),
    /** Update on major or minor version changes. */
    MINOR("Minor version change"),
    /** Update on major, minor, and revision changes. */
    REVISION("Revision change"),
    /** Update on all changes. */
    DAILY("Price update (daily)"),
    /** Don't update. */
    NEVER("Never");
    
    /** String label for this UpdateFrequency. */
    private final String label;

    /**
     * Create a new UpdateFrequency with the specified label.
     *
     * @param n label of the update frequency
     */
    UpdateFrequency(String n)
    {
        label = n;
    }

    @Override
    public String toString()
    {
        return label;
    }
}