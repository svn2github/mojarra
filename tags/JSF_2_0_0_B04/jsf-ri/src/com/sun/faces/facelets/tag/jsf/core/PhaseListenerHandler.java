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

package com.sun.faces.facelets.tag.jsf.core;

import java.io.IOException;
import java.io.Serializable;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import com.sun.faces.facelets.FaceletContext;
import com.sun.faces.facelets.FaceletException;
import com.sun.faces.facelets.tag.TagAttribute;
import com.sun.faces.facelets.tag.TagAttributeException;
import com.sun.faces.facelets.tag.TagConfig;
import com.sun.faces.facelets.tag.TagException;
import com.sun.faces.facelets.tag.TagHandler;
import com.sun.faces.facelets.tag.jsf.ComponentSupport;
import com.sun.faces.facelets.util.ReflectionUtil;

public class PhaseListenerHandler extends TagHandler {

    private final static class LazyPhaseListener implements PhaseListener,
                                                            Serializable {

        private static final long serialVersionUID = -6496143057319213401L;

        private final String type;

        private final ValueExpression binding;

        public LazyPhaseListener(String type, ValueExpression binding) {
            this.type = type;
            this.binding = binding;
        }

        private PhaseListener getInstance() {
            PhaseListener instance = null;
            FacesContext faces = FacesContext.getCurrentInstance();
            if (faces == null) {
                return null;
            }
            if (this.binding != null) {
                instance = (PhaseListener) binding.getValue(faces
                      .getELContext());
            }
            if (instance == null && type != null) {
                try {
                    instance = (PhaseListener) ReflectionUtil.forName(
                          this.type).newInstance();
                } catch (Exception e) {
                    throw new AbortProcessingException(
                          "Couldn't Lazily instantiate PhaseListener", e);
                }
                if (this.binding != null) {
                    binding.setValue(faces.getELContext(), instance);
                }
            }
            return instance;
        }

        public void afterPhase(PhaseEvent event) {
            PhaseListener pl = this.getInstance();
            if (pl != null) {
                pl.afterPhase(event);
            }
        }

        public void beforePhase(PhaseEvent event) {
            PhaseListener pl = this.getInstance();
            if (pl != null) {
                pl.beforePhase(event);
            }
        }

        public PhaseId getPhaseId() {
            PhaseListener pl = this.getInstance();
            return (pl != null) ? pl.getPhaseId() : PhaseId.ANY_PHASE;
        }

    }

    private final TagAttribute binding;

    private final String listenerType;

    public PhaseListenerHandler(TagConfig config) {
        super(config);
        TagAttribute type = this.getAttribute("type");
        this.binding = this.getAttribute("binding");
        if (type != null) {
            if (!type.isLiteral()) {
                throw new TagAttributeException(type,
                                                "Must be a literal class name of type PhaseListener");
            } else {
                // test it out
                try {
                    ReflectionUtil.forName(type.getValue());
                } catch (ClassNotFoundException e) {
                    throw new TagAttributeException(type,
                                                    "Couldn't qualify PhaseListener", e);
                }
            }
            this.listenerType = type.getValue();
        } else {
            this.listenerType = null;
        }
    }

    public void apply(FaceletContext ctx, UIComponent parent)
          throws IOException, FacesException, FaceletException, ELException {
        if (ComponentSupport.isNew(parent)) {
            UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
            if (root == null) {
                throw new TagException(this.tag, "UIViewRoot not available");
            }
            ValueExpression b = null;
            if (this.binding != null) {
                b = this.binding.getValueExpression(ctx, PhaseListener.class);
            }

            PhaseListener pl = new LazyPhaseListener(this.listenerType, b);

            root.addPhaseListener(pl);
        }
    }
}
