package editor.gui.filter;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.Expansion;
import editor.database.attributes.Rarity;
import editor.database.card.Card;
import editor.database.card.CardLayout;
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
        return switch (filter.type()) {
            case NAME, RULES_TEXT, FLAVOR_TEXT, PRINTED_TEXT, ARTIST, PRINTED_TYPES -> new TextFilterPanel((TextFilter)filter);
            case POWER, TOUGHNESS, LOYALTY -> new VariableNumberFilterPanel((VariableNumberFilter)filter);
            case MANA_VALUE, CARD_NUMBER -> new NumberFilterPanel((NumberFilter)filter);
            case COLORS, COLOR_IDENTITY -> new ColorFilterPanel((ColorFilter)filter);
            case LAYOUT          -> new OptionsFilterPanel<>((LayoutFilter)filter, CardLayout.values());
            case MANA_COST       -> new ManaCostFilterPanel((ManaCostFilter)filter);
            case TYPE_LINE       -> new TypeLineFilterPanel((TypeLineFilter)filter);
            case SUPERTYPE       -> new OptionsFilterPanel<>((SupertypeFilter)filter, SupertypeFilter.supertypeList);
            case CARD_TYPE       -> new OptionsFilterPanel<>((CardTypeFilter)filter, CardTypeFilter.typeList);
            case SUBTYPE         -> new OptionsFilterPanel<>((SubtypeFilter)filter, SubtypeFilter.subtypeList);
            case EXPANSION  -> new OptionsFilterPanel<>((ExpansionFilter)filter, Expansion.expansions);
            case BLOCK           -> new OptionsFilterPanel<>((BlockFilter)filter, Expansion.blocks);
            case RARITY          -> new OptionsFilterPanel<>((RarityFilter)filter, Rarity.values());
            case LEGAL_IN        -> new LegalityFilterPanel((LegalityFilter)filter);
            case TAGS            -> new OptionsFilterPanel<>((TagsFilter)filter, Card.tags().stream().sorted().toArray(String[]::new));
            case DEFAULTS        -> new DefaultsFilterPanel();
            case NONE            -> new BinaryFilterPanel(false);
            case ANY             -> new BinaryFilterPanel(true);
            default -> throw new IllegalArgumentException("No panel exists for filters of type " + filter.toString());
        };
    }

    /**
     * Create a new FilterEditorPanel with default values.
     *
     * @param type type of filter to create a panel for
     * @return a new #FilterEditorPanel corresponding to the given type with
     * its fields filled out with default values.
     */
    public static FilterEditorPanel<?> createFilterPanel(CardAttribute type)
    {
        if (type.equals(CardAttribute.DEFAULTS))
            return new DefaultsFilterPanel();
        else
            return createFilterPanel(type.get());
    }
}
