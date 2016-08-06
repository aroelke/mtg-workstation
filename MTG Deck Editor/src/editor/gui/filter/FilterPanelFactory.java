package editor.gui.filter;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.Rarity;
import editor.filter.FilterFactory;
import editor.filter.FilterType;
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

public interface FilterPanelFactory
{
	public static FilterEditorPanel<?> createFilterPanel(FilterLeaf<?> filter)
	{
		switch (filter.type)
		{
		case NAME: case RULES_TEXT: case FLAVOR_TEXT: case ARTIST:
			return new TextFilterPanel((TextFilter)filter);
		case LAYOUT:
			return new OptionsFilterPanel<CardLayout>((LayoutFilter)filter, CardLayout.values());
		case MANA_COST:
			return new ManaCostFilterPanel((ManaCostFilter)filter);
		case CMC: case LOYALTY: case CARD_NUMBER:
			return new NumberFilterPanel((NumberFilter)filter);
		case COLOR: case COLOR_IDENTITY:
			return new ColorFilterPanel((ColorFilter)filter);
		case TYPE_LINE:
			return new TypeLineFilterPanel((TypeLineFilter)filter);
		case SUPERTYPE:
			return new OptionsFilterPanel<String>((SupertypeFilter)filter, SupertypeFilter.supertypeList);
		case TYPE:
			return new OptionsFilterPanel<String>((CardTypeFilter)filter, CardTypeFilter.typeList);
		case SUBTYPE:
			return new OptionsFilterPanel<String>((SubtypeFilter)filter, SubtypeFilter.subtypeList);
		case EXPANSION:
			return new OptionsFilterPanel<Expansion>((ExpansionFilter)filter, Expansion.expansions);
		case BLOCK:
			return new OptionsFilterPanel<String>((BlockFilter)filter, Expansion.blocks);
		case RARITY:
			return new OptionsFilterPanel<Rarity>((RarityFilter)filter, Rarity.values());
		case POWER: case TOUGHNESS:
			return new VariableNumberFilterPanel((VariableNumberFilter)filter);
		case FORMAT_LEGALITY:
			return new LegalityFilterPanel((LegalityFilter)filter);
		case TAGS:
			return new OptionsFilterPanel<String>((TagsFilter)filter, Card.tags().stream().sorted().toArray(String[]::new));
		case DEFAULTS:
			return new DefaultsFilterPanel();
		case NONE:
			return new BinaryFilterPanel(false);
		case ALL:
			return new BinaryFilterPanel(true);
		default:
			return null;
		}
	}
	
	public static FilterEditorPanel<?> createFilterPanel(FilterType type)
	{
		if (type == FilterType.DEFAULTS)
			return new DefaultsFilterPanel();
		else
			return createFilterPanel(FilterFactory.createFilter(type));
	}
}
