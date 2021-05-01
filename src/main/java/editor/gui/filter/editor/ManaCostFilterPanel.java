package editor.gui.filter.editor;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import editor.database.attributes.ManaCost;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.ManaCostFilter;
import editor.gui.generic.ComboBoxPanel;
import editor.gui.generic.DocumentChangeListener;
import editor.util.Containment;

/**
 * This class represents a panel corresponding to a filter that groups
 * cards by mana cost.
 *
 * @author Alec Roelke
 */
public class ManaCostFilterPanel extends FilterEditorPanel<ManaCostFilter>
{
    /** Color to display if the entered cost is valid (i.e. can be parsed according to {@link ManaCost#tryParseManaCost(String)}). */
    public static final Color VALID = Color.WHITE;
    /** Color to display if the entered cost is invalid (i.e. can't be parsed according to {@link ManaCost#tryParseManaCost(String)}). */
    public static final Color INVALID = Color.PINK;

    /**
     * Combo box indicating containment.
     */
    private ComboBoxPanel<Containment> contain;
    /**
     * Text field for entering the text version of the mana cost.
     */
    private JTextField cost;

    /**
     * Create a new ManaCostFilterPanel.
     */
    public ManaCostFilterPanel()
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        contain = new ComboBoxPanel<>(Containment.values());
        add(contain);
        cost = new JTextField();
        cost.getDocument().addDocumentListener(new DocumentChangeListener()
        {
            @Override
            public void update(DocumentEvent e)
            {
                cost.setBackground(ManaCost.tryParseManaCost(cost.getText()).map((c) -> VALID).orElse(INVALID));
            }
        });
        add(cost);
    }

    /**
     * Create a new ManaCostFilterPanel using the given ManaCostFilter
     * to set the contents of its fields.
     *
     * @param f filter to use for initialization
     */
    public ManaCostFilterPanel(ManaCostFilter f)
    {
        this();
        setContents(f);
    }

    @Override
    public Filter filter()
    {
        ManaCostFilter filter = new ManaCostFilter();
        filter.contain = contain.getSelectedItem();
        filter.cost = ManaCost.tryParseManaCost(cost.getText()).orElse(new ManaCost());
        return filter;
    }

    @Override
    public void setContents(ManaCostFilter filter)
    {
        contain.setSelectedItem(filter.contain);
        cost.setText(filter.cost.toString());
    }

    @Override
    public void setContents(FilterLeaf<?> filter) throws IllegalArgumentException
    {
        if (filter instanceof ManaCostFilter f)
            setContents(f);
        else
            throw new IllegalArgumentException("Illegal mana cost filter " + filter.type());
    }
}
