package editor.serialization.legacy;

import java.io.IOException;
import java.io.ObjectInput;
import java.time.LocalDate;

import editor.collection.deck.CategorySpec;
import editor.collection.deck.Deck;
import editor.database.card.Card;
import editor.gui.MainFrame;

public interface DeckDeserializer
{
    /**
     * Extension used for legacy saved files.
     */
    public static final String EXTENSION = "dek";

    public static Deck readExternal(ObjectInput in) throws ClassNotFoundException, IOException
    {
        Deck d = new Deck();
        int n = in.readInt();
        for (int i = 0; i < n; i++)
        {
            Card card = MainFrame.inventory().get(in.readLong());
            int count = in.readInt();
            LocalDate added = (LocalDate)in.readObject();
            d.add(card, count, added);
        }
        n = in.readInt();
        for (int i = 0; i < n; i++)
        {
            CategorySpec spec = CategoryDeserializer.readExternal(in);
            d.addCategory(spec, in.readInt());
        }
        return d;
    }
}