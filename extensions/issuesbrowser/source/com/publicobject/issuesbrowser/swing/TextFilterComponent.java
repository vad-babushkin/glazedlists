/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import javax.swing.*;

import com.publicobject.issuesbrowser.Issue;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TextFilterComponent implements FilterComponent<Issue> {

    private JTextField filterEdit = new JTextField(15);
    private TextComponentMatcherEditor<Issue> textComponentMatcherEditor = new TextComponentMatcherEditor<Issue>(filterEdit, null);

    public String getName() {
        return "Filter";
    }

    public JComponent getComponent() {
        return filterEdit;
    }

    public MatcherEditor<Issue> getMatcherEditor() {
        return textComponentMatcherEditor;
    }
}