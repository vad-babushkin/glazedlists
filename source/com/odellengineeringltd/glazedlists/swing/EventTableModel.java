/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.swing;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import java.awt.GridBagLayout;
import java.awt.Point;
// for responding to user actions
import javax.swing.event.*;
import java.awt.event.*;
// tables for displaying lists
import javax.swing.table.*;
// standard collections as support
import java.util.*;

/**
 * A table model that holds an event list.
 *
 * <p>The EventTableModel class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the event dispatch thread.
 * To do this programmatically, use <code>SwingUtilities.invokeAndWait()</code>.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part1/index.html#listtable">Glazed
 * Lists Tutorial Part 1 - Basics</a>
 *
 * @see SwingUtilities#invokeAndWait(Runnable)
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventTableModel extends AbstractTableModel implements ListEventListener {

    /** the complete list of messages before filters */
    protected EventList source;

    /** Specifies how to render table headers and sort */
    private TableFormat tableFormat;

    /** Reusable table event for broadcasting changes */
    private MutableTableModelEvent tableModelEvent;
    
    /** whenever a list change covers greater than this many rows, redraw the whole thing */
    public int changeSizeRepaintAllThreshhold = Integer.MAX_VALUE;
    
    /**
     * Creates a new table that renders the specified list in the specified
     * format.
     */
    public EventTableModel(EventList source, TableFormat tableFormat) {
        this.source = source;
        tableModelEvent = new MutableTableModelEvent(this);
        this.tableFormat = tableFormat;

        // prepare listeners
        source.addListEventListener(new EventThreadProxy(this));
    }
    
    /**
     * Gets the Table Format.
     */
    public TableFormat getTableFormat() {
        return tableFormat;
    }
    /**
     * Sets this table to be rendered by a different table format. This has
     * some very important consequences. The selection will be lost - this is
     * due to the fact that the table formats may have different numbers of
     * columns. Another consequence is that the entire table will require
     * repainting. In a ScrollPane, only the currently displayed cells and
     * those above (before) them will require repainting. In order to provide
     * the best performance, the scroll pane may be scrolled to the top to
     * prevent a delay while rendering off-screen cells.
     */
    public void setTableFormat(TableFormat tableFormat) {
        this.tableFormat = tableFormat;
        tableModelEvent.setStructureChanged();
        fireTableChanged(tableModelEvent);
    }
    
    /**
     * For implementing the ListEventListener interface. This sends changes
     * to the table which can repaint the table cells. Because this class uses
     * a EventThreadProxy, it is guaranteed that all natural
     * calls to this method use the Swing thread.
     *
     * <p>This tests the size of the change to determine how to handle it. If the
     * size of the change is greater than the changeSizeRepaintAllThreshhold,
     * then the entire table is notified as changed. Otherwise only the descrete
     * areas that changed are notified.
     */
    public void listChanged(ListEvent listChanges) {
        // when all events hae already been processed by clearing the event queue
        if(!listChanges.hasNext()) return;

        // notify all changes simultaneously
        if(listChanges.getBlocksRemaining() >= changeSizeRepaintAllThreshhold) {
            listChanges.clearEventQueue();
            // first scroll to row zero
            //tableScrollPane.getViewport().setViewPosition(table.getCellRect(0, 0, true).getLocation());
            fireTableDataChanged();

        // for all changes, one block at a time
        } else {
            while(listChanges.nextBlock()) {
                // get the current change info
                int startIndex = listChanges.getBlockStartIndex();
                int endIndex = listChanges.getBlockEndIndex();
                int changeType = listChanges.getType();
                // create a table model event for this block
                tableModelEvent.setValues(startIndex, endIndex, changeType);
                fireTableChanged(tableModelEvent);
            }
        }
    }

    /**
     * Fetch the name for the specified column.
     */
    public String getColumnName(int column) {
        return tableFormat.getFieldName(column);
    }
    
    /**
     * The number of rows equals the number of entries in the source event list.
     */
    public int getRowCount() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.size();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * Get the column count as specified by the table format.
     */
    public int getColumnCount() {
        return tableFormat.getFieldCount();
    }

    /**
     * Retrieves the value at the specified location from the table.
     * 
     * <p>Before every get, we need to validate the row because there may be an
     * update waiting in the event queue. For example, it is possible that
     * the source list has been updated by a database thread. Such a change
     * may have been sent as notification, but after this request in the
     * event queue. In the case where a row is no longer available, null is
     * returned. The value returned is insignificant in this case because the
     * Event queue will very shortly be repainting (or removing) the row
     * anyway.
     */
    public Object getValueAt(int row, int column) {
        source.getReadWriteLock().readLock().lock();
        try {
            // ensure that this value still exists before retrieval
            if(row < getRowCount()) {
                return tableFormat.getFieldValue(source.get(row), column);
            } else {
                return null;
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * The list table is not editable. For an editable list table, use the
     * WritableListTable instead.
     */
    public boolean isCellEditable(int row, int column) {
        // ensure this is a writable table
        if(tableFormat instanceof WritableTableFormat) {
            WritableTableFormat writableTableFormat = (WritableTableFormat)tableFormat;
            // test if this cell is editable
            return writableTableFormat.isFieldEditable(column);
        // this is not a writable table
        } else {
            return false;
        }
    }

    /**
     * The list table is not editable. For an editable list table, use the
     * WritableListTable instead.
     */
    public void setValueAt(Object editedValue, int row, int column) {
        // ensure this is a writable table
        if(tableFormat instanceof WritableTableFormat) {
            WritableTableFormat writableTableFormat = (WritableTableFormat)tableFormat;
            // get the object being edited from the source list
            Object baseObject = source.get(row);
            // tell the table format to set the value based on what it knows
            writableTableFormat.setFieldValue(baseObject, editedValue, column);
        // this is not a writable table
        } else {
            throw new UnsupportedOperationException("Unexpected set() on read-only table");
        }
    }
    
    /**
     * Gets the minimum number of changes that will be combined into one uniform
     * change and cause selection and scrolling to be lost.
     */
    public int getRepaintAllThreshhold() {
        return changeSizeRepaintAllThreshhold;
    }
    
    /**
     * Sets the threshhold of the number of change blocks that will be handled
     * individually before the ListTable collapses such changes into one and simply
     * repaints the entire table. This is a work around to the JTable's poor
     * performance when handling large sets of small changes. <strong>This
     * work-around is only necessary when the JTable has variable row height</strong>.
     * When the JTable has a fixed row height, there is no performance problem and
     * this work around is unnecessary.
     *
     * <p>Two problems occur when using this work around. It will cause the table's
     * selection to be destroyed and it will cause the table's scrolling to be lost.
     *
     * <p>By default, this work around is disabled and users must enable it by calling
     * <code>setRepaintAllThreshhold()</code> to enable it. In practice, tests have shown
     * that 100 is a decent value for the repaintAllThreshhold of tables that have variable
     * height rows.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=30">Bug 30</a>
     * @see <a href="https://glazedlists.dev.java.net/tutorial/part8/index.html#rowheight">Glazed
     * Lists Tutorial Part 8 - Performance Tuning</a>
     */
    public void setRepaintAllThreshhold(int repaintAllThreshhold) {
        this.changeSizeRepaintAllThreshhold = repaintAllThreshhold;
    }
}