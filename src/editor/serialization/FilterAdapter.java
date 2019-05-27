package editor.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import editor.filter.Filter;
import editor.filter.FilterAttribute;

/**
 * This class serializes a {@link Filter} to and deserializes from JSON format.
 * 
 * @author Alec Roelke
 */
public class FilterAdapter implements JsonSerializer<Filter>, JsonDeserializer<Filter>
{
    @Override
    public JsonElement serialize(Filter src, Type typeOfSrc, JsonSerializationContext context)
    {
        return src.toJsonObject();
    }

    @Override
    public Filter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Filter filter = FilterAttribute.createFilter(FilterAttribute.fromString(json.getAsJsonObject().get("type").getAsString()));
        filter.fromJsonObject(json.getAsJsonObject());
        return filter;
    }
}