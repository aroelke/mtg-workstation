package editor.gui.filter;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.Rarity;
import editor.filter.FilterFactory;
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
 * This class is a factory for creating new FitlerEditorPanels.
 * 
 * @author Alec Roelke
 */
public interface FilterPanelFactory
{
	/**
	 * Create a new FilterEditorPanel and fill it out with values from the given
	 * Filter.
	 * 
	 * @param filter Filter to create a new panel from
	 * @return A FilterEditorPanel whose type is determined by the given filter and
	 * fill out its field using the values of the given filter.
	 */
	public static FilterEditorPanel<?> createFilterPanel(FilterLeaf<?> filter)
	{
		switch (filter.type)
		{
		case FilterFactory.NAME: case FilterFactory.RULES_TEXT: case FilterFactory.FLAVOR_TEXT: case FilterFactory.ARTIST:
			return new TextFilterPanel((TextFilter)filter);
		case FilterFactory.LAYOUT:
			return new OptionsFilterPanel<CardLayout>((LayoutFilter)filter, CardLayout.values());
		case FilterFactory.MANA_COST:
			return new ManaCostFilterPanel((ManaCostFilter)filter);
		case FilterFactory.CMC: case FilterFactory.LOYALTY: case FilterFactory.CARD_NUMBER:
			return new NumberFilterPanel((NumberFilter)filter);
		case FilterFactory.COLOR: case FilterFactory.COLOR_IDENTITY:
			return new ColorFilterPanel((ColorFilter)filter);
		case FilterFactory.TYPE_LINE:
			return new TypeLineFilterPanel((TypeLineFilter)filter);
		case FilterFactory.SUPERTYPE:
			return new OptionsFilterPanel<String>((SupertypeFilter)filter, SupertypeFilter.supertypeList);
		case FilterFactory.TYPE:
			return new OptionsFilterPanel<String>((CardTypeFilter)filter, CardTypeFilter.typeList);
		case FilterFactory.SUBTYPE:
			return new OptionsFilterPanel<String>((SubtypeFilter)filter, SubtypeFilter.subtypeList);
		case FilterFactory.EXPANSION:
			return new OptionsFilterPanel<Expansion>((ExpansionFilter)filter, Expansion.expansions);
		case FilterFactory.BLOCK:
			return new OptionsFilterPanel<String>((BlockFilter)filter, Expansion.blocks);
		case FilterFactory.RARITY:
			return new OptionsFilterPanel<Rarity>((RarityFilter)filter, Rarity.values());
		case FilterFactory.POWER: case FilterFactory.TOUGHNESS:
			return new VariableNumberFilterPanel((VariableNumberFilter)filter);
		case FilterFactory.FORMAT_LEGALITY:
			return new LegalityFilterPanel((LegalityFilter)filter);
		case FilterFactory.TAGS:
			return new OptionsFilterPanel<String>((TagsFilter)filter, Card.tags().stream().sorted().toArray(String[]::new));
		case FilterFactory.DEFAULTS:
			return new DefaultsFilterPanel();
		case FilterFactory.NONE:
			return new BinaryFilterPanel(false);
		case FilterFactory.ALL:
			return new BinaryFilterPanel(true);
		default:
			return null;
		}
	}
	
	/**
	 * Create a new FilterEditorPanel with default values.
	 * 
	 * @param type Type of filter to create a panel for
	 * @return A new FilterEditorPanel corresponding to the given type with
	 * its fields filled out with default values.
	 */
	public static FilterEditorPanel<?> createFilterPanel(String type)
	{
		if (type.equals(FilterFactory.DEFAULTS))
			return new DefaultsFilterPanel();
		else
			return createFilterPanel(FilterFactory.createFilter(type));
	}
}
