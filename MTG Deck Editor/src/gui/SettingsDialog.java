package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog
{
	public SettingsDialog(JFrame owner)
	{
		super(owner, "Preferences", Dialog.ModalityType.APPLICATION_MODAL);
		this.setLocationRelativeTo(owner);
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Preferences");
		DefaultMutableTreeNode paths = new DefaultMutableTreeNode("Paths");
		root.add(paths);
		JTree tree = new JTree(root);
		getContentPane().add(tree, BorderLayout.WEST);
		
		JPanel panel = new JPanel();
		panel.setLayout(new CardLayout());
		getContentPane().add(panel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		JButton applyButton = new JButton("Apply");
		buttonPanel.add(applyButton);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener((e) -> dispose());
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		
		pack();
	}
}
