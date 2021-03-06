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

package com.sun.faces.systest.lifecycle;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;


/**
 * <B>ReloadPhaseListener</B> is a class that looks for a Restore or Render
 * phase. If it finds that a phase has been entered other than a Restore or
 * Render, it sets a system property to false.
 *
 * This listener is used to determine whether a client refresh with no
 * request parameters or save state has occurred.
 *
 * @version $Id: ReloadPhaseListenerImpl.java,v 1.9 2007/04/27 22:01:13 ofung Exp $
 */
public class ReloadPhaseListenerImpl implements PhaseListener {

    PhaseId phaseId = null;
    String pageRefresh;


    public ReloadPhaseListenerImpl(PhaseId newPhaseId) {
        phaseId = newPhaseId;
        pageRefresh = "true";
    }


    public void afterPhase(PhaseEvent event) {
        System.setProperty("PageRefreshPhases", pageRefresh);
    }


    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            //reset System property to true when starting phase processing
            pageRefresh = "true";
            return;
        } else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            //no other phases should be called
            return;
        }

        //phase other than Restore or Render is called
        pageRefresh = "false";
    }


    public PhaseId getPhaseId() {
        return phaseId;
    }

} // end of class ReloadPhaseListenerImpl

