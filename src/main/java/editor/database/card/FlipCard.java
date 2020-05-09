package editor.database.card;

import java.util.List;
import java.util.function.Function;

import editor.database.characteristics.ManaCost;
import editor.util.Lazy;

/**
 * This class represents a flip card, which has two faces:  Top and bottom.
 *
 * @author Alec Roelke
 */
public class FlipCard extends MultiCard
{
    /**
     * List containing the converted mana cost of each side of this FlipCard (which should just be two
     * copies of the same value).
     */
    private Lazy<List<Double>> cmc;
    /**
     * Tuple containing the mana cost of each side of this FlipCard (which should just be two copies
     * of the same value).
     */
    private Lazy<List<ManaCost>> manaCost;
    /**
     * Card representing the top face of this FlipCard.
     */
    private final Card top;

    /**
     * Create a new FlipCard with the given Cards as its faces.  They should indicate that
     * their layouts are flip card layouts.
     *
     * @param t top face of this FlipCard
     * @param b bottom face of this FlipCard
     */
    public FlipCard(Card t, Card b)
    {
        super(CardLayout.FLIP, t, b);
        top = t;
        if (top.layout() != CardLayout.FLIP || b.layout() != CardLayout.FLIP)
            throw new IllegalArgumentException("can't join non-flip cards into flip cards");

        manaCost = new Lazy<>(() -> collect(Card::manaCost));
        cmc = new Lazy<>(() -> collect(Card::cmc));
    }

    /**
     * {@inheritDoc}
     * Both faces of a FlipCard have the same converted mana cost.
     */
    @Override
    public List<Double> cmc()
    {
        return cmc.get();
    }

    /**
     * Duplicate the value of the given characteristic for the front face into a list.
     *
     * @param characteristic characteristic to collect
     * @return a list containing the given characteristic repeated twice (once for the front
     * face, and once for the back).
     */
    private <T> List<T> collect(Function<Card, List<T>> characteristic)
    {
        return List.of(characteristic.apply(top).get(0), characteristic.apply(top).get(0));
    }

    /**
     * {@inheritDoc}
     * Both faces of a FlipCard have the same mana cost.
     */
    @Override
    public List<ManaCost> manaCost()
    {
        return manaCost.get();
    }

    @Override
    public double minCmc()
    {
        return top.minCmc();
    }
}
