/*
 * $Id: SecretRenderer.java,v 1.37 2003/01/24 18:23:41 rkitain Exp $
 */

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

// SecretRenderer.java

package com.sun.faces.renderkit.html_basic;

import com.sun.faces.util.Util;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.AttributeDescriptor;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.mozilla.util.Assert;
import org.mozilla.util.Debug;
import org.mozilla.util.Log;
import org.mozilla.util.ParameterCheck;

/**
 *
 *  <B>SecretRenderer</B> is a class ...
 *
 * <B>Lifetime And Scope</B> <P>
 *
 * @version $Id: SecretRenderer.java,v 1.37 2003/01/24 18:23:41 rkitain Exp $
 * 
 * @see	Blah
 * @see	Bloo
 *
 */

public class SecretRenderer extends HtmlBasicRenderer {
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

    public SecretRenderer() {
        super();
    }

    //
    // Class methods
    //

    //
    // General Methods
    //

    //
    // Methods From Renderer
    //

    public boolean supportsComponentType(String componentType) {
        if ( componentType == null ) {
            throw new NullPointerException(Util.getExceptionMessage(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
        }
        return (componentType.equals(UIInput.TYPE));
    }

    public Object getConvertedValue(FacesContext context, UIComponent component,
        String newValue) throws IOException {
        Converter converter = getConverter(component);
        if (converter != null) {
            try {
                Object converted =
                    converter.getAsObject(context, component, newValue);
                return(converted);
            } catch (ConverterException e) {
                throw new IOException(e.getMessage());
            }
        } else {
            return newValue;
        }
    }

    public void encodeBegin(FacesContext context, UIComponent component) 
        throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException(Util.getExceptionMessage(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
        }
    }

    public void encodeChildren(FacesContext context, UIComponent component) 
        throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException(Util.getExceptionMessage(Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
        }
    }

    public void setPreviousValue(UIComponent component, Object value) {
        // component should be UIInput only..
        component.setAttribute(UIInput.PREVIOUS_VALUE, value);
    }

    protected void getEndTextToRender(FacesContext context, 
        UIComponent component, String currentValue, StringBuffer buffer ) {

        String inputClass = null;
        
        String redisplay = (String)component.getAttribute("redisplay");
        if (redisplay == null || !redisplay.equals("true")) {
            currentValue = "";
        }

        buffer.append("<input type=\"password\"");
        buffer.append(" name=\"");
        buffer.append(component.getClientId(context));
        buffer.append("\"");

        // render default text specified
        if (currentValue != null) {
            buffer.append(" value=\"");
            buffer.append(currentValue);
            buffer.append("\"");
        }
        buffer.append(Util.renderPassthruAttributes(context, component));
        buffer.append(Util.renderBooleanPassthruAttributes(context, component));
	if (null != (inputClass = (String) 
		     component.getAttribute("inputClass"))) {
	    buffer.append(" class=\"" + inputClass + "\" ");
	}
	
        buffer.append(">");         
    }

    protected String getFormattedValue(FacesContext context,
        UIComponent component, Object currentValue ) {

        Converter converter = getConverter(component);
        if (converter != null) {
            try {
                return converter.getAsString(context, component, currentValue);
            } catch (ConverterException e) {
                return currentValue.toString();
            }
        } else {
            return currentValue.toString();
        }
    }

} // end of class SecretRenderer
