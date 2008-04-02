/*
 * $Id: Validator.java,v 1.6 2003/01/17 01:47:01 craigmcc Exp $
 */

/*
 * Copyright 2002-2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.faces.validator;


import java.io.Serializable;
import java.util.Iterator;
import javax.faces.component.AttributeDescriptor;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.Message;


/**
 * <p>A <strong>Validator</strong> is a class that can perform validation
 * (correctness checks) on a {@link UIComponent}.  Zero or more
 * <code>Validator</code>s can be associated with each {@link UIComponent}
 * in the request tree, and are called during the <em>Process
 * Validations Phase</em>.</p>
 *
 * <p>Individual <code>Validator</code>s should examine the component that
 * they are passed, and add {@link Message} instances to the
 * {@link FacesContext} for the current request, documenting
 * any failures to conform to the required rules.  In general, such
 * messages should be associated with the {@link UIComponent} on which
 * the validation failure occurred.  In addition, the <code>valid</code>
 * property of the corresponding {@link UIComponent} should be set to
 * <code>false</code>.</p>
 *
 * <p>For maximum generality, <code>Validator</code> instances should be
 * configurable based on properties of the {@link Validator} implementation
 * class.  For example, a range check {@link Validator} might support
 * configuration of the minimum and maximum values to be used.</p>
 */

public interface Validator extends Serializable {


    /**
     * <p>Perform the correctness checks implemented by this
     * {@link Validator} against the specified {@link UIComponent}.
     * If any violations are found:</p>
     * <ul>
     * <li>Add zero or more {@link Message}s to the specified
     *     {@link FacesContext}, specifying this {@link UIComponent} as
     *     associated with the message, describing the nature of the
     *     violation(s) encountered.</li>
     * <li>Set the <code>valid</code> property on the specified
     *     {@link UIComponent} to <code>false</code>.</li>
     * </ul>
     *
     * @param context FacesContext for the request we are processing
     * @param component UIComponent we are checking for correctness
     *
     * @exception NullPointerException if <code>context</code>
     *  or <code>component</code> is <code>null</code>
     */
    public void validate(FacesContext context, UIComponent component);


}
