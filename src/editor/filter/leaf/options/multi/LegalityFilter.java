package editor.filter.leaf.options.multi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import editor.database.card.Card;
import editor.database.characteristics.Legality;
import editor.filter.Filter;
import editor.filter.FilterFactory;

/**
 * This class represents a filter that groups cards by format legality.
 *
 * @author Alec Roelke
 */
public class LegalityFilter extends MultiOptionsFilter<String>
{
    /**
     * List of all formats cards can be played in.
     */
    public static String[] formatList = {};

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
        super(FilterFactory.FORMAT_LEGALITY, Card::legalIn);
        restricted = false;
    }

    @Override
    public String convertFromString(String str)
    {
        return str;
    }

    @Override
    public Filter copy()
    {
        LegalityFilter filter = (LegalityFilter)FilterFactory.createFilter(FilterFactory.FORMAT_LEGALITY);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        filter.restricted = restricted;
        return filter;
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

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        restricted = in.readBoolean();
    }

    /**
     * {@inheritDoc}
     * Filter cards not only according to the selection of formats, but also
     * optionally check if they are restricted in those formats.
     */
    @Override
    public boolean test(Card c)
    {
        if (!super.test(c))
            return false;
        else if (restricted)
        {
            Collection<String> formats = new ArrayList<>(c.legalIn());
            formats.retainAll(selected);
            for (String format : formats)
                if (c.legality().get(format) != Legality.RESTRICTED)
                    return false;
            return true;
        }
        else
            return true;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        super.writeExternal(out);
        out.writeBoolean(restricted);
    }
}
