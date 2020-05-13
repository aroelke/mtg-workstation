package editor.serialization.legacy;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.AbstractMap;
import java.util.Map;

import editor.database.card.CardLayout;
import editor.database.characteristics.CardAttribute;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.Rarity;
import editor.filter.Filter;
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
    public final Map<CardAttribute, String> CODES = Map.ofEntries(
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.CARD_TYPE, "cardtype"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.ANY, "*"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.LEGAL_IN, "legal"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.TYPE_LINE, "type"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.BLOCK, "b"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.EXPANSION_NAME, "x"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.LAYOUT, "L"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.MANA_COST, "m"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.NAME, "n"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.NONE, "0"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.RARITY, "r"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.SUBTYPE, "sub"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.SUPERTYPE, "super"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.TAGS, "tag"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.LOYALTY, "l"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.ARTIST, "a"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.CARD_NUMBER, "#"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.CMC, "cmc"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.COLORS, "c"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.COLOR_IDENTITY, "ci"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.FLAVOR_TEXT, "f"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.POWER, "p"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.PRINTED_TEXT, "ptext"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.PRINTED_TYPES, "ptypes"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.RULES_TEXT, "o"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.TOUGHNESS, "t"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.GROUP, "group"),
        new AbstractMap.SimpleImmutableEntry<>(CardAttribute.DEFAULTS, "")
    );

    public static Filter readExternal(ObjectInput in) throws ClassNotFoundException, IOException
    {
        CardAttribute type = null;
        String code = in.readUTF();
        for (CardAttribute attribute: CardAttribute.values())
        {
            if (code.equals(CODES.get(attribute)))
            {
                type = attribute;
                break;
            }
        }
        if (type == CardAttribute.GROUP)
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
            case COLORS:
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
            case EXPANSION_NAME:
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
            case LEGAL_IN:
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