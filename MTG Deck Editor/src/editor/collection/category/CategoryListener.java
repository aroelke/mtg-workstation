package editor.collection.category;

import java.util.EventListener;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
@FunctionalInterface
public interface CategoryListener extends EventListener
{
	public void categoryChanged(CategoryEvent e);
}
