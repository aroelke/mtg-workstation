package editor.filter;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This class serializes a {@link Filter} to JSON format.
 * 
 * @author Alec Roelke
 */
public class FilterSerializer implements JsonSerializer<Filter>
{
    @Override
    public JsonElement serialize(Filter filter, Type t, JsonSerializationContext context)
    {
        return filter.toJsonObject();
    }
}