package editor.util;

public interface StringUtils
{
    static String formatDouble(double n, int precision)
    {
        if (n == (int)n)
            return Integer.toString((int)n);
        else
            return String.format("%." + precision + "f", n);
    }    
}
