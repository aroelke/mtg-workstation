package editor.collection.category;

import java.util.EventListener;

/**
 * This is an interface for a listener that performs an action when
 * a CategorySpec's parameters change.
 * 
 * @author Alec Roelke
 */
@FunctionalInterface
public interface CategoryListener extends EventListener
{
	/**
	 * Based on the CategorySpec changes specified by the given
	 * CategoryEvent, perform some action.
	 * 
	 * @param e CategoryEvent specifying changes to a CategorySpec.
	 */
	public void categoryChanged(CategoryEvent e);
}
