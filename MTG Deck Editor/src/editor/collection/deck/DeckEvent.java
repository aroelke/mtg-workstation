package editor.collection.deck;

/**
 * TODO: Comment this class
 * 
 * @author Alec Roelke
 */
public class DeckEvent
{
	private Deck source;
	private boolean cardsAdded;
	private boolean cardsRemoved;
	private boolean categoryAdded;
	private boolean categoryRemoved;
	
	public DeckEvent(Deck s,
			boolean cardAdd, boolean cardRem,
			boolean catAdd, boolean catRem)
	{
		source = s;
		
		cardsAdded = cardAdd;
		cardsRemoved = cardRem;
		categoryAdded = catAdd;
		categoryRemoved = catRem;
	}
	
	public Deck getSource()
	{
		return source;
	}
	
	public boolean cardsAdded()
	{
		return cardsAdded;
	}
	
	public boolean cardsRemoved()
	{
		return cardsRemoved;
	}
	
	public boolean categoryAdded()
	{
		return categoryAdded;
	}
	
	public boolean categoryRemoved()
	{
		return categoryRemoved;
	}
}
