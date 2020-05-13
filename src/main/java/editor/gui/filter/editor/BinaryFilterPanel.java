package editor.gui.filter.editor;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import editor.database.characteristics.CardAttribute;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents a panel that corresponds to a filter that
 * returns <code>true</code> or <code>false</code> for all cards.
 * There are no fields to edit.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class BinaryFilterPanel extends FilterEditorPanel<FilterLeaf<?>>
{
    /**
     * String to display for letting all cards through the filter.
     */
    private static final String ALL = "This clause will match every card.";
    /**
     * String to display for letting no cards through the filter.
     */
    private static final String NONE = "This clause will not match any card.";

    /**
     * Whether or not to let cards through the filter.
     */
    private boolean through;

    /**
     * Create a new BinaryFilterPanel.
     *
     * @param let whether or not cards should pass through the filter
     */
    public BinaryFilterPanel(boolean let)
    {
        super();
        setLayout(new GridLayout(1, 1));
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        JLabel label = new JLabel((through = let) ? ALL : NONE);
        add(label);
    }

    @Override
    public Filter filter()
    {
        return CardAttribute.createFilter(through ? CardAttribute.ANY : CardAttribute.NONE);
    }

    /**
     * {@inheritDoc}
     * There are no contents to set, so this doesn't do anything.
     */
    @Override
    public void setContents(FilterLeaf<?> filter)
    {
    }
}
