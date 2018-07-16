package editor.gui.display;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import editor.collection.category.CategorySpec;
import editor.gui.editor.CategoryEditorPanel;
import editor.util.MouseListenerFactory;

@SuppressWarnings("serial")
public class DefaultCategoryTable extends JTable
{
    private static final Class<?> columnClasses[] = {Boolean.class, String.class};
    private static final String columnNames[] = {"", "Category"};

    private class DefaultCategoryModel extends AbstractTableModel
    {
        @Override
        public Class<?> getColumnClass(int column)
        {
            return columnClasses[column];
        }

        @Override
        public int getColumnCount()
        {
            return 2;
        }

        @Override
        public String getColumnName(int column)
        {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            switch (columnIndex)
            {
            case 0:
                if (rowIndex < categories.size())
                    return initial.get(categories.get(rowIndex));
                else
                    return null;
            case 1:
                if (rowIndex < categories.size())
                    return categories.get(rowIndex).getName();
                else
                    return hint;
            default:
                throw new IndexOutOfBoundsException(columnIndex);
            }
        }

        @Override
        public int getRowCount()
        {
            return categories.size() + 1;
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column == 0;
        }

        @Override
        public void setValueAt(Object value, int row, int column)
        {
            if (isCellEditable(row, column) && row < categories.size())
            {
                if (value instanceof Boolean)
                    initial.put(categories.get(row), (Boolean)value);
                else
                    throw new IllegalArgumentException("Illegal boolean value " + value);
            }
            else
                super.setValueAt(value, row, column);
        }
    }

    private class DefaultCategoryRenderer implements TableCellRenderer
    {
        private final TableCellRenderer parent;

        public DefaultCategoryRenderer(TableCellRenderer original)
        {
            parent = original;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            if (row < categories.size())
                return parent.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            else
            {
                switch (column)
                {
                case 0:
                    return new JLabel();
                case 1:
                    return new JLabel(hint);
                default:
                    throw new IndexOutOfBoundsException(column);
                }
            }
        }
    }

    /**
     * Categories to show.
     */
    private final List<CategorySpec> categories;
    private final String hint;
    private final Map<CategorySpec, Boolean> initial;
    private final DefaultCategoryModel model;

    public DefaultCategoryTable(Collection<CategorySpec> c)
    {
        this(c, "");
    }

    public DefaultCategoryTable(Collection<CategorySpec> c, String h)
    {
        super();

        setModel(model = new DefaultCategoryModel());
        setFillsViewportHeight(true);
        setShowGrid(false);
        setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
        getTableHeader().setEnabled(false);
        for (final Class<?> clazz: columnClasses)
            setDefaultRenderer(clazz, new DefaultCategoryRenderer(getDefaultRenderer(clazz)));

        hint = h;
        categories = new ArrayList<>(c);
        initial = categories.stream().collect(Collectors.toMap(Function.identity(), (cat) -> false));
        sortCategories();

        final TableColumn firstColumn = getColumnModel().getColumn(0);
        final int firstColumnWidth = new JCheckBox().getPreferredSize().width;
        firstColumn.setPreferredWidth(firstColumnWidth);
        firstColumn.setMaxWidth(firstColumnWidth);

        addMouseListener(MouseListenerFactory.createReleaseListener((e) -> {
            if (e.getClickCount() == 2)
            {
                final int row = rowAtPoint(e.getPoint());
                final int column = columnAtPoint(e.getPoint());

                if (column == 1)
                {
                    if (row >= 0 && row < categories.size())
                    {
                        final CategorySpec spec = CategoryEditorPanel.showCategoryEditor(DefaultCategoryTable.this, categories.get(row));
                        if (spec != null)
                            setCategoryAt(row, spec);
                    }
                    else
                    {
                        final CategorySpec spec = CategoryEditorPanel.showCategoryEditor(DefaultCategoryTable.this);
                        if (spec != null)
                            addCategory(spec);
                    }
                }
            }
        }));
    }

    public void addCategory(CategorySpec spec)
    {
        categories.add(spec);
        sortCategories();
        initial.put(spec, false);
        model.fireTableDataChanged();
    }

    public CategorySpec getCategoryAt(int row)
    {
        return categories.get(row);
    }

    public int getCategoryCount()
    {
        return categories.size();
    }

    public void removeCategoryAt(int row)
    {
        initial.remove(categories.remove(row));
        model.fireTableDataChanged();
    }

    public void setCategoryAt(int row, CategorySpec spec)
    {
        final boolean init = initial.remove(categories.get(row));
        categories.set(row, spec);
        sortCategories();
        initial.put(spec, init);
        model.fireTableDataChanged();
    }

    private void sortCategories()
    {
        categories.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
    }
}