/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.faces.facelets.tag.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELException;
import javax.el.Expression;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.webapp.pdl.AttachedObjectHandler;

import com.sun.faces.facelets.Facelet;
import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;
import com.sun.faces.facelets.FaceletFactory;
import com.sun.faces.facelets.el.VariableMapperWrapper;
import com.sun.faces.facelets.tag.TagAttribute;
import com.sun.faces.facelets.tag.TagAttributes;
import com.sun.faces.util.RequestStateManager;
import javax.el.MethodExpression;
import javax.faces.application.ViewHandler;

/**
 * RELEASE_PENDING (rlubke,driscoll) document
 */
public class CompositeComponentTagHandler extends ComponentHandler {
    
    CompositeComponentTagHandler(Resource compositeComponentResource,
            ComponentConfig config) {
        super(config);
        this.compositeComponentResource = compositeComponentResource;
    }
    
    private void copyTagAttributesIntoComponentAttributes(FaceletContext ctx,
            UIComponent compositeComponent) {
        TagAttributes tagAttributes = this.tag.getAttributes();
        TagAttribute attrs[] = tagAttributes.getAll();
        String name, value;
        ExpressionFactory expressionFactory = null;
        Expression expression = null;
        for (int i = 0; i < attrs.length; i++) {
            name = attrs[i].getLocalName();
            if (null != name && 0 < name.length() && 
                !name.equals("id") && !name.equals("binding")){
                value = attrs[i].getValue();
                if (null != value && 0 < value.length()) {
                    // lazily initialize this local variable
                    if (null == expressionFactory) {
                        expressionFactory = ctx.getFacesContext().getApplication().
                                getExpressionFactory();
                    }
                    if (value.startsWith("#{")) {
                        expression = expressionFactory.
                                createValueExpression(ctx, value, Object.class);
                    } else {
                        expression = expressionFactory.
                                createValueExpression(value, Object.class);
                    }
                    // PENDING: I don't think copyTagAttributesIntoComponentAttributes
                    // should be getting called 
                    // on postback, yet it is.  In lieu of a real fix, I'll
                    // make sure I'm not overwriting a MethodExpression with a 
                    // ValueExpression.
                    Map<String, Object> map = compositeComponent.getAttributes();
                    boolean doPut = true;
                    if (map.containsKey(name)) {
                        Object curVal = map.get(name);
                        if (curVal instanceof MethodExpression) {
                            doPut = false;
                        }
                    }
                    if (doPut) {
                        map.put(name, expression);
                    }
                }
            }
        }
        
    }
    
    private Resource compositeComponentResource;
    
    

    @Override
    protected UIComponent createComponent(FaceletContext ctx) {
        UIComponent result = null;
        FacesContext context = ctx.getFacesContext();
        result = context.getApplication().createComponent(context, compositeComponentResource);

        return result;
    }
    
    @Override
    protected void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException, FacesException, ELException {
        // Allow any nested elements that reside inside the markup element
        // for this tag to get applied
        super.applyNextHandler(ctx, c);
        // Apply the facelet for this composite component
        applyCompositeComponent(ctx, c);
        // Allow any PDL declared attached objects to be retargeted
        if (ComponentSupport.isNew(c)) {
            FacesContext context = ctx.getFacesContext();
            ViewHandler viewHandler = context.getApplication().getViewHandler();
            viewHandler.retargetAttachedObjects(context, c,
                    getAttachedObjectHandlers(c, false));
            viewHandler.retargetMethodExpressions(context, c);
        }

    }
    
    private void applyCompositeComponent(FaceletContext ctx, UIComponent c) {
        Facelet f = null;
        FacesContext facesContext = ctx.getFacesContext();
        FaceletFactory factory = (FaceletFactory)
              RequestStateManager.get(facesContext, RequestStateManager.FACELET_FACTORY);
        VariableMapper orig = ctx.getVariableMapper();
        
        UIPanel facetComponent = null;
        if (ComponentSupport.isNew(c)) {
            facetComponent = (UIPanel)
             facesContext.getApplication().createComponent("javax.faces.Panel");
            facetComponent.setRendererType("javax.faces.Group");
            c.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, facetComponent);
        }
        else {
            facetComponent = (UIPanel) 
                    c.getFacets().get(UIComponent.COMPOSITE_FACET_NAME);
        }
        assert(null != facetComponent);
        
        try {
            f = factory.getFacelet(compositeComponentResource.getURL());
            copyTagAttributesIntoComponentAttributes(ctx, c);
            VariableMapper wrapper = new VariableMapperWrapper(orig) {

                @Override
                public ValueExpression resolveVariable(String variable) {
                    return super.resolveVariable(variable);
                }
                
            };
            ctx.setVariableMapper(wrapper);
            f.apply(facesContext, facetComponent);
        } catch (IOException ex) {
            Logger.getLogger(CompositeComponentTagHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FaceletException ex) {
            Logger.getLogger(CompositeComponentTagHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FacesException ex) {
            Logger.getLogger(CompositeComponentTagHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ELException ex) {
            Logger.getLogger(CompositeComponentTagHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            ctx.setVariableMapper(orig);
        }

    }

    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component) {
        return getAttachedObjectHandlers(component, true);
    }
    
    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component,
            boolean create) {
        Map<String, Object> attrs = component.getAttributes();
        List<AttachedObjectHandler> result = (List<AttachedObjectHandler>)
                attrs.get("javax.faces.RetargetableHandlers");
        
        if (null == result) {
            if (create) {
                result = new ArrayList<AttachedObjectHandler>();
                attrs.put("javax.faces.RetargetableHandlers", result);
            }
            else {
                result = Collections.EMPTY_LIST;
            }
        }
        return result;
    }
    
    
    
}
