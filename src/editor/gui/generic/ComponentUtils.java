package editor.gui.generic;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

/**
 * This class contains static methods that perform useful operations on Swing components.
 * 
 * @author Alec Roelke
 */
public interface ComponentUtils
{
	/**
	 * Set the font of the given component, and then repeat this process for all of its
	 * children if it is a container.
	 * 
	 * @param component component to set the font of
	 * @param f font to set the component and its children to
	 */
	static void changeFontRecursive(Component component, Font f)
	{
		component.setFont(f);
		if (component instanceof Container)
			for (Component child: ((Container)component).getComponents())
				changeFontRecursive(child, f);
	}
}
