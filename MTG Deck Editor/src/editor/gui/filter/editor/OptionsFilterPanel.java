package editor.gui.filter.editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.options.OptionsFilter;
import editor.gui.ButtonScrollPane;
import editor.gui.ScrollablePanel;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Containment;

@SuppressWarnings("serial")
public class OptionsFilterPanel<T> extends FilterEditorPanel<OptionsFilter<T>>
{
	public static <O> OptionsFilterPanel<O> create(OptionsFilter<O> f, O[] o)
	{
		OptionsFilterPanel<O> panel = new OptionsFilterPanel<O>(f.type, o);
		panel.setContents(f);
		return panel;
	}
	
	private FilterType type;
	private T[] options;
	private ScrollablePanel optionsPanel;
	private List<JComboBox<T>> optionsBoxes;
	private ComboBoxPanel<Containment> contain;
	
	protected OptionsFilterPanel(FilterType t, T[] o)
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
		add(new ButtonScrollPane(optionsPanel));
	}
	
	private void addItem(T value)
	{
		JPanel boxPanel = new JPanel(new BorderLayout());
		JComboBox<T> box = new JComboBox<T>(options);
		boxPanel.add(box, BorderLayout.CENTER);
		optionsBoxes.add(box);
		box.setSelectedItem(value);
		
		JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		JLabel addButton = new JLabel("+", JLabel.CENTER);
		Font buttonFont = new Font(addButton.getFont().getFontName(), Font.PLAIN, addButton.getFont().getSize() - 2);
		addButton.setAlignmentX(CENTER_ALIGNMENT);
		addButton.setFont(buttonFont);
		addButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
			{
				addItem(options[0]);
				optionsPanel.revalidate();
			}
		});
		JLabel removeButton = new JLabel("x", JLabel.CENTER);
		removeButton.setAlignmentX(CENTER_ALIGNMENT);
		removeButton.setFont(buttonFont);
		removeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (optionsBoxes.size() > 1)
				{
					optionsPanel.remove(boxPanel);
					optionsBoxes.remove(box);
					optionsPanel.revalidate();
				}
			}
		});
		buttonPanel.add(removeButton);
		buttonPanel.add(addButton);
		boxPanel.add(buttonPanel, BorderLayout.EAST);
		
		optionsPanel.add(boxPanel);
	}
	
	@Override
	public Filter filter()
	{
		@SuppressWarnings("unchecked")
		OptionsFilter<T> filter = (OptionsFilter<T>)type.createFilter();
		filter.contain = contain.getSelectedItem();
		filter.selected = optionsBoxes.stream().map((b) -> b.getItemAt(b.getSelectedIndex())).collect(Collectors.toSet());
		return filter;
	}

	@Override
	public void setContents(OptionsFilter<T> filter)
	{
		if (filter.type == type)
		{
			contain.setSelectedItem(filter.contain);
			optionsBoxes.clear();
			optionsPanel.removeAll();
			if (filter.selected.isEmpty())
				addItem(options[0]);
			else
				for (T selected: filter.selected)
					addItem(selected);
		}
		else
			throw new IllegalArgumentException("Options filter type " + filter.type.name() + " does not match type " + type.name());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof OptionsFilter && filter.type == type)
			setContents((OptionsFilter<T>)filter);
		else if (filter instanceof OptionsFilter)
			throw new IllegalArgumentException("Options filter type " + filter.type.name() + " does not match type " + type.name());
		else
			throw new IllegalArgumentException("Illegal options filter " + filter.type.name());
	}
}
