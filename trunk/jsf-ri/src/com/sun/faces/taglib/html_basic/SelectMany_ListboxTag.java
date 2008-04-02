/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * $Id: SelectMany_ListboxTag.java,v 1.10 2003/09/08 20:10:16 jvisvanathan Exp $
 *
 * (C) Copyright International Business Machines Corp., 2001,2002
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U. S. Copyright Office.   
 */

package com.sun.faces.taglib.html_basic;

import javax.servlet.jsp.JspException;
import javax.faces.component.UISelectMany;

import com.sun.faces.taglib.FacesTag;

/**
 * This class is the tag handler that evaluates the 
 * <code>selectmany_listbox</code> custom tag.
 */

public class SelectMany_ListboxTag extends FacesTag
{
    //
    // Protected Constants
    //

    //
    // Class Variables
    //

    //
    // Instance Variables
    //

    // Attribute Instance Variables

    // Relationship Instance Variables

    //
    // Constructors and Initializers    
    //

    public SelectMany_ListboxTag()
    {
        super();
    }

    //
    // Class methods
    //

    // 
    // Accessors
    //

    //
    // General Methods
    //

    public String getRendererType() { 
        return "Listbox"; 
    }
    public String getComponentType() { 
        return "SelectMany"; 
    }

    //
    // Methods from TagSupport
    // 

    public int doEndTag() throws JspException {
        int rc = super.doEndTag();
        return rc;
    }


} // end of class SelectMany_MenuTag
