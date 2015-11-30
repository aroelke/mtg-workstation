package editor.gui.filter.editor.options;

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

import editor.gui.ButtonScrollPane;
import editor.gui.ScrollablePanel;
import editor.gui.filter.ComboBoxPanel;
import editor.gui.filter.FilterType;
import editor.gui.filter.editor.FilterEditorPanel;
import editor.util.Containment;

/**
 * This class represents a FilterPanel that presents a set of options
 * to the user to choose from to fill out the filter.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class OptionsFilterPanel<T> extends FilterEditorPanel
{
	/**
	 * Maximum number of rows to show in the list pane.
	 */
	public static final int MAX_ROWS = 7;
	
	/**
	 * Combo box indicating containment.
	 */
	protected ComboBoxPanel<Containment> contain;
	/**
	 * Options that can be chosen from.
	 */
	private T[] options;
	/**
	 * TODO: Comment this
	 */
	private List<JComboBox<T>> optionsBoxes;
	/**
	 * TODO: Comment this
	 */
	private ScrollablePanel optionsPanel;
	
	/**
	 * Create a new OptionsFilterPanel.
	 * 
	 * @param type Type of filter the new OptionsFilterPanel edits
	 */
	public OptionsFilterPanel(FilterType type, Containment[] c, T[] o)
	{
		super(type);
		setLayout(new BorderLayout());
		
		options = o;
		optionsBoxes = new ArrayList<JComboBox<T>>();
		
		// Set containment combo box
		add(contain = new ComboBoxPanel<Containment>(c), BorderLayout.WEST);
		
		// Panel showing combo boxes that allow the user to select items
		optionsPanel = new ScrollablePanel(ScrollablePanel.TRACK_HEIGHT);
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		addItem();
		add(new ButtonScrollPane(optionsPanel));
	}
	
	private JComboBox<T> addItem()
	{
		JPanel boxPanel = new JPanel(new BorderLayout());
		JComboBox<T> box = new JComboBox<T>(options);
		boxPanel.add(box, BorderLayout.CENTER);
		optionsBoxes.add(box);
		
		JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		JLabel addButton = new JLabel("+", JLabel.CENTER);
		Font buttonFont = new Font(addButton.getFont().getFontName(), Font.PLAIN, addButton.getFont().getSize() - 2);
		addButton.setAlignmentX(CENTER_ALIGNMENT);
		addButton.setFont(buttonFont);
		addButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e)
			{
				addItem();
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
		
		return box;
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public List<T> getSelectedValues()
	{
		return optionsBoxes.stream().map((b) -> b.getItemAt(b.getSelectedIndex())).distinct().collect(Collectors.toList());
	}
	
	/**
	 * @return <code>false</code>, since there should always be at least one drop-down
	 * available.
	 */
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	/**
	 * @return A String representation of this MultiOptionsFilterPanel's contents, which
	 * is its containment type followed by the selected options surrounded by braces.
	 */
	@Override
	protected String repr()
	{
		return contain.getSelectedItem().toString() + "{" + String.join(",", getSelectedValues().stream().map(String::valueOf).collect(Collectors.toList())) + "}";
	}
	
	/**
	 * Automatically selects values in the list from the given String.
	 * 
	 * @param content String to parse for values.
	 */
	@Override
	public void setContents(String content)
	{
		int index = content.indexOf('{');
		contain.setSelectedItem(Containment.get(content.substring(0, index)));
		String[] selectedOptions = content.substring(index + 1, content.length() - 1).split(",");
		for (String o: selectedOptions)
		{
			JComboBox<T> box;
			if (optionsBoxes.size() == 1)
				box = addItem();
			else
				box = optionsBoxes.get(0);
			for (T option: options)
				if (o.equalsIgnoreCase(option.toString()))
					box.setSelectedItem(option);
					
		}
	}
}
