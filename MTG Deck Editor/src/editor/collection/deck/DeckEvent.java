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
	private String categoryAdded;
	private String categoryRemoved;
	
	public DeckEvent(Deck s,
			boolean cardAdd, boolean cardRem,
			String catAdd, String catRem)
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
		return categoryAdded != null;
	}
	
	public String addedName()
	{
		if (categoryAdded())
			return categoryAdded;
		else
			throw new IllegalStateException("No card has been added to the deck.");
	}
	
	public boolean categoryRemoved()
	{
		return categoryRemoved != null;
	}
	
	public String removedName()
	{
		if (categoryRemoved())
			return categoryRemoved;
		else
			throw new IllegalStateException("No card has been removed from the deck.");
	}
}
