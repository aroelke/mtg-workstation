package editor.filter.leaf.options.multi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.Legality;
import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents a filter that groups cards by format legality.
 *
 * @author Alec Roelke
 */
public class LegalityFilter extends MultiOptionsFilter<String>
{
    /**
     * Whether or not the card should be restricted in the formats
     * selected.
     */
    public boolean restricted;

    /**
     * Create a new LegalityFilter.
     */
    public LegalityFilter()
    {
        super(CardAttribute.LEGAL_IN, Card::legalIn);
        restricted = false;
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    protected FilterLeaf<String> subCopy()
    {
        LegalityFilter filter = (LegalityFilter)CardAttribute.createFilter(CardAttribute.LEGAL_IN);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        filter.restricted = restricted;
        return filter;
    }

    /**
     * {@inheritDoc}
     * Filter cards not only according to the selection of formats, but also
     * optionally check if they are restricted in those formats.
     */
    @Override
    protected boolean testFace(Card c)
    {
        if (!super.testFace(c))
            return false;
        else if (restricted)
        {
            var formats = new ArrayList<>(c.legalIn());
            formats.retainAll(selected);
            return formats.stream().noneMatch((f) -> c.legality().get(f) != Legality.RESTRICTED);
        }
        else
            return true;
    }

    @Override
    protected JsonElement convertToJson(String item)
    {
        return new JsonPrimitive(item);
    }

    @Override
    protected void serializeFields(JsonObject fields)
    {
        super.serializeFields(fields);
        fields.addProperty("restricted", restricted);
    }

    @Override
    protected String convertFromJson(JsonElement item)
    {
        return item.getAsString();
    }

    @Override
    protected void deserializeFields(JsonObject fields)
    {
        super.deserializeFields(fields);
        restricted = fields.get("restricted").getAsBoolean();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (other.getClass() != getClass())
            return false;
        LegalityFilter o = (LegalityFilter)other;
        return o.contain == contain && o.selected.equals(selected) && o.restricted == restricted;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(contain, multifunction(), selected, restricted);
    }
}