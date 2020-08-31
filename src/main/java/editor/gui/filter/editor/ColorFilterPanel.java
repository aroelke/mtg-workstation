package editor.gui.filter.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.ManaType;
import editor.database.symbol.ColorSymbol;
import editor.database.symbol.StaticSymbol;
import editor.filter.Filter;
import editor.filter.leaf.ColorFilter;
import editor.filter.leaf.FilterLeaf;
import editor.gui.generic.ComboBoxPanel;
import editor.gui.generic.ComponentUtils;
import editor.util.Containment;

/**
 * This class represents a panel corresponding to a filter that groups
 * cards by a color characteristic.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ColorFilterPanel extends FilterEditorPanel<ColorFilter>
{
    /**
     * Map of colors onto their corresponding check boxes.
     */
    private Map<ManaType, JCheckBox> colors;
    /**
     * Combo box showing the containment options.
     */
    private ComboBoxPanel<Containment> contain;
    /**
     * Check box indicating that only multicolored cards should be matched.
     */
    private JCheckBox multiCheckBox;
    private JCheckBox colorlessBox;
    /**
     * Type of the filter being edited.
     */
    private CardAttribute type;

    /**
     * Create a new ColorFilterPanel.
     */
    public ColorFilterPanel()
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Containment options
        add(contain = new ComboBoxPanel<>(Containment.values()));

        // Check box for filtering for colorless
        colorlessBox = new JCheckBox();

        // Check boxes for selecting colors
        colors = new HashMap<>();
        for (ManaType color : ManaType.colors())
        {
            JCheckBox box = new JCheckBox();
            colors.put(color, box);
            add(box);
            add(new JLabel(ColorSymbol.SYMBOLS.get(color).getIcon(13)));
            box.addActionListener((e) -> {
                if (box.isSelected())
                    colorlessBox.setSelected(false);
            });
        }
        add(Box.createHorizontalStrut(4));
        add(ComponentUtils.createHorizontalSeparator(4, contain.getPreferredSize().height));

        // Check box for multicolored cards
        add(multiCheckBox = new JCheckBox());
        add(new JLabel(StaticSymbol.SYMBOLS.get("M").getIcon(13)));
        multiCheckBox.addActionListener((e) -> {
            if (multiCheckBox.isSelected())
                colorlessBox.setSelected(false);
        });

        // Actually add the colorless box here
        colorlessBox.setSelected(true);
        add(colorlessBox);
        add (new JLabel(ColorSymbol.SYMBOLS.get(ManaType.COLORLESS).getIcon(13)));
        colorlessBox.addActionListener((e) -> {
            if (colorlessBox.isSelected())
            {
                for (JCheckBox box : colors.values())
                    box.setSelected(false);
                multiCheckBox.setSelected(false);
            }
        });

        add(Box.createHorizontalStrut(2));
    }

    /**
     * Create a new ColorFilterPanel with initial contents obtained
     * from the given filter.
     *
     * @param f filter to get the contents from
     */
    public ColorFilterPanel(ColorFilter f)
    {
        this();
        setContents(f);
    }

    @Override
    public Filter filter()
    {
        ColorFilter filter = (ColorFilter)CardAttribute.createFilter(type);
        filter.contain = contain.getSelectedItem();
        filter.colors.addAll(colors.keySet().stream().filter((c) -> colors.get(c).isSelected()).collect(Collectors.toSet()));
        filter.multicolored = multiCheckBox.isSelected();
        return filter;
    }

    @Override
    public void setContents(ColorFilter filter)
    {
        type = filter.type();
        contain.setSelectedItem(filter.contain);
        for (ManaType color : ManaType.colors())
            colors.get(color).setSelected(filter.colors.contains(color));
        multiCheckBox.setSelected(filter.multicolored);
        colorlessBox.setSelected(!filter.multicolored && filter.colors.isEmpty());
    }

    @Override
    public void setContents(FilterLeaf<?> filter) throws IllegalArgumentException
    {
        if (filter instanceof ColorFilter)
            setContents((ColorFilter)filter);
        else
            throw new IllegalArgumentException("Illegal color filter " + filter.type());
    }
}
