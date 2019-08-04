package editor.serialization;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
        return deck;
    }

    @Override
    public Deck deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return null;
    }
}