package editor.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface Stats
{

    /**
     * @param n parameter for factorial
     * @return the factorial of n, or n!
     */
    static BigInteger factorial(int n)
    {
        BigInteger f = BigInteger.ONE;
        for (int i = 1; i <= n; i++)
            f = f.multiply(BigInteger.valueOf(i));
        return f;
    }

    /**
     * Calculate n choose k based on the {@link factorial} function.
     *
     * @param n number of items to choose from
     * @param k number of items to choose
     * @return the number of ways to choose k out of n items.
     */
    static BigDecimal nchoosek(int n, int k)
    {
        if (k == 0)
            return BigDecimal.ONE;
        else if (n == 0)
            return BigDecimal.ZERO;
        else
            return new BigDecimal(factorial(n)).divide(new BigDecimal((factorial(n - k).multiply(factorial(k)))));
    }

    /**
     * Calculate a hypergeometric distribution for drawing the given number
     * of cards in a hand of the given size from a deck of the given size,
     * when the given number of cards are successes.
     *
     * @param n number of desired cards
     * @param hand size of hand drawn
     * @param count number of successful cards in deck
     * @param total number of cards in the deck
     * @return the hypergeometric distribution with parameters hand, count, and
     * total and argument n.
     */
    static double hypergeometric(int n, int hand, int count, int total)
    {
        if (hand - (total - count) > n)
            return 0;
        else
            return nchoosek(count, n).multiply(nchoosek(total - count, hand - n)).divide(nchoosek(total, hand)).doubleValue();
    }
    
}
