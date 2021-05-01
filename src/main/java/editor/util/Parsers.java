package editor.util;

import java.util.OptionalDouble;

/**
 * Additional methods for parsing Strings.
 */
public interface Parsers
{
    /**
     * Try to parse a double from a string.
     * 
     * @param s string to parse
     * @return the numerical value of the string, or {@link OptionalDouble#empty} if the string
     * can't be parsed.
     */
    static OptionalDouble tryParseDouble(String s)
    {
        try
        {
            return OptionalDouble.of(Double.parseDouble(s));
        }
        catch (NumberFormatException e)
        {
            return OptionalDouble.empty();
        }
    }
}
