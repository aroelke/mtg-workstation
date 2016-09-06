package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.StringJoiner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import editor.collection.LegalityChecker;

/**
 * This class represents a panel that shows the formats a deck is legal and illegal in.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class LegalityPanel extends JPanel
{
	/**
	 * Create a new LegalityPanel showing the legality of a deck.
	 * 
	 * @param legality Legality of a deck.  Make sure to have it calculate
	 * the legality of a deck, or nothing will be shown.
	 */
	public LegalityPanel(LegalityChecker legality)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(400, 250));
		
		// Panel containing format lists
		JPanel listsPanel = new JPanel(new GridLayout(1, 2));
		add(listsPanel);
		
		// Panel containing legal formats list
		JPanel legalPanel = new JPanel(new BorderLayout());
		legalPanel.setBorder(BorderFactory.createTitledBorder("Legal in:"));
		listsPanel.add(legalPanel);
		
		// Legal formats list.  Selection is disabled in this list
		JList<String> legalList = new JList<String>(legality.legalFormats());
		legalList.setSelectionModel(new DefaultListSelectionModel() {
			@Override
			public void setSelectionInterval(int index0, int index1)
			{
				super.setSelectionInterval(-1, -1);
			}
			
			@Override
			public int getSelectionMode()
			{
				return ListSelectionModel.SINGLE_SELECTION;
			}
		});
		legalPanel.add(new JScrollPane(legalList), BorderLayout.CENTER);
		
		// Panel containing illegal formats list
		JPanel illegalPanel = new JPanel(new BorderLayout());
		illegalPanel.setBorder(BorderFactory.createTitledBorder("Illegal in:"));
		listsPanel.add(illegalPanel);
		
		// Illegal formats list.  Only one element can be selected at a time.
		JList<String> illegalList = new JList<String>(legality.illegalFormats());
		illegalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		illegalPanel.add(new JScrollPane(illegalList), BorderLayout.CENTER);
		
		// Panel containing text box that shows why a deck is illegal in a format
		JPanel warningsPanel = new JPanel(new BorderLayout());
		warningsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Warnings"), BorderFactory.createLoweredBevelBorder()));
		add(warningsPanel);
		
		// Text box that shows reasons for illegality
		JTextPane warningsPane = new JTextPane();
		warningsPane.setEditable(false);
		warningsPane.setFont(UIManager.getFont("Label.font"));
		warningsPanel.add(new JScrollPane(warningsPane), BorderLayout.CENTER);
		
		// Click on a list element to show why it is illegal
		illegalList.addListSelectionListener((e) -> {
			StringJoiner str = new StringJoiner("\n• ", "• ", "");
			for (String warning: legality.getWarnings(illegalList.getSelectedValue()))
				str.add(warning);
			warningsPane.setText(str.toString());
			warningsPane.setCaretPosition(0);
		});
	}
}
