/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package ajaxrequest;

import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * This <code>PhaseListener</code> executes before Apply Request Values 
 * Phase of the request processing lifecycle.  
 */
public class RequestPhaseListener implements PhaseListener {


    public RequestPhaseListener() {
    }

    public void afterPhase(PhaseEvent event) {
    }

    public void beforePhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        if (context.getPartialViewContext().getExecuteIds().isEmpty()) {
            context.getPartialViewContext().getExecuteIds().add("ajax10");
        }
        if (context.getPartialViewContext().getRenderIds().isEmpty()) {
            context.getPartialViewContext().getRenderIds().add("form");
            UIForm form = (UIForm)context.getViewRoot().findComponent("form");
            if (form != null) {
                UIComponent component = form.findComponent("_0");
                setInitialRender(component);
            }
        }
        if (context.getPartialViewContext().isExecuteAll() &&
            context.getPartialViewContext().getRenderIds().isEmpty()) {
            context.getPartialViewContext().getRenderIds().add("form");
            UIForm form = (UIForm)context.getViewRoot().findComponent("form");
            if (form != null) {
                UIComponent component = form.findComponent("_0");
                setExecuteNoRender(component);
            }
        }

    }

    public PhaseId getPhaseId() {
        return PhaseId.APPLY_REQUEST_VALUES;
    }

    private void setExecuteNoRender(UIComponent component) {
        Iterator<UIComponent> kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            setExecuteNoRender(kid);
        }
        component.getAttributes().put("styleClass", "execute-norender");
    }

    private void setInitialRender(UIComponent component) {
        Iterator<UIComponent> kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            setInitialRender(kid);
        }
        if (component.getAttributes().get("styleClass") != null) {
            component.getAttributes().remove("styleClass");
        }
    }

}
