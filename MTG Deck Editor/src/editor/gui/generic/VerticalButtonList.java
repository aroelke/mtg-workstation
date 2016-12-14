package editor.gui.generic;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * This class represents a panel that contains several {@link JButton}s arranged
 * in a vertical line centered in the panel.  Each button will expand to fill the
 * horizontal space.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class VerticalButtonList extends JPanel
{
	/**
	 * List of buttons.
	 */
	private List<JButton> buttonsList;
	/**
	 * Map of button text onto the corresponding button.
	 */
	private Map<String, JButton> buttonsMap;
	
	/**
	 * Create a new ButtonList.
	 * 
	 * @param texts text of each button
	 */
	public VerticalButtonList(String... texts)
	{
		this(Arrays.asList(texts));
	}
	
	/**
	 * Create a new ButtonList.
	 * 
	 * @param texts text of each button
	 */
	public VerticalButtonList(Collection<String> texts)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		buttonsList = texts.stream().map(JButton::new).collect(Collectors.toList());
		buttonsMap = buttonsList.stream().collect(Collectors.toMap(JButton::getText, Function.identity()));
		add(Box.createVerticalGlue());
		for (JButton button: buttonsList)
		{
			add(button);
			button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getMaximumSize().height));
		}
		add(Box.createVerticalGlue());
	}
	
	/**
	 * Get the button at the specified index.
	 * 
	 * @param index index to search
	 * @return the button at the specified index.
	 * @throws IndexOutOfBoundsException if the index is too big or -1
	 */
	public JButton get(int index) throws IndexOutOfBoundsException
	{
		return buttonsList.get(index);
	}
	
	/**
	 * Get the button with the specified text.
	 * 
	 * @param text text to search for a button for
	 * @return the button with the given text, or null if there isn't one.
	 */
	public JButton get(String text)
	{
		return buttonsMap.get(text);
	}
}
