/* 
 * $Id: TestViewHandlerImpl.java,v 1.38 2007/12/17 21:46:11 rlubke Exp $
 */


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


// TestViewHandlerImpl.java


package com.sun.faces.application;


import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.event.PhaseId;
import javax.faces.application.StateManager;
import javax.faces.application.StateManager.SerializedView;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import org.apache.cactus.WebRequest;

import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.Util;
import com.sun.faces.util.RequestStateManager;


/**
 * <B>TestViewHandlerImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 * @version $Id: TestViewHandlerImpl.java,v 1.38 2007/12/17 21:46:11 rlubke Exp $
 */


public class TestViewHandlerImpl extends JspFacesTestCase {

//
// Protected Constants
//


    public static final String TEST_URI = "/greeting.jsp";


    public String getExpectedOutputFilename() {
        return "TestViewHandlerImpl_correct";
    }


    public static final String ignore[] = {
    };

    public String[] getLinesToIgnore() {
        return ignore;
    }


    public boolean sendResponseToFile() {
        return true;
    }


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


    public TestViewHandlerImpl() {
        super("TestViewHandlerImpl");
    }


    public TestViewHandlerImpl(String name) {
        super(name);
    }


    //
    // Class methods
    //


    //
    // General Methods
    //



    public void beginRender(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/faces", TEST_URI, null);
    }


    public void beginRender2(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/somepath/greeting.jsf",
                          null, null);
    }

    public void beginTransient(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/faces", TEST_URI, null);
	theRequest.addParameter("javax.faces.ViewState", "j_id1:j_id2");
    }

    public void beginCalculateLocaleLang(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/somepath/greeting.jsf",
                          null, null);
        theRequest.addHeader("Accept-Language", "es-ES,tg-AF,tk-IQ,en-US");
    }


    public void beginCalculateLocaleExact(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/somepath/greeting.jsf",
                          null, null);
        theRequest.addHeader("Accept-Language", "tg-AF,tk-IQ,ps-PS,en-US");
    }


    public void beginCalculateLocaleLowerCase(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/somepath/greeting.jsf",
                          null, null);
        theRequest.addHeader("Accept-Language", "tg-af,tk-iq,ps-ps");
    }


    public void beginCalculateLocaleNoMatch(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/somepath/greeting.jsf",
                          null, null);
        theRequest.addHeader("Accept-Language", "es-ES,tg-AF,tk-IQ");
    }


    public void beginCalculateLocaleFindDefault(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/somepath/greeting.jsf",
                          null, null);
        theRequest.addHeader("Accept-Language", "en,fr");
    }

    public void beginRestoreViewNegative(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/faces", null, null);
    }

    public void testGetActionURL() {

        LifecycleFactory factory = (LifecycleFactory)
            FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle =
            factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

        // wrap the request so we can control the return value of getServletPath
        TestRequest testRequest = new TestRequest(request);

        ExternalContext extContext = new ExternalContextImpl(
            config.getServletContext(),
            testRequest,
            response);
        FacesContext facesContext = new FacesContextImpl(extContext, lifecycle);
        String contextPath = request.getContextPath();

        ViewHandlerImpl handler = new ViewHandlerImpl();

        // if getServletPath() returns "" then the viewId path returned should
        // be the same as what was passed, prefixed by the context path.
        //testRequest.setServletPath("");
        //testRequest.setAttribute("com.sun.faces.INVOCATION_PATH", null);
        //String path = handler.getActionURL(facesContext, "/test.jsp");
        //System.out.println("VIEW ID PATH 1: " + path);
        //assertEquals(contextPath + "/test.jsp", path);

        // if getServletPath() returns a path prefix, then the viewId path
        // returned must have that path prefixed.
        testRequest.setServletPath("/faces");
        testRequest.setPathInfo("/path/test.jsp");
        RequestStateManager.remove(facesContext, RequestStateManager.INVOCATION_PATH);
        String path = handler.getActionURL(facesContext, "/path/test.jsp");
        System.out.println("VIEW ID PATH 2: " + path);
        String expected = contextPath + "/faces/path/test.jsp";
        assertEquals("Expected: " + expected + ", recieved: " + path, expected, path);



        // if getServletPath() returns a path indicating extension mapping
        // and the viewId passed has no extension, append the extension
        // to the provided viewId
        testRequest.setServletPath("/path/firstRequest.jsf");
        testRequest.setPathInfo(null);
        RequestStateManager.remove(facesContext, RequestStateManager.INVOCATION_PATH);
        path = handler.getActionURL(facesContext, "/path/test");
        System.out.println("VIEW ID PATH 3: " + path);
        expected = contextPath + "/path/test";
        assertEquals("Expected: " + expected + ", recieved: " + path, expected, path);


        // if getServletPath() returns a path indicating extension mapping
        // and the viewId passed has an extension, replace the extension with
        // the extension defined in the deployment descriptor
        testRequest.setServletPath("/path/firstRequest.jsf");
        testRequest.setPathInfo(null);
        RequestStateManager.remove(facesContext, RequestStateManager.INVOCATION_PATH);
        path = handler.getActionURL(facesContext, "/path/t.est.jsp");
        System.out.println("VIEW ID PATH 4: " + path);
        expected = contextPath + "/path/t.est.jsf";
        assertEquals("Expected: " + expected + ", recieved: " + path, expected, path);



        // if path info is null, the impl must check to see if
        // there is an exact match on the servlet path, if so, return
        // the servlet path
        testRequest.setServletPath("/faces");
        testRequest.setPathInfo(null);
        RequestStateManager.remove(facesContext, RequestStateManager.INVOCATION_PATH);
        path = handler.getActionURL(facesContext, "/path/t.est");
        System.out.println("VIEW ID PATH 5: " + path);
        expected = contextPath + "/faces/path/t.est";
        assertEquals("Expected: " + expected + ", recieved: " + path, expected, path);


    }


    public void testGetActionURLExceptions() throws Exception {
        boolean exceptionThrown = false;
        ViewHandler handler =
            Util.getViewHandler(getFacesContext());
        try {
            handler.getActionURL(null, "/test.jsp");
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            handler.getActionURL(getFacesContext(), null);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            handler.getActionURL(getFacesContext(), "test.jsp");
        } catch (IllegalArgumentException iae) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }


    public void testGetResourceURL() throws Exception {

        LifecycleFactory factory = (LifecycleFactory)
            FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle =
            factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        ExternalContext extContext =
            new ExternalContextImpl(config.getServletContext(),
                                    request, response);
        FacesContext context =
            new FacesContextImpl(extContext, lifecycle);

        // Validate correct calculations
        assertEquals(request.getContextPath() + "/index.jsp",
                     Util.getViewHandler(getFacesContext()).
                     getResourceURL(context, "/index.jsp"));
        assertEquals("index.jsp",
                     Util.getViewHandler(getFacesContext()).
                     getResourceURL(context, "index.jsp"));

    }


    public void testRender() {
        getFacesContext().setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        UIViewRoot newView = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), TEST_URI);
        //newView.setViewId(TEST_URI);
        getFacesContext().setViewRoot(newView);

        try {
            ViewHandler viewHandler =
                Util.getViewHandler(getFacesContext());
            viewHandler.renderView(getFacesContext(),
                                   getFacesContext().getViewRoot());
        } catch (IOException e) {
            System.out.println("ViewHandler IOException:" + e);
        } catch (FacesException fe) {
            System.out.println("ViewHandler FacesException: " + fe);
        }

        assertTrue(!(getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));

        assertTrue(verifyExpectedOutput());
    }


   /* public void testRender2() {
        // Change the viewID to end with .jsf and make sure that
        // the implementation changes .jsf to .jsp and properly dispatches
        // the message.
        UIViewRoot newView = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        newView.setViewId(TEST_URI);
        getFacesContext().setViewRoot(newView);

        newView.setViewId("/faces/greeting.jsf");
        getFacesContext().setViewRoot(newView);
        try {
            ViewHandler viewHandler =
                Util.getViewHandler(getFacesContext());
            viewHandler.renderView(getFacesContext(),
                                   getFacesContext().getViewRoot());
        } catch (IOException ioe) {
            System.out.println("ViewHandler IOException: " + ioe);
        } catch (FacesException fe) {
            System.out.println("ViewHandler FacesException: " + fe);
        }

        assertTrue(!(getFacesContext().getRenderResponse()) &&
                   !(getFacesContext().getResponseComplete()));

        assertTrue(verifyExpectedOutput());
    } */


    public void testCalculateLocaleLang() {
        System.out.println("Testing calculateLocale - Language Match case");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertTrue(locale.equals(Locale.ENGLISH));
    }


    public void testCalculateLocaleExact() {
        System.out.println("Testing calculateLocale - Exact Match case ");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertTrue(locale.equals(new Locale("ps", "PS")));
    }


    public void testCalculateLocaleNoMatch() {
        System.out.println("Testing calculateLocale - No Match case");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertTrue(locale.equals(Locale.US));
    }


    public void testCalculateLocaleFindDefault() {
        System.out.println("Testing calculateLocale - find default");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertEquals(Locale.ENGLISH.toString(), locale.toString());
    }


    public void testCalculateLocaleLowerCase() {
        System.out.println("Testing calculateLocale - case sensitivity");
        ViewHandler handler = new ViewHandlerImpl();
        Locale locale = handler.calculateLocale(getFacesContext());
        assertTrue(locale.equals(new Locale("ps", "PS")));
    }


    public void testTransient() {

        // precreate tree and set it in session and make sure the tree is
        // restored from session.        
        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        root.setViewId(TEST_URI);

        UIForm basicForm = new UIForm();
        basicForm.setId("basicForm");
        UIInput userName = new UIInput();

        userName.setId("userName");
        userName.setTransient(true);
        root.getChildren().add(basicForm);
        basicForm.getChildren().add(userName);

        UIPanel panel1 = new UIPanel();
        panel1.setId("panel1");
        basicForm.getChildren().add(panel1);

        UIInput userName1 = new UIInput();
        userName1.setId("userName1");
        userName1.setTransient(true);
        panel1.getChildren().add(userName1);

        UIInput userName2 = new UIInput();
        userName2.setId("userName2");
        panel1.getChildren().add(userName2);

        UIInput userName3 = new UIInput();
        userName3.setTransient(true);
        panel1.getFacets().put("userName3", userName3);

        UIInput userName4 = new UIInput();
        panel1.getFacets().put("userName4", userName4);

        HttpSession session = (HttpSession)
            getFacesContext().getExternalContext().getSession(false);
        session.setAttribute(TEST_URI, root);

        getFacesContext().setViewRoot(root);

        StateManager stateManager =
            getFacesContext().getApplication().getStateManager();
	SerializedView viewState = 
	    stateManager.saveSerializedView(getFacesContext());
	assertTrue(null != viewState);
	try {
	    RenderKit curKit = RenderKitUtils.getCurrentRenderKit(getFacesContext());
	    StringWriter writer = new StringWriter();
	    ResponseWriter responseWriter = 
		curKit.createResponseWriter(writer, "text/html",
					    "ISO-8859-1");
	    getFacesContext().setResponseWriter(responseWriter);
	    stateManager.writeState(getFacesContext(), viewState);
	    root = stateManager.restoreView(getFacesContext(), TEST_URI, 
					    RenderKitFactory.HTML_BASIC_RENDER_KIT);
	    getFacesContext().setViewRoot(root);
	}
	catch (Throwable ioe) {
        ioe.printStackTrace();
        fail();
	}

        // make sure that the transient property is not persisted.
        basicForm =
            (UIForm) (getFacesContext().getViewRoot()).findComponent(
                "basicForm");
        assertTrue(basicForm != null);

        userName = (UIInput) basicForm.findComponent("userName");
        assertTrue(userName == null);

        panel1 = (UIPanel) basicForm.findComponent("panel1");
        assertTrue(panel1 != null);

        userName1 = (UIInput) panel1.findComponent("userName1");
        assertTrue(userName1 == null);

        userName2 = (UIInput) panel1.findComponent("userName2");
        assertTrue(userName2 != null);

        // make sure facets work correctly when marked transient.
        Map facetList = panel1.getFacets();
        assertTrue(!(facetList.containsKey("userName3")));
        assertTrue(facetList.containsKey("userName4"));
    }

    public void testRestoreViewNegative() throws Exception {

	// make sure the returned view is null if the viewId is the same
	// as the servlet mapping.
	assertNull(Util.getViewHandler(getFacesContext()).restoreView(getFacesContext(), 
							   "/faces"));
    }


    private class TestRequest extends HttpServletRequestWrapper {

        String servletPath;
        String pathInfo;


        public TestRequest(HttpServletRequest request) {
            super(request);
        }


        public String getServletPath() {
            return servletPath;
        }


        public void setServletPath(String servletPath) {
            this.servletPath = servletPath;
        }


        public String getPathInfo() {
            return pathInfo;
        }


        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }
    }

} // end of class TestViewHandlerImpl
