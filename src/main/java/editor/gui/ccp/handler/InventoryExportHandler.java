package editor.gui.ccp.handler;

import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.database.card.Card;
import editor.gui.ccp.data.CardTransferData;

@SuppressWarnings("serial")
public class InventoryExportHandler extends TransferHandler
{
    private final Supplier<? extends Collection<? extends Card>> cards;

    public InventoryExportHandler(Supplier<? extends Collection<? extends Card>> c)
    {
        cards = c;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.COPY;
    }

    @Override
    public Transferable createTransferable(JComponent c)
    {
        return new CardTransferData(cards.get());
    }

    @Override
    public void exportDone(JComponent source, Transferable data, int action)
    {}
}