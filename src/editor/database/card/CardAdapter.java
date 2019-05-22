package editor.database.card;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import editor.gui.MainFrame;

public class CardAdapter implements JsonSerializer<Card>, JsonDeserializer<Card>
{
    @Override
    public Card deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (MainFrame.inventory().contains(json.getAsLong()))
            return MainFrame.inventory().get(json.getAsLong());
        else
            throw new JsonParseException("no card with multiverseid " + json.getAsLong() + " exists");
    }

    @Override
    public JsonElement serialize(Card src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.multiverseid().get(0));
    }
    
}