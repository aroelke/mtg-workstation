package editor.gui.display;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import editor.collection.deck.Category;
import editor.database.card.Card;
import editor.gui.ccp.CCPItems;
import editor.gui.ccp.data.DataFlavors;
import editor.gui.ccp.handler.CategoryTransferHandler;
import editor.gui.editor.CategoryEditorPanel;
import editor.util.MouseListenerFactory;
import editor.util.PopupMenuListenerFactory;

/**
 * This class represents an element that can display a list of {@link Category}s.
 * Optionally, it can show an extra line that is not an element, which is useful
 * for a hint of how to perform an action on the list.
 *
 * @author Alec Roelke
 */
public class CategoryList extends JList<String>
{
    /**
     * This class represents a model for displaying a list of {@link Category}s.
     *
     * @author Alec Roelke
     */
    private class CategoryListModel extends DefaultListModel<String>
    {
        /**
         * Get the name of the {@link Category} at the specified position.
         *
         * @param index index into the list to look at
         * @return the name of the {@link Category} at the index.
         */
        @Override
        public String getElementAt(int index)
        {
            if (index < categories.size())
                return categories.get(index).getName();
            else if (!hint.isEmpty() && index == categories.size())
                return hint;
            else
                throw new IndexOutOfBoundsException("Illegal list index " + index);
        }

        /**
         * {@inheritDoc}
         * If there is a hint to show, the size is one more than the number of
         * {@link Category}s.  Otherwise it's just the number of
         * {@link Category}s.
         *
         * @return the number of elements to show.
         */
        @Override
        public int getSize()
        {
            return categories.size() + (!hint.isEmpty() ? 1 : 0);
        }
    }

    /**
     * Categories to show.
     */
    private List<Category> categories;
    /**
     * Hint to show for activating the CategoryList (for example, to edit
     * or add categories).  If it's the empty string, don't show it.
     */
    private String hint;
    /**
     * Model for how to display categories.
     */
    private CategoryListModel model;
    /**
     * Whether or not the popup menu was triggered by the mouse (some keyboards
     * have keys to open the context menu).
     */
    private boolean mouseTriggeredPopup;

    /**
     * Create a new empty CategoryList.
     *
     * @param h extra line to show in italics.
     */
    public CategoryList(String h)
    {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        hint = h;
        categories = new ArrayList<>();
        setModel(model = new CategoryListModel());

        if (!hint.isEmpty())
        {
            addMouseListener(MouseListenerFactory.createDoubleClickListener((e) -> {
                int index = locationToIndex(e.getPoint());
                var rec = Optional.ofNullable(getCellBounds(index, index));
                CategoryEditorPanel.showCategoryEditor(CategoryList.this, rec.filter((r) -> r.contains(e.getPoint())).map((r) -> getCategoryAt(index))).ifPresent((s) -> {
                    if (index < 0)
                        addCategory(s);
                    else
                        setCategoryAt(index, s);
                });
            }));
        }

        setTransferHandler(new CategoryTransferHandler(
            () -> {
                int row = getSelectedIndex();
                if (row < 0 || row >= categories.size())
                    return null;
                else
                    return categories.get(row);
            },
            (c) -> false, // Duplicate categories are allowed
            (c) -> {
                int row = getSelectedIndex();
                if (row < 0 || row >= categories.size())
                    addCategory(c);
                else
                {
                    categories.add(row, c);
                    model.add(row, c.getName());
                }
                return true;
            },
            (c) -> removeCategoryAt(categories.indexOf(c))
        ));

        // Popup menu for copying and pasting categories
        mouseTriggeredPopup = false;
        JPopupMenu menu = new JPopupMenu() {
            @Override
            public void show(Component invoker, int x, int y) {
                if (mouseTriggeredPopup)
                {
                    int row = locationToIndex(new Point(x, y));
                    if (row >= 0)
                        setSelectedIndex(row);
                    else
                        clearSelection();
                }
                super.show(invoker, x, y);
            }
        };
        setComponentPopupMenu(menu);

        CCPItems ccp = new CCPItems(this, true);
        menu.add(ccp.cut());
        menu.add(ccp.copy());
        menu.add(ccp.paste());

        menu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            ccp.paste().setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.categoryFlavor));
        }));
    }

    /**
     * Create a new CategoryList with the specified {@link Category}s.
     *
     * @param h extra line to show in italics
     * @param c {@link Category}s to show
     */
    public CategoryList(String h, Category... c)
    {
        this(h, Arrays.asList(c));
    }

    /**
     * Create a new CategoryList with the specified list
     * of {@link Category}.
     *
     * @param h extra line to show in italics
     * @param c list of {@link Category}s to show
     */
    public CategoryList(String h, List<Category> c)
    {
        this(h);
        categories.addAll(c);
    }

    /**
     * If the category specification contains explicitly included or excluded cards,
     * warn the user that they will be removed before being added to this list.
     * 
     * @param spec specification to check
     * @return <code>true</code> if the specification has no explicitly-included or
     * -excluded cards or if it does and the user selects to add it anyway, and
     * <code>false</code> otherwise.
     */
    private boolean confirmListClean(Category spec)
    {
        if (!spec.getWhitelist().isEmpty() || !spec.getBlacklist().isEmpty())
        {
            return JOptionPane.showConfirmDialog(this,
                "Category "
                        + spec.getName()
                        + " contains cards in its whitelist or blacklist which will not be included in the preset category."
                        + "  Continue?",
                "Add to Presets",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
        else
            return true;
    }

    /**
     * Add a new {@link Category} to the list.
     *
     * @param c {@link Category} to display
     */
    public void addCategory(Category c)
    {
        if (confirmListClean(c))
        {
            Category copy = new Category(c);
            for (final Card card : copy.getBlacklist())
                copy.include(card);
            for (final Card card : copy.getWhitelist())
                copy.exclude(card);
            categories.add(copy);
            model.addElement(copy.getName());
        }
    }

    /**
     * Get all of the {@link Category}s.
     *
     * @return the {@link Category} list this CategoryList displays.
     */
    public List<Category> getCategories()
    {
        return Collections.unmodifiableList(categories);
    }

    /**
     * Get the {@link Category} at a certain position.
     *
     * @param index index into the list to search
     * @return the {@link Category} at the given index.
     */
    public Category getCategoryAt(int index)
    {
        return categories.get(index);
    }

    /**
     * Get the number of items in this CategoryList.
     *
     * @return the number of {@link Category}s in this CategoryList.
     */
    public int getCount()
    {
        return categories.size();
    }

    @Override
    public int locationToIndex(Point p)
    {
        int index = super.locationToIndex(p);
        return index < categories.size() ? index : -1;
    }

    /**
     * Remove the {@link Category} at a particular index.
     *
     * @param index index to remove the Category at
     */
    public void removeCategoryAt(int index)
    {
        categories.remove(index);
        model.remove(index);
    }

    /**
     * Set the {@link Category} at a particular position in the list.
     *
     * @param index index to set
     * @param c {@link Category} to display
     */
    public void setCategoryAt(int index, Category c)
    {
        if (confirmListClean(c))
        {
            Category copy = new Category(c);
            for (final Card card : copy.getBlacklist())
                copy.include(card);
            for (final Card card : copy.getWhitelist())
                copy.exclude(card);;
            categories.set(index, copy);
            model.setElementAt(copy.getName(), index);
        }
    }

    @Override
    public Point getPopupLocation(MouseEvent event)
    {
        mouseTriggeredPopup = event != null;
        return super.getPopupLocation(event);
    }
}
