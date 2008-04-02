/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package renderkits.renderkit.svg;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import renderkits.util.Util;

/**
 * <B>BaseRenderer</B> is a base class for implementing renderers
 * for <code>SVGRenderKit</code>.
 */

public abstract class BaseRenderer extends Renderer {

    protected static Logger logger =
          Util.getLogger(Util.FACES_LOGGER + Util.RENDERKIT_LOGGER);

    /** @return true if this renderer should render an id attribute. */
    protected boolean shouldWriteIdAttribute(UIComponent component) {
        String id;
        return (null != (id = component.getId()) &&
                !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX));
    }

    protected void writeIdAttributeIfNecessary(FacesContext context,
                                               ResponseWriter writer,
                                               UIComponent component) {
        String id;
        if (shouldWriteIdAttribute(component)) {
            try {
                writer.writeAttribute("id", component.getClientId(context),
                                      "id");
            } catch (IOException e) {
                if (logger.isLoggable(Level.WARNING)) {
                    // PENDING I18N
                    logger.warning("Can't write ID attribute" + e.getMessage());
                }
            }
        }
    }


}

