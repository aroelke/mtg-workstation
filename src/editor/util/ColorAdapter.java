package editor.util;

import java.awt.Color;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This class serializes {@link Color} using its {@link Color#getRGB()} value.
 */
public class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color>
{
    @Override
    public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(Integer.toHexString(src.getRGB()));
    }

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        // Need to parse as a long and cast to int in case the color is white,
        // which has value 0xFFFFFFFF and is outside the range of an int
        return new Color((int)Long.parseLong(json.getAsString(), 16), true);
    }
}