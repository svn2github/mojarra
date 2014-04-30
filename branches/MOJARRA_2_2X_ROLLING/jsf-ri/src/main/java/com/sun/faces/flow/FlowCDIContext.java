/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
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

package com.sun.faces.flow;

import com.sun.faces.RIConstants;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;
import java.io.Serializable;
import javax.faces.flow.FlowScoped;
import java.lang.annotation.Annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowHandler;
import javax.faces.lifecycle.ClientWindow;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

public class FlowCDIContext implements Context, Serializable {
    
    private static final long serialVersionUID = -7144653402477623609L;
    private static final String FLOW_SCOPE_MAP_KEY = RIConstants.FACES_PREFIX + "FLOW_SCOPE_MAP";
    private static final Logger LOGGER = FacesLogger.FLOW.getLogger();
    
    private transient Map<Contextual<?>, FlowBeanInfo> flowIds;

    static class FlowBeanInfo {
        String definingDocumentId;
        String id;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FlowBeanInfo other = (FlowBeanInfo) obj;
            if ((this.definingDocumentId == null) ? (other.definingDocumentId != null) : !this.definingDocumentId.equals(other.definingDocumentId)) {
                return false;
            }
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.definingDocumentId != null ? this.definingDocumentId.hashCode() : 0);
            hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "FlowBeanInfo{" + "definingDocumentId=" + definingDocumentId + ", id=" + id + '}';
        }
        
        
    }
    
    // This should be vended from a factory for decoration purposes.
    
    FlowCDIContext(Map<Contextual<?>, FlowBeanInfo> flowIds) {
        this.flowIds = new ConcurrentHashMap<Contextual<?>, FlowBeanInfo>(flowIds);
    }
    
    private static final String PER_SESSION_BEAN_MAP_LIST = FlowCDIContext.class.getPackage().getName() + ".PER_SESSION_BEAN_MAP_LIST";
    private static final String PER_SESSION_CREATIONAL_LIST = FlowCDIContext.class.getPackage().getName() + ".PER_SESSION_CREATIONAL_LIST";
    
    // -------------------------------------------------------- Private Methods
    
    // <editor-fold defaultstate="collapsed" desc="Private helpers">       
    
    private static Map<String, Object> getFlowScopedBeanMapForCurrentFlow() {

        Map<String, Object> result;
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = context.getExternalContext();
        Map<String, Object> sessionMap = extContext.getSessionMap();
        Flow currentFlow = getCurrentFlow(context);
        
        if (null == currentFlow) {
            return null;
        }
        
        ClientWindow curWindow = context.getExternalContext().getClientWindow();
        if (null == curWindow) { 
            throw new IllegalStateException("Unable to obtain current ClientWindow.  Is the ClientWindow feature enabled?");
        }

        String flowBeansForClientWindow = currentFlow.getClientWindowFlowId(curWindow) + "_beans";
        result = (Map<String, Object>) sessionMap.get(flowBeansForClientWindow);
        if (null == result) {
            result = new ConcurrentHashMap<String, Object>();
            sessionMap.put(flowBeansForClientWindow, result);
            ensureBeanMapCleanupOnSessionDestroyed(sessionMap, flowBeansForClientWindow);
        }
        
        return result;
    }
    
    private static Map<String, CreationalContext<?>> getFlowScopedCreationalMapForCurrentFlow() {
        Map<String, CreationalContext<?>> result;
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = context.getExternalContext();
        Map<String, Object> sessionMap = extContext.getSessionMap();
        Flow currentFlow = getCurrentFlow(context);

        ClientWindow curWindow = context.getExternalContext().getClientWindow();
        if (null == curWindow) { 
            throw new IllegalStateException("Unable to obtain current ClientWindow.  Is the ClientWindow feature enabled?");
        }

        String creationalForClientWindow = currentFlow.getClientWindowFlowId(curWindow) + "_creational";
        result = (Map<String, CreationalContext<?>>) sessionMap.get(creationalForClientWindow);
        if (null == result) {
            result = new ConcurrentHashMap<String, CreationalContext<?>>();
            sessionMap.put(creationalForClientWindow, result);
            ensureCreationalCleanupOnSessionDestroyed(sessionMap, creationalForClientWindow);
        }
        
        return result;

    }
    
    private static void ensureBeanMapCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String flowBeansForClientWindow) {
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
        if (null == beanMapList) {
            beanMapList = new ArrayList<String>();
            sessionMap.put(PER_SESSION_BEAN_MAP_LIST, beanMapList);
        }
        beanMapList.add(flowBeansForClientWindow);
    }
    
    private static void ensureCreationalCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String creationalForClientWindow) {
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_CREATIONAL_LIST);
        if (null == beanMapList) {
            beanMapList = new ArrayList<String>();
            sessionMap.put(PER_SESSION_CREATIONAL_LIST, beanMapList);
        }
        beanMapList.add(creationalForClientWindow);
    }
    
    @SuppressWarnings({"FinalPrivateMethod"})
    private final void assertNotReleased() {
        if (!isActive()) {
            throw new IllegalStateException();
        }
    }
    
    private Flow getCurrentFlow() {
        Flow result = null;
        
        FacesContext context = FacesContext.getCurrentInstance();
        result = getCurrentFlow(context);
        
        return result;
    }
    
    private static Flow getCurrentFlow(FacesContext context) {
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        if (null == flowHandler) {
            return null;
        }
        
        Flow result = flowHandler.getCurrentFlow(context);
        
        return result;
        
    }
    
    // </editor-fold>    

    // <editor-fold defaultstate="collapsed" desc="Called from code not related to flow">       
    
    /*
     * Called from WebappLifecycleListener.sessionDestroyed()
     */
    
    public static void sessionDestroyed(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        
        List<String> beanMapList = (List<String>) session.getAttribute(PER_SESSION_BEAN_MAP_LIST);
        if (null != beanMapList) {
            for (String cur : beanMapList) {
                Map<Contextual<?>, Object> beanMap = 
                        (Map<Contextual<?>, Object>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(PER_SESSION_BEAN_MAP_LIST);
            beanMapList.clear();
        }
        
        List<String> creationalList = (List<String>) session.getAttribute(PER_SESSION_CREATIONAL_LIST);
        if (null != creationalList) {
            for (String cur : creationalList) {
                Map<Contextual<?>, CreationalContext<?>> beanMap = 
                        (Map<Contextual<?>, CreationalContext<?>>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(PER_SESSION_CREATIONAL_LIST);
            creationalList.clear();
        }
        
        
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Called from code related to flow">  
    
    static Map<Object, Object> getCurrentFlowScope() {
        Map<String, Object> flowScopedBeanMap = getFlowScopedBeanMapForCurrentFlow();
        Map<Object, Object> result = null;
        if (null != flowScopedBeanMap) {
            result = (Map<Object, Object>) flowScopedBeanMap.get(FLOW_SCOPE_MAP_KEY);
            if (null == result) {
                result = new ConcurrentHashMap<Object, Object>();
                flowScopedBeanMap.put(FLOW_SCOPE_MAP_KEY, result);
            }
        }
        return result; 
    }
        
    static void flowExited() {
        Map<String, Object> flowScopedBeanMap = getFlowScopedBeanMapForCurrentFlow();
        Map<String, CreationalContext<?>> creationalMap = getFlowScopedCreationalMapForCurrentFlow();
        assert(!flowScopedBeanMap.isEmpty());
        assert(!creationalMap.isEmpty());
        List<String> flowScopedBeansToRemove = new ArrayList<String>();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        BeanManager beanManager = (BeanManager) Util.getCDIBeanManager(facesContext.getExternalContext().getApplicationMap());
        
        for (Entry<String, Object> entry : flowScopedBeanMap.entrySet()) {
            String passivationCapableId = entry.getKey();
            if (FLOW_SCOPE_MAP_KEY.equals(passivationCapableId)) {
                continue;
            }
            Contextual owner = beanManager.getPassivationCapableBean(passivationCapableId);
            Object bean = entry.getValue();
            CreationalContext creational = creationalMap.get(passivationCapableId);
            
            owner.destroy(bean, creational);
            flowScopedBeansToRemove.add(passivationCapableId);
        }
        
        for (String cur : flowScopedBeansToRemove) {
            flowScopedBeanMap.remove(cur);
            creationalMap.remove(cur);
        }
        
        if (Util.isCdiOneOneOrGreater()) {
            Class flowCDIEventFireHelperImplClass = null;
            try {
                flowCDIEventFireHelperImplClass = Class.forName("com.sun.faces.flow.FlowCDIEventFireHelperImpl");
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
            }
            
            if (null != flowCDIEventFireHelperImplClass) {
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext =
                            beanManager.createCreationalContext(null);
                    FlowCDIEventFireHelper eventHelper = 
                            (FlowCDIEventFireHelper)  beanManager.getReference(bean, bean.getBeanClass(),
                            creationalContext);
                    eventHelper.fireDestroyedEvent(getCurrentFlow(facesContext));
                }
            }
        }
    }
    
    static void flowEntered() {
        getFlowScopedBeanMapForCurrentFlow();
        getFlowScopedCreationalMapForCurrentFlow();
        
        getCurrentFlowScope();
        
        if (Util.isCdiOneOneOrGreater()) {
            Class flowCDIEventFireHelperImplClass = null;
            try {
                flowCDIEventFireHelperImplClass = Class.forName("com.sun.faces.flow.FlowCDIEventFireHelperImpl");
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
            }
            if (null != flowCDIEventFireHelperImplClass) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                BeanManager beanManager = (BeanManager) Util.getCDIBeanManager(facesContext.getExternalContext().getApplicationMap());
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext =
                            beanManager.createCreationalContext(null);
                    FlowCDIEventFireHelper eventHelper = 
                            (FlowCDIEventFireHelper)  beanManager.getReference(bean, bean.getBeanClass(),
                            creationalContext);
                    eventHelper.fireInitializedEvent(getCurrentFlow(facesContext));
                }
            }
        }
    }

// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="spi.Context implementation">       
    
    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        assertNotReleased();
        
        T result = get(contextual);
        
        if (null == result) {
            Map<String, Object> flowScopedBeanMap = getFlowScopedBeanMapForCurrentFlow();
            Map<String, CreationalContext<?>> creationalMap = getFlowScopedCreationalMapForCurrentFlow();
            
            String passivationCapableId = ((PassivationCapable)contextual).getId();

            synchronized (flowScopedBeanMap) {
                result = (T) flowScopedBeanMap.get(passivationCapableId);
                if (null == result) {
                    
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
                    
                    if (null == flowHandler) {
                        return null;
                    }
                    
                    FlowBeanInfo fbi = flowIds.get(contextual);
                    if (!flowHandler.isActive(facesContext, fbi.definingDocumentId, fbi.id)) {
                        throw new ContextNotActiveException("Request to activate bean in flow '" + fbi + "', but that flow is not active.");
                    }

                    
                    result = contextual.create(creational);
                    
                    if (null != result) {
                        flowScopedBeanMap.put(passivationCapableId, result);
                        creationalMap.put(passivationCapableId, creational);
                    }
                }
            }
        }
        
        return result;

    }
    
    @Override
    public <T> T get(Contextual<T> contextual) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("FlowScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }
        String passivationCapableId = ((PassivationCapable)contextual).getId();

        return (T) getFlowScopedBeanMapForCurrentFlow().get(passivationCapableId);
    }
    
    @Override
    public Class<? extends Annotation> getScope() {
        return FlowScoped.class;
    }
    
    @Override
    public boolean isActive() {
        return null != getCurrentFlow();
    }
    
    void beforeShutdown(@Observes final BeforeShutdown event, BeanManager beanManager) {
    }
    
    // </editor-fold>
    
}
