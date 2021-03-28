package editor.database.card;

/**
 * This class represents a Card with two faces: One on the front, and one on the
 * back. During a game, one side can be cast.
 *
 * @author Alec Roelke
 */
public class ModalCard extends MultiCard
{
    /**
     * Card representing the front face.
     */
    private final Card front;

    /**
     * Create a new ModalCard with the given Cards as faces.  Their layouts should
     * say that they are modal double-faced cards.
     *
     * @param f card representing the front face
     * @param b card representing the back face
     */
    public ModalCard(Card f, Card b)
    {
        super(CardLayout.MODAL_DFC, f, b);
        front = f;
        if (front.layout() != CardLayout.MODAL_DFC || b.layout() != CardLayout.MODAL_DFC)
            throw new IllegalArgumentException("can't join non-modal-double-faced cards into modal double-faced cards");
    }

    /**
     * {@inheritDoc}
     * The mana value of a modal card is that of the front face (until you cast it or it enters the battlefield,
     * at which point it becomes that of the side facing up).
     */
    public double manaValue()
    {
        return front.manaValue();
    }
}
