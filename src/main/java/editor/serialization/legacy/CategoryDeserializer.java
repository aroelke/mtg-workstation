package editor.serialization.legacy;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInput;

import editor.collection.deck.CategorySpec;
import editor.gui.MainFrame;

public interface CategoryDeserializer
{
    public static CategorySpec readExternal(ObjectInput in) throws ClassNotFoundException, IOException
    {
        CategorySpec spec = new CategorySpec();
        spec.setName(in.readUTF());
        spec.setColor((Color)in.readObject());
        spec.setFilter(FilterDeserializer.readExternal(in));
        for (int i = in.readInt(); i > 0; i--)
            spec.exclude(MainFrame.inventory().get(in.readLong()));
        for (int i = in.readInt(); i > 0; i--)
            spec.include(MainFrame.inventory().get(in.readLong()));
        return spec;
    }
}