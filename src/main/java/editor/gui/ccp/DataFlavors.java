package editor.gui.ccp;

import java.awt.datatransfer.DataFlavor;

import editor.database.card.Card;

/**
 * Collection of data flavors used for drag-and-drop in the editor.
 */
public interface DataFlavors
{
	/**
	 * Data flavor representing cards being transferred.
	 */
	DataFlavor cardFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Card[].class.getName() + "\"", "Card Array");
	/**
	 * Data flavor representing entries in a deck.  Transfer data will appear as a
	 * map of cards onto an integer representing the number of copies to transfer.
     * It will also have auxiliary data including the source EditorFrame and ID.
	 */
    DataFlavor entryFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + DeckTransferData.class.getName() + "\"", "Deck Entries");
}