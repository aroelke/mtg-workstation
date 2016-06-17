package editor.gui.filter.editor;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import editor.database.characteristics.ManaCost;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.ManaCostFilter;
import editor.gui.filter.ComboBoxPanel;
import editor.util.Containment;

/**
 * This class represents a panel corresponding to a filter that groups
 * cards by mana cost.
 * 
 * TODO: Make this display the mana symbols filtered for instead of just their text
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class ManaCostFilterPanel extends FilterEditorPanel<ManaCostFilter>
{
	/**
	 * Combo box indicating containment.
	 */
	private ComboBoxPanel<Containment> contain;
	/**
	 * Text field for entering the text version of the mana cost.
	 */
	private JTextField cost;
//	private JTextPane cost;
//	private String manaStr = "";
	
	/**
	 * Create a new ManaCostFilterPanel.
	 */
	public ManaCostFilterPanel()
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		contain = new ComboBoxPanel<Containment>(Containment.values());
		add(contain);

		cost = new JTextField();
		cost.getDocument().addDocumentListener(new DocumentListener()
		{
			private void update(DocumentEvent e)
			{
				cost.setBackground(ManaCost.valueOf(cost.getText()) != null ? Color.WHITE : Color.PINK);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				update(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				update(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				update(e);
			}
		});
		add(cost);
/*
		cost = new JTextPane();
		cost.setBorder(new JTextField().getBorder());
		cost.setContentType("text/html");
		cost.setFont(UIManager.getFont("Label.font"));
		cost.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		((HTMLEditorKit)cost.getEditorKit()).setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));
		cost.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				if (e.getKeyChar() != KeyEvent.VK_ENTER)
				{
					if (e.getKeyChar() != KeyEvent.VK_BACK_SPACE)
						manaStr += e.getKeyChar();
					else if (!manaStr.isEmpty())
						manaStr = manaStr.substring(0, manaStr.length() - 1);
				}
				ManaCost mana = ManaCost.valueOf(manaStr);
				if (mana != null)
				{
					cost.setText(mana.toHTMLString());
					cost.setBackground(Color.WHITE);
				}
				else
				{
					cost.setText(manaStr);
					cost.setBackground(Color.PINK);
				}
				e.consume();
			}
		});
		add(cost);
*/
	}
	
	/**
	 * Create a new ManaCostFilterPanel using the given ManaCostFilter
	 * to set the contents of its fields.
	 * 
	 * @param f Filter to use for initialization
	 */
	public ManaCostFilterPanel(ManaCostFilter f)
	{
		this();
		setContents(f);
	}
	
	/**
	 * @return The ManaCostFilter that corresponds to the entries in
	 * this ManaCostFilterPanel's fields.
	 */
	@Override
	public Filter filter()
	{
		ManaCostFilter filter = new ManaCostFilter();
		filter.contain = contain.getSelectedItem();
//		filter.cost = ManaCost.valueOf(manaStr);
		filter.cost = ManaCost.valueOf(cost.getText());
		if (filter.cost == null)
			filter.cost = new ManaCost();
		return filter;
	}

	/**
	 * Set the contents of this ManaCostFilter's fields according
	 * to the contents of the given ManaCostFilter.
	 * 
	 * @param filter Filter to use for setting fields
	 */
	@Override
	public void setContents(ManaCostFilter filter)
	{
		contain.setSelectedItem(filter.contain);
//		cost.setText(manaStr = filter.cost.toHTMLString());
		cost.setText(filter.cost.toString());
	}

	/**
	 * Set the contents of this ManaCostFilter's fields according
	 * to the contents of the given FilterLeaf.
	 * 
	 * @param filter Filter to use for setting fields
	 * @throws IllegalArgumentException if the given filter is not
	 * a ManaCostFilter.
	 */
	@Override
	public void setContents(FilterLeaf<?> filter)
	{
		if (filter instanceof ManaCostFilter)
			setContents((ManaCostFilter)filter);
		else
			throw new IllegalArgumentException("Illegal mana cost filter " + filter.type.name());
	}
}
