package editor.serialization.legacy;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInput;

import editor.collection.deck.Category;
import editor.gui.MainFrame;

public interface CategoryDeserializer
{
    public static Category readExternal(ObjectInput in) throws ClassNotFoundException, IOException
    {
        Category spec = new Category();
        spec.setName(in.readUTF());
        spec.setColor((Color)in.readObject());
        spec.setFilter(FilterDeserializer.readExternal(in));
        for (int i = in.readInt(); i > 0; i--)
            spec.exclude(MainFrame.inventory().find((int)in.readLong()));
        for (int i = in.readInt(); i > 0; i--)
            spec.include(MainFrame.inventory().find((int)in.readLong()));
        return spec;
    }
}