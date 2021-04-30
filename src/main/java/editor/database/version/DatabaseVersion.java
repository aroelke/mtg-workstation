package editor.database.version;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class contains version information about the card database. Versions
 * are expected to conform to major.minor.rev-YYYYMMDD, where the date
 * represents daily minor updates (i.e. prices) and is optional.
 * 
 * @param major major version of the database
 * @param minor minor version of the database
 * @param revision revision number of the database
 * @param date date of the latest minor update
 * 
 * @author Alec Roelke
 */
public record DatabaseVersion(int major, int minor, int revision, Optional<Date> date) implements Comparable<DatabaseVersion>
{
    /**
     * Regular expression pattern used to match version info.
     */
    public static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:(?:\\+|-)(\\d{4}\\d{2}\\d{2}))?$");
    /**
     * Date formatter for parsing and formatting dates to strings.
     */
    public static final SimpleDateFormat VERSION_DATE = new SimpleDateFormat("yyyyMMdd");

    /**
     * Create a new database version from the string of the form
     * major.minor.rev-YYYYMMDD, with the date being optional. Anything
     * else throws an exception.
     * 
     * @param s string to parse
     */
    public static DatabaseVersion parseVersion(String s) throws ParseException
    {
        Matcher m = VERSION_PATTERN.matcher(s);
        if (m.matches())
        {
            return new DatabaseVersion(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                m.group(4) != null ? Optional.of(VERSION_DATE.parse(m.group(4))) : Optional.empty()
            );
        }
        else
            throw new ParseException(s, 0);
    }

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
        this(maj, min, rev, Optional.of(d));
    }

    /**
     * Create a new database version with a specific version number and no date.
     * 
     * @param maj major version
     * @param min minor version
     * @param rev revision
     */
    public DatabaseVersion(int maj, int min, int rev)
    {
        this(maj, min, rev, Optional.empty());
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
        {
            if (date.isPresent() && other.date.isPresent())
                return date.get().compareTo(other.date.get());
            else if (date.isPresent() && other.date.isEmpty())
                return 1;
            else if (date.isEmpty() && other.date.isPresent())
                return -1;
            else // date.isEmpty() && other.date.isEmpty()
                return 0;
        }
    }

    @Override
    public String toString()
    {
        return List.of(major, minor, revision).stream().map(String::valueOf).collect(Collectors.joining(".")) + date.map(d -> "-" + VERSION_DATE.format(d)).orElse("");
    }
}