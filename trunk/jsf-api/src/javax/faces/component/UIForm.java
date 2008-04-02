/*
 * $Id: UIForm.java,v 1.24 2003/03/13 01:11:58 craigmcc Exp $
 */

/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.faces.component;


/**
 * <p><strong>UIForm</strong> is a {@link UIComponent} that represents an
 * input form to be presented to the user, and whose child components represent
 * (among other things) the input fields to be included when the form is
 * submitted.</p>
 *
 * <p>By default, the <code>rendererType</code> property is set to
 * "<code>Form</code>".  This value can be changed by calling the
 * <code>setRendererType()</code> method.</p>
 */

public class UIForm extends UIComponentBase {


    // ------------------------------------------------------- Static Variables


    /**
     * The component type of this {@link UIComponent} subclass.
     */
    public static final String TYPE = "javax.faces.component.UIForm";


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Create a new {@link UIForm} instance with default property
     * values.</p>
     */
    public UIForm() {

        super();
        setRendererType("Form");

    }


    // ------------------------------------------------------------- Properties


    public String getComponentType() {

        return (TYPE);

    }


    /**
     * <p>The form name for this {@link UIForm}.</p>
     */
    private String formName = null;


    /**
     * <p>Return the form name for this {@link UIForm}.</p>
     */
    public String getFormName() {

        return (this.formName);

    }


    /**
     * <p>Set the form name for this {@link UIForm}.</p>
     *
     * @param formName The new form name
     */
    public void setFormName(String formName) {

        this.formName = formName;

    }

    
}
