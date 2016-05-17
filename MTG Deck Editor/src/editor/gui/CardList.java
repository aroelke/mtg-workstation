package editor.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;

import editor.database.Card;

/**
 * TODO: Comment this class
 * @author Alec
 */
@SuppressWarnings("serial")
public class CardList extends JList<String>
{
	/**
	 * TODO: Make this a setting
	 */
	private static final int CARDS_TO_DISPLAY = 3;
	
	private List<Card> cards;
	
	public CardList(List<Card> c)
	{
		super();
		cards = c;
		setSelectionModel(new DefaultListSelectionModel() {
			{
				setSelectionMode(SINGLE_SELECTION);
			}
			
			@Override
			public void setSelectionInterval(int index0, int index1)
			{
				super.setSelectionInterval(-1, -1);
			}
		});
		setModel(new CardListModel());
		setVisibleRowCount(CARDS_TO_DISPLAY);
	}
	
	public CardList()
	{
		this(new ArrayList<Card>());
	}
	
	public void setCards(List<Card> c)
	{
		cards = c;
	}
	
	private class CardListModel extends DefaultListModel<String>
	{
		@Override
		public String getElementAt(int index)
		{
			if (index > cards.size())
				throw new ArrayIndexOutOfBoundsException("Illegal list index " + index);
			else
				return cards.get(index).name();
		}
		
		@Override
		public int getSize()
		{
			return cards.size();
		}
	}
}
