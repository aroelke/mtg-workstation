package editor.database.card;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import editor.util.Lazy;

/**
 * This class represents a card that has several faces all printed on the front.
 *
 * @author Alec Roelke
 */
public class SplitCard extends MultiCard
{
    /**
     * Converted mana costs of this SplitCard's faces.
     */
    private Lazy<List<Double>> cmc;

    /**
     * Create a new SplitCard with the given Cards as faces.
     *
     * @param f cards to use as faces
     */
    public SplitCard(Card... f)
    {
        this(Arrays.asList(f));
    }

    /**
     * Create a new SplitCard with the given Cards as faces.  They should all indicate
     * with their layouts that they are split cards.
     *
     * @param f list of Cards representing faces
     */
    public SplitCard(List<Card> f)
    {
        super(f.get(0).layout(), f);
        for (Card face : f)
            if (face.layout() != CardLayout.SPLIT && face.layout() != CardLayout.AFTERMATH)
                throw new IllegalArgumentException("can't create split cards out of non-split cards");
        for (Card face : f)
            if (face.layout() != f.get(0).layout())
                throw new IllegalArgumentException("all faces of a split card must be of the same type");

        cmc = new Lazy<>(() -> Collections.unmodifiableList(Collections.nCopies(f.size(), f.stream().mapToDouble((c) -> c.cmc().get(0)).sum())));
    }

    /**
     * {@inheritDoc}
     * The converted mana cost of a split card is the sum of the converted mana costs of all
     * of its faces.
     */
    @Override
    public List<Double> cmc()
    {
        return cmc.get();
    }

    /**
     * {@inheritDoc}
     * All of the faces of a SplitCard are on the front, so there is only one image for it.
     */
    @Override
    public List<String> imageNames()
    {
        return super.imageNames().subList(0, 1);
    }

    /**
     * {@inheritDoc}
     * All of the faces of a SplitCard are on the front, so only one multiverseid is necessary.
     */
    @Override
    public List<Long> multiverseid()
    {
        return super.multiverseid().subList(0, 1);
    }
}
