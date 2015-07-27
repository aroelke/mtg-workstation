package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JButton;

/**
 * TODO: Comment this
 * @author Alec
 *
 */
@SuppressWarnings("serial")
public class ColorButton extends JButton
{
	public Color color;
	private int border;
	
	public ColorButton(Color col, int b)
	{
		super(" ");
		color = col;
		border = b;
	}
	
	public ColorButton(Color col)
	{
		this(col, 5);
	}
	
	public ColorButton()
	{
		this(null);
		Random rand = new Random();
		color = Color.getHSBColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(color);
		g.fillRect(border, border, getWidth() - 2*border, getHeight() - 2*border);
	}
}
