package editor.database.version;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains version information about the card database. Versions
 * are expected to conform to major.minor.rev+YYYYMMDD, where the date
 * represents daily minor updates (i.e. prices).
 * 
 * @author Alec Roelke
 */
public class DatabaseVersion implements Comparable<DatabaseVersion>
{
    /**
     * Regular expression pattern used to match version info.
     */
    public static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)\\+(\\d{4}\\d{2}\\d{2})$");
    /**
     * Date formatter for parsing and formatting dates to strings.
     */
    public static final SimpleDateFormat VERSION_DATE = new SimpleDateFormat("yyyyMMdd");

    /** Major version of the database. */
    public final int major;
    /** Minor version of the database. */
    public final int minor;
    /** Revision number of the database version. */
    public final int revision;
    /** Date of the latest minor update. */
    public final Date date;

    /**
     * Create a new database version with a specific version number and date.
     * 
     * @param maj major version
     * @param min minor version
     * @param rev revision
     * @param d daily update date
     */
    public DatabaseVersion(int maj, int min, int rev, Date d)
    {
        major = maj;
        minor = min;
        revision = rev;
        date = d;
    }

    /**
     * Create a new database version from the string of the form
     * major.minor.rev+YYYYMMDD. Anything else throws an exception.
     * 
     * @param version string to parse
     */
    public DatabaseVersion(String version) throws ParseException
    {
        Matcher m = VERSION_PATTERN.matcher(version);
        if (m.matches())
        {
            major = Integer.parseInt(m.group(1));
            minor = Integer.parseInt(m.group(2));
            revision = Integer.parseInt(m.group(3));
            date = VERSION_DATE.parse(m.group(4));
        }
        else
            throw new ParseException(version, 0);
    }

    /**
     * Check if another version needs an update compared to this one based
     * on the desired update frequency.
     * 
     * @param other version to check
     * @param freq frequency of desired update
     * @return <code>true</code> if an update is needed, and <code>false</code>
     * otherwise.
     */
    public boolean needsUpdate(DatabaseVersion other, UpdateFrequency freq)
    {
        switch (freq)
        {
        case NEVER:
            return false;
        // The rest of the cases fall through on purpose
        case DAILY:
            if (!date.equals(other.date))
                return true;
        case REVISION:
            if (revision != other.revision)
                return true;
        case MINOR:
            if (minor != other.minor)
                return true;
        case MAJOR:
            if (major != other.major)
                return true;
        }
        return false;
    }

    @Override
    public int compareTo(DatabaseVersion other)
    {
        if (major != other.major)
            return major - other.major;
        else if (minor != other.minor)
            return minor - other.minor;
        else if (revision != other.revision)
            return revision - other.revision;
        else
            return date.compareTo(other.date);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof DatabaseVersion))
            return false;
        return compareTo((DatabaseVersion)other) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(major, minor, revision, date);
    }

    @Override
    public String toString()
    {
        return List.of(major, minor, revision).stream().map(String::valueOf).reduce((a, b) -> a + "." + b).get() + "+" + VERSION_DATE.format(date);
    }
}