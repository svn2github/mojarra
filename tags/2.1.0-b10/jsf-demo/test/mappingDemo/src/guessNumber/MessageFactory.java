/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package guessNumber;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>supported filters: <code>package</code> and
 * <code>protection</code>.</p>
 */

public class MessageFactory extends Object {

    //
    // Protected Constants
    //

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

    private MessageFactory() {
    }

    //
    // Class methods
    //

    //
    // General Methods
    //

    public static String substituteParams(Locale locale, String msgtext,
                                          Object params[]) {
        String localizedStr = null;

        if (params == null || msgtext == null) {
            return msgtext;
        }
        StringBuffer b = new StringBuffer(100);
        MessageFormat mf = new MessageFormat(msgtext);
        if (locale != null) {
            mf.setLocale(locale);
            b.append(mf.format(params));
            localizedStr = b.toString();
        }
        return localizedStr;
    }


    /**
     * This version of getMessage() is used in the RI for localizing RI
     * specific messages.
     */

    public static FacesMessage getMessage(String messageId, Object params[]) {
        Locale locale = null;
        FacesContext context = FacesContext.getCurrentInstance();
        // context.getViewRoot() may not have been initialized at this point.
        if (context != null && context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
        } else {
            locale = Locale.getDefault();
        }

        return getMessage(locale, messageId, params);
    }


    public static FacesMessage getMessage(Locale locale, String messageId,
                                          Object params[]) {
        FacesMessage result = null;
        String
              summary = null,
              detail = null,
              bundleName = null;
        ResourceBundle bundle = null;

        // see if we have a user-provided bundle
        if (null != (bundleName = getApplication().getMessageBundle())) {
            if (null !=
                (bundle =
                      ResourceBundle.getBundle(bundleName, locale,
                                               getCurrentLoader(bundleName)))) {
                // see if we have a hit
                try {
                    summary = bundle.getString(messageId);
                } catch (MissingResourceException e) {
                }
            }
        }

        // we couldn't find a summary in the user-provided bundle
        if (null == summary) {
            // see if we have a summary in the app provided bundle
            bundle = ResourceBundle.getBundle(FacesMessage.FACES_MESSAGES,
                                              locale,
                                              getCurrentLoader(bundleName));
            if (null == bundle) {
                throw new NullPointerException();
            }
            // see if we have a hit
            try {
                summary = bundle.getString(messageId);
            } catch (MissingResourceException e) {
            }
        }

        // we couldn't find a summary anywhere!  Return null
        if (null == summary) {
            return null;
        }

        // At this point, we have a summary and a bundle.
        if (null == summary || null == bundle) {
            throw new NullPointerException();
        }
        summary = substituteParams(locale, summary, params);

        try {
            detail = substituteParams(locale,
                                      bundle.getString(messageId + "_detail"),
                                      params);
        } catch (MissingResourceException e) {
        }

        return (new FacesMessage(summary, detail));
    }


    //
    // Methods from MessageFactory
    // 
    public static FacesMessage getMessage(FacesContext context,
                                          String messageId) {
        return getMessage(context, messageId, null);
    }


    public static FacesMessage getMessage(FacesContext context,
                                          String messageId,
                                          Object params[]) {
        if (context == null || messageId == null) {
            throw new NullPointerException(
                  "One or more parameters could be null");
        }
        Locale locale = null;
        // viewRoot may not have been initialized at this point.
        if (context != null && context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
        } else {
            locale = Locale.getDefault();
        }
        if (null == locale) {
            throw new NullPointerException();
        }
        FacesMessage message = getMessage(locale, messageId, params);
        if (message != null) {
            return message;
        }
        locale = Locale.getDefault();
        return (getMessage(locale, messageId, params));
    }


    public static FacesMessage getMessage(FacesContext context,
                                          String messageId,
                                          Object param0) {
        return getMessage(context, messageId, new Object[]{param0});
    }


    public static FacesMessage getMessage(FacesContext context,
                                          String messageId,
                                          Object param0, Object param1) {
        return getMessage(context, messageId, new Object[]{param0, param1});
    }


    public static FacesMessage getMessage(FacesContext context,
                                          String messageId,
                                          Object param0, Object param1,
                                          Object param2) {
        return getMessage(context, messageId,
                          new Object[]{param0, param1, param2});
    }


    public static FacesMessage getMessage(FacesContext context,
                                          String messageId,
                                          Object param0, Object param1,
                                          Object param2, Object param3) {
        return getMessage(context, messageId,
                          new Object[]{param0, param1, param2, param3});
    }


    protected static Application getApplication() {
        return (FacesContext.getCurrentInstance().getApplication());
    }


    protected static ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader =
              Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }


} // end of class MessageFactory
