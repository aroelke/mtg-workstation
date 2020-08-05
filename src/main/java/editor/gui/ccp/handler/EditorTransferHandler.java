package editor.gui.ccp.handler;

import java.util.Set;

import javax.swing.TransferHandler;

import editor.gui.editor.EditorFrame;

@SuppressWarnings("serial")
public class EditorTransferHandler extends TransferHandler
{
    private Set<ImportHandler> importers;

    public EditorTransferHandler(EditorFrame e, int i)
    {
        importers = Set.of(new CardImportHandler(e, i), new EntryImportHandler(e, i));
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        return importers.stream().anyMatch(i -> i.canImport(supp));
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        return importers.stream().filter(i -> i.canImport(supp)).findAny().get().importData(supp);
    }
}