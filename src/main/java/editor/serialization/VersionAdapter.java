package editor.serialization;

import java.lang.reflect.Type;
import java.text.ParseException;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import editor.database.version.DatabaseVersion;

public class VersionAdapter implements JsonSerializer<DatabaseVersion>, JsonDeserializer<DatabaseVersion>
{
    @Override
    public JsonElement serialize(DatabaseVersion src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public DatabaseVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        try
        {
            return new DatabaseVersion(json.getAsString());
        }
        catch (ParseException e)
        {
            throw new JsonParseException(e);
        }
    }
}