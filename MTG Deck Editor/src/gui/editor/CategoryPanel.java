package gui.editor;

import gui.CardTable;
import gui.CardTableModel;
import gui.ColorButton;
import gui.SettingsDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import database.Card;
import database.Deck;
import database.characteristics.CardCharacteristic;

/**
 * This class represents a panel that shows information about a category in a deck.
 * 
 * TODO: Make it so that each category can have its own columns
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryPanel extends JPanel
{
	/**
	 * Number of rows in the card table to display.
	 */
	public static final int MAX_ROWS_TO_DISPLAY = 6;
	
	/**
	 * Category in the Deck data structure.
	 */
	private Deck.Category category;
	/**
	 * Table to display the contents of the category.
	 */
	protected CardTable table;
	/**
	 * Model to tell the table how to display the contents of the category.
	 */
	private CardTableModel model;
	/**
	 * Label showing the number of cards in the category.
	 */
	private JLabel countLabel;
	/**
	 * Button for editing the category.
	 */
	protected JButton editButton;
	/**
	 * Button to remove the category.
	 */
	protected JButton removeButton;
	/**
	 * Button displaying and allowing editing of the category's color.
	 */
	protected ColorButton colorButton;
	/**
	 * Border showing the name of the category.
	 */
	private TitledBorder border;
	/**
	 * Default background color of this panel.
	 */
	private Color background;
	/**
	 * Timer timing flashing of the border of this panel when it is skipped to.
	 */
	private Timer flashTimer;
	
	/**
	 * Create a new CategoryPanel.
	 * 
	 * @param n Name of the new category
	 * @param r String representation of the new category
	 * @param whitelist Set of Cards that should always be included in the new category
	 * @param blacklist Set of Cards that should never be included in the new category
	 * @param col Color of the new category
	 * @param p Filter for the new category
	 * 
	 * @param cat Category for the new panel to display
	 * @param editor EditorFrame containing the new category
	 */
	public CategoryPanel(Deck.Category cat, EditorFrame editor)
	{
		super();
		category = cat;
		background = getBackground();
		flashTimer = new FlashTimer();
		
		// Each category is surrounded by a border with a title
		border = new TitledBorder(category.name());
		setBorder(border);
		
		setLayout(new BorderLayout());
		
		// Label showing the number of cards in the category
		JPanel countPanel = new JPanel();
		countPanel.setLayout(new BorderLayout(0, 0));
		countLabel = new JLabel("Cards: " + category.size());
		countLabel.setVerticalAlignment(SwingConstants.TOP);
		countPanel.add(countLabel, BorderLayout.WEST);
		
		// Panel containing edit and remove buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		colorButton = new ColorButton(category.color());
		buttonPanel.add(colorButton);
		editButton = new JButton("…");
		buttonPanel.add(editButton);
		removeButton = new JButton("−");
		buttonPanel.add(removeButton);
		countPanel.add(buttonPanel, BorderLayout.EAST);
		
		add(countPanel, BorderLayout.NORTH);
		
		// Table showing the cards in the category
		model = new CardTableModel(editor, category, Arrays.stream(SettingsDialog.getSetting(SettingsDialog.EDITOR_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList()));
		table = new CardTable(model)
		{
			@Override
			public Dimension getPreferredScrollableViewportSize()
			{
				Dimension d = getPreferredSize();
				d.height = getRowHeight()*Math.min(MAX_ROWS_TO_DISPLAY, category.size());
				return d;
			}
		};
		table.setAutoCreateRowSorter(true);
		table.setStripeColor(SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.EDITOR_STRIPE)));
		JScrollPane tablePane = new JScrollPane(table);
		tablePane.addMouseWheelListener(new PDMouseWheelListener(tablePane));
		tablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(tablePane, BorderLayout.CENTER);
	}
	
	/**
	 * Update the GUI to reflect changes in a category.
	 */
	public void update()
	{
		model.fireTableDataChanged();
		countLabel.setText("Cards: " + category.total());
		border.setTitle(category.name());
		table.revalidate();
		table.repaint();
		colorButton.setColor(category.color());
		colorButton.repaint();
		revalidate();
		repaint();
	}
	
	/**
	 * @return The name of the category.
	 */
	public String name()
	{
		return category.name();
	}
	
	/**
	 * @return The String representation of the category.
	 * @see CategoryDialog#setContents(String)
	 */
	@Override
	public String toString()
	{
		return category.toString();
	}
	
	/**
	 * @return The Card filter for the category.
	 */
	public Predicate<Card> filter()
	{
		return category.filter();
	}
	
	/**
	 * @return The category's whitelist.
	 */
	public Set<Card> whitelist()
	{
		return category.whitelist();
	}
	
	/**
	 * @return The category's blacklist
	 */
	public Set<Card> blacklist()
	{
		return category.blacklist();
	}
	
	/**
	 * Change the parameters of the category.
	 * 
	 * @param newName New name for the category
	 * @param newRepr New String representation of the category's filter
	 * @param newFilter New filter for the category
	 */
	public void edit(String newName, Color newColor, String newRepr, Predicate<Card> newFilter)
	{
		if (!category.edit(newName, newColor, newRepr, newFilter))
			throw new IllegalArgumentException("Category \"" + newName + "\" already exists");
		update();
	}
	
	/**
	 * @return The list of Cards corresponding to the selected rows in the category's table.
	 */
	public List<Card> getSelectedCards()
	{
		return Arrays.stream(table.getSelectedRows())
					 .mapToObj((r) -> category.get(table.convertRowIndexToModel(r)))
					 .collect(Collectors.toList());
	}
	
	/**
	 * @param c Card to check for
	 * @return <code>true</code> if the category contains the given Card, and
	 * <code>false</code> otherwise.
	 */
	public boolean contains(Card c)
	{
		return category.contains(c);
	}
	
	/**
	 * Include the given Card in the category.  This will remove it from
	 * the blacklist if it is in the blacklist.  No copies of the Card will
	 * be added to the deck.
	 * 
	 * @param c Card to include in the category
	 * @return <code>true</code> if the category was changed as a result of
	 * the inclusion and <code>false</code> otherwise.
	 */
	public boolean include(Card c)
	{
		boolean changed = category.include(c);
		update();
		return changed;
	}
	
	/**
	 * Exclude the given Card from the category.  This will remove it from
	 * the whitelist if it is in the whitelist.  No copies of the Card will be
	 * removed from the deck.
	 * 
	 * @param c Card to exclude from the category
	 * @return <code>true</code> if the category was changed as a result of the
	 * exclusion and <code>false</code> otherwise.
	 */
	public boolean exclude(Card c)
	{
		boolean changed = category.exclude(c);
		update();
		return changed;
	}
	
	/**
	 * Change the columns to display in this category's table.
	 * 
	 * @param columns CardCharacteristics to display.
	 */
	public void setColumns(List<CardCharacteristic> columns)
	{
		model.setColumns(columns);
	}
	
	/**
	 * Change the color of alternate stripes in this category's table.
	 * 
	 * @param color Color to change stripes to.
	 */
	public void setStripeColor(Color color)
	{
		table.setStripeColor(color);
	}
	
	public void flash()
	{
		flashTimer.restart();
	}
	
	/**
	 * This class represents a mouse wheel listener that returns mouse wheel control to an outer scroll
	 * pane when this one's scroll pane has reached a limit.
	 * 
	 * It is adapted from a StackOverflow answer to the same problem, which can be found at
	 * @link{http://stackoverflow.com/questions/1377887/jtextpane-prevents-scrolling-in-the-parent-jscrollpane}.
	 * 
	 * @author Nemi
	 * @since November 24, 2009
	 */
	private class PDMouseWheelListener implements MouseWheelListener
	{
		private JScrollBar bar;
		private int previousValue = 0;
		private JScrollPane parentScrollPane;
		private JScrollPane parent;

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

		public PDMouseWheelListener(JScrollPane p)
		{
			parent = p;
			bar = parent.getVerticalScrollBar();
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
		
		private int getMax()
		{
			return bar.getMaximum() - bar.getVisibleAmount();
		}
		
		private MouseWheelEvent cloneEvent(MouseWheelEvent e)
		{
			return new MouseWheelEvent(getParentScrollPane(), e.getID(), e
					.getWhen(), e.getModifiers(), 1, 1, e
					.getClickCount(), false, e.getScrollType(), e
					.getScrollAmount(), e.getWheelRotation());
		}
	}
	
	/**
	 * This class represents a timer controlling the flash of the panel when it is skipped to.
	 * The flash will last 400ms.
	 * 
	 * @author Alec Roelke
	 */
	private class FlashTimer extends Timer
	{
		/**
		 * Amount of ticks until the flash disappears.
		 */
		private final int END = 20;
		/**
		 * Color of the flash.
		 */
		private final Color FLASH = Color.CYAN;
		
		/**
		 * Progress of the flash.
		 */
		private int count;
		
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
					double ratio = (double)count/(double)END;
					int r = (int)(FLASH.getRed() + (background.getRed() - FLASH.getRed())*ratio);
					int g = (int)(FLASH.getGreen() + (background.getGreen() - FLASH.getGreen())*ratio);
					int b = (int)(FLASH.getBlue() + (background.getBlue() - FLASH.getBlue())*ratio);
					setBackground(new Color(r, g, b));
					repaint();
				}
			});
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
		
		/**
		 * Restart the timer, resetting the progress of the flash.
		 */
		@Override
		public void restart()
		{
			count = 0;
			super.restart();
		}
	}
}
