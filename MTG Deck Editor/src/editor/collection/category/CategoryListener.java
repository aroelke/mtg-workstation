package editor.collection.category;

import java.util.EventListener;

/**
 * This is an interface for a listener that performs an action when a
 * {@link CategorySpec}'s parameters change.
 * 
 * @author Alec Roelke
 */
@FunctionalInterface
public interface CategoryListener extends EventListener
{
	/**
	 * Based on the specification changes specified by the given event,
	 * perform some action.
	 * 
	 * @param e event specifying changes to a category specification.
	 */
	public void categoryChanged(CategorySpec.Event e);
}
