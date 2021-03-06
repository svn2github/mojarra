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

import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;

import com.sun.faces.facelets.FaceletContext;
import com.sun.faces.facelets.el.LegacyMethodBinding;
import com.sun.faces.facelets.tag.TagAttribute;
import com.sun.faces.facelets.tag.Metadata;
import com.sun.faces.facelets.tag.MetaRule;
import com.sun.faces.facelets.tag.MetadataTarget;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
final class ActionSourceRule extends MetaRule {

    public final static Class[] ACTION_SIG = new Class[0];

    public final static Class[] ACTION_LISTENER_SIG = new Class[] { ActionEvent.class };

    final static class ActionMapper extends Metadata {

        private final TagAttribute attr;

        public ActionMapper(TagAttribute attr) {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((ActionSource) instance).setAction(new LegacyMethodBinding(
                    this.attr.getMethodExpression(ctx, String.class,
                            ActionSourceRule.ACTION_SIG)));
        }
    }

    final static class ActionMapper2 extends Metadata {

        private final TagAttribute attr;

        public ActionMapper2(TagAttribute attr) {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((ActionSource2) instance).setActionExpression(this.attr
                    .getMethodExpression(ctx, String.class,
                            ActionSourceRule.ACTION_SIG));
        }

    }

    final static class ActionListenerMapper extends Metadata {

        private final TagAttribute attr;

        public ActionListenerMapper(TagAttribute attr) {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((ActionSource) instance)
                    .setActionListener(new LegacyMethodBinding(this.attr
                            .getMethodExpression(ctx, null,
                                    ActionSourceRule.ACTION_LISTENER_SIG)));
        }

    }

    final static class ActionListenerMapper2 extends Metadata {

        private final TagAttribute attr;

        public ActionListenerMapper2(TagAttribute attr) {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((ActionSource2) instance)
                    .addActionListener(new MethodExpressionActionListener(
                            this.attr.getMethodExpression(ctx, null,
                                    ActionSourceRule.ACTION_LISTENER_SIG)));

        }

    }

    public final static ActionSourceRule Instance = new ActionSourceRule();

    public ActionSourceRule() {
        super();
    }

    public Metadata applyRule(String name, TagAttribute attribute,
            MetadataTarget meta) {
        if (meta.isTargetInstanceOf(ActionSource.class)) {

            if ("action".equals(name)) {
                if (meta.isTargetInstanceOf(ActionSource2.class)) {
                    return new ActionMapper2(attribute);
                } else {
                    return new ActionMapper(attribute);
                }
            }

            if ("actionListener".equals(name)) {
                if (meta.isTargetInstanceOf(ActionSource2.class)) {
                    return new ActionListenerMapper2(attribute);
                } else {
                    return new ActionListenerMapper(attribute);
                }
            }
        }
        return null;
    }
}
