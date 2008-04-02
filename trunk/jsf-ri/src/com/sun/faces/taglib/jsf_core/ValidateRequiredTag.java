/*
 * $Id: ValidateRequiredTag.java,v 1.13 2005/05/19 13:27:00 rlubke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

// ValidateRequiredTag.java

package com.sun.faces.taglib.jsf_core;

import javax.faces.context.FacesContext;
import javax.servlet.jsp.JspException;
import javax.el.ValueExpression;
import javax.el.ExpressionFactory;


/**
 * <B>ValidateRequiredTag</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 * @version $Id: ValidateRequiredTag.java,v 1.13 2005/05/19 13:27:00 rlubke Exp $
 */

public class ValidateRequiredTag extends ValidatorTag {

    private static final long serialVersionUID = -4925861676709072353L;
    private static ValueExpression VALIDATOR_ID_EXPR = null;

//
// Constructors and Initializers    
//

    public ValidateRequiredTag() {
        super();
        if (VALIDATOR_ID_EXPR == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExpressionFactory factory = context.getApplication().
                    getExpressionFactory();
            VALIDATOR_ID_EXPR =
                factory.createValueExpression(context.getELContext(), 
                    "javax.faces.Required", String.class);
        }
    }

//
// Class methods
//

//
// General Methods
//

    public int doStartTag() throws JspException {
        super.setValidatorId(VALIDATOR_ID_EXPR);
        return super.doStartTag();
    }


// 
// Methods from ValidatorTag
// 

} // end of class ValidateRequiredTag
