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

package com.sun.faces.config.rules;


import java.util.Arrays;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.ManagedBeanBean;
import com.sun.faces.util.ToolsUtil;


/**
 * <p>Digester rule for the <code>&lt;managed-bean&gt;</code> element.</p>
 */

public class ManagedBeanRule extends FeatureRule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.ManagedBeanBean";

    private static final String[] SCOPES = {
        "none", "application", "session", "request"
    };

    static {
        Arrays.sort(SCOPES);
    }


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create an empty instance of <code>ManagedBeanBean</code>
     * and push it on to the object stack.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param attributes The attribute list of this element
     *
     * @exception IllegalStateException if the parent stack element is not
     *  of type FacesConfigBean
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {
        
        assert digester.peek() instanceof FacesConfigBean
              : "Assertion Error: Expected FacesConfigBean to be at the top of the stack";
       
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[ManagedBeanRule]{" +
                                       digester.getMatch() +
                                       "} Push " + CLASS_NAME);
        }
        Class clazz =
            digester.getClassLoader().loadClass(CLASS_NAME);
        ManagedBeanBean mbb = (ManagedBeanBean) clazz.newInstance();
        digester.push(mbb);

    }


    /**
     * <p>No body processing is required.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param text The text of the body of this element
     */
    public void body(String namespace, String name,
                     String text) throws Exception {
    }


    /**
     * <p>Pop the <code>ManagedBeanBean</code> off the top of the stack,
     * and either add or merge it with previous information.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     *
     * @exception IllegalStateException if the popped object is not
     *  of the correct type
     */
    public void end(String namespace, String name) throws Exception {

        ManagedBeanBean top;
        try {
            top = (ManagedBeanBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }

        validate(top);

        FacesConfigBean fcb = (FacesConfigBean) digester.peek();
        ManagedBeanBean old = fcb.getManagedBean(top.getManagedBeanName());
        if (old == null) {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[ManagedBeanRule]{" +
                                           digester.getMatch() +
                                           "} New(" +
                                           top.getManagedBeanName() +
                                           ")");
            }
            fcb.addManagedBean(top);
        } else {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[ManagedBeanRule]{" +
                                          digester.getMatch() +
                                          "} Merge(" +
                                          top.getManagedBeanName() +
                                          ")");
            }
            mergeManagedBean(top, old);
        }

    }


    /**
     * <p>No finish processing is required.</p>
     *
     */
    public void finish() throws Exception {
    }


    // ---------------------------------------------------------- Public Methods


    public String toString() {

        StringBuffer sb = new StringBuffer("ManagedBeanRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


    // Merge "top" into "old"
    static void mergeManagedBean(ManagedBeanBean top, ManagedBeanBean old) {

        // Merge singleton properties
        if (top.getManagedBeanClass() != null) {
            old.setManagedBeanClass(top.getManagedBeanClass());
        }
        if (top.getManagedBeanScope() != null) {
            old.setManagedBeanScope(top.getManagedBeanScope());
        }

        // Merge common collections
        mergeFeatures(top, old);

        // Merge unique collections
        ListEntriesRule.mergeListEntries(top, old);
        ManagedPropertyRule.mergeManagedProperties(top, old);
        MapEntriesRule.mergeMapEntries(top, old);

    }


    // --------------------------------------------------------- Private Methods

    /**
     * <p>Provides simple sanity checks.</p>
     * @param bean the <code>ManagedBeanBean</code> instance to validate
     */
    private void validate(ManagedBeanBean bean) {

        String val = bean.getManagedBeanName();
        if (val == null || val.length() == 0) {
            Locator locator = digester.getDocumentLocator();
            String documentName = "UNKNOWN";
            String lineNumber = "UNKNWOWN";

            if (locator != null) {
                documentName = locator.getSystemId();
                lineNumber = Integer.toString(locator.getLineNumber());
            }

            throw new IllegalStateException(ToolsUtil.getMessage(
                ToolsUtil.MANAGED_BEAN_NO_MANAGED_BEAN_NAME_ID,
                new Object[]{documentName, lineNumber}));
        }

        val = bean.getManagedBeanClass();
        if (val == null || val.length() == 0) {
            throw new IllegalStateException(ToolsUtil.getMessage(
                ToolsUtil.MANAGED_BEAN_NO_MANAGED_BEAN_CLASS_ID,
                new Object[]{ bean.getManagedBeanName() }));
        }

        val = bean.getManagedBeanScope();
        if (val == null || val.length() == 0) {
            throw new IllegalStateException(ToolsUtil.getMessage(
                ToolsUtil.MANAGED_BEAN_NO_MANAGED_BEAN_SCOPE_ID,
                new Object[]{ bean.getManagedBeanName() }));
        }

        if (Arrays.binarySearch(SCOPES, val) < 0) {
            throw new IllegalStateException(ToolsUtil.getMessage(
                ToolsUtil.MANAGED_BEAN_INVALID_SCOPE_ID,
                new Object[]{ val, bean.getManagedBeanName() }));
        }

        // - if the managed bean is itself a List, make sure it has no
        //   map entries or managed properties
        // - if the managed bean is itself a Map, make sure it has no
        //   managed properties
        if (bean.getListEntries() != null) {
            if (bean.getMapEntries() != null ||
                bean.getManagedProperties().length != 0) {
                throw new IllegalStateException (
                    ToolsUtil.getMessage(
                        ToolsUtil.MANAGED_BEAN_AS_LIST_CONFIG_ERROR_ID,
                        new Object[]{ bean.getManagedBeanName() }));
            }
        } else if (bean.getMapEntries() != null) {
            if (bean.getManagedProperties().length != 0) {
                throw new IllegalStateException (
                    ToolsUtil.getMessage(
                        ToolsUtil.MANAGED_BEAN_AS_MAP_CONFIG_ERROR_ID,
                        new Object[]{ bean.getManagedBeanName() }));
            }
        }

    } // END validate



}
