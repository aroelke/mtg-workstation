package editor.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import editor.database.attributes.CardAttribute;

/**
 * A JSON serializer and deserializer for {@link CardAttribute}.  Basically
 * just uses its {@link CardAttribute#toString()} result for readability.
 * 
 * @author Alec Roelke
 */
public class AttributeAdapter implements JsonSerializer<CardAttribute>, JsonDeserializer<CardAttribute>
{
    @Override
    public CardAttribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        try
        {
            return CardAttribute.fromString(json.getAsString());
        }
        catch (IllegalArgumentException e)
        {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(CardAttribute src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }
}