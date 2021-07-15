package editor.util;

/**
 * This interface contains some useful string utilities to make formatting easier.
 * 
 * @author Alec Roelke
 */
public interface StringUtils
{
    /**
     * Format a double with the desired precision as if with %f, unless it's a whole number,
     * in which case forgo the decimal place.
     * 
     * @param n number to format
     * @param precision precision to use if the number is fractional
     * @return The formatted string.
     */
    static String formatDouble(double n, int precision)
    {
        if (n == (int)n)
            return Integer.toString((int)n);
        else
            return String.format("%." + precision + "f", n);
    }    
}
