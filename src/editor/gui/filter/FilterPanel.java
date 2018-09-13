package editor.gui.filter;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import editor.filter.Filter;

/**
 * This class represents a panel that corresponds to a filter but
 * allows the user to edit its contents.
 *
 * TODO: Add a component to switch between only checking the front side of a card or checking all sides
 * TODO: Add a capability to comment filters
 * 
 * @param <F> Type of filter being edited
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public abstract class FilterPanel<F extends Filter> extends JPanel
{
    /**
     * Group that this FilterPanel belongs to.
     */
    protected FilterGroupPanel group;
    /**
     * Change listeners that have been registered with this FilterPanel.
     */
    private Set<ChangeListener> listeners;

    /**
     * Create a new FilterPanel that belongs to no group.
     */
    public FilterPanel()
    {
        super();
        group = null;
        listeners = new HashSet<>();
    }

    /**
     * Register a new #ChangeListener with this FilterPanel, which will fire
     * when panels or groups are added or removed.
     *
     * @param listener #ChangeListener to register
     */
    public void addChangeListener(ChangeListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Get the filter from this FilterPanel.
     *
     * @return the filter currently being edited by this FilterPanel.
     */
    public abstract Filter filter();

    /**
     * Indicate that a group or panel has been added or removed, and fire this
     * FilterPanel's listeners and all the listeners of its parents up the tree.
     */
    public void firePanelsChanged()
    {
        if (group != null)
            group.firePanelsChanged();
        for (ChangeListener listener : listeners)
            listener.stateChanged(new ChangeEvent(this));
    }

    /**
     * De-register a #ChangeListener with this FilterPanel, so it will no longer
     * fire.
     *
     * @param listener #ChangeListener to remove
     */
    public void removeChangeListener(ChangeListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Set the contents of this FilterPanel.  Any changes made in this
     * FilterPanel will not be reflected in the given #Filter; the value
     * returned by {@link #filter()} is a copy of it.
     *
     * @param filter filter containing the information that should
     *               be displayed by this FilterPanel.
     * @throws IllegalArgumentException if the given filter is of the wrong
     *                                  type
     */
    public abstract void setContents(F filter) throws IllegalArgumentException;
}
