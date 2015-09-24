package gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import database.Card;

/**
 * TODO: Comment this
 * @author Alec
 *
 */
public class CardTransferable implements Transferable
{
	private Card[] cards;
	
	public CardTransferable(Card[] c)
	{
		cards = c;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(Card.cardFlavor))
			return cards;
		else if (flavor.equals(DataFlavor.stringFlavor))
			return Arrays.stream(cards).map(Card::name).reduce((a, b) -> a + "\n" + b).get();
		else
			throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] {Card.cardFlavor, DataFlavor.stringFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return Arrays.asList(getTransferDataFlavors()).contains(flavor);
	}
}
