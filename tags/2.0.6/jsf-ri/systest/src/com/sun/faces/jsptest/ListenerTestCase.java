/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.faces.jsptest;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.htmlunit.AbstractTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;

/**
 * <p>Test that invalid values don't cause valueChangeEvents to occur.</p>
 */

public class ListenerTestCase extends AbstractTestCase {

    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ListenerTestCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ListenerTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    public void testListener() throws Exception {
        HtmlPage page = getPage("/faces/listener.jsp");
        List list;
        list = getAllElementsOfGivenClass(page, null,
                HtmlTextInput.class);

        // set the initial value to be 1 for all input fields
        ((HtmlTextInput) list.get(0)).setValueAttribute("1");
        ((HtmlTextInput) list.get(1)).setValueAttribute("1");
        ((HtmlTextInput) list.get(2)).setValueAttribute("1");
        ((HtmlTextInput) list.get(3)).setValueAttribute("1");

        list = getAllElementsOfGivenClass(page, null,
                HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();
        assertTrue(-1 != page.asText().indexOf("text1 value was changed"));

        assertTrue(-1 != page.asText().indexOf("text2 value was changed"));

        assertTrue(-1 != page.asText().indexOf("text3 value was changed"));

        assertTrue(-1 != page.asText().indexOf("text4 value was changed"));

        // re-submit the form, make sure no valueChangeEvents are fired
        list = getAllElementsOfGivenClass(page, null,
                HtmlSubmitInput.class);
        button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();

        assertTrue(-1 == page.asText().indexOf("text1 value was changed"));
        assertTrue(-1 == page.asText().indexOf("text2 value was changed"));
        assertTrue(-1 == page.asText().indexOf("text3 value was changed"));
        assertTrue(-1 == page.asText().indexOf("text4 value was changed"));

        list = getAllElementsOfGivenClass(page, null,
                HtmlSubmitInput.class);
        button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();

        assertTrue(-1 != page.asText().indexOf("button1 was pressed"));

        button = (HtmlSubmitInput) list.get(1);
        page = (HtmlPage) button.click();

        assertTrue(-1 != page.asText().indexOf("button2 was pressed"));

        button = (HtmlSubmitInput) list.get(2);
        page = (HtmlPage) button.click();

        assertTrue(-1 != page.asText().indexOf("button3 was pressed"));

        button = (HtmlSubmitInput) list.get(3);
        page = (HtmlPage) button.click();

        assertTrue(-1 != page.asText().indexOf("button4 was pressed"));
    }
}
