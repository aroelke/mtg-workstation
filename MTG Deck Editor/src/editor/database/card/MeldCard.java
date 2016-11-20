package editor.database.card;

import java.util.Arrays;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import editor.database.characteristics.ManaCost;
import editor.util.Lazy;

/**
 * This class represents a "meld" card, which is a double-faced card where the back face is
 * only half of a card (with the other half belonging to another card).  It knows about its
 * "sibling" for deriving characteristics of the back face but doesn't display any of its
 * "sibling's" information.
 * 
 * @author Alec Roelke
 */
public class MeldCard extends MultiCard
{
	/**
	 * Converted mana costs of this MeldCard's faces. 
	 */
	private Lazy<List<Double>> cmc;
	/**
	 * Front face of this MeldCard.
	 */
	private final Card front;
	/**
	 * Tuple of this MeldCard's faces' mana costs.
	 */
	private Lazy<ManaCost.Tuple> manaCost;
	/**
	 * This MeldCard's "sibling," with which it melds to form the back face.
	 */
	private final Card other;
	
	/**
	 * Create a new MeldCard, with the given cards as front, other front, and
	 * back faces, respectively.
	 * 
	 * @param f front face of the new MeldCard
	 * @param o other front face of the new MeldCard
	 * @param b back face of the two front faces.
	 */
	public MeldCard(Card f, Card o, Card b)
	{
		super(CardLayout.MELD, f, b);
		front = f;
		other = o;
		
		if (front.layout() != CardLayout.MELD || other.layout() != CardLayout.MELD || b.layout() != CardLayout.MELD)
			throw new IllegalArgumentException("can't join non-meld cards into meld cards");
		
		manaCost = new Lazy<ManaCost.Tuple>(() -> new ManaCost.Tuple(front.manaCost()[0], new ManaCost()));
		cmc = new Lazy<List<Double>>(() -> Arrays.asList(front.cmc()[0], front.cmc()[0] + other.cmc()[0]));
	}
	
	/**
	 * {@inheritDoc}
	 * While only the front face has a mana cost, the back face's converted mana cost
	 * is the sum of those of its two front faces.
	 */
	@Override
	public List<Double> cmc()
	{
		return cmc.get();
	}

	/**
	 * {@inheritDoc}
	 * Includes some information to the front face indicating what the other front face is.
	 */
	@Override
	public void formatDocument(StyledDocument document, int f)
	{
		Style reminderStyle = document.getStyle("reminder");
		super.formatDocument(document, f);
		if (f == 0)
		{
			try
			{
				document.insertString(document.getLength(), "\nMelds with " + other.unifiedName(), reminderStyle);
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * The back face of a MeldCard has no mana cost (but does have a converted mana cost,
	 * see {@link MeldCard#cmc()}).
	 */
	@Override
	public ManaCost.Tuple manaCost()
	{
		return manaCost.get();
	}

	/**
	 * Get the card this MeldCard melds with.
	 * 
	 * @return the Card this MeldCard melds with. 
	 */
	public Card meldsWith()
	{
		return other;
	}
	
	@Override
	public double minCmc()
	{
		return front.minCmc();
	}
}
