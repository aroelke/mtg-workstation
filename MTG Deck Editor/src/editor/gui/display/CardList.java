package editor.gui.display;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;

import editor.database.card.Card;

/**
 * This class represents an element that can display a list of Cards by name.
 * It cannot be edited and does not support selection.
 * 
 * TODO: Implement editing
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardList extends JList<String>
{
	/**
	 * TODO: Make this a setting
	 */
	private static final int CARDS_TO_DISPLAY = 3;
	
	/**
	 * List of cards to display.
	 */
	private List<Card> cards;
	
	/**
	 * Create a new CardList displaying the given list of Cards.
	 * 
	 * @param c Card list to display
	 */
	public CardList(List<Card> c)
	{
		super();
		cards = c;
		setSelectionModel(new DefaultListSelectionModel()
		{
			{
				setSelectionMode(SINGLE_SELECTION);
			}
			
			@Override
			public void setSelectionInterval(int index0, int index1)
			{
				super.setSelectionInterval(-1, -1);
			}
		});
		setModel(new DefaultListModel<String>()
		{
			@Override
			public String getElementAt(int index)
			{
				return cards.get(index).unifiedName();
			}
			
			@Override
			public int getSize()
			{
				return cards.size();
			}
		});
		setVisibleRowCount(CARDS_TO_DISPLAY);
	}
	
	/**
	 * Create an empty CardList.
	 */
	public CardList()
	{
		this(new ArrayList<Card>());
	}
	
	/**
	 * Set the list of Cards to display.
	 * 
	 * @param c New list to display
	 */
	public void setCards(List<Card> c)
	{
		cards = c;
	}
}
