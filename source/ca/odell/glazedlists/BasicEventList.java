/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// for iterators and sublists
import ca.odell.glazedlists.util.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// volatile implementation support
import ca.odell.glazedlists.util.impl.*;
// Java collections are used for underlying data storage
import java.util.*;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;
// for being serializable
import java.io.Serializable;

/**
 * An event list that wraps a Java Collections list. This list provides an
 * event notifying interface to a plain Java list. This may be useful to wrap
 * filtering or sorting on to an existing list, or to simply receive events
 * when a list changes.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial-0.9.1/">Glazed
 * Lists Tutorial</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class BasicEventList extends AbstractEventList {

    /** the underlying data list */
    private List data;

    /**
     * Creates a new EventArrayList that uses an ArrayList as the source list
     * implementation.
     */
    public BasicEventList() {
        this(new ArrayList());
    }
    
    /**
     * Creates a new EventArrayList that uses the specified list as the source
     * list. All editing to the specified source list <strong>must</strong> be
     * done through the BasicEventList interface. Otherwise the two lists will
     * become out of sync and the BasicEventList will fail.
     */
    public BasicEventList(List list) {
        data = list;
        readWriteLock = new J2SE12ReadWriteLock();
    }

    /**
     * Inserts the specified element at the specified position in this list.
     */
    public void add(int index, Object element) {
        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addInsert(index);
            // do the actual add
            data.add(index, element);
            // fire the event
            updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
            
    /**
     * Appends the specified element to the end of this list.
     */
    public boolean add(Object element) {
        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addInsert(size());
            // do the actual add
            boolean result = data.add(element);
            // fire the event
            updates.commitEvent();
            return result;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Appends all of the elements in the specified Collection to the end of
     * this list, in the order that they are returned by the specified 
     * Collection's Iterator.
     */
    public boolean addAll(Collection collection) {
        return addAll(size(), collection);
    }
          
    /**
     * Inserts all of the elements in the specified Collection into this
     * list, starting at the specified position.
     */
    public boolean addAll(int index, Collection collection) {
        // don't do an add of an empty set
        if(collection.size() == 0) return false;

        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addInsert(index, index + collection.size() - 1);
            // do the actual add
            boolean result = data.addAll(index, collection);
            // fire the event
            updates.commitEvent();
            return result;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Appends all of the elements in the specified array to the end of
     * this list.
     */
    public boolean addAll(Object[] objects) {
        return addAll(size(), objects);
    }

    /**
     * Inserts all of the elements in the specified array into this
     * list, starting at the specified position.
     */
    public boolean addAll(int index, Object[] objects) {
        // don't do an add of an empty set
        if(objects.length == 0) return false;

        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addInsert(index, index + objects.length - 1);
            // do the actual add
            boolean overallResult = true;
            boolean elementResult = true;
            for(int i = 0; i < objects.length; i++) {
                elementResult = data.add(objects[i]);
                overallResult = (overallResult && elementResult);
            }
            // fire the event
            updates.commitEvent();
            return overallResult;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Removes the element at the specified position in this list.
     */
    public Object remove(int index) {
        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addDelete(index);
            // do the actual remove
            Object removed = data.remove(index);
            // fire the event
            updates.commitEvent();
            return removed;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    /**
     * Removes a single instance of the specified element from this 
     * collection, if it is present (optional operation).
     *
     * This uses indexOf and remove(index) to do the actual remove.
     */
    public boolean remove(Object element) {
        getReadWriteLock().writeLock().lock();
        try {
            int index = data.indexOf(element);
            if(index == -1) return false;
            remove(index);
            return true;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
          
    /**
     * Removes all of the elements from this list (optional operation).
     */
    public void clear() {
        getReadWriteLock().writeLock().lock();
        try {
            // don't do a clear on an empty set
            if(size() == 0) return;
            // create the change event
            updates.beginEvent();
            updates.addDelete(0, size() - 1);
            // do the actual clear
            data.clear();
            // fire the event
            updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Replaces the element at the specified position in this list with the 
     * specified element.
     */
    public Object set(int index, Object element) {
        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addUpdate(index);
            // do the actual set
            Object previous = data.set(index, element);
            // fire the event
            updates.commitEvent();
            return previous;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
          
    /**
     * Returns the element at the specified position in this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public Object get(int index) {
        return data.get(index);
    }

    /**
     * Returns the number of elements in this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public int size() {
        return data.size();
    }

    /**
     * Removes from this collection all of its elements that are contained
     * in the specified collection (optional operation). This method has been
     * is available in this implementation, although the not particularly
     * high performance.
     */
    public boolean removeAll(Collection collection) {
        getReadWriteLock().writeLock().lock();
        try {
            boolean changed = false;
            updates.beginEvent();
            for(Iterator i = collection.iterator(); i.hasNext(); ) {
                int index = -1;
                if((index = data.indexOf(i.next())) != -1) {
                    updates.addDelete(index);
                    data.remove(index);
                    changed = true;
                }
            }
            updates.commitEvent();
            return changed;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Retains only the elements in this collection that are contained in 
     * the specified collection (optional operation). This method is available
     * in this implementation, although not particularly high performance.
     */
    public boolean retainAll(Collection collection) {
        getReadWriteLock().writeLock().lock();
        try {
            boolean changed = false;
            updates.beginEvent();
            int index = 0;
            while(index < data.size()) {
                if(collection.contains(data.get(index))) {
                    index++;
                } else {
                    updates.addDelete(index);
                    data.remove(index);
                    changed = true;
                }
            }
            updates.commitEvent();
            return changed;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
}
