/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.util.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * Tests that an event list can depend upon a single source via multiple independent
 * transformations.
 *
 * @see <a href="https://glazedlists.dev.java.net/servlets/ReadMsg?list=users&msgNo=117">Users list #117</a>
 * @see <a href="https://glazedlists.dev.java.net/servlets/ReadMsg?list=users&msgNo=214">Users list #214</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class MultipleSourcesTest extends TestCase {

    /**
     * Prepare for the test.
     */
    public void setUp() {
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
    }

    /**
     * Tests whether an EventList can depend upon multiple sources simultaneously.
     * This test populates a source list and two transformation lists that depend
     * upon it. There is also a listener that 'depends' upon both of these
     * transformation lists. When the source changes, the transformation lists will
     * be notified one at a time. This verifies that Glazed Lists behaves correctly
     * after the first has been notified but before the second has been notified.
     *
     * <p>This test currently fails. A fix should be available within the next
     * couple of weeks!
     */
    public void testMultipleSources() {
        BasicEventList source = new BasicEventList();
        EasyFilterList filterOne = new EasyFilterList(source);
        EasyFilterList filterTwo = new EasyFilterList(source);
        
        source.add("Game Cube");
        source.add("Genesis");
        source.add("XBox");
        source.add("PlayStation");
        source.add("Turbo Graphics 16");
        

        List filterLists = new ArrayList();
        filterLists.add(filterOne);
        filterLists.add(filterTwo);
        MultipleSourcesListener filtersListener = new MultipleSourcesListener(filterLists);
        
        source.clear();

        source.add("Atari 2600");
        source.add("Intellivision");
        source.add("Game Gear");

        filterOne.setMatch(true);
        filterOne.setMatch(false);
    }
    
    /**
     * Listens to multiple sources, and when one source changes, this iterates all
     * sources.
     */
    class MultipleSourcesListener implements ListEventListener {
        private List sources;
        public MultipleSourcesListener(List sources) {
            this.sources = sources;
            for(Iterator i = sources.iterator(); i.hasNext(); ) {
                EventList eventList = (EventList)i.next();
                eventList.addListEventListener(this);
            }
        }
        public void listChanged(ListEvent e) {
            e.clearEventQueue();
            for(Iterator i = sources.iterator(); i.hasNext(); ) {
                EventList eventList = (EventList)i.next();
                eventList.toArray();
            }
        }
    }

    /**
     * Simple TextFilterator for Strings.
     */
    class EasyFilterList extends AbstractFilterList {
        public boolean match = true;
        public EasyFilterList(EventList source) {
            super(source);
        }
        public boolean filterMatches(Object element) {
            return match;
        }
        public void setMatch(boolean match) {
            this.match = match;
            handleFilterChanged();
        }
    }
}