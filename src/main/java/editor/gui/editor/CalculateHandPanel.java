package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import editor.collection.deck.CategorySpec;
import editor.collection.deck.Deck;
import editor.gui.settings.SettingsDialog;

/**
 * This class represents a panel that shows the probability of getting a certain
 * amount of cards that match a category in an initial hand and after a given
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CalculateHandPanel extends JPanel
{
    /**
     * This class represents the data model backing the main table showing
     * probabilities.
     *
     * @author Alec Roelke
     */
    private class CalculationTableModel extends AbstractTableModel
    {
        @Override
        public Class<?> getColumnClass(int column)
        {
            return switch (modeBox.getItemAt(modeBox.getSelectedIndex())) {
                case DESIRED_PROBABILITY -> switch (column) {
                    case CATEGORY -> String.class;
                    case COUNT, DESIRED -> Integer.class;
                    case RELATION -> Relation.class;
                    default -> String.class;
                };
                case EXPECTED_COUNT -> String.class;
                default -> throw new IllegalStateException("There should not be any other values of DisplayMode.");
            };
        }

        /**
         * {@inheritDoc}
         * The number of columns depends on what is being displayed.  If the probabilities
         * of drawing given numbers of cards from categories is displayed, then there is a column
         * for category name, a column for desired count, a column for the relation to the desired
         * count, a column for the opening hand, and then a column for each subsequent draw.  Otherwise,
         * the desired count column is omitted.
         */
        @Override
        public int getColumnCount()
        {
            return (int)drawsSpinner.getValue() + switch (modeBox.getItemAt(modeBox.getSelectedIndex())) {
                case DESIRED_PROBABILITY -> P_INFO_COLS;
                case EXPECTED_COUNT -> E_INFO_COLS;
                default -> throw new IllegalStateException("There should not be any other values of DisplayMode.");
            };
        }

        @Override
        public String getColumnName(int column)
        {
            return switch (modeBox.getItemAt(modeBox.getSelectedIndex())) {
                case DESIRED_PROBABILITY -> switch (column) {
                    case CATEGORY  -> "Kind of Card";
                    case COUNT     -> "Count";
                    case DESIRED   -> "Desired";
                    case RELATION  -> "Relation";
                    case P_INITIAL -> "Initial Hand";
                    default        -> "Draw " + (column - (P_INFO_COLS - 1));
                };
                case EXPECTED_COUNT -> switch (column) {
                    case CATEGORY  -> "Kind of Card";
                    case E_INITIAL -> "Initial Hand";
                    default        -> "Draw " + (column - (E_INFO_COLS - 1));
                };
                default -> throw new IllegalStateException("There should not be any other values of DisplayMode.");
            };
        }

        /**
         * {@inheritDoc}
         * The number of rows is the number of categories in the deck.
         */
        @Override
        public int getRowCount()
        {
            return deck.numCategories();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            String category = deck.categories().stream().map(CategorySpec::getName).sorted().collect(Collectors.toList()).get(rowIndex);
            return switch (modeBox.getItemAt(modeBox.getSelectedIndex())) {
                case DESIRED_PROBABILITY -> switch (columnIndex) {
                    case CATEGORY -> category;
                    case COUNT -> deck.getCategoryList(category).total();
                    case DESIRED -> desiredBoxes.get(category).getSelectedItem();
                    case RELATION -> relationBoxes.get(category).getSelectedItem();
                    default -> String.format("%.2f%%", probabilities.get(category).get(columnIndex - (P_INFO_COLS - 1)) * 100.0);
                };
                case EXPECTED_COUNT -> {
                    if (columnIndex == CATEGORY)
                        yield category;
                    else if (columnIndex - (E_INFO_COLS - 1) < expectedCounts.get(category).size())
                        yield ROUND_MODE.get(SettingsDialog.settings().editor().hand().rounding()).apply(expectedCounts.get(category).get(columnIndex - (E_INFO_COLS - 1)));
                    else
                        yield "";
                }
                default -> throw new IllegalStateException("There should not be any other values of DisplayMode.");
            };
        }
    }

    /**
     * This enum enumerates the modes that a CalculateHandPanel can be placed in.
     * Those modes are to display probabilities of getting given amounts of cards in
     * each category and to display the expected number of each kind of card that will
     * be drawn.
     *
     * @author Alec
     */
    private enum DisplayMode
    {
        /**
         * Show the probability of drawing a given number of cards from each category in
         * the opening hand.
         */
        DESIRED_PROBABILITY("Probabilities"),
        /**
         * Show the expected number of cards from each category that will be drawn in the
         * opening hand.
         */
        EXPECTED_COUNT("Expected Counts");

        /**
         * String representation of this DisplayMode.
         */
        private final String mode;

        /**
         * Create a new DisplayMode.
         *
         * @param m String representation of the new DisplayMode
         */
        DisplayMode(final String m)
        {
            mode = m;
        }

        @Override
        public String toString()
        {
            return mode;
        }
    }

    /**
     * This enum represents the three relations to the desired number of cards
     * in a category to draw.
     *
     * @author Alec Roelke
     */
    private enum Relation
    {
        /**
         * The opening hand should have at least as many cards.
         */
        AT_LEAST("At least"),
        /**
         * The opening hand should have at most as many cards.
         */
        AT_MOST("At most"),
        /**
         * The opening hand should have exactly as many cards.
         */
        EXACTLY("Exactly");

        /**
         * String representation to display in a combo box.
         */
        private final String relation;

        /**
         * Create a new Relation.
         *
         * @param r String representation of the new Relation
         */
        Relation(String r)
        {
            relation = r;
        }

        @Override
        public String toString()
        {
            return relation;
        }
    }

    /**
     * Column in the table for the category.
     */
    private static final int CATEGORY = 0;
    /**
     * Column in the table for the number of cards in that category.
     */
    private static final int COUNT = 1;
    /**
     * Column in the table showing the relation with the desired number.
     */
    private static final int RELATION = 2;
    /**
     * Column in the table showing the desired number of cards in hand.
     */
    private static final int DESIRED = 3;
    /**
     * Column in the table showing the probability for the initial hand.
     */
    private static final int P_INITIAL = 4;
    /**
     * Number of columns before draw columns in probability mode.
     */
    private static final int P_INFO_COLS = 5;
    /**
     * Column in the table showing the expected values for the initial hand.
     */
    private static final int E_INITIAL = 1;
    /**
     * Number of columns before draw columns in expected value mode.
     */
    private static final int E_INFO_COLS = 2;

    /**
     * Rounding modes for expected value mode.
     */
    public static final Map<String, Function<Double, String>> ROUND_MODE = Map.of(
        "No rounding", (x) -> String.format("%.2f", x),
        "Round to nearest", (x) -> String.format("%d", Math.round(x)),
        "Truncate", (x) -> String.format("%d", x.intValue())
    );

    /**
     * Calculate the exact value of n!, as long as it will fit into a double.
     *
     * @param n parameter for factorial
     * @return the factorial of n, or n!
     */
    public static double factorial(int n)
    {
        double f = 1.0;
        for (int i = 1; i <= n; i++)
            f *= i;
        return f;
    }

    /**
     * Calculate a hypergeometric distribution for drawing the given number
     * of cards in a hand of the given size from a deck of the given size,
     * when the given number of cards are successes.
     *
     * @param n     number of desired cards
     * @param hand  size of hand drawn
     * @param count number of successful cards in deck
     * @param total number of cards in the deck
     * @return the hypergeometric distribution with parameters hand, count, and
     * total and argument n.
     */
    public static double hypergeom(int n, int hand, int count, int total)
    {
        return nchoosek(count, n) * nchoosek(total - count, hand - n) / nchoosek(total, hand);
    }

    /**
     * Calculate n choose k based on the {@link CalculateHandPanel#factorial(int)}
     * function.
     *
     * @param n number of items to choose from
     * @param k number of items to choose
     * @return the number of ways to choose k out of n items.
     */
    public static double nchoosek(int n, int k)
    {
        return factorial(n) / (factorial(n - k) * factorial(k));
    }

    /**
     * Deck containing cards to draw from.
     */
    private Deck deck;
    /**
     * List of boxes displaying the desired number of cards in hand.
     */
    private Map<String, JComboBox<Integer>> desiredBoxes;
    /**
     * Spinner controlling the number of drawn cards to show.
     */
    private JSpinner drawsSpinner;
    /**
     * List of lists of expected counts for opening hand and each draw afterward
     * for each category.
     */
    private Map<String, List<Double>> expectedCounts;
    /**
     * Spinner controlling the number of cards in the initial hand.
     */
    private JSpinner handSpinner;
    /**
     * Combo box for picking the display mode.
     */
    private JComboBox<DisplayMode> modeBox;
    /**
     * Data model for the table showing probabilities.
     */
    private CalculationTableModel model;
    /**
     * List of lists of probabilities for opening and hand each draw afterward
     * for each category.
     */
    private Map<String, List<Double>> probabilities;
    /**
     * List of combo boxes displaying relations to the desired numbers.
     */
    private Map<String, JComboBox<Relation>> relationBoxes;

    /**
     * Create a new CalculateHandPanel and populate it with its initial
     * categories.
     *
     * @param d Deck containing cards to draw
     */
    public CalculateHandPanel(Deck d)
    {
        super(new BorderLayout());

        // Parameter initialization
        deck = d;
        desiredBoxes = new HashMap<>();
        relationBoxes = new HashMap<>();
        probabilities = new HashMap<>();
        expectedCounts = new HashMap<>();

        // Right panel containing table and settings
        JPanel tablePanel = new JPanel(new BorderLayout());
        add(tablePanel, BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout());
        tablePanel.add(northPanel, BorderLayout.NORTH);

        // Spinners controlling draws to show and initial hand size
        JPanel leftControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        northPanel.add(leftControlPanel, BorderLayout.WEST);
        leftControlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        leftControlPanel.add(new JLabel("Hand Size: "));
        leftControlPanel.add(handSpinner = new JSpinner(new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1)));
        leftControlPanel.add(Box.createHorizontalStrut(15));
        leftControlPanel.add(new JLabel("Show Draws: "));
        leftControlPanel.add(drawsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1)));

        // Combo box controlling what numbers to show in the table
        JPanel rightControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightControlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
        northPanel.add(rightControlPanel);
        modeBox = new JComboBox<>(DisplayMode.values());
        modeBox.addActionListener((e) -> {
            recalculate();
            model.fireTableStructureChanged();
        });
        rightControlPanel.add(modeBox);

        JTable table = new JTable(model = new CalculationTableModel())
        {
            @Override
            public TableCellEditor getCellEditor(int row, int column)
            {
                String category = deck.categories().stream().map(CategorySpec::getName).sorted().collect(Collectors.toList()).get(row);
                return switch (column) {
                    case DESIRED  -> new DefaultCellEditor(desiredBoxes.get(category));
                    case RELATION -> new DefaultCellEditor(relationBoxes.get(category));
                    default       -> super.getCellEditor(row, column);
                };
            }

            @Override
            public boolean getScrollableTracksViewportWidth()
            {
                return getPreferredSize().width < getParent().getWidth();
            }

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return column == DESIRED || column == RELATION;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
            {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row) || !getRowSelectionAllowed())
                    c.setBackground(row % 2 == 0 ? new Color(getBackground().getRGB()) : SettingsDialog.settings().editor().stripe());
                if (model.getValueAt(row, RELATION).equals(Relation.AT_LEAST) && model.getValueAt(row, DESIRED).equals(0))
                {
                    c.setForeground(c.getBackground().darker());
                    c.setFont(new Font(c.getFont().getFontName(), Font.ITALIC, c.getFont().getSize()));
                }
                else
                {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer intRenderer = new DefaultTableCellRenderer();
        intRenderer.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
        table.setDefaultRenderer(Integer.class, intRenderer);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Actions
        drawsSpinner.addChangeListener((e) -> {
            recalculate();
            model.fireTableStructureChanged();
        });
        handSpinner.addChangeListener((e) -> recalculate());
    }

    /**
     * Recalculate the probabilities of drawing the desired number of cards in
     * each category in the initial hand and after each draw and the expected
     * number of cards from each category.
     */
    public void recalculate()
    {
        var categories = deck.categories().stream().map(CategorySpec::getName).sorted().collect(Collectors.toList());

        probabilities.clear();
        int hand = (int)handSpinner.getValue();
        int draws = (int)drawsSpinner.getValue();

        for (String category : categories)
        {
            probabilities.put(category, new ArrayList<>(Collections.nCopies(1 + draws, 0.0)));
            expectedCounts.put(category, new ArrayList<>(Collections.nCopies(1 + draws, 0.0)));
            var box = relationBoxes.get(category);
            Relation r = box.getItemAt(box.getSelectedIndex());
            for (int j = 0; j <= draws; j++)
            {
                double p = 0.0;
                switch (r)
                {
                case AT_LEAST:
                    for (int k = 0; k < desiredBoxes.get(category).getSelectedIndex(); k++)
                        p += hypergeom(k, hand + j, deck.getCategoryList(category).total(), deck.total());
                    p = 1.0 - p;
                    break;
                case EXACTLY:
                    p = hypergeom(desiredBoxes.get(category).getSelectedIndex(), hand + j, deck.getCategoryList(category).total(), deck.total());
                    break;
                case AT_MOST:
                    for (int k = 0; k <= desiredBoxes.get(category).getSelectedIndex(); k++)
                        p += hypergeom(k, hand + j, deck.getCategoryList(category).total(), deck.total());
                    break;
                }
                probabilities.get(category).set(j, p);
                expectedCounts.get(category).set(j, (double)deck.getCategoryList(category).total() / deck.total() * (hand + j));
            }
        }
        model.fireTableDataChanged();
    }

    /**
     * Update the values in the probabilities/expected values table.
     */
    public void update()
    {
        var categories = deck.categories().stream().map(CategorySpec::getName).sorted().collect(Collectors.toList());

        var oldDesired = desiredBoxes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> e.getValue().getSelectedIndex()));
        var oldRelations = relationBoxes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> e.getValue().getItemAt(e.getValue().getSelectedIndex())));

        desiredBoxes.clear();
        relationBoxes.clear();
        probabilities.clear();

        for (String category : categories)
        {
            var desiredBox = new JComboBox<Integer>();
            for (int i = 0; i <= deck.getCategoryList(category).total(); i++)
                desiredBox.addItem(i);
            if (oldDesired.containsKey(category) && oldDesired.get(category) < deck.getCategoryList(category).total())
                desiredBox.setSelectedIndex(oldDesired.get(category));
            desiredBox.addActionListener((e) -> recalculate());
            desiredBoxes.put(category, desiredBox);

            var relationBox = new JComboBox<>(Relation.values());
            if (oldRelations.containsKey(category))
                relationBox.setSelectedItem(oldRelations.get(category));
            relationBox.addActionListener((e) -> recalculate());
            relationBoxes.put(category, relationBox);
        }

        recalculate();
        model.fireTableStructureChanged();
    }
}
