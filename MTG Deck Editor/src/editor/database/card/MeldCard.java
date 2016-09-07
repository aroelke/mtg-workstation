package editor.database.card;

import java.util.Arrays;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import editor.database.characteristics.ManaCost;

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
	 * Front face of this MeldCard.
	 */
	private final Card front;
	/**
	 * This MeldCard's "sibling," with which it melds to form the back face.
	 */
	private final Card other;
	/**
	 * TODO: Comment these
	 */
	private ManaCost.Tuple manaCost;
	private List<Double> cmc;
	
	/**
	 * Create a new MeldCard, with the given Cards as front, other front, and
	 * back faces, respectively.
	 * 
	 * @param f Front face of the new MeldCard
	 * @param o Other front face of the new MeldCard
	 * @param b Back face of the two front faces.
	 */
	public MeldCard(Card f, Card o, Card b)
	{
		super(CardLayout.MELD, f, b);
		front = f;
		other = o;
		
		if (front.layout() != CardLayout.MELD || other.layout() != CardLayout.MELD || b.layout() != CardLayout.MELD)
			throw new IllegalArgumentException("can't join non-meld cards into meld cards");
		
		manaCost = null;
		cmc = null;
	}
	
	/**
	 * @return The Card this MeldCard melds with. 
	 */
	public Card meldsWith()
	{
		return other;
	}
	
	/**
	 * @return A list containing the mana cost of this DoubleFacedCard.  Only the front
	 * face has a mana cost.
	 */
	@Override
	public ManaCost.Tuple manaCost()
	{
		if (manaCost == null)
			manaCost = new ManaCost.Tuple(front.manaCost().get(0), new ManaCost());
		return manaCost;
	}
	
	/**
	 * @return A list containing the converted mana cost of this DoubleFacedCard.  While only
	 * the front face has a mana cost, the back face's converted mana cost is the sum of those
	 * of its two front faces.
	 */
	@Override
	public List<Double> cmc()
	{
		if (cmc == null)
			cmc = Arrays.asList(front.cmc().get(0), front.cmc().get(0) + other.cmc().get(0));
		return cmc;
	}
	
	/**
	 * @return The converted mana cost of this MeldCard's front face.
	 */
	@Override
	public double minCmc()
	{
		return front.minCmc();
	}
	
	/**
	 * The same as Card's {@link Card#formatDocument(StyledDocument, int)}, except
	 * add some information to the front face indicating what the other front face is.
	 * 
	 * @param document Document to add text to
	 * @param f Face to add text from.  If 0, then an additional line is added indicating
	 * what the other half of the meld card is.
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
}
