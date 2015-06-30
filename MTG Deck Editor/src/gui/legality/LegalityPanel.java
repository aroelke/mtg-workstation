package gui.legality;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.StringJoiner;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
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
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {0, 0};
		layout.columnWeights = new double[] {1.0, 1.0};
		layout.rowHeights = new int[] {0, 0};
		layout.rowWeights = new double[] {1.0, 0.5};
		setLayout(layout);
		
		JPanel legalPanel = new JPanel(new BorderLayout());
		legalPanel.setBorder(new TitledBorder("Legal in:"));
		GridBagConstraints legalConstraints = new GridBagConstraints();
		legalConstraints.gridx = 0;
		legalConstraints.gridy = 0;
		legalConstraints.fill = GridBagConstraints.BOTH;
		add(legalPanel, legalConstraints);
		
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
		legalPanel.add(legalList, BorderLayout.CENTER);
		
		JPanel illegalPanel = new JPanel(new BorderLayout());
		illegalPanel.setBorder(new TitledBorder("Illegal in:"));
		GridBagConstraints illegalConstraints = new GridBagConstraints();
		illegalConstraints.gridx = 1;
		illegalConstraints.gridy = 0;
		illegalConstraints.fill = GridBagConstraints.BOTH;
		add(illegalPanel, illegalConstraints);
		
		JList<String> illegalList = new JList<String>(legality.illegalFormats());
		illegalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		illegalPanel.add(illegalList, BorderLayout.CENTER);
		
		JPanel warningsPanel = new JPanel(new BorderLayout());
		warningsPanel.setBorder(new CompoundBorder(new TitledBorder("Warnings"), new BevelBorder(BevelBorder.LOWERED)));
		GridBagConstraints warningsConstraints = new GridBagConstraints();
		warningsConstraints.gridx = 0;
		warningsConstraints.gridy = 1;
		warningsConstraints.gridwidth = 2;
		warningsConstraints.fill = GridBagConstraints.BOTH;
		add(warningsPanel, warningsConstraints);
		
		JTextPane warningsPane = new JTextPane();
		warningsPane.setEditable(false);
		warningsPane.setContentType("text/html");
		warningsPane.setFont(UIManager.getFont("Label.font"));
		warningsPanel.add(warningsPane, BorderLayout.CENTER);
		
		illegalList.addListSelectionListener((e) -> {
			StringJoiner join = new StringJoiner("</li>\n<li>", "<ul>\n<li>", "</li>\n</ul>");
			for (String warning: legality.getWarnings(illegalList.getSelectedValue()))
				join.add(warning);
			warningsPane.setText(join.toString());
		});
	}
}
