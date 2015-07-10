package gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTree;
import java.awt.CardLayout;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel
{
	public SettingsPanel()
	{
		super();
		setLayout(new BorderLayout());
		
		JTree tree = new JTree();
		add(tree, BorderLayout.WEST);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new CardLayout());
	}
}
