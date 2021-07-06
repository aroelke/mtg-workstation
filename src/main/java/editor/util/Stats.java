package editor.util;

public interface Stats
{

    /**
     * Calculate the exact value of n!, as long as it will fit into a double.
     *
     * @param n parameter for factorial
     * @return the factorial of n, or n!
     */
    static double factorial(int n)
    {
        double f = 1.0;
        for (int i = 1; i <= n; i++)
            f *= i;
        return f;
    }

    /**
     * Calculate n choose k based on the {@link factorial}
     * function.
     *
     * @param n number of items to choose from
     * @param k number of items to choose
     * @return the number of ways to choose k out of n items.
     */
    static double nchoosek(int n, int k)
    {
        return factorial(n)/(factorial(n - k)*factorial(k));
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
        return nchoosek(count, n)*nchoosek(total - count, hand - n)/nchoosek(total, hand);
    }
    
}
