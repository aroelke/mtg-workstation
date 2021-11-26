package editor.gui.filter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import editor.filter.Filter;
import editor.filter.FilterGroup;
import editor.filter.leaf.FilterLeaf;
import editor.gui.generic.ChangeTitleListener;
import editor.util.UnicodeSymbols;

/**
 * This class represents a group of filter panels that corresponds to a
 * group of filters.
 *
 * @author Alec Roelke
 */
public class FilterGroupPanel extends FilterPanel<Filter>
{
    /** Amount of empty space before titled border line. */
    private static final int GAP = 10;

    /** {@link FilterPanel}s contained by this FilterGroupPanel. */
    private List<FilterPanel<?>> children;
    /** Panel containing the children. */
    private Box filtersPanel;
    /** Combo box showing the combination mode of the filter group. */
    private JComboBox<FilterGroup.Mode> modeBox;
    /** Titled border for showing the group's comment. */
    private TitledBorder border;

    /**
     * Create a new FilterGroupPanel with one child.
     */
    public FilterGroupPanel()
    {
        super();
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP), border = BorderFactory.createTitledBorder("")));
        setLayout(new BorderLayout());

        children = new ArrayList<>();

        // Panel containing the mode selector and edit buttons
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        add(topPanel, BorderLayout.NORTH);

        // Mode selection combo box
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modeBox = new JComboBox<>();
        modeBox.setModel(new DefaultComboBoxModel<>(FilterGroup.Mode.values()));
        modePanel.add(modeBox);
        topPanel.add(modePanel);

        // Add, remove, and group buttons
        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("+");
        addButton.addActionListener((e) -> {
            add(new FilterSelectorPanel());
            firePanelsChanged();
        });
        editPanel.add(addButton);
        JButton removeButton = new JButton(String.valueOf(UnicodeSymbols.MINUS));
        removeButton.addActionListener((e) -> {
            if (group == null)
            {
                clear();
                add(new FilterSelectorPanel());
                firePanelsChanged();
            }
            else
            {
                group.remove(this);
                group.firePanelsChanged();
            }
        });
        editPanel.add(removeButton);
        JButton groupButton = new JButton(String.valueOf(UnicodeSymbols.ELLIPSIS));
        groupButton.addActionListener((e) -> {
            if (group == null)
            {
                FilterGroupPanel newGroup = new FilterGroupPanel();
                newGroup.clear();
                newGroup.modeBox.setSelectedIndex(modeBox.getSelectedIndex());
                for (FilterPanel<?> child : children)
                    newGroup.add(child);
                clear();
                add(newGroup);
            }
            else
                group.group(this);
            firePanelsChanged();
        });
        editPanel.add(groupButton);
        topPanel.add(editPanel);

        // Panel containing child filters
        filtersPanel = new Box(BoxLayout.Y_AXIS);
        add(filtersPanel, BorderLayout.CENTER);

        add(new FilterSelectorPanel());

        addMouseListener(new ChangeTitleListener(this, border, (t) -> {
            border.setTitle(t);
            revalidate();
            repaint();
            firePanelsChanged();
        }, (s) -> GAP, (s) -> s.isEmpty() ? 0 : GAP));
    }

    /**
     * Add a new child filter.  This will extend the size of this
     * FilterGroupPanel, but will not redo layout of the containing
     * frame.
     *
     * @param panel #FilterPanel to add
     */
    public void add(FilterPanel<?> panel)
    {
        children.add(panel);
        filtersPanel.add(panel);
        panel.group = this;
    }

    /**
     * Clear all contents of this FilterGroupPanel.  A new filter is not
     * replaced, and the layout of the container is not redone.
     */
    public void clear()
    {
        children.clear();
        filtersPanel.removeAll();
        modeBox.setSelectedIndex(0);
        border.setTitle("");
    }

    /**
     * {@inheritDoc}
     * The filter will be the composed of this FilterGroupPanel's children according
     * to the #FilterGroup's mode.
     */
    @Override
    public Filter filter()
    {
        FilterGroup group = new FilterGroup();
        group.mode = modeBox.getItemAt(modeBox.getSelectedIndex());
        group.comment = border.getTitle();
        for (FilterPanel<?> child : children)
            group.addChild(child.filter());
        return group;
    }

    /**
     * If the given #FilterPanel is a child of this FilterGroupPanel,
     * create a new group for it and assign that group in its place.
     * Otherwise, make it a child of this FilterGroupPanel first and
     * do it anyway.  This does not redo layout of the container.
     *
     * @param panel panel to group.
     */
    public void group(FilterPanel<?> panel)
    {
        if (panel.group != this)
            add(panel);

        int index = children.indexOf(panel);
        if (index >= 0)
        {
            filtersPanel.removeAll();
            FilterGroupPanel newGroup = new FilterGroupPanel();
            newGroup.clear();
            newGroup.add(panel);
            children.set(index, newGroup);
            newGroup.group = this;
            for (FilterPanel<?> child : children)
                filtersPanel.add(child);
        }
    }

    /**
     * Removes the given child filter if it is in this FilterGroupPanel.
     * This will shrink the size of this FilterGroupPanel, but will not
     * redo layout of the containing frame.
     *
     * @param panel #FilterPanel to remove
     */
    public void remove(FilterPanel<?> panel)
    {
        if (children.contains(panel))
        {
            if (panel instanceof FilterGroupPanel g)
            {
                // Make this insert in place of the old group
                filtersPanel.remove(panel);
                children.remove(panel);
                for (FilterPanel<?> child : g.children)
                    add(child);
            }
            else if (children.size() > 1)
            {
                filtersPanel.remove(panel);
                children.remove(panel);
            }
        }
    }

    /**
     * {@inheritDoc}
     * This FilterGroupPanel's contents will be entirely replaced according to the
     * given filter.
     */
    @Override
    public void setContents(Filter filter)
    {
        FilterGroup group;
        clear();
        group = filter instanceof FilterGroup g ? g : new FilterGroup(filter);
        modeBox.setSelectedItem(group.mode);
        border.setTitle(group.comment);
        for (Filter child : group)
        {
            if (child instanceof FilterGroup)
            {
                FilterGroupPanel g = new FilterGroupPanel();
                g.setContents(child);
                add(g);
            }
            else
            {
                FilterSelectorPanel panel = new FilterSelectorPanel();
                panel.setContents((FilterLeaf<?>)child);
                add(panel);
            }
        }
    }
}
