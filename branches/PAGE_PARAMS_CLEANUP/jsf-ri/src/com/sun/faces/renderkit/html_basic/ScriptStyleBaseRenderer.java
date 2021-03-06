/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.faces.renderkit.html_basic;

import com.sun.faces.util.FacesLogger;
import java.io.IOException;
import java.util.Map;

import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;

/**
 * <p>Base class for shared behavior between Script and Stylesheet renderers.
 * Maybe composition would be better, but inheritance is easier</p>
 */
@ListenerFor(systemEventClass=PostAddToViewEvent.class)
public abstract class ScriptStyleBaseRenderer extends Renderer implements ComponentSystemEventListener {
    
    
    // Log instance for this class
    protected static final Logger logger = FacesLogger.RENDERKIT.getLogger();

    /*
     * Indicates that the component associated with this Renderer has already
     * been added to the facet in the view.
     */ 

    /* When this method is called, we know that there is a component
     * with a script renderer somewhere in the view.  We need to make it
     * so that when an element with a name given by the value of the optional
     * "target" component attribute is encountered, this component 
     * can be called upon to render itself.
     * This method will add the component (associated with this Renderer)
     * to a facet in the view only if a "target" component attribute is set.
     * 
     */
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        UIComponent component = event.getComponent();
        FacesContext context = FacesContext.getCurrentInstance();

        String target = verifyTarget((String) component.getAttributes().get("target"));
        if (target != null) {
            context.getViewRoot().addComponentResource(context, component, target);
        }
    }
    
    @Override
    public final void decode(FacesContext context, UIComponent component) {
        // no-op
    }

    @Override
    public final boolean getRendersChildren() {
        return true;
    }

    @Override
    public final void encodeBegin(FacesContext context, UIComponent component)
          throws IOException {
        // no-op
    }
        
    @Override
    public final void encodeChildren(FacesContext context, UIComponent component)
          throws IOException {
        Map<String,Object> attributes = component.getAttributes();

        String name = (String) attributes.get("name");
        int childCount = component.getChildCount();
        boolean renderChildren = (0 < childCount);
        
        // If we have no "name" attribute...
        if (null == name) {
            // and no child content...
            if (0 == childCount) {
                // this is user error, so put up a message if desired
                if (ProjectStage.Production != context.getApplication().getProjectStage()) {
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "outputScript with no library, no name, and no body content",
                            "Is body content intended?");
                    context.addMessage(component.getClientId(context), message);
                }
                // We have no children, but don't bother with the method 
                // invocation anyway.
                renderChildren = false;
            }            
        } else if (0 < childCount) {
            // If we have a "name" and also have child content, ignore
            // the child content and log a message.
            logger.info("outputScript with \"name\" attribute and nested content.  Ignoring nested content.");
            renderChildren = false;
        }
        if (renderChildren) {
            ResponseWriter writer = context.getResponseWriter();
            startElement(writer, component);
            super.encodeChildren(context, component);
            endElement(writer);
        }
        
    }
    /**
     * <p>Allow the subclass to customize the start element content</p>
     */
    protected abstract void startElement(ResponseWriter writer, 
            UIComponent component) throws IOException;
    
    /**
     * <p>Allow the subclass to customize the start element content</p>
     */
    protected abstract void endElement(ResponseWriter writer) throws IOException;
    
    /**
     * <p>Allow a subclass to control what's a valid value for "target".
     */
    protected String verifyTarget(String toVerify) {
        return toVerify;
    }

}
