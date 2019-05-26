package editor.serialization;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import editor.collection.deck.CategorySpec;
import editor.filter.Filter;
import editor.gui.MainFrame;

/**
 * A JSON serializer and deserializer for {@link CategorySpec}.
 * 
 * @author Alec Roelke
 */
public class CategoryAdapter implements JsonSerializer<CategorySpec>, JsonDeserializer<CategorySpec>
{
    @Override
    public JsonElement serialize(CategorySpec src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject category = new JsonObject();
        category.addProperty("name", src.getName());
        category.add("whitelist", context.serialize(src.getWhitelist().stream().map((c) -> c.multiverseid().get(0)).collect(Collectors.toSet())));
        category.add("blacklist", context.serialize(src.getBlacklist().stream().map((c) -> c.multiverseid().get(0)).collect(Collectors.toSet())));
        category.add("color", context.serialize(src.getColor()));
        category.add("filter", context.serialize(src.getFilter()));

        return category;
    }

    @Override
    public CategorySpec deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject obj = json.getAsJsonObject();

        CategorySpec category = new CategorySpec();
        category.setName(obj.get("name").getAsString());
        for (JsonElement element : obj.get("whitelist").getAsJsonArray())
            category.include(MainFrame.inventory().get(element.getAsLong()));
        for (JsonElement element : obj.get("blacklist").getAsJsonArray())
            category.exclude(MainFrame.inventory().get(element.getAsLong()));
        category.setColor(context.deserialize(obj.get("color"), Color.class));
        category.setFilter(context.deserialize(obj.get("filter"), Filter.class));

        return category;
    }
}