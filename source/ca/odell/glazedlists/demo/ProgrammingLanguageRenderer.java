/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

import ca.odell.glazedlists.migrationkit.*;
import javax.swing.*;

/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class ProgrammingLanguageRenderer extends StyledDocumentRenderer {
    
    public ProgrammingLanguageRenderer() {
        super(true);
    }

    public void writeObject(JTable table, Object value, 
        boolean isSelected, boolean hasFocus, int row, int column) {
        if(value == null) return;
        
        ProgrammingLanguage language = (ProgrammingLanguage)value;
        
        append(language.getName(), strong);
        append(", ", strong);
        append(language.getYear(), strong);
        append("\n", plain);
        append(language.getDescription(), plain);
        
    }
}