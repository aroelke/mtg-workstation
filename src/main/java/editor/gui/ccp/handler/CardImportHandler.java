package editor.gui.ccp.handler;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.TransferHandler;

import editor.database.card.Card;
import editor.gui.ccp.DataFlavors;
import editor.gui.editor.EditorFrame;

@SuppressWarnings("serial")
public class CardImportHandler extends TransferHandler implements ImportHandler
{
    private final EditorFrame editor;
    private final int id;

    public CardImportHandler(EditorFrame e, int i)
    {
        editor = e;
        id = i;
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        return !supp.isDrop() && supp.isDataFlavorSupported(DataFlavors.cardFlavor);
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        if (!canImport(supp))
            return false;
        else
        {
            try
            {
                var data = Arrays.stream((Card[])supp.getTransferable().getTransferData(DataFlavors.cardFlavor)).collect(Collectors.toSet());
                return editor.addCards(id, data, 1);
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                return false;
            }
        }
    }
}