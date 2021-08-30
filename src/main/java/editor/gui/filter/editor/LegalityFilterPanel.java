package editor.gui.filter.editor;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;

import scala.jdk.javaapi.CollectionConverters;

import editor.database.FormatConstraints;
import editor.database.attributes.CardAttribute;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.options.OptionsFilter;
import editor.filter.leaf.options.multi.LegalityFilter;

/**
 * This class represents a panel corresponding to a filter that groups
 * cards by format legality and whether they are restricted or not.
 *
 * @author Alec Roelke
 */
public class LegalityFilterPanel extends OptionsFilterPanel<String>
{
    /**
     * Check box indicating whether or not restricted cards should
     * be filtered.
     */
    private JCheckBox restrictedBox;

    /**
     * Create a new LegalityFilterPanel.
     */
    public LegalityFilterPanel()
    {
        super(CardAttribute.LEGAL_IN, CollectionConverters.asJava(FormatConstraints.FORMAT_NAMES()).toArray(String[]::new));
        add(restrictedBox = new JCheckBox("Restricted"), BorderLayout.EAST);
    }

    /**
     * Create a new LegalityFilterPanel using the given LegalityFilter
     * to initialize its fields.
     *
     * @param f filter to use for initialization
     */
    public LegalityFilterPanel(LegalityFilter f)
    {
        this();
        setContents(f);
    }

    @Override
    public Filter filter()
    {
        LegalityFilter filter = (LegalityFilter)super.filter();
        filter.restricted = restrictedBox.isSelected();
        return filter;
    }

    @Override
    public void setContents(FilterLeaf<?> filter) throws IllegalArgumentException
    {
        if (filter instanceof LegalityFilter f)
            setContents(f);
        else
            throw new IllegalArgumentException("Illegal legality filter " + filter.type());

    }

    /**
     * Set the fields of this LegalityFilterPanel to the given LegalityFilter's contents
     *
     * @param filter Filter to use for setting contents
     */
    public void setContents(LegalityFilter filter)
    {
        super.setContents(filter);
        restrictedBox.setSelected(filter.restricted);
    }

    @Override
    public void setContents(OptionsFilter<String> filter) throws IllegalArgumentException
    {
        if (filter.type() != CardAttribute.LEGAL_IN)
            throw new IllegalArgumentException("Illegal legality filter type " + filter.type());
        else
            setContents((LegalityFilter)filter);
    }
}
