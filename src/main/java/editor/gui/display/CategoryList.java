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
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import editor.collection.deck.CategorySpec;
import editor.gui.ccp.CCPItems;
import editor.gui.ccp.data.DataFlavors;
import editor.gui.ccp.handler.CategoryTransferHandler;
import editor.gui.editor.CategoryEditorPanel;
import editor.util.MouseListenerFactory;
import editor.util.PopupMenuListenerFactory;

/**
 * This class represents an element that can display a list of {@link CategorySpec}s.
 * Optionally, it can show an extra line that is not an element, which is useful
 * for a hint of how to perform an action on the list.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryList extends JList<String>
{
    /**
     * This class represents a model for displaying a list of {@link CategorySpec}s.
     *
     * @author Alec Roelke
     */
    private class CategoryListModel extends DefaultListModel<String>
    {
        /**
         * Get the name of the {@link CategorySpec} at the specified position.
         *
         * @param index index into the list to look at
         * @return the name of the {@link CategorySpec} at the index.
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
         * {@link CategorySpec}s.  Otherwise it's just the number of
         * {@link CategorySpec}s.
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
    private List<CategorySpec> categories;
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
        menu.add(ccp.cut);
        menu.add(ccp.copy);
        menu.add(ccp.paste);

        menu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            ccp.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.categoryFlavor));
        }));
    }

    /**
     * Create a new CategoryList with the specified {@link CategorySpec}s.
     *
     * @param h extra line to show in italics
     * @param c {@link CategorySpec}s to show
     */
    public CategoryList(String h, CategorySpec... c)
    {
        this(h, Arrays.asList(c));
    }

    /**
     * Create a new CategoryList with the specified list
     * of {@link CategorySpec}.
     *
     * @param h extra line to show in italics
     * @param c list of {@link CategorySpec}s to show
     */
    public CategoryList(String h, List<CategorySpec> c)
    {
        this(h);

        categories.addAll(c);
    }

    /**
     * Add a new {@link CategorySpec} to the list.
     *
     * @param c {@link CategorySpec} to display
     */
    public void addCategory(CategorySpec c)
    {
        categories.add(c);
        model.addElement(c.getName());
    }

    /**
     * Get all of the {@link CategorySpec}s.
     *
     * @return the {@link CategorySpec} list this CategoryList displays.
     */
    public List<CategorySpec> getCategories()
    {
        return Collections.unmodifiableList(categories);
    }

    /**
     * Get the {@link CategorySpec} at a certain position.
     *
     * @param index index into the list to search
     * @return the {@link CategorySpec} at the given index.
     */
    public CategorySpec getCategoryAt(int index)
    {
        return categories.get(index);
    }

    /**
     * Get the number of items in this CategoryList.
     *
     * @return the number of {@link CategorySpec}s in this CategoryList.
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
     * Remove the {@link CategorySpec} at a particular index.
     *
     * @param index index to remove the CategorySpec at
     */
    public void removeCategoryAt(int index)
    {
        categories.remove(index);
        model.remove(index);
    }

    /**
     * Set the {@link CategorySpec} at a particular position in the list.
     *
     * @param index index to set
     * @param c {@link CategorySpec} to display
     */
    public void setCategoryAt(int index, CategorySpec c)
    {
        categories.set(index, c);
        model.setElementAt(c.getName(), index);
    }

    @Override
    public Point getPopupLocation(MouseEvent event)
    {
        mouseTriggeredPopup = event != null;
        return super.getPopupLocation(event);
    }
}
