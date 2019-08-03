package editor.serialization.legacy;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.AbstractMap;
import java.util.Map;

import editor.collection.deck.CategorySpec;
import editor.database.card.CardLayout;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.Rarity;
import editor.filter.Filter;
import editor.filter.FilterAttribute;
import editor.filter.FilterGroup;
import editor.filter.leaf.BinaryFilter;
import editor.filter.leaf.ColorFilter;
import editor.filter.leaf.ManaCostFilter;
import editor.filter.leaf.NumberFilter;
import editor.filter.leaf.TextFilter;
import editor.filter.leaf.TypeLineFilter;
import editor.filter.leaf.VariableNumberFilter;
import editor.filter.leaf.options.multi.LegalityFilter;
import editor.filter.leaf.options.multi.MultiOptionsFilter;
import editor.filter.leaf.options.single.ExpansionFilter;
import editor.filter.leaf.options.single.LayoutFilter;
import editor.filter.leaf.options.single.RarityFilter;
import editor.util.Comparison;
import editor.util.Containment;

public interface FilterDeserializer
{
    public final Map<FilterAttribute, String> CODES = Map.ofEntries(
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.CARD_TYPE, "cardtype"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.ANY, "*"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.FORMAT_LEGALITY, "legal"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.TYPE_LINE, "type"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.BLOCK, "b"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.EXPANSION, "x"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.LAYOUT, "L"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.MANA_COST, "m"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.NAME, "n"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.NONE, "0"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.RARITY, "r"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.SUBTYPE, "sub"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.SUPERTYPE, "super"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.TAGS, "tag"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.LOYALTY, "l"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.ARTIST, "a"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.CARD_NUMBER, "#"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.CMC, "cmc"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.COLOR, "c"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.COLOR_IDENTITY, "ci"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.FLAVOR_TEXT, "f"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.POWER, "p"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.PRINTED_TEXT, "ptext"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.PRINTED_TYPES, "ptypes"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.RULES_TEXT, "o"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.TOUGHNESS, "t"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.GROUP, "group"),
        new AbstractMap.SimpleImmutableEntry<>(FilterAttribute.DEFAULTS, "")
    );

    public static Filter readExternal(ObjectInput in) throws ClassNotFoundException, IOException
    {
        FilterAttribute type = null;
        String code = in.readUTF();
        for (FilterAttribute attribute: FilterAttribute.values())
        {
            if (code.equals(CODES.get(attribute)))
            {
                type = attribute;
                break;
            }
        }
        if (type == FilterAttribute.GROUP)
        {
            FilterGroup filter = new FilterGroup();
            filter.mode = (FilterGroup.Mode)in.readObject();
            int n = in.readInt();
            for (int i = 0; i < n; i++)
                filter.addChild(readExternal(in));
            return filter;
        }
        else
        {
            int n = 0;
            switch (type)
            {
            case NAME:
            case RULES_TEXT:
            case FLAVOR_TEXT:
            case PRINTED_TEXT:
            case ARTIST:
            case PRINTED_TYPES:
                TextFilter text = (TextFilter)type.get();
                text.contain = (Containment)in.readObject();
                text.regex = in.readBoolean();
                text.text = in.readUTF();
                return text;
            case LAYOUT:
                LayoutFilter layout = (LayoutFilter)type.get();
                layout.contain = (Containment)in.readObject();
                n = in.readInt();
                for (int i = 0; i < n; i++)
                {
                    in.readBoolean(); // Should be true, since CardLayout is serializable
                    layout.selected.add((CardLayout)in.readObject());
                }
                return layout;
            case MANA_COST:
                ManaCostFilter mana = (ManaCostFilter)type.get();
                mana.contain = (Containment)in.readObject();
                mana.cost = ManaCost.parseManaCost(in.readUTF());
                return mana;
            case CMC:
            case CARD_NUMBER:
                NumberFilter number = (NumberFilter)type.get();
                number.operand = in.readDouble();
                number.operation = (Comparison)in.readObject();
                return number;
            case COLOR:
            case COLOR_IDENTITY:
                ColorFilter color = (ColorFilter)type.get();
                color.contain = (Containment)in.readObject();
                n = in.readInt();
                for (int i = 0; i < n; i++)
                    color.colors.add((ManaType)in.readObject());
                color.multicolored = in.readBoolean();
                return color;
            case TYPE_LINE:
                TypeLineFilter line = (TypeLineFilter)type.get();
                line.contain = (Containment)in.readObject();
                line.line = in.readUTF();
                return line;
            case SUPERTYPE:
            case CARD_TYPE:
            case SUBTYPE:
            case BLOCK:
            case TAGS:
                @SuppressWarnings("unchecked")
                MultiOptionsFilter<String> string = (MultiOptionsFilter<String>)type.get();
                string.contain = (Containment)in.readObject();
                n = in.readInt();
                for (int i = 0; i < n; i++)
                {
                    in.readBoolean(); // Should be true since strings are serializable
                    string.selected.add((String)in.readObject());
                }
                return string;
            case EXPANSION:
                ExpansionFilter expansion = (ExpansionFilter)type.get();
                expansion.contain = (Containment)in.readObject();
                n = in.readInt();
                for (int i = 0; i < n; i++)
                {
                    in.readBoolean(); // Should be false since Expansions are not serializable
                    String name = in.readUTF();
                    for (Expansion e : Expansion.expansions)
                    {
                        if (name.equalsIgnoreCase(e.name))
                        {
                            expansion.selected.add(e);
                            break;
                        }
                    }
                }
                return expansion;
            case RARITY:
                RarityFilter rarity = (RarityFilter)type.get();
                rarity.contain = (Containment)in.readObject();
                n = in.readInt();
                for (int i = 0; i < n; i++)
                {
                    in.readBoolean(); // Should be false, since Raritys are not serializable
                    rarity.selected.add(Rarity.parseRarity(in.readUTF()));
                }
                return rarity;
            case POWER:
            case TOUGHNESS:
            case LOYALTY:
                VariableNumberFilter stat = (VariableNumberFilter)type.get();
                stat.operand = in.readDouble();
                stat.operation = (Comparison)in.readObject();
                stat.varies = in.readBoolean();
                return stat;
            case FORMAT_LEGALITY:
                LegalityFilter legality = (LegalityFilter)type.get();
                legality.contain = (Containment)in.readObject();
                n = in.readInt();
                for (int i = 0; i < n; i++)
                {
                    in.readBoolean(); // Should be true since strings are serializable
                    legality.selected.add((String)in.readObject());
                }
                legality.restricted = in.readBoolean();
                return legality;
            case NONE:
            case ANY:
                return new BinaryFilter(in.readBoolean());
            case DEFAULTS: // Shouldn't actually show up
            default:
                return null;
            }
        }
    }    
}