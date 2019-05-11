package editor.collection.deck;

import java.lang.reflect.Type;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CategorySerializer implements JsonSerializer<CategorySpec>
{
    @Override
    public JsonElement serialize(CategorySpec src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject category = new JsonObject();
        category.addProperty("name", src.getName());
        category.add("whitelist", context.serialize(src.getWhitelist().stream().map((c) -> c.multiverseid().get(0)).collect(Collectors.toSet())));
        category.add("blacklist", context.serialize(src.getBlacklist().stream().map((c) -> c.multiverseid().get(0)).collect(Collectors.toSet())));
        category.addProperty("color", src.getColor().getRGB());
        category.add("filter", src.getFilter().toJsonObject());

        return category;
    }
}