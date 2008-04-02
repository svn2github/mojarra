/*
 * $Id: DebugUtil.java,v 1.15 2003/09/24 23:17:37 horwat Exp $
 */

/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.faces.util;

// DebugUtil.java

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.model.SelectItem;

import java.util.Iterator;

import java.io.PrintStream;

/**
 *
 *  <B>DebugUtil</B> is a class ...
 *
 * <B>Lifetime And Scope</B> <P>
 *
 * @version $Id: DebugUtil.java,v 1.15 2003/09/24 23:17:37 horwat Exp $
 * 
 * @see	Blah
 * @see	Bloo
 *
 */

public class DebugUtil extends Object
{
//
// Protected Constants
//

//
// Class Variables
//

public static boolean keepWaiting = true;

private static int curDepth = 0;

//
// Instance Variables
//

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

public DebugUtil()
{
    super();
    // ParameterCheck.nonNull();
    this.init();
}

protected void init()
{
    // super.init();
}

//
// Class methods
//

/** 

* Usage: <P>

* Place a call to this method in the earliest possible entry point of
* your servlet app.  It will cause the app to enter into an infinite
* loop, sleeping until the static var keepWaiting is set to false.  The
* idea is that you attach your debugger to the servlet, then, set a
* breakpont in this method.  When it is hit, you use the debugger to set
* the keepWaiting class var to false.

*/

public static void waitForDebugger() {
    while (keepWaiting) {
	try {
	    Thread.sleep(5000);
	}
	catch (InterruptedException e) {
	    System.out.println("DebugUtil.waitForDebugger(): Exception: " + 
			       e.getMessage());
	}
    }
}

private static void indentPrintln(PrintStream out, String str)
{
    int i = 0;
    
    // handle indentation
    for (i = 0; i < curDepth; i++) {
	out.print("  ");
    }
    out.print(str + "\n");
}

public static void printTree(UIComponent root, PrintStream out) 
{
    if (null == root) {
	return;
    }
    int i = 0;
    Object value = null;
    
/* PENDING
    indentPrintln(out, "===>Type:" + root.getComponentType());
*/
    indentPrintln(out, "id:"+root.getId());

    if ( root instanceof ValueHolder) {
        ValueHolder valueHolder = (ValueHolder) root;
        value = valueHolder.getValue();
        if (valueHolder.getValueRef() != null) {
	    indentPrintln(out, "valueReference: "+ valueHolder.getValueRef());
        }
    }
    
    Iterator items = null;
    SelectItemWrapper curItemWrapper = null;
    SelectItem curItem = null;
    int j = 0;

    if (root instanceof javax.faces.component.UISelectOne) {
	items = Util.getSelectItemWrappers(null, root);
	indentPrintln(out, " {");
	while (items.hasNext()) {
            curItemWrapper = (SelectItemWrapper) items.next();
            curItem = curItemWrapper.getSelectItem();
	    indentPrintln(out, "\t value=" + curItem.getValue() + 
			  " label=" + curItem.getLabel() + " description=" + 
			  curItem.getDescription());
	}
	indentPrintln(out, " }");
    } else {
        indentPrintln(out, "value= " + value );
	
	Iterator it = root.getAttributes().keySet().iterator();
	if (it != null) {
	    while (it.hasNext()) {
		String attrValue = null, attrName = (String)it.next();
		Object attrObj = root.getAttributes().get(attrName);
		
		if (!(attrValue instanceof String)) {
		    // chop off the address since we don't want to print
		    // out anything that'll vary from invocation to
		    // invocation
		    attrValue = attrObj.toString();
		    int at = 0;
		    boolean doTruncate = false;
		    if (-1 == (at = attrValue.indexOf("$"))) {
			if (-1 != (at = attrValue.indexOf("@"))) {
			    doTruncate = true;
			}
		    }
		    else {
			doTruncate = true;
		    }
		    
		    if (doTruncate) {
			attrValue = attrValue.substring(0, at);
		    }
		}
		else {
		    attrValue = (String) attrObj;
		}
		    
		indentPrintln(out, "attr=" + attrName + 
			      " : " + attrValue); 
	    }
	}
    }

    curDepth++;
    Iterator it = root.getChildren().iterator();
    while (it.hasNext()) {
	printTree((UIComponent) it.next(), out);
    }
    curDepth--;
}
//
// General Methods
//


} // end of class DebugUtil
