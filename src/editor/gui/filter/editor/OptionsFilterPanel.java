package editor.gui.filter.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;

import editor.filter.Filter;
import editor.filter.FilterAttribute;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.options.OptionsFilter;
import editor.gui.generic.ButtonScrollPane;
import editor.gui.generic.ComboBoxPanel;
import editor.gui.generic.ScrollablePanel;
import editor.util.Containment;
import editor.util.MouseListenerFactory;
import editor.util.PopupMenuListenerFactory;
import editor.util.UnicodeSymbols;

/**
 * This class represents a panel that corresponds to a filter that groups
 * cards according to a characteristic that takes on distinct values.  Unlike
 * other filter panels, which can be switched among different types of filters
 * as long as they are the same class, OptionsFilterPanel cannot.
 *
 * @param <T> Type that the options presented have
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class OptionsFilterPanel<T> extends FilterEditorPanel<OptionsFilter<T>>
{
    /**
     * Maximum width for combo boxes.  Sizes of the drop-down menus remain
     * unaffected.
     */
    private static final int MAX_COMBO_WIDTH = 100;

    /**
     * Set containment combo box.
     */
    private ComboBoxPanel<Containment> contain;
    /**
     * List of options that are available to choose from.
     */
    private T[] options;
    /**
     * List of boxes displaying the currently-selected options.
     */
    private List<JComboBox<T>> optionsBoxes;
    /**
     * Panel displaying the combo boxes to be used to choose
     * options.
     */
    private ScrollablePanel optionsPanel;
    /**
     * Type of filter this OptionsFilterPanel edits.
     */
    private FilterAttribute type;

    /**
     * Create a new OptionsFilterPanel using the given filter to initialize its
     * fields and the given array to specify the set of options to choose from.
     *
     * @param f filter to use for initialization
     * @param t list of options to choose from
     */
    public OptionsFilterPanel(OptionsFilter<T> f, T[] t)
    {
        this(f.type(), t);
        setContents(f);
    }

    /**
     * Create a new OptionsFilterPanel.
     *
     * @param t type of the new OptionsFilterPanel
     * @param o list of options to choose from
     */
    public OptionsFilterPanel(FilterAttribute t, T[] o)
    {
        super();
        setLayout(new BorderLayout());

        type = t;
        options = o;
        optionsBoxes = new ArrayList<>();

        contain = new ComboBoxPanel<>(Containment.values());
        add(contain, BorderLayout.WEST);

        optionsPanel = new ScrollablePanel(ScrollablePanel.TRACK_HEIGHT);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
        ButtonScrollPane optionsPane = new ButtonScrollPane(optionsPanel);
        optionsPane.setBorder(BorderFactory.createEmptyBorder());
        add(optionsPane, BorderLayout.CENTER);
    }

    /**
     * Add a new combo box for an additional option.
     *
     * @param value initial value of the new combo box.
     */
    private void addItem(T value)
    {
        JPanel boxPanel = new JPanel(new BorderLayout());
        var box = new JComboBox<>(options);
        box.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            if (options.length > 0)
            {
                Object child = box.getAccessibleContext().getAccessibleChild(0);
                if (child instanceof BasicComboPopup)
                    SwingUtilities.invokeLater(() -> {
                        BasicComboPopup popup = (BasicComboPopup)child;
                        JScrollPane scrollPane = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, popup.getList());

                        int popupWidth = popup.getList().getPreferredSize().width +
                                (options.length > box.getMaximumRowCount() ? scrollPane.getVerticalScrollBar().getPreferredSize().width : 0);
                        scrollPane.setPreferredSize(new Dimension(Math.max(popupWidth, scrollPane.getPreferredSize().width), scrollPane.getPreferredSize().height));
                        scrollPane.setMaximumSize(scrollPane.getPreferredSize());
                        Point location = box.getLocationOnScreen();
                        popup.setLocation(location.x, location.y + box.getHeight() - 1);
                        popup.setLocation(location.x, location.y + box.getHeight());
                    });
            }
        }));
        box.setPreferredSize(new Dimension(MAX_COMBO_WIDTH, box.getPreferredSize().height));

        boxPanel.add(box, BorderLayout.CENTER);
        optionsBoxes.add(box);
        box.setSelectedItem(value);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        JLabel addButton = new JLabel("+", JLabel.CENTER);
        Font buttonFont = new Font(addButton.getFont().getFontName(), Font.PLAIN, addButton.getFont().getSize() - 2);
        addButton.setAlignmentX(CENTER_ALIGNMENT);
        addButton.setFont(buttonFont);
        addButton.addMouseListener(MouseListenerFactory.createPressListener((e) -> {
            addItem(options[0]);
            optionsPanel.revalidate();
        }));
        JLabel removeButton = new JLabel(String.valueOf(UnicodeSymbols.MULTIPLY), JLabel.CENTER);
        removeButton.setForeground(Color.RED);
        removeButton.setAlignmentX(CENTER_ALIGNMENT);
        removeButton.setFont(buttonFont);
        removeButton.addMouseListener(MouseListenerFactory.createPressListener((e) -> {
            if (optionsBoxes.size() > 1)
            {
                optionsPanel.remove(boxPanel);
                optionsBoxes.remove(box);
                optionsPanel.revalidate();
            }
        }));
        buttonPanel.add(removeButton);
        buttonPanel.add(addButton);
        boxPanel.add(buttonPanel, BorderLayout.EAST);

        optionsPanel.add(boxPanel);
    }

    @Override
    public Filter filter()
    {
        @SuppressWarnings("unchecked")
        var filter = (OptionsFilter<T>)FilterAttribute.createFilter(type);
        filter.contain = contain.getSelectedItem();
        filter.selected = optionsBoxes.stream().map((b) -> b.getItemAt(b.getSelectedIndex())).collect(Collectors.toSet());
        return filter;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the given filter is not the same
     *                                  type as this OptionsFilterPanel or isn't even an {@link OptionsFilter}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setContents(FilterLeaf<?> filter) throws IllegalArgumentException
    {
        if (filter instanceof OptionsFilter && filter.type().equals(type))
            setContents((OptionsFilter<T>)filter);
        else if (filter instanceof OptionsFilter)
            throw new IllegalArgumentException("Options filter type " + filter.type() + " does not match type " + type);
        else
            throw new IllegalArgumentException("Illegal options filter " + filter.type());
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the given filter is of the wrong type
     */
    @Override
    public void setContents(OptionsFilter<T> filter) throws IllegalArgumentException
    {
        if (filter.type().equals(type))
        {
            contain.setSelectedItem(filter.contain);
            if (options.length == 0)
                contain.setVisible(false);
            optionsBoxes.clear();
            optionsPanel.removeAll();
            if (filter.selected.isEmpty() && options.length > 0)
                addItem(options[0]);
            else
                for (T selected : filter.selected)
                    addItem(selected);
        }
        else
            throw new IllegalArgumentException("Options filter type " + filter.type() + " does not match type " + type);
    }
}
