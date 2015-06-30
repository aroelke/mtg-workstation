package gui.legality;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.StringJoiner;

import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class LegalityPanel extends JPanel
{
	public LegalityPanel(LegalityChecker legality)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel listsPanel = new JPanel(new GridLayout(1, 2));
		add(listsPanel);
		
		JPanel legalPanel = new JPanel(new BorderLayout());
		legalPanel.setBorder(new TitledBorder("Legal in:"));
		listsPanel.add(legalPanel);
		
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
		
		JPanel illegalPanel = new JPanel(new BorderLayout());
		illegalPanel.setBorder(new TitledBorder("Illegal in:"));
		listsPanel.add(illegalPanel);
		
		JList<String> illegalList = new JList<String>(legality.illegalFormats());
		illegalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		illegalPanel.add(new JScrollPane(illegalList), BorderLayout.CENTER);
		
		JPanel warningsPanel = new JPanel(new BorderLayout());
		warningsPanel.setBorder(new CompoundBorder(new TitledBorder("Warnings"), new BevelBorder(BevelBorder.LOWERED)));
		Dimension warningSize = warningsPanel.getPreferredSize();
		warningSize.height = 100;
		warningsPanel.setPreferredSize(warningSize);
		add(warningsPanel);
		
		JTextPane warningsPane = new JTextPane();
		warningsPane.setEditable(false);
		warningsPane.setFont(UIManager.getFont("Label.font"));
		warningsPanel.add(new JScrollPane(warningsPane), BorderLayout.CENTER);
		
		illegalList.addListSelectionListener((e) -> {
			StringJoiner str = new StringJoiner("\n\u2022 ", "\u2022 ", "");
			for (String warning: legality.getWarnings(illegalList.getSelectedValue()))
				str.add(warning);
			warningsPane.setText(str.toString());
			warningsPane.setCaretPosition(0);
		});
	}
}
