package editor.gui.display;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;

import editor.database.card.Card;
import editor.gui.SettingsDialog;

/**
 * This class represents an element that can display a list of cards by name.
 * It cannot be edited and does not support selection.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardJList extends JList<String>
{
	/**
	 * List of cards to display.
	 */
	private List<Card> cards;
	
	/**
	 * Create an empty CardJList.
	 */
	public CardJList()
	{
		this(new ArrayList<>());
	}
	
	/**
	 * Create a new CardJList displaying the given list of cards.
	 * 
	 * @param c card list to display
	 */
	public CardJList(List<Card> c)
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
		setVisibleRowCount(SettingsDialog.getAsInt(SettingsDialog.EXPLICITS_ROWS));
	}
	
	/**
	 * Set the list of cards to display.
	 * 
	 * @param c New list to display
	 */
	public void setCards(List<Card> c)
	{
		cards = c;
	}
}
