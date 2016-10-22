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

import com.jidesoft.swing.SimpleScrollPane;

import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.options.OptionsFilter;
import editor.gui.generic.ComboBoxPanel;
import editor.gui.generic.ScrollablePanel;
import editor.util.Containment;
import editor.util.MouseListenerFactory;
import editor.util.PopupMenuListenerFactory;

/**
 * This class represents a panel that corresponds to a filter that groups
 * cards according to a characteristic that takes on distinct values.  Unlike
 * other filter panels, which can be switched among different types of filters
 * as long as they are the same class, OptionsFilterPanel cannot.
 *
 * @author Alec Roelke
 *
 * @param <T> Type that the options presented have
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
	 * Type of filter this OptionsFilterPanel edits.
	 */
	private String type;
	/**
	 * List of options that are available to choose from.
	 */
	private T[] options;
	/**
	 * Panel displaying the combo boxes to be used to choose
	 * options.
	 */
	private ScrollablePanel optionsPanel;
	/**
	 * List of boxes displaying the currently-selected options.
	 */
	private List<JComboBox<T>> optionsBoxes;
	/**
	 * Set containment combo box.
	 */
	private ComboBoxPanel<Containment> contain;

	/**
	 * Create a new OptionsFilterPanel.
	 *
	 * @param t Type of the new OptionsFilterPanel
	 * @param o List of options to choose from
	 */
	public OptionsFilterPanel(String t, T[] o)
	{
		super();
		setLayout(new BorderLayout());

		type = t;
		options = o;
		optionsBoxes = new ArrayList<JComboBox<T>>();

		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain, BorderLayout.WEST);

		optionsPanel = new ScrollablePanel(ScrollablePanel.TRACK_HEIGHT);
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		SimpleScrollPane optionsPane = new SimpleScrollPane(optionsPanel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		optionsPane.setBorder(BorderFactory.createEmptyBorder());
		add(optionsPane, BorderLayout.CENTER);
	}

	/**
	 * Create a new OptionsFilterPanel using the given filter to initialize its
	 * fields and the given array to specify the set of options to choose from.
	 *
	 * @param f Filter to use for initialization
	 * @param o List of options to choose from
	 */
	public OptionsFilterPanel(OptionsFilter<T> f, T[] t)
	{
		this(f.type, t);
		setContents(f);
	}

	/**
	 * Add a new combo box for an additional option.
	 *
	 * @param value Initial value of the new combo box.
	 */
	private void addItem(T value)
	{
		JPanel boxPanel = new JPanel(new BorderLayout());
		JComboBox<T> box = new JComboBox<T>(options);
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
		JLabel removeButton = new JLabel("\u2212", JLabel.CENTER);
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

	/**
	 * @return The OptionsFilter corresponding to the fields of this
	 * OptionsFilterPanel.
	 */
	@Override
	public Filter filter()
	{
		@SuppressWarnings("unchecked")
		OptionsFilter<T> filter = (OptionsFilter<T>)FilterFactory.createFilter(type);
		filter.contain = contain.getSelectedItem();
		filter.selected = optionsBoxes.stream().map((b) -> b.getItemAt(b.getSelectedIndex())).collect(Collectors.toSet());
		return filter;
	}

	/**
	 * If the given filter's type is the same as this OptionsFilterPanel's type,
	 * add combo boxes and set their values to reflect its settings.
	 *
	 * @param filter Filter to set fields from
	 * @throws IllegalArgumentException if the given filter is not the same
	 * type as this OptionsFilterPanel
	 */
	@Override
	public void setContents(OptionsFilter<T> filter)
	{
		if (filter.type.equals(type))
		{
			contain.setSelectedItem(filter.contain);
			if (options.length == 0)
				contain.setVisible(false);
			optionsBoxes.clear();
			optionsPanel.removeAll();
			if (filter.selected.isEmpty() && options.length > 0)
				addItem(options[0]);
			else
				for (T selected: filter.selected)
					addItem(selected);
		}
		else
			throw new IllegalArgumentException("Options filter type " + filter.type + " does not match type " + type);
	}

	/**
	 * If the given filter's type is the same as this OptionsFilterPanel's type,
	 * add combo boxes and set their values to reflect its settings.
	 *
	 * @param filter Filter to set fields from
	 * @throws IllegalArgumentException if the given filter is not the same
	 * type as this OptionsFilterPanel or isn't even an OptionsFilter
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof OptionsFilter && filter.type.equals(type))
			setContents((OptionsFilter<T>)filter);
		else if (filter instanceof OptionsFilter)
			throw new IllegalArgumentException("Options filter type " + filter.type + " does not match type " + type);
		else
			throw new IllegalArgumentException("Illegal options filter " + filter.type);
	}
}
