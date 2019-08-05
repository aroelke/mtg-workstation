package editor.serialization;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import editor.collection.deck.CategorySpec;
import editor.collection.deck.Deck;
import editor.database.card.Card;

public class DeckAdapter implements JsonSerializer<Deck>, JsonDeserializer<Deck>
{
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public JsonElement serialize(Deck src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject deck = new JsonObject();

        JsonArray cards = new JsonArray();
        for (Card card : src)
        {
            JsonObject entry = new JsonObject();
            entry.add("card", context.serialize(card));
            entry.addProperty("count", src.getEntry(card).count());
            entry.addProperty("date", src.getEntry(card).dateAdded().format(FORMATTER));
            cards.add(entry);
        }
        deck.add("cards", cards);

        JsonArray categories = new JsonArray();
        for (CategorySpec spec : src.categories())
        {
            JsonObject category = context.serialize(spec).getAsJsonObject();
            category.addProperty("rank", src.getCategoryRank(spec.getName()));
            categories.add(category);
        }
        deck.add("categories", categories);
        return deck;
    }

    @Override
    public Deck deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        Deck d = new Deck();
        JsonObject obj = json.getAsJsonObject();
        for (JsonElement element : obj.get("cards").getAsJsonArray())
        {
            JsonObject entry = element.getAsJsonObject();
            d.add(
                context.deserialize(entry.get("card"), Card.class),
                entry.get("count").getAsInt(),
                LocalDate.parse(entry.get("date").getAsString(), FORMATTER)
            );
        }
        for (JsonElement element : obj.get("categories").getAsJsonArray())
            d.addCategory(context.deserialize(element, CategorySpec.class), element.getAsJsonObject().get("rank").getAsInt());
        return d;
    }
}