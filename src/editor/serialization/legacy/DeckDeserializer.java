package editor.serialization.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import editor.collection.deck.CategorySpec;
import editor.collection.deck.Deck;
import editor.database.card.Card;
import editor.gui.MainFrame;
import editor.gui.editor.DeckSerializer;

public interface DeckDeserializer
{
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

    public static DeckSerializer readFile(File f) throws ClassNotFoundException, IOException
    {
        DeckSerializer manager = null;

        long version;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f)))
        {
            version = ois.readLong();
        }
        // Assume that high bits in the first 64 bits are used by the serialization of a Deck
        // object and that SAVE_VERSION will never be that high.
        if (version > DeckSerializer.SAVE_VERSION)
            version = 0;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f)))
        {
            Map<String, Deck> sideboard = new HashMap<>();

            if (version > 0)
                ois.readLong(); // Throw out first 64 bits that have already been read
            Deck deck = DeckDeserializer.readExternal(ois);
            if (version <= 2)
                sideboard.put("Sideboard", DeckDeserializer.readExternal(ois));
            else
            {
                int boards = ois.readInt();
                for (int i = 0; i < boards; i++)
                {
                    String name = ois.readUTF();
                    sideboard.put(name, DeckDeserializer.readExternal(ois));
                }
            }
            String changelog = version < 2 ? (String)ois.readObject() : ois.readUTF();
            manager = new DeckSerializer(deck, sideboard, changelog);
        }
        return manager;
    }
}