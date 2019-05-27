package editor.gui.filter;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.Rarity;
import editor.filter.FilterAttribute;
import editor.filter.leaf.ColorFilter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.ManaCostFilter;
import editor.filter.leaf.NumberFilter;
import editor.filter.leaf.TextFilter;
import editor.filter.leaf.TypeLineFilter;
import editor.filter.leaf.VariableNumberFilter;
import editor.filter.leaf.options.multi.CardTypeFilter;
import editor.filter.leaf.options.multi.LegalityFilter;
import editor.filter.leaf.options.multi.SubtypeFilter;
import editor.filter.leaf.options.multi.SupertypeFilter;
import editor.filter.leaf.options.multi.TagsFilter;
import editor.filter.leaf.options.single.BlockFilter;
import editor.filter.leaf.options.single.ExpansionFilter;
import editor.filter.leaf.options.single.LayoutFilter;
import editor.filter.leaf.options.single.RarityFilter;
import editor.gui.filter.editor.BinaryFilterPanel;
import editor.gui.filter.editor.ColorFilterPanel;
import editor.gui.filter.editor.DefaultsFilterPanel;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.gui.filter.editor.LegalityFilterPanel;
import editor.gui.filter.editor.ManaCostFilterPanel;
import editor.gui.filter.editor.NumberFilterPanel;
import editor.gui.filter.editor.OptionsFilterPanel;
import editor.gui.filter.editor.TextFilterPanel;
import editor.gui.filter.editor.TypeLineFilterPanel;
import editor.gui.filter.editor.VariableNumberFilterPanel;

/**
 * This class is a factory for creating new {@link FilterEditorPanel}s.
 *
 * @author Alec Roelke
 */
public interface FilterPanelFactory
{
    /**
     * Create a new FilterEditorPanel and fill it out with values from the given
     * Filter.
     *
     * @param filter filter to create a new panel from
     * @return a #FilterEditorPanel whose type is determined by the given filter and
     * fill out its field using the values of the given filter.
     */
    static FilterEditorPanel<?> createFilterPanel(FilterLeaf<?> filter)
    {
        switch (filter.type())
        {
        case NAME:
        case RULES_TEXT:
        case FLAVOR_TEXT:
        case PRINTED_TEXT:
        case ARTIST:
        case PRINTED_TYPES:
            return new TextFilterPanel((TextFilter)filter);
        case LAYOUT:
            return new OptionsFilterPanel<>((LayoutFilter)filter, CardLayout.values());
        case MANA_COST:
            return new ManaCostFilterPanel((ManaCostFilter)filter);
        case CMC:
        case CARD_NUMBER:
            return new NumberFilterPanel((NumberFilter)filter);
        case COLOR:
        case COLOR_IDENTITY:
            return new ColorFilterPanel((ColorFilter)filter);
        case TYPE_LINE:
            return new TypeLineFilterPanel((TypeLineFilter)filter);
        case SUPERTYPE:
            return new OptionsFilterPanel<>((SupertypeFilter)filter, SupertypeFilter.supertypeList);
        case CARD_TYPE:
            return new OptionsFilterPanel<>((CardTypeFilter)filter, CardTypeFilter.typeList);
        case SUBTYPE:
            return new OptionsFilterPanel<>((SubtypeFilter)filter, SubtypeFilter.subtypeList);
        case EXPANSION:
            return new OptionsFilterPanel<>((ExpansionFilter)filter, Expansion.expansions);
        case BLOCK:
            return new OptionsFilterPanel<>((BlockFilter)filter, Expansion.blocks);
        case RARITY:
            return new OptionsFilterPanel<>((RarityFilter)filter, Rarity.values());
        case POWER:
        case TOUGHNESS:
        case LOYALTY:
            return new VariableNumberFilterPanel((VariableNumberFilter)filter);
        case FORMAT_LEGALITY:
            return new LegalityFilterPanel((LegalityFilter)filter);
        case TAGS:
            return new OptionsFilterPanel<>((TagsFilter)filter, Card.tags().stream().sorted().toArray(String[]::new));
        case DEFAULTS:
            return new DefaultsFilterPanel();
        case NONE:
            return new BinaryFilterPanel(false);
        case ANY:
            return new BinaryFilterPanel(true);
        default:
            return new BinaryFilterPanel(false);
        }
    }

    /**
     * Create a new FilterEditorPanel with default values.
     *
     * @param type type of filter to create a panel for
     * @return a new #FilterEditorPanel corresponding to the given type with
     * its fields filled out with default values.
     */
    static FilterEditorPanel<?> createFilterPanel(FilterAttribute type)
    {
        if (type.equals(FilterAttribute.DEFAULTS))
            return new DefaultsFilterPanel();
        else
            return createFilterPanel(type.create());
    }
}
