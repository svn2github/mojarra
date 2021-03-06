/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.faces.config;


import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.EventListener;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIColumn;
import javax.faces.component.UICommand;
import javax.faces.component.UIData;
import javax.faces.component.UIForm;
import javax.faces.component.UIGraphic;
import javax.faces.component.UIInput;
import javax.faces.component.UIMessage;
import javax.faces.component.UIMessages;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlInputSecret;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.component.html.HtmlMessage;
import javax.faces.component.html.HtmlMessages;
import javax.faces.component.html.HtmlOutputFormat;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectManyListbox;
import javax.faces.component.html.HtmlSelectManyMenu;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.BigIntegerConverter;
import javax.faces.convert.BooleanConverter;
import javax.faces.convert.ByteConverter;
import javax.faces.convert.CharacterConverter;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.FloatConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.convert.NumberConverter;
import javax.faces.convert.ShortConverter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.LongRangeValidator;
import javax.faces.webapp.FacesServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.FilterRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter;
import com.sun.faces.application.ApplicationAssociate;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * <p>Unit tests for <code>ConfigureListener</code>.</p>
 */
public class ConfigureListenerTestCase extends ServletFacesTestCase {



    // ------------------------------------------------------------ Constructors


    // Construct a new instance of this test case.
    public ConfigureListenerTestCase(String name) {
        super(name);
    }

    public ConfigureListenerTestCase() {
        this("ConfigureListenerTestCase");
    }

     // Return the tests included in this test case.
    public static Test suite() {

        return (new TestSuite(ConfigureListenerTestCase.class));

    }


    // ------------------------------------------------- Individual Test Methods



    // Test a basic environment with no application configuration resources
    public void testBasic() throws Exception {

        // Perform tests on the environment
        checkComponentsGeneric();
        checkComponentsHtml();
        checkConvertersByClass();
        checkConvertersById();
        checkRenderers();
        checkValidators();

    }


    // Representative sample only
    private String rendersChildrenFalse[][] = {
        
    };

    private String rendersChildrenTrue[][] = {
        {"javax.faces.Command", "javax.faces.Link"},
        {"javax.faces.Data", "javax.faces.Table"},
        {"javax.faces.Output", "javax.faces.Link"},
        {"javax.faces.Panel", "javax.faces.Grid"},
        {"javax.faces.Panel", "javax.faces.Group"},
        {"javax.faces.Command", "javax.faces.Button"},
        {"javax.faces.Form", "javax.faces.Form"}
    };


    // Test some boolean attributes that should have been set explicitly
    public void testBoolean() throws Exception {

        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        // Test for isRendersChildren=false
        for (int i = 0; i < rendersChildrenFalse.length; i++) {
            Renderer r = rk.getRenderer(rendersChildrenFalse[i][0],
                                        rendersChildrenFalse[i][1]);
            assertEquals("(" + rendersChildrenFalse[i][0] + "," +
                         rendersChildrenFalse[i][1] + ")", false,
                         r.getRendersChildren());
        }

        // Test for isRendersChildren=true
        for (int i = 0; i < rendersChildrenTrue.length; i++) {
            Renderer r = rk.getRenderer(rendersChildrenTrue[i][0],
                                        rendersChildrenTrue[i][1]);
            assertEquals("(" + rendersChildrenTrue[i][0] + "," +
                         rendersChildrenTrue[i][1] + ")", true,
                         r.getRendersChildren());
        }

    }


    // Test a webapp with a default faces-config.xml resource
    public void testDefault() throws Exception {

        // Validate standard configuration
        checkComponentsGeneric();
        checkComponentsHtml();
        checkConvertersByClass();
        checkConvertersById();
        checkRenderers();
        checkValidators();

        // Validate what was actually configured
        checkDefaultConfiguration();
        checkExtraConfiguration(false);
        checkEmbedConfiguration(false);


    }


    // Test a webapp with a default and extra and embedded resources
    public void testEmbed() throws Exception {

        ServletContext ctx = (ServletContext)
              getFacesContext().getExternalContext().getContext();
        ApplicationAssociate.clearInstance(getFacesContext().getExternalContext());
        ctx.removeAttribute("com.sun.faces.config.WebConfiguration");  
        ServletContextWrapper w = new ServletContextWrapper(ctx);
        ServletContextEvent sce = new ServletContextEvent(w);
        w.addInitParameter(FacesServlet.CONFIG_FILES_ATTR,
                           "/WEB-INF/embed-config.xml,/WEB-INF/extra-config.xml");
        FactoryFinder.releaseFactories();
        ConfigureListener listener = new ConfigureListener();
        // Initialize the context
        try {
            listener.contextInitialized(sce);
        } catch (FacesException e) {
            if (e.getCause() != null) {
                throw (Exception) e.getCause();
            } else {
                throw e;
            }
        }

        // Validate standard configuration
        checkComponentsGeneric();
        checkComponentsHtml();
        checkConvertersByClass();
        checkConvertersById();
        checkRenderers();
        checkValidators();

        // Validate what was actually configured
        checkDefaultConfiguration();
        checkExtraConfiguration(true);
        checkEmbedConfiguration(true);

        // Destroy the context
        listener.contextDestroyed(sce);

    }


    // Test a webapp with a default and extra faces-config.xml resources
    public void testExtra() throws Exception {

        ServletContext ctx = (ServletContext)
              getFacesContext().getExternalContext().getContext();
        ApplicationAssociate.clearInstance(getFacesContext().getExternalContext());
        ctx.removeAttribute("com.sun.faces.config.WebConfiguration");
        ServletContextWrapper w = new ServletContextWrapper(ctx);
        ServletContextEvent sce = new ServletContextEvent(w);
        w.addInitParameter(FacesServlet.CONFIG_FILES_ATTR,
                           "/WEB-INF/extra-config.xml");
        FactoryFinder.releaseFactories();
        ConfigureListener listener = new ConfigureListener();

        // Initialize the context
        try {
            listener.contextInitialized(sce);
        } catch (FacesException e) {
            if (e.getCause() != null) {
                throw (Exception) e.getCause();
            } else {
                throw e;
            }
        }

        // Validate standard configuration
        checkComponentsGeneric();
        checkComponentsHtml();
        checkConvertersByClass();
        checkConvertersById();
        checkRenderers();
        checkValidators();

        // Validate what was actually configured
        checkDefaultConfiguration();
        checkExtraConfiguration(true);
        checkEmbedConfiguration(false);

        // Destroy the context
        listener.contextDestroyed(sce);

    }


    // --------------------------------------------------------- Support Methods


    // Check that all of the required generic components have been registered
    private void checkComponentsGeneric() throws Exception {

        ApplicationFactory afactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = afactory.getApplication();

        assertTrue(application.createComponent
                   ("javax.faces.Column") instanceof UIColumn);
        assertTrue(application.createComponent
                   (UIColumn.COMPONENT_TYPE) instanceof UIColumn);
        assertTrue(application.createComponent
                   ("javax.faces.Command") instanceof UICommand);
        assertTrue(application.createComponent
                   (UICommand.COMPONENT_TYPE) instanceof UICommand);
        assertTrue(application.createComponent
                   ("javax.faces.Data") instanceof UIData);
        assertTrue(application.createComponent
                   (UIData.COMPONENT_TYPE) instanceof UIData);
        assertTrue(application.createComponent
                   ("javax.faces.Form") instanceof UIForm);
        assertTrue(application.createComponent
                   (UIForm.COMPONENT_TYPE) instanceof UIForm);
        assertTrue(application.createComponent
                   ("javax.faces.Graphic") instanceof UIGraphic);
        assertTrue(application.createComponent
                   (UIGraphic.COMPONENT_TYPE) instanceof UIGraphic);
        assertTrue(application.createComponent
                   ("javax.faces.Input") instanceof UIInput);
        assertTrue(application.createComponent
                   (UIInput.COMPONENT_TYPE) instanceof UIInput);
        assertTrue(application.createComponent
                   ("javax.faces.Message") instanceof UIMessage);
        assertTrue(application.createComponent
                   (UIMessage.COMPONENT_TYPE) instanceof UIMessage);
        assertTrue(application.createComponent
                   ("javax.faces.Messages") instanceof UIMessages);
        assertTrue(application.createComponent
                   (UIMessages.COMPONENT_TYPE) instanceof UIMessages);
        assertTrue(application.createComponent
                   ("javax.faces.NamingContainer") instanceof UINamingContainer);
        assertTrue(application.createComponent
                   (UINamingContainer.COMPONENT_TYPE) instanceof UINamingContainer);
        assertTrue(application.createComponent
                   ("javax.faces.Output") instanceof UIOutput);
        assertTrue(application.createComponent
                   (UIOutput.COMPONENT_TYPE) instanceof UIOutput);
        assertTrue(application.createComponent
                   ("javax.faces.Panel") instanceof UIPanel);
        assertTrue(application.createComponent
                   (UIPanel.COMPONENT_TYPE) instanceof UIPanel);
        assertTrue(application.createComponent
                   ("javax.faces.Parameter") instanceof UIParameter);
        assertTrue(application.createComponent
                   (UIParameter.COMPONENT_TYPE) instanceof UIParameter);
        assertTrue(application.createComponent
                   ("javax.faces.SelectBoolean") instanceof UISelectBoolean);
        assertTrue(application.createComponent
                   (UISelectBoolean.COMPONENT_TYPE) instanceof UISelectBoolean);
        assertTrue(application.createComponent
                   ("javax.faces.SelectItem") instanceof UISelectItem);
        assertTrue(application.createComponent
                   (UISelectItem.COMPONENT_TYPE) instanceof UISelectItem);
        assertTrue(application.createComponent
                   ("javax.faces.SelectItems") instanceof UISelectItems);
        assertTrue(application.createComponent
                   (UISelectItems.COMPONENT_TYPE) instanceof UISelectItems);
        assertTrue(application.createComponent
                   ("javax.faces.SelectMany") instanceof UISelectMany);
        assertTrue(application.createComponent
                   (UISelectMany.COMPONENT_TYPE) instanceof UISelectMany);
        assertTrue(application.createComponent
                   ("javax.faces.SelectOne") instanceof UISelectOne);
        assertTrue(application.createComponent
                   (UISelectOne.COMPONENT_TYPE) instanceof UISelectOne);

    }


    // Check that all of the required HTML components have been registered
    private void checkComponentsHtml() throws Exception {

        ApplicationFactory afactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = afactory.getApplication();

        assertTrue(application.createComponent
                   ("javax.faces.HtmlCommandButton") instanceof HtmlCommandButton);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlCommandLink") instanceof HtmlCommandLink);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlDataTable") instanceof HtmlDataTable);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlForm") instanceof HtmlForm);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlGraphicImage") instanceof HtmlGraphicImage);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlInputHidden") instanceof HtmlInputHidden);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlInputSecret") instanceof HtmlInputSecret);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlInputText") instanceof HtmlInputText);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlInputTextarea") instanceof HtmlInputTextarea);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlMessage") instanceof HtmlMessage);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlMessages") instanceof HtmlMessages);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlOutputFormat") instanceof HtmlOutputFormat);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlOutputLabel") instanceof HtmlOutputLabel);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlOutputLink") instanceof HtmlOutputLink);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlOutputText") instanceof HtmlOutputText);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlPanelGrid") instanceof HtmlPanelGrid);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlPanelGroup") instanceof HtmlPanelGroup);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectBooleanCheckbox") instanceof HtmlSelectBooleanCheckbox);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectManyCheckbox") instanceof HtmlSelectManyCheckbox);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectManyListbox") instanceof HtmlSelectManyListbox);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectManyMenu") instanceof HtmlSelectManyMenu);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectOneListbox") instanceof HtmlSelectOneListbox);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlSelectOneMenu") instanceof HtmlSelectOneMenu);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectOneRadio") instanceof HtmlSelectOneRadio);

    }


    // Check that all required by-class Converters have been registered
    private void checkConvertersByClass() throws Exception {

        ApplicationFactory afactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = afactory.getApplication();

        assertTrue(application.createConverter
                   (BigDecimal.class) instanceof BigDecimalConverter);
        assertTrue(application.createConverter
                   (BigInteger.class) instanceof BigIntegerConverter);
        assertTrue(application.createConverter
                   (Boolean.class) instanceof BooleanConverter);
        assertTrue(application.createConverter
                   (Byte.class) instanceof ByteConverter);
        assertTrue(application.createConverter
                   (Character.class) instanceof CharacterConverter);
        assertTrue(application.createConverter
                   (Double.class) instanceof DoubleConverter);
        assertTrue(application.createConverter
                   (Float.class) instanceof FloatConverter);
        assertTrue(application.createConverter
                   (Integer.class) instanceof IntegerConverter);
        assertTrue(application.createConverter
                   (Long.class) instanceof LongConverter);
        assertTrue(application.createConverter
                   (Short.class) instanceof ShortConverter);

    }


    // Check that all required by-id Converters have been registered
    private void checkConvertersById() throws Exception {

        ApplicationFactory afactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = afactory.getApplication();

        assertTrue(application.createConverter
                   ("javax.faces.BigDecimal") instanceof BigDecimalConverter);
        assertTrue(application.createConverter
                   ("javax.faces.BigInteger") instanceof BigIntegerConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Boolean") instanceof BooleanConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Byte") instanceof ByteConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Character") instanceof CharacterConverter);
        assertTrue(application.createConverter
                   ("javax.faces.DateTime") instanceof DateTimeConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Double") instanceof DoubleConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Float") instanceof FloatConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Integer") instanceof IntegerConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Long") instanceof LongConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Number") instanceof NumberConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Short") instanceof ShortConverter);

    }


    // Check that the default configuration took place
    private void checkDefaultConfiguration() throws Exception {

        ApplicationFactory afactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = afactory.getApplication();

        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        assertTrue(application.createComponent
                   ("DefaultComponent") instanceof TestComponent);
        assertTrue(application.createConverter
                   ("DefaultConverter") instanceof TestConverter);
        assertTrue(application.createValidator
                   ("DefaultValidator") instanceof TestValidator);
        assertNotNull(rk.getRenderer("Test", "DefaultRenderer"));

    }


    // Check whether embed configuration occurred or did not occur
    private void checkEmbedConfiguration(boolean should) throws Exception {

        ApplicationFactory afactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = afactory.getApplication();

        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        if (should) {
            assertTrue(application.createComponent
                       ("EmbedComponent") instanceof TestComponent);
            assertTrue(application.createConverter
                       ("EmbedConverter") instanceof TestConverter);
            assertTrue(application.createValidator
                       ("EmbedValidator") instanceof TestValidator);
            assertNotNull(rk.getRenderer("Test", "EmbedRenderer"));
        } else {
            try {
                application.createComponent("EmbedComponent");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            try {
                application.createConverter("EmbedConverter");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            try {
                application.createValidator("EmbedValidator");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            assertNull(rk.getRenderer("Test", "EmbedRenderer"));
        }

    }


    // Check whether extra configuration occurred or did not occur
    private void checkExtraConfiguration(boolean should) throws Exception {

        ApplicationFactory afactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = afactory.getApplication();

        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        if (should) {
            assertTrue(application.createComponent
                       ("ExtraComponent") instanceof TestComponent);
            assertTrue(application.createConverter
                       ("ExtraConverter") instanceof TestConverter);
            assertTrue(application.createValidator
                       ("ExtraValidator") instanceof TestValidator);
            assertNotNull(rk.getRenderer("Test", "ExtraRenderer"));
        } else {
            try {
                application.createComponent("ExtraComponent");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            try {
                application.createConverter("ExtraConverter");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            try {
                application.createValidator("ExtraValidator");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            assertNull(rk.getRenderer("Test", "ExtraRenderer"));
        }

    }


    // Check that all required Renderers have been registered
    private void checkRenderers() throws Exception {

        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        assertNotNull(
            rk.getRenderer("javax.faces.Command", "javax.faces.Button"));
        assertNotNull(
            rk.getRenderer("javax.faces.Command", "javax.faces.Link"));
        assertNotNull(rk.getRenderer("javax.faces.Data", "javax.faces.Table"));
        assertNotNull(rk.getRenderer("javax.faces.Form", "javax.faces.Form"));
        assertNotNull(
            rk.getRenderer("javax.faces.Graphic", "javax.faces.Image"));
        assertNotNull(
            rk.getRenderer("javax.faces.Input", "javax.faces.Hidden"));
        assertNotNull(
            rk.getRenderer("javax.faces.Input", "javax.faces.Secret"));
        assertNotNull(rk.getRenderer("javax.faces.Input", "javax.faces.Text"));
        assertNotNull(
            rk.getRenderer("javax.faces.Input", "javax.faces.Textarea"));
        assertNotNull(
            rk.getRenderer("javax.faces.Message", "javax.faces.Message"));
        assertNotNull(
            rk.getRenderer("javax.faces.Messages", "javax.faces.Messages"));
        assertNotNull(
            rk.getRenderer("javax.faces.Output", "javax.faces.Format"));
        assertNotNull(
            rk.getRenderer("javax.faces.Output", "javax.faces.Label"));
        assertNotNull(rk.getRenderer("javax.faces.Output", "javax.faces.Link"));
        assertNotNull(rk.getRenderer("javax.faces.Output", "javax.faces.Text"));
        assertNotNull(rk.getRenderer("javax.faces.Panel", "javax.faces.Grid"));
        assertNotNull(rk.getRenderer("javax.faces.Panel", "javax.faces.Group"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectBoolean", "javax.faces.Checkbox"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectMany", "javax.faces.Checkbox"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectMany", "javax.faces.Listbox"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectMany", "javax.faces.Menu"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectOne", "javax.faces.Listbox"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectOne", "javax.faces.Menu"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectOne", "javax.faces.Radio"));

    }


    // Check that all required Validators have been registered
    private void checkValidators() throws Exception {

        ApplicationFactory afactory = (ApplicationFactory)
            FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application application = afactory.getApplication();

        assertTrue(application.createValidator
                   ("javax.faces.DoubleRange") instanceof DoubleRangeValidator);
        assertTrue(application.createValidator
                   ("javax.faces.Length") instanceof LengthValidator);
        assertTrue(application.createValidator
                   ("javax.faces.LongRange") instanceof LongRangeValidator);


    }




    // Tests if a particular reset message got logged
    // See testLogOverriddenContextConfigValues
    private static class GotMessageFilter implements Filter {
        private boolean gotLogMessage = false;
        
        public boolean isLoggable(LogRecord record) {
            
            if (record.getMessage().equals("jsf.config.webconfig.configinfo.reset.enabled") &&
                record.getParameters()[1].equals(BooleanWebContextInitParameter.ValidateFacesConfigFiles.getQualifiedName())) {
                gotLogMessage = true;
            }
            
            return true;
        }
        
        public boolean gotLogMessage() {
            return gotLogMessage;
        }
    }


    // ---------------------------------------------------------- Nested Classes


    private static final class ServletContextWrapper implements ServletContext {

        private ServletContext delegate;
        private Map<String,String> initParameters;

        ServletContextWrapper(ServletContext delegate) {
            this.delegate = delegate;
        }

        void addInitParameter(String name, String value) {
            if (initParameters == null) {
                initParameters = new HashMap<String,String>();
            }
            initParameters.put(name, value);
        }

        public String getContextPath() {
            return delegate.getContextPath();
        }

        public ServletContext getContext(String s) {
            return delegate.getContext(s);
        }

        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        public String getMimeType(String s) {
            return delegate.getMimeType(s);
        }

        public Set getResourcePaths(String s) {
            return delegate.getResourcePaths(s);
        }

        public URL getResource(String s) throws MalformedURLException {
            return delegate.getResource(s);
        }

        public InputStream getResourceAsStream(String s) {
            return delegate.getResourceAsStream(s);
        }

        public RequestDispatcher getRequestDispatcher(String s) {
            return delegate.getRequestDispatcher(s);
        }

        public RequestDispatcher getNamedDispatcher(String s) {
            return delegate.getNamedDispatcher(s);
        }

        public Servlet getServlet(String s) throws ServletException {
            return delegate.getServlet(s);
        }

        public Enumeration getServlets() {
            return delegate.getServlets();
        }

        public Enumeration getServletNames() {
            return getServletNames();
        }

        public void log(String s) {
            delegate.log(s);
        }

        public void log(Exception e, String s) {
            delegate.log(e, s);
        }

        public void log(String s, Throwable throwable) {
            delegate.log(s, throwable);
        }

        public String getRealPath(String s) {
            return delegate.getRealPath(s);
        }

        public String getServerInfo() {
            return delegate.getServerInfo();
        }

        public String getInitParameter(String s) {
            String v = null;
            if (initParameters != null) {
                v = initParameters.get(s);
            }
            if (v == null) {
                v = delegate.getInitParameter(s);
            }
            return v;
        }

        public Enumeration getInitParameterNames() {
            Vector<String> v = new Vector<String>();
            if (initParameters != null) {
                for (String key : initParameters.keySet()) {
                    v.add(key);
                }
            }
            for (Enumeration e = delegate.getInitParameterNames(); e.hasMoreElements(); ) {
                v.add((String) e.nextElement());
            }
            return v.elements();
        }

        public Object getAttribute(String s) {
            return delegate.getAttribute(s);
        }

        public Enumeration getAttributeNames() {
            return delegate.getAttributeNames();
        }

        public void setAttribute(String s, Object o) {
            delegate.setAttribute(s, o);
        }

        public void removeAttribute(String s) {
            delegate.removeAttribute(s);
        }

        public String getServletContextName() {
            return delegate.getServletContextName();
        }

        public int getEffectiveMajorVersion() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public int getEffectiveMinorVersion() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean setInitParameter(String s, String s1) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ServletRegistration.Dynamic addServlet(String s, String s1) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public <T extends Servlet> T createServlet(Class<T> tClass)
              throws ServletException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ServletRegistration getServletRegistration(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public FilterRegistration.Dynamic addFilter(String s, String s1) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public FilterRegistration.Dynamic addFilter(String s, javax.servlet.Filter filter) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public FilterRegistration.Dynamic addFilter(String s, Class<? extends javax.servlet.Filter> aClass) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public <T extends javax.servlet.Filter> T createFilter(Class<T> tClass)
              throws ServletException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public FilterRegistration getFilterRegistration(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public SessionCookieConfig getSessionCookieConfig() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void addListener(String s) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public <T extends EventListener> void addListener(T t) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void addListener(Class<? extends EventListener> aClass) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public <T extends EventListener> T createListener(Class<T> tClass)
              throws ServletException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public JspConfigDescriptor getJspConfigDescriptor() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ClassLoader getClassLoader() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void declareRoles(String... strings) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}
