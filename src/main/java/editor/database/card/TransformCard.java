package editor.database.card;

import java.util.List;

import editor.database.attributes.ManaCost;
import editor.util.Lazy;

/**
 * This class represents a Card with two faces:  One on the front, and one on the back.
 * During a game, it will transform from one to the other.
 *
 * @author Alec Roelke
 */
public class TransformCard extends MultiCard
{
    /**
     * Converted mana costs of this DoubleFacedCard's faces.
     */
    private Lazy<List<Double>> cmc;
    /**
     * Card representing the front face.
     */
    private final Card front;
    /**
     * Tuple of this DoubleFacedCard's faces' mana costs.
     */
    private Lazy<List<ManaCost>> manaCost;

    /**
     * Create a new DoubleFacedCard with the given Cards as faces.  Their layouts should
     * say that they are double-faced cards.
     *
     * @param f card representing the front face
     * @param b card representing the back face
     */
    public TransformCard(Card f, Card b)
    {
        super(CardLayout.TRANSFORM, f, b);
        front = f;
        if (front.layout() != CardLayout.TRANSFORM || b.layout() != CardLayout.TRANSFORM)
            throw new IllegalArgumentException("can't join non-double-faced cards into double-faced cards");

        manaCost = new Lazy<>(() -> List.of(front.manaCost().get(0), new ManaCost()));
        cmc = new Lazy<>(() -> List.of(front.cmc().get(0), front.cmc().get(0)));
    }

    /**
     * {@inheritDoc}
     * While only the front face has a mana cost (see {@link TransformCard#manaCost()},
     * both faces have the same converted mana cost.
     */
    @Override
    public List<Double> cmc()
    {
        return cmc.get();
    }

    /**
     * {@inheritDoc}
     * Only the front face has a mana cost.
     */
    @Override
    public List<ManaCost> manaCost()
    {
        return manaCost.get();
    }
}
