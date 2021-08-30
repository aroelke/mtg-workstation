package editor.serialization.legacy;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.Expansion;
import editor.database.attributes.ManaCost;
import editor.database.attributes.ManaType;
import editor.database.attributes.Rarity;
import editor.database.card.CardLayout;
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
    public final Map<String, CardAttribute> CODES = Map.ofEntries(
        new SimpleImmutableEntry<>("cardtype", CardAttribute.CARD_TYPE),
        new SimpleImmutableEntry<>("*", CardAttribute.ANY),
        new SimpleImmutableEntry<>("legal", CardAttribute.LEGAL_IN),
        new SimpleImmutableEntry<>("type", CardAttribute.TYPE_LINE),
        new SimpleImmutableEntry<>("b", CardAttribute.BLOCK),
        new SimpleImmutableEntry<>("x", CardAttribute.EXPANSION),
        new SimpleImmutableEntry<>("L", CardAttribute.LAYOUT),
        new SimpleImmutableEntry<>("m", CardAttribute.MANA_COST),
        new SimpleImmutableEntry<>("n", CardAttribute.NAME),
        new SimpleImmutableEntry<>("0", CardAttribute.NONE),
        new SimpleImmutableEntry<>("r", CardAttribute.RARITY),
        new SimpleImmutableEntry<>("sub", CardAttribute.SUBTYPE),
        new SimpleImmutableEntry<>("super", CardAttribute.SUPERTYPE),
        new SimpleImmutableEntry<>("tag", CardAttribute.TAGS),
        new SimpleImmutableEntry<>("l", CardAttribute.LOYALTY),
        new SimpleImmutableEntry<>("a", CardAttribute.ARTIST),
        new SimpleImmutableEntry<>("#", CardAttribute.CARD_NUMBER),
        new SimpleImmutableEntry<>("cmc", CardAttribute.MANA_VALUE),
        new SimpleImmutableEntry<>("c", CardAttribute.COLORS),
        new SimpleImmutableEntry<>("ci", CardAttribute.COLOR_IDENTITY),
        new SimpleImmutableEntry<>("f", CardAttribute.FLAVOR_TEXT),
        new SimpleImmutableEntry<>("p", CardAttribute.POWER),
        new SimpleImmutableEntry<>("ptext", CardAttribute.PRINTED_TEXT),
        new SimpleImmutableEntry<>("ptypes", CardAttribute.PRINTED_TYPES),
        new SimpleImmutableEntry<>("o", CardAttribute.RULES_TEXT),
        new SimpleImmutableEntry<>("t", CardAttribute.TOUGHNESS),
        new SimpleImmutableEntry<>("group", CardAttribute.GROUP),
        new SimpleImmutableEntry<>("", CardAttribute.DEFAULTS)
    );

    public static Filter readExternal(ObjectInput in) throws ClassNotFoundException, IOException
    {
        CardAttribute type = CODES.get(in.readUTF());
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
            case MANA_VALUE:
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
            case EXPANSION:
                ExpansionFilter expansion = (ExpansionFilter)type.get();
                expansion.contain = (Containment)in.readObject();
                n = in.readInt();
                for (int i = 0; i < n; i++)
                {
                    in.readBoolean(); // Should be false since Expansions are not serializable
                    String name = in.readUTF();
                    for (Expansion e : Expansion.expansions())
                    {
                        if (name.equalsIgnoreCase(e.name()))
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