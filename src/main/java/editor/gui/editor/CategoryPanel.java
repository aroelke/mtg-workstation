package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

import editor.collection.deck.Deck;
import editor.database.card.Card;
import editor.gui.display.CardTable;
import editor.gui.display.CardTableModel;
import editor.gui.generic.ColorButton;
import editor.gui.generic.ComponentUtils;
import editor.gui.settings.SettingsDialog;
import editor.util.UnicodeSymbols;

/**
 * This class represents a panel that shows information about a category in a deck.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryPanel extends JPanel
{
    /**
     * This class represents a timer controlling the flash of the panel when it is skipped to.
     * The flash will last 400ms.
     *
     * @author Alec Roelke
     */
    private class FlashTimer extends Timer
    {
        /**
         * Progress of the flash.
         */
        private int count;
        /**
         * Amount of ticks until the flash disappears.
         */
        private final int END = 20;

        /**
         * Color of the flash.
         */
        private final Color FLASH = SystemColor.textHighlight;

        /**
         * Create a new FlashTimer.
         */
        public FlashTimer()
        {
            super(20, null);
            count = 0;
            addActionListener((e) -> {
                if (++count > END)
                    stop();
                else
                {
                    double ratio = (double)count / (double)END;
                    int r = (int)(FLASH.getRed() + (background.getRed() - FLASH.getRed()) * ratio);
                    int g = (int)(FLASH.getGreen() + (background.getGreen() - FLASH.getGreen()) * ratio);
                    int b = (int)(FLASH.getBlue() + (background.getBlue() - FLASH.getBlue()) * ratio);
                    setBackground(new Color(r, g, b));
                    repaint();
                }
            });
        }

        /**
         * Restart the timer, resetting the progress of the flash.
         */
        @Override
        public void restart()
        {
            count = 0;
            super.restart();
        }

        /**
         * Stop the timer and set the panel to its default background color.
         */
        @Override
        public void stop()
        {
            super.stop();
            setBackground(background);
            repaint();
        }
    }

    /**
     * This class represents a mouse wheel listener that returns mouse wheel control to an outer scroll
     * pane when this one's scroll pane has reached a limit.
     * <p>
     * It is adapted from a StackOverflow answer to the same problem, which can be found at
     *
     * @author Nemi
     * @link{http://stackoverflow.com/questions/1377887/jtextpane-prevents-scrolling-in-the-parent-jscrollpane}. TODO: Adapt this code better
     * @since November 24, 2009
     */
    private class PDMouseWheelListener implements MouseWheelListener
    {
        private JScrollBar bar;
        private JScrollPane parent;
        private JScrollPane parentScrollPane;
        private int previousValue = 0;

        public PDMouseWheelListener(JScrollPane p)
        {
            parent = p;
            bar = parent.getVerticalScrollBar();
        }

        private MouseWheelEvent cloneEvent(MouseWheelEvent e)
        {
            return new MouseWheelEvent(getParentScrollPane(), e.getID(), e.getWhen(),
                    e.getModifiersEx(), 1, 1, e.getClickCount(),
                    false, e.getScrollType(), e.getScrollAmount(), e.getWheelRotation());
        }

        private int getMax()
        {
            return bar.getMaximum() - bar.getVisibleAmount();
        }

        private JScrollPane getParentScrollPane()
        {
            if (parentScrollPane == null)
            {
                Component parent = getParent();
                while (!(parent instanceof JScrollPane) && parent != null)
                    parent = parent.getParent();
                parentScrollPane = (JScrollPane)parent;
            }
            return parentScrollPane;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            JScrollPane parent = getParentScrollPane();
            if (parent != null)
            {
                /*
                 * Only dispatch if we have reached top/bottom on previous scroll
                 */
                if (e.getWheelRotation() < 0)
                {
                    if (bar.getValue() == 0 && previousValue == 0)
                        parent.dispatchEvent(cloneEvent(e));
                }
                else
                {
                    if (bar.getValue() == getMax() && previousValue == getMax())
                        parent.dispatchEvent(cloneEvent(e));
                }
                previousValue = bar.getValue();
            }
            /*
             * If parent scrollpane doesn't exist, remove this as a listener.
             * We have to defer this till now (vs doing it in constructor)
             * because in the constructor this item has no parent yet.
             */
            else
                this.parent.removeMouseWheelListener(this);
        }
    }

    /**
     * Label showing the average CMC of cards in the category.
     */
    private JLabel avgCMCLabel;
    /**
     * Default background color of this panel.
     */
    private Color background;
    /**
     * Border showing the name of the category.
     */
    protected TitledBorder border;
    /**
     * Button displaying and allowing editing of the category's color.
     */
    protected ColorButton colorButton;
    /**
     * Label showing the number of cards in the category.
     */
    private JLabel countLabel;
    /**
     * Category in the Deck data structure.
     */
    private Deck deck;
    /**
     * Button for editing the category.
     */
    protected JButton editButton;
    /**
     * Timer timing flashing of the border of this panel when it is skipped to.
     */
    private Timer flashTimer;
    /**
     * Model to tell the table how to display the contents of the category.
     */
    private CardTableModel model;
    /**
     * Name of this category for display purposes.
     */
    private String name;
    /**
     * Combo box showing the user-defined rank of the category.
     */
    protected JComboBox<Integer> rankBox;
    /**
     * Button to remove the category.
     */
    protected JButton removeButton;
    /**
     * Table to display the contents of the category.
     */
    protected CardTable table;
    /**
     * Number of rows to display in the table.
     */
    private int tableRows;

    /**
     * Create a new CategoryPanel.
     *
     * @param d deck containing the category to display
     * @param n name of the category to display
     * @param editor {@link EditorFrame} containing the new CategoryPanel
     */
    public CategoryPanel(Deck d, String n, EditorFrame editor)
    {
        super();
        deck = d;
        name = n;
        background = getBackground();
        flashTimer = new FlashTimer();
        tableRows = SettingsDialog.settings().editor.categories.rows;

        // Each category is surrounded by a border with a title
        setBorder(border = BorderFactory.createTitledBorder(name));

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 0));

        // Labels showing category stats
        Box statsPanel = new Box(BoxLayout.X_AXIS);
        countLabel = new JLabel("Cards: " + deck.getCategoryList(name).total());
        statsPanel.add(countLabel);
        statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE));
        avgCMCLabel = new JLabel("Average CMC: 0");
        statsPanel.add(avgCMCLabel);
        statsPanel.add(Box.createHorizontalGlue());
        topPanel.add(statsPanel, BorderLayout.WEST);

        // Panel containing edit and remove buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rankBox = new JComboBox<>(IntStream.range(0, deck.categories().size()).boxed().toArray(Integer[]::new));
        rankBox.setSelectedIndex(deck.getCategoryRank(name));
        buttonPanel.add(rankBox);
        colorButton = new ColorButton(deck.getCategorySpec(name).getColor());
        buttonPanel.add(colorButton);
        editButton = new JButton(String.valueOf(UnicodeSymbols.ELLIPSIS));
        buttonPanel.add(editButton);
        removeButton = new JButton(String.valueOf(UnicodeSymbols.MINUS));
        buttonPanel.add(removeButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table showing the cards in the category
        model = new CardTableModel(editor, deck.getCategoryList(name), SettingsDialog.settings().editor.columns);
        table = new CardTable(model)
        {
            @Override
            public Dimension getPreferredScrollableViewportSize()
            {
                return new Dimension(getPreferredSize().width, tableRows*getRowHeight());
            }
        };
        table.setStripeColor(SettingsDialog.settings().editor.stripe);
        for (int i = 0; i < table.getColumnCount(); i++)
            if (model.isCellEditable(0, i))
                table.getColumn(model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(editor, model.getColumnData(i)));
        JScrollPane tablePane = new JScrollPane(table);
        tablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tablePane.addMouseWheelListener(new PDMouseWheelListener(tablePane));
        var resizeAdapter = new MouseInputAdapter()
        {
            private boolean resizing = false;
            private int base = 0;

            @Override
            public void mousePressed(MouseEvent e)
            {
                Point p = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), tablePane);
                resizing = p.y >= tablePane.getHeight() - 2;
                if (resizing)
                {
                    p = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), table);
                    base = p.y;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                resizing = false;
                Point p = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), tablePane);
                if (p.y >= tablePane.getHeight() - 2)
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                else
                    setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                Point p = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), tablePane);
                if (p.y >= tablePane.getHeight() - 2)
                    setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                else
                    setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(MouseEvent e)
            {
                if (resizing)
                {
                    Point p = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), table);
                    setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
                    final int minRows = 1;
                    final int maxRows = Math.max(deck.getCategoryList(name).total(), SettingsDialog.settings().editor.categories.rows);
                    if (p.y <= base - table.getRowHeight()/2 && tableRows > minRows)
                    {
                        int n = Math.min(((base - p.y) + table.getRowHeight() - 1)/table.getRowHeight(), tableRows - minRows);
                        tableRows -= n;
                        base -= n*table.getRowHeight();
                        table.revalidate();
                        table.repaint();
                        revalidate();
                        repaint();
                    }
                    else if (p.y >= base + table.getRowHeight()/2 && tableRows < maxRows)
                    {
                        int n = Math.min(((p.y - base) + table.getRowHeight() - 1)/table.getRowHeight(), maxRows - tableRows);
                        tableRows += n;
                        base += n*table.getRowHeight();
                        table.revalidate();
                        table.repaint();
                        revalidate();
                        repaint();
                    }
                }
            }
        };
        tablePane.addMouseMotionListener(resizeAdapter);
        tablePane.addMouseListener(resizeAdapter);
        tablePane.getViewport().addMouseMotionListener(resizeAdapter);
        tablePane.getViewport().addMouseListener(resizeAdapter);
        table.addMouseMotionListener(resizeAdapter);
        table.addMouseListener(resizeAdapter);
        tablePane.getHorizontalScrollBar().addMouseMotionListener(resizeAdapter);
        tablePane.getHorizontalScrollBar().addMouseListener(resizeAdapter);
        tablePane.getVerticalScrollBar().addMouseMotionListener(resizeAdapter);
        tablePane.getVerticalScrollBar().addMouseListener(resizeAdapter);
        addMouseMotionListener(resizeAdapter);
        addMouseListener(resizeAdapter);
        
        add(tablePane, BorderLayout.CENTER);

        update();
    }

    /**
     * Apply settings to this CategoryPanel.
     *
     * @param editor {@link EditorFrame} containing this CategoryPanel
     */
    public void applySettings(EditorFrame editor)
    {
        var columns = SettingsDialog.settings().editor.columns;
        Color stripe = SettingsDialog.settings().editor.stripe;
        model.setColumns(columns);
        table.setStripeColor(stripe);
        for (int i = 0; i < table.getColumnCount(); i++)
            if (model.isCellEditable(0, i))
                table.getColumn(model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(editor, model.getColumnData(i)));
    }

    /**
     * Briefly flash to draw attention to this CategoryPanel.
     */
    public void flash()
    {
        flashTimer.restart();
    }

    /**
     * Get the name of the corresponding category in the deck.
     *
     * @return The name of the category this CategoryPanel is displaying.
     */
    public String getCategoryName()
    {
        return name;
    }

    /**
     * Get the cards selected from this CategoryPanel's table.
     *
     * @return The list of Cards corresponding to the selected rows in the category's table.
     */
    public List<Card> getSelectedCards()
    {
        return Arrays.stream(table.getSelectedRows())
                .mapToObj((r) -> deck.getCategoryList(name).get(table.convertRowIndexToModel(r)))
                .collect(Collectors.toList());
    }

    /**
     * Change the category this panel should display to a new one.
     *
     * @param n name of the new category to display
     * @throws IllegalArgumentException if the deck does not have a category with that name
     */
    public void setCategoryName(String n)
    {
        if (!deck.containsCategory(n))
            throw new IllegalArgumentException("deck does not have a category named " + n);
        name = n;
    }

    /**
     * Update the GUI to reflect changes in a category.
     */
    public void update()
    {
        countLabel.setText("Cards: " + deck.getCategoryList(name).total());

        double avgCMC = 0.0;
        int count = 0;
        for (Card card : deck)
        {
            if (deck.getCategorySpec(name).includes(card))
            {
                avgCMC += card.minCmc() * deck.getEntry(card).count();
                count += deck.getEntry(card).count();
            }
        }
        if (count > 0)
            avgCMC /= count;
        if (avgCMC == (int)avgCMC)
            avgCMCLabel.setText("Average CMC: " + (int)avgCMC);
        else
            avgCMCLabel.setText("Average CMC: " + String.format("%.2f", avgCMC));

        border.setTitle(name);
        table.revalidate();
        table.repaint();
        colorButton.setColor(deck.getCategorySpec(name).getColor());
        colorButton.repaint();
        revalidate();
        repaint();
    }
}
