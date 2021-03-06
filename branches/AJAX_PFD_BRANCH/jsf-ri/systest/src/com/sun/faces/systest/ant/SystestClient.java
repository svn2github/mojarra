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

/* ========================================================================= *
 *                                                                           *
 *                 The Apache Software License,  Version 1.1                 *
 *                                                                           *
 *         Copyright (c) 1999, 2000  The Apache Software Foundation.         *
 *                           All rights reserved.                            *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * Redistribution and use in source and binary forms,  with or without modi- *
 * fication, are permitted provided that the following conditions are met:   *
 *                                                                           *
 * 1. Redistributions of source code  must retain the above copyright notice *
 *    notice, this list of conditions and the following disclaimer.          *
 *                                                                           *
 * 2. Redistributions  in binary  form  must  reproduce the  above copyright *
 *    notice,  this list of conditions  and the following  disclaimer in the *
 *    documentation and/or other materials provided with the distribution.   *
 *                                                                           *
 * 3. The end-user documentation  included with the redistribution,  if any, *
 *    must include the following acknowlegement:                             *
 *                                                                           *
 *       "This product includes  software developed  by the Apache  Software *
 *        Foundation <http://www.apache.org/>."                              *
 *                                                                           *
 *    Alternately, this acknowlegement may appear in the software itself, if *
 *    and wherever such third-party acknowlegements normally appear.         *
 *                                                                           *
 * 4. The names  "The  Jakarta  Project",  "Tomcat",  and  "Apache  Software *
 *    Foundation"  must not be used  to endorse or promote  products derived *
 *    from this  software without  prior  written  permission.  For  written *
 *    permission, please contact <apache@apache.org>.                        *
 *                                                                           *
 * 5. Products derived from this software may not be called "Apache" nor may *
 *    "Apache" appear in their names without prior written permission of the *
 *    Apache Software Foundation.                                            *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES *
 * INCLUDING, BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR  A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL *
 * THE APACHE  SOFTWARE  FOUNDATION OR  ITS CONTRIBUTORS  BE LIABLE  FOR ANY *
 * DIRECT,  INDIRECT,   INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR  CONSEQUENTIAL *
 * DAMAGES (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS *
 * OR SERVICES;  LOSS OF USE,  DATA,  OR PROFITS;  OR BUSINESS INTERRUPTION) *
 * HOWEVER CAUSED AND  ON ANY  THEORY  OF  LIABILITY,  WHETHER IN  CONTRACT, *
 * STRICT LIABILITY, OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN *
 * ANY  WAY  OUT OF  THE  USE OF  THIS  SOFTWARE,  EVEN  IF  ADVISED  OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                               *
 *                                                                           *
 * ========================================================================= *
 *                                                                           *
 * This software  consists of voluntary  contributions made  by many indivi- *
 * duals on behalf of the  Apache Software Foundation.  For more information *
 * on the Apache Software Foundation, please see <http://www.apache.org/>.   *
 *                                                                           *
 * ========================================================================= */

package com.sun.faces.systest.ant;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * <p>This class contains a <strong>Task</strong> for Ant that is used to
 * send HTTP requests to a servlet container, and examine the responses.
 * It is similar in purpose to the <code>GTest</code> task in Watchdog,
 * but uses the JDK's HttpURLConnection for underlying connectivity.</p>
 *
 * <p>The task is registered with Ant using a <code>taskdef</code> directive:
 * <pre>
 *   &lt;taskdef name="systest"
 *       classname="com.sun.faces.systest.ant.SystestClient"&gt;
 * </pre>
 * and accepts the following configuration properties:</p>
 * <ul>
 * <li><strong>golden</strong> - The server-relative path of the static
 * resource containing the golden file for this request.</li>
 * <li><strong>host</strong> - The server name to which this request will be
 * sent.  Defaults to <code>localhost</code> if not specified.</li>
 * <li><strong>ignore</strong> - The server-relative path of the static
 * resource containing lines from the specified golden file that should
 * not be matched against the actual response.  If a golden file is
 * specified but not an ignore file, then the contents must match
 * exactly.</li>

 * <li><strong>ignoreIfContains</strong> - The server-relative path of
 * the static resource containing a String on each line, the presence of
 * which in the actual response line will cause that response line to be
 * ignored.</li>

 * <li><strong>inContent</strong> - The data content that will be submitted
 * with this request.  The test client will transparently add a carriage
 * return and line feed, and set the content length header, if this is
 * specified.  Otherwise, no content will be included in the request.</li>
 * <li><strong>inHeaders</strong> - The set of one or more HTTP headers that
 * will be included on the request, in the format
 * <code>{name}:{value}{##{name}:{value}...</li>
 * <li><strong>joinSession</strong> - Should we join the session whose session
 * identifier was returned on the previous request.  [false]</li>
 * <li><strong>message</strong> - The HTTP response message that is expected
 * in the response from the server.  No check is made if no message
 * is specified.</li>
 * <li><strong>method</strong> - The HTTP request method to be used on this
 * request.  Defaults to <ocde>GET</code> if not specified.</li>
 * <li><strong>outContent</strong> - The first line of the response data
 * content that we expect to receive.  No check is made if no content is
 * specified.</li>
 * <li><strong>outHeaders</strong> - The set of one or more HTTP headers that
 * are expected in the response (order independent).</li>
 * <li><strong>port</strong> - The port number to which this request will be
 * sent.  Defaults to <code>8080</code> if not specified.</li>
 * <li><strong>protocol</strong> - The protocol and version (such as
 * "HTTP/1.0") to include in the request, if executed as a direct
 * socket connection.  If not specified, HttpURLConnection will be used
 * instead.</li>
 * <li><strong>redirect</strong> - If set to true, follow any redirect that
 * is returned by the server.  (Only works when using HttpURLConnection).
 * </li>
 * <li><strong>request</strong> - The request URI to be transmitted for this
 * request.  This value should start with a slash character ("/"), and
 * be the server-relative URI of the requested resource.</li>
 * <li><strong>status</strong> - The HTTP status code that is expected in the
 * response from the server.  Defaults to <code>200</code> if not
 * specified.  Set to zero to disable checking the return value.</li>
 * <li><strong>recordGolden</strong> - Record a goldenfile of the response
 * if a goldenfile is specifed for the request and the goldenfile doesn't
 * already exist.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.20 $ $Date: 2007/04/30 23:35:43 $
 */

public class SystestClient extends Task {


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>Log</code> instance for this class.
     */
    protected static final Logger log = Logger.getLogger("Systest");


    /**
     * The saved golden file we will compare to the response.  Each element
     * contains a line of text without any line delimiters.
     */
    protected List saveGolden = new ArrayList();


    /**
     * The saved headers we received in our response.  The key is the header
     * name (converted to lower case), and the value is an ArrayList of the
     * string value(s) received for that header.
     */
    protected Map saveHeaders = new HashMap();


    /**
     * The saved ignore lines for modifying our golden file comparison to the
     * response.  Each element contains a line of text without any line
     * delimiters.
     */
    protected List saveIgnore = new ArrayList();

    /**
     * The saved ignoreIfContains lines for modifying our golden file
     * comparison to the response.  Each element contains a line of text
     * without any line delimiters.  The presence of that text as a
     * substring in a response line causes that line to be ignored.
     */
    protected List saveIgnoreIfContains = new ArrayList();


    /**
     * The response file to be compared to the golden file.  Each element
     * contains a line of text without any line delimiters.
     */
    protected List saveResponse = new ArrayList();


    // ------------------------------------------------------------- Properties


    /**
     * <p>Flag indicating whether we should throw an exception when a test
     * fails.</p>
     */
    protected boolean failonerror = true;


    public boolean getFailonerror() {
        return (this.failonerror);
    }


    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }


    /**
     * The server-relative request URI of the golden file for this request.
     */
    protected String golden = null;


    public String getGolden() {
        return (this.golden);
    }


    public void setGolden(String golden) {
        if (golden.length() > 0) {
            this.golden = golden;
        }
    }


    /**
     * The host name to which we will connect.
     */
    protected String host = "localhost";


    public String getHost() {
        return (this.host);
    }


    public void setHost(String host) {
        this.host = host;
    }


    /**
     * The server-relative request URI of the ignore file for this request.
     */
    protected String ignore = null;


    public String getIgnore() {
        return (this.ignore);
    }


    public void setIgnore(String ignore) {
        if (ignore.length() > 0) {
            this.ignore = ignore;
        }
    }

    /**
     * The server-relative request URI of the ignoreIfContains file for this request.
     */
    protected String ignoreIfContains = null;


    public String getIgnoreIfContains() {
        return (this.ignoreIfContains);
    }


    public void setIgnoreIfContains(String ignoreIfContains) {
        if (ignoreIfContains.length() > 0) {
            this.ignoreIfContains = ignoreIfContains;
        }
    }




    /**
     * The first line of the request data that will be included on this
     * request.
     */
    protected String inContent = null;


    public String getInContent() {
        return (this.inContent);
    }


    public void setInContent(String inContent) {
        this.inContent = inContent;
    }


    /**
     * The HTTP headers to be included on the request.  Syntax is
     * <code>{name}:{value}[##{name}:{value}] ...</code>.
     */
    protected String inHeaders = null;


    public String getInHeaders() {
        return (this.inHeaders);
    }


    public void setInHeaders(String inHeaders) {
        this.inHeaders = inHeaders;
    }


    /**
     * Should we join the session whose session identifier was returned
     * on the previous request.
     */
    protected boolean joinSession = false;


    public boolean getJoinSession() {
        return (this.joinSession);
    }


    public void setJoinSession(boolean joinSession) {
        this.joinSession = true;
    }


    /**
     * The HTTP response message to be expected in the response.
     */
    protected String message = null;


    public String getMessage() {
        return (this.message);
    }


    public void setMessage(String message) {
        this.message = message;
    }


    /**
     * The HTTP request method that will be used.
     */
    protected String method = "GET";


    public String getMethod() {
        return (this.method);
    }


    public void setMethod(String method) {
        this.method = method;
    }


    /**
     * The first line of the response data content that we expect to receive.
     */
    protected String outContent = null;


    public String getOutContent() {
        return (this.outContent);
    }


    public void setOutContent(String outContent) {
        if (outContent.length() > 0) {
            this.outContent = outContent;
        }
    }


    /**
     * The HTTP headers to be checked on the response.  Syntax is
     * <code>{name}:{value}[##{name}:{value}] ...</code>.
     */
    protected String outHeaders = null;


    public String getOutHeaders() {
        return (this.outHeaders);
    }


    public void setOutHeaders(String outHeaders) {
        this.outHeaders = outHeaders;
    }


    /**
     * The port number to which we will connect.
     */
    protected int port = 8080;


    public int getPort() {
        return (this.port);
    }


    public void setPort(int port) {
        this.port = port;
    }


    /**
     * The protocol and version to include in the request, if executed as
     * a direct socket connection.  Lack of a value here indicates that an
     * HttpURLConnection should be used instead.
     */
    protected String protocol = null;


    public String getProtocol() {
        return (this.protocol);
    }


    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    /**
     * Should we follow redirects returned by the server?
     */
    protected boolean redirect = false;


    public boolean getRedirect() {
        return (this.redirect);
    }


    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }


    /**
     * The request URI to be sent to the server.  This value is required.
     */
    protected String request = null;


    public String getRequest() {
        return (this.request);
    }


    public void setRequest(String request) {
        this.request = request;
    }


    /**
     * The HTTP status code expected on the response.
     */
    protected int status = 200;


    public int getStatus() {
        return (this.status);
    }


    public void setStatus(int status) {
        this.status = status;
    }


    /**
     * Goldenfile recording.
     */
    protected String recordGolden;


    public String getRecordGolden() {
        return (this.recordGolden);
    }


    public void setRecordGolden(String recordGolden) {
        if (recordGolden.length() > 0) {
            this.recordGolden = recordGolden;
        }
    }


    // ------------------------------------------------------- Static Variables


    /**
     * The session identifier returned by the most recent request, or
     * <code>null</code> if the previous request did not specify a session
     * identifier.
     */
    protected static String sessionId = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Execute the test that has been configured by our property settings.
     *
     * @throws BuildException if an exception occurs
     */
    public void execute() throws BuildException {

        saveHeaders.clear();
        try {
            readGolden();
        } catch (IOException e) {
            System.out.println("FAIL:  readGolden(" + golden + ")");
            e.printStackTrace(System.out);
            if (failonerror) {
                throw new BuildException("Failure reading golden file", e);
            }
        }
        try {
            readIgnore();
        } catch (IOException e) {
            System.out.println("FAIL:  readIgnore(" + ignore + ")");
            e.printStackTrace(System.out);
            if (failonerror) {
                throw new BuildException("Failure reading golden file", e);
            }
        }

        try {
            readIgnoreIfContains();
        } catch (IOException e) {
            System.out.println("FAIL:  readIgnoreIfContains(" + 
			       ignoreIfContains + ")");
            e.printStackTrace(System.out);
            if (failonerror) {
                throw new BuildException("Failure reading golden file", e);
            }
        }

        if ((protocol == null) || (protocol.length() == 0)) {
            executeHttp();
        } else {
            executeSocket();
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Execute the test via use of an HttpURLConnection.
     *
     * @throws BuildException if an exception occurs
     */
    protected void executeHttp() throws BuildException {

        // Construct a summary of the request we will be sending
        String summary = "[" + method + " " + request + "]";
        boolean success = true;
        String result = null;
        Throwable throwable = null;
        HttpURLConnection conn = null;

        try {

            // Configure an HttpURLConnection for this request
            if (log.isLoggable(Level.FINE)) {
                log.fine("Configuring HttpURLConnection for this request");
            }
            URL url = new URL("http", host, port, request);
            conn = (HttpURLConnection) url.openConnection();
            conn.setAllowUserInteraction(false);
            conn.setDoInput(true);
            if (inContent != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Length",
                                        "" + inContent.length());
                if (log.isLoggable(Level.FINE)) {
                    log.fine("INPH: Content-Length: " + inContent.length());
                }
            } else {
                conn.setDoOutput(false);
            }

            // Send the session id cookie (if any)
            if (joinSession && (sessionId != null)) {
                conn.setRequestProperty("Cookie",
                                        "JSESSIONID=" + sessionId);
                if (log.isLoggable(Level.FINE)) {
                    log.fine("INPH: Cookie: JSESSIONID=" + sessionId);
                }
            }

            if (this.redirect && log.isLoggable(Level.FINE)) {
                log.fine("FLAG: setInstanceFollowRedirects(" +
                          this.redirect + ")");
            }
            conn.setInstanceFollowRedirects(this.redirect);
            conn.setRequestMethod(method);
            if (inHeaders != null) {
                String headers = inHeaders;
                while (headers.length() > 0) {
                    int delimiter = headers.indexOf("##");
                    String header = null;
                    if (delimiter < 0) {
                        header = headers;
                        headers = "";
                    } else {
                        header = headers.substring(0, delimiter);
                        headers = headers.substring(delimiter + 2);
                    }
                    int colon = header.indexOf(":");
                    if (colon < 0)
                        break;
                    String name = header.substring(0, colon).trim();
                    String value = header.substring(colon + 1).trim();
                    conn.setRequestProperty(name, value);
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("INPH: " + name + ": " + value);
                    }
                }
            }

            // Connect to the server and send our output if necessary
            conn.connect();
            if (inContent != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("INPD: " + inContent);
                }
                OutputStream os = conn.getOutputStream();
                for (int i = 0, length = inContent.length(); i < length; i++)
                    os.write(inContent.charAt(i));
                os.close();
            }

            // Acquire the response data, if there is any
            String outData = "";
            String outText = "";
            boolean eol = false;
            InputStream is = conn.getInputStream();
            int lines = 0;
            while (true) {
                String line = read(is);
                if (line == null)
                    break;
                if (lines == 0)
                    outData = line;
                else
                    outText += line + "\r\n";

                if (line.trim().length() == 0 && saveResponse.isEmpty()) {
                    lines++;
                    continue;
                }
                saveResponse.add(line);
                lines++;
            }
            is.close();

            // Dump out the response stuff
            if (log.isLoggable(Level.FINE)) {
                log.fine("RESP: " + conn.getResponseCode() + " " +
                          conn.getResponseMessage());
            }
            for (int i = 1; i < 1000; i++) {
                String name = conn.getHeaderFieldKey(i);
                String value = conn.getHeaderField(i);
                if ((name == null) || (value == null))
                    break;
                if (log.isLoggable(Level.FINE)) {
                    log.fine("HEAD: " + name + ": " + value);
                }
                save(name, value);
                if ("Set-Cookie".equals(name))
                    parseSession(value);
            }
            if (log.isLoggable(Level.FINE)) {
                log.fine("DATA: " + outData);
                if (outText.length() > 2) {
                    log.fine("TEXT: " + outText);
                }
            }

            // Validate the response against our criteria
            if (success) {
                result = validateStatus(conn.getResponseCode());
                if (result != null)
                    success = false;
            }
            if (success) {
                result = validateMessage(conn.getResponseMessage());
                if (result != null)
                    success = false;
            }
            if (success) {
                result = validateHeaders();
                if (result != null)
                    success = false;
            }
            if (success) {
                result = validateData(outText);
                if (result != null)
                    success = false;
            }
            if (success) {
                result = validateGolden();
                if (result != null)
                    success = false;
            }

        } catch (Throwable t) {
            if (t instanceof FileNotFoundException) {
                if (status == 404) {
                    success = true;
                    result = "Not Found";
                    throwable = null;
                } else {
                    success = false;
                    try {
                        result = "Status=" + conn.getResponseCode() +
                            ", Message=" + conn.getResponseMessage();
                    } catch (IOException e) {
                        result = e.toString();
                    }
                    throwable = null;
                }
            } else if (t instanceof ConnectException) {
                success = false;
                result = t.getMessage();
                throwable = null;
            } else {
                success = false;
                result = t.getMessage();
                throwable = t;
            }
        }

        // Log the results of executing this request
        if (success) {
            System.out.println("OK   " + summary);
        } else {
            System.out.println("FAIL " + summary + " " + result);
            if (throwable != null)
                throwable.printStackTrace(System.out);
            if (failonerror) {
                if (throwable != null) {
                    throw new BuildException("System test failed", throwable);
                } else {
                    throw new BuildException("System test failed");
                }
            }
        }

    }


    /**
     * Execute the test via use of a socket with direct input/output.
     *
     * @throws BuildException if an exception occurs
     */
    protected void executeSocket() throws BuildException {

        // Construct a summary of the request we will be sending
        String command = method + " " + request + " " + protocol;
        String summary = "[" + command + "]";
        if (log.isLoggable(Level.FINE)) {
            log.fine("RQST: " + summary);
        }
        boolean success = true;
        String result = null;
        Socket socket = null;
        OutputStream os = null;
        PrintWriter pw = null;
        InputStream is = null;
        Throwable throwable = null;
        int outStatus = 0;
        String outMessage = null;

        try {

            // Open a client socket for this request
            socket = new Socket(host, port);
            os = socket.getOutputStream();
            pw = new PrintWriter(os);
            is = socket.getInputStream();

            // Send the command and content length header (if any)
            pw.print(command + "\r\n");
            if (inContent != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("INPH: " + "Content-Length: " +
                              inContent.length());
                }
                pw.print("Content-Length: " + inContent.length() + "\r\n");
            }

            // Send the session id cookie (if any)
            if (joinSession && (sessionId != null)) {
                pw.println("Cookie: JSESSIONID=" + sessionId);
                if (log.isLoggable(Level.FINE)) {
                    log.fine("INPH: Cookie: JSESSIONID=" +
                              sessionId);
                }
            }

            // Send the specified headers (if any)
            if (inHeaders != null) {
                String headers = inHeaders;
                while (headers.length() > 0) {
                    int delimiter = headers.indexOf("##");
                    String header = null;
                    if (delimiter < 0) {
                        header = headers;
                        headers = "";
                    } else {
                        header = headers.substring(0, delimiter);
                        headers = headers.substring(delimiter + 2);
                    }
                    int colon = header.indexOf(":");
                    if (colon < 0)
                        break;
                    String name = header.substring(0, colon).trim();
                    String value = header.substring(colon + 1).trim();
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("INPH: " + name + ": " + value);
                    }
                    pw.print(name + ": " + value + "\r\n");
                }
            }
            pw.print("\r\n");

            // Send our content (if any)
            if (inContent != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("INPD: " + inContent);
                }
                for (int i = 0, length = inContent.length(); i < length; i++)
                    pw.print(inContent.charAt(i));
            }
            pw.flush();

            // Read the response status and associated message
            String line = read(is);
            if (line == null) {
                outStatus = -1;
                outMessage = "NO RESPONSE";
            } else {
                line = line.trim();
                if (log.isLoggable(Level.FINE)) {
                    log.fine("RESP: " + line);
                }
                int space = line.indexOf(" ");
                if (space >= 0) {
                    line = line.substring(space + 1).trim();
                    space = line.indexOf(" ");
                }
                try {
                    if (space < 0) {
                        outStatus = Integer.parseInt(line);
                        outMessage = "";
                    } else {
                        outStatus = Integer.parseInt(line.substring(0, space));
                        outMessage = line.substring(space + 1).trim();
                    }
                } catch (NumberFormatException e) {
                    outStatus = -1;
                    outMessage = "NUMBER FORMAT EXCEPTION";
                }
            }
            if (log.isLoggable(Level.FINE)) {
                log.fine("STAT: " + outStatus + " MESG: " + outMessage);
            }

            // Read the response headers (if any)
            String headerName = null;
            String headerValue = null;
            while (true) {
                line = read(is);
                if ((line == null) || (line.length() == 0))
                    break;
                int colon = line.indexOf(":");
                if (colon < 0) {
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("????: " + line);
                    }
                } else {
                    headerName = line.substring(0, colon).trim();
                    headerValue = line.substring(colon + 1).trim();
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("HEAD: " + headerName + ": " +
                                  headerValue);
                    }
                    save(headerName, headerValue);
                    if ("Set-Cookie".equals(headerName))
                        parseSession(headerValue);
                }
            }

            // Acquire the response data (if any)
            String outData = "";
            String outText = "";
            int lines = 0;
            while (true) {
                line = read(is);
                if (line == null)
                    break;
                if (lines == 0)
                    outData = line;
                else
                    outText += line + "\r\n";

                if (line.trim().length() == 0 && saveResponse.isEmpty()) {
                    lines++;
                    continue;
                }
                saveResponse.add(line);
                lines++;
            }
            is.close();
            if (log.isLoggable(Level.FINE)) {
                log.fine("DATA: " + outData);
                if (outText.length() > 2) {
                    log.fine("TEXT: " + outText);
                }
            }

            // Validate the response against our criteria
            if (success) {
                result = validateStatus(outStatus);
                if (result != null)
                    success = false;
            }
            if (success) {
                result = validateMessage(message);
                if (result != null)
                    success = false;
            }
            if (success) {
                result = validateHeaders();
                if (result != null)
                    success = false;
            }
            if (success) {
                result = validateData(outText);
                if (result != null)
                    success = false;
            }
            if (success) {
                result = validateGolden();
                if (result != null)
                    success = false;
            }

        } catch (Throwable t) {
            success = false;
            result = "Status=" + outStatus +
                ", Message=" + outMessage;
            throwable = null;
        } finally {
            if (pw != null) {
                try {
                    pw.close();
                } catch (Throwable w) {
                    ;
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Throwable w) {
                    ;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable w) {
                    ;
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable w) {
                    ;
                }
            }
        }

        if (success) {
            System.out.println("OK   " + summary);
        } else {
            System.out.println("FAIL " + summary + " " + result);
            if (throwable != null)
                throwable.printStackTrace(System.out);
            if (failonerror) {
                if (throwable != null) {
                    throw new BuildException("System test failed", throwable);
                } else {
                    throw new BuildException("System test failed");
                }
            }
        }

    }


    /**
     * Parse the session identifier from the specified Set-Cookie value.
     *
     * @param value The Set-Cookie value to parse
     */
    protected void parseSession(String value) {

        if (value == null) {
            return;
        }
        int equals = value.indexOf("JSESSIONID=");
        if (equals < 0) {
            return;
        }
        value = value.substring(equals + "JSESSIONID=".length());
        int semi = value.indexOf(";");
        if (semi >= 0) {
            value = value.substring(0, semi);
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("S ID: " + value);
        }
        sessionId = value;

    }


    /**
     * Read and return the next line from the specified input stream, with
     * no carriage return or line feed delimiters.  If
     * end of file is reached, return <code>null</code> instead.
     *
     * @param stream The input stream to read from
     *
     * @throws IOException if an input/output error occurs
     */
    protected String read(InputStream stream) throws IOException {

        StringBuffer result = new StringBuffer();
        while (true) {
            int b = stream.read();
            if (b < 0) {
                if (result.length() == 0) {
                    return (null);
                } else {
                    break;
                }
            }
            char c = (char) b;
            if (c == '\r') {
                continue;
            } else if (c == '\n') {
                break;
            } else {
                result.append(c);
            }
        }
        return (result.toString());

    }


    /**
     * Read and save the contents of the golden file for this test, if any.
     * Otherwise, the <code>saveGolden</code> list will be empty.
     *
     * @throws IOException if an input/output error occurs
     */
    protected void readGolden() throws IOException {

        // Was a golden file specified?
        saveGolden.clear();
        if (golden == null) {
            return;
        }

        // Create a connection to receive the golden file contents
        URL url = new URL("http", host, port, golden);
        HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setFollowRedirects(true);
        conn.setRequestMethod("GET");

        // Connect to the server and retrieve the golden file
        conn.connect();
        InputStream is = conn.getInputStream();
        while (true) {
            String line = read(is);
            if (line == null) {
                break;
            }
            if (line.trim().length() == 0 && saveGolden.isEmpty()) {
                continue;
            }
            saveGolden.add(line);
        }
        is.close();
        conn.disconnect();

    }


    /**
     * Read and save the contents of the ignore file for this test, if any.
     * Otherwise, the <code>saveIgnore</code> list will be empty.
     *
     * @throws IOException if an input/output error occurs
     */
    protected void readIgnore() throws IOException {

        // Was an ignore file specified?
        saveIgnore.clear();
        if (ignore == null) {
            return;
        }

        // Create a connection to receive the ignore file contents
        URL url = new URL("http", host, port, ignore);
        HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setFollowRedirects(true);
        conn.setRequestMethod("GET");

        // Connect to the server and retrieve the ignore file
        conn.connect();
        InputStream is = conn.getInputStream();
        while (true) {
            String line = read(is);
            if (line == null) {
                break;
            }
            saveIgnore.add(line);
        }
        is.close();
        conn.disconnect();

    }

    /**
     * Read and save the contents of the ignoreIfContains file for this
     * test, if any.  Otherwise, the <code>saveIgnoreIfContains</code>
     * list will be empty.
     *
     * @throws IOException if an input/output error occurs
     */
    protected void readIgnoreIfContains() throws IOException {

        // Was an ignoreIfContains file specified?
        saveIgnoreIfContains.clear();
        if (ignoreIfContains == null) {
            return;
        }

        // Create a connection to receive the ignoreIfContains file contents
        URL url = new URL("http", host, port, ignoreIfContains);
        HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setFollowRedirects(true);
        conn.setRequestMethod("GET");

        // Connect to the server and retrieve the ignoreIfContains file
        conn.connect();
        InputStream is = conn.getInputStream();
        while (true) {
            String line = read(is);
            if (line == null) {
                break;
            }
            saveIgnoreIfContains.add(line);
        }
        is.close();
        conn.disconnect();

    }


    /**
     * Save the specified header name and value in our collection.
     *
     * @param name  Header name to save
     * @param value Header value to save
     */
    protected void save(String name, String value) {

        String key = name.toLowerCase();
        ArrayList list = (ArrayList) saveHeaders.get(key);
        if (list == null) {
            list = new ArrayList();
            saveHeaders.put(key, list);
        }
        list.add(value);

    }


    /**
     * Validate the output data against what we expected.  Return
     * <code>null</code> for no problems, or an error message.
     *
     * @param data The output data to be tested
     */
    protected String validateData(String data) {
        data = data.trim();
        if (outContent == null) {
            return (null);
        } else if (data.startsWith(outContent)) {
            return (null);
        } else {
            return ("Expected data '" + outContent + "', got data '" +
                data + "'");
        }

    }


    protected String stripJsessionidFromLine(String line) {
        if (null == line) {
            return line;
        }
        int
            start = 0,
            end = 0;
        String result = line;

        if (-1 == (start = line.indexOf(";jsessionid="))) {
            return result;
        }

        if (-1 == (end = line.indexOf("?", start))) {
            if (-1 == (end = line.indexOf("\"", start))) {
                throw new IllegalStateException();
            }
        }
        result = stripJsessionidFromLine(line.substring(0, start) +
                                         line.substring(end));
        return result;
    }


    /**
     * Validate the response against the golden file (if any), skipping the
     * comparison on any golden file line that is also in the ignore file
     * (if any).  Return <code>null</code> for no problems, or an error
     * message.
     */
    protected String validateGolden() {

        if (golden == null) {
            return (null);
        }
        boolean ok = true;
        if (saveGolden.size() != saveResponse.size()) {
            ok = false;
        }
        if (ok) {
            for (int i = 0, size = saveGolden.size(); i < size; i++) {
                String golden = (String) saveGolden.get(i);
                String response = (String) saveResponse.get(i);
                if (!validateIgnore(golden) && 
		    !validateIgnoreIfContains(golden) && 
		    !golden.equals(response)) {
                    response = stripJsessionidFromLine(response);
                    golden = stripJsessionidFromLine(golden);
                    if (!golden.trim().equals(response.trim())) {
                        ok = false;
                        break;
                    }
                }
            }
        }
        if (ok) {
            return (null);
        }
        System.out.println("EXPECTED: ======================================");
        for (int i = 0, size = saveGolden.size(); i < size; i++) {
            System.out.println((String) saveGolden.get(i));
        }
        System.out.println("================================================");
        if (saveIgnore.size() >= 1) {
            System.out.println(
                "IGNORED: =======================================");
            for (int i = 0, size = saveIgnore.size(); i < size; i++) {
                System.out.println((String) saveIgnore.get(i));
            }
            System.out.println(
                "================================================");
        }
        System.out.println("RECEIVED: ======================================");
        for (int i = 0, size = saveResponse.size(); i < size; i++) {
            System.out.println((String) saveResponse.get(i));
        }
        System.out.println("================================================");
        
        // write the goldenfile if the GF size from the server was 0
        // and the goldenfile doesn't already exist on the local filesystem.
        System.out.println("RECORD GOLDEN: " + recordGolden);
        if (recordGolden != null) {
            File gf = new File(recordGolden);
            if (!gf.exists() || gf.length() == 0) {
                System.out.println(
                    "[INFO] RECORDING GOLDENFILE: " + recordGolden);
                // write the goldenfile using the encoding specified in the response.
                // if there is no encoding available, default to ISO-8859-1
                String encoding = "ISO-8859-1";
                if (saveHeaders.containsKey("content-type")) {
                    List vals = (List) saveHeaders.get("content-type");
                    if (vals != null) {
                        String val = (String) vals.get(0);
                        int charIdx = val.indexOf('=');
                        if (charIdx > -1) {
                            encoding = val.substring(charIdx + 1).trim();
                        }
                    }
                }
                OutputStreamWriter out = null;
                try {
                    out = new OutputStreamWriter(new FileOutputStream(gf),
                                                 encoding);
                    for (int i = 0, size = saveResponse.size(); i < size; i++) {
                        out.write((String) saveResponse.get(i));
                        out.write('\n');
                    }
                    out.flush();
                } catch (Throwable t) {
                    System.out.println(
                        "[WARNING] Unable to write goldenfile: " +
                        t.toString());
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException ioe) {
                        ; // do nothing
                    }
                }
            }
        }
        return ("Failed Golden File Comparison");

    }


    /**
     * Validate the saved headers against the <code>outHeaders</code>
     * property, and return an error message if there is anything missing.
     * If all of the expected headers are present, return <code>null</code>.
     */
    protected String validateHeaders() {

        // Do we have any headers to check for?
        if (outHeaders == null) {
            return (null);
        }

        // Check each specified name:value combination
        String headers = outHeaders;
        while (headers.length() > 0) {
            // Parse the next name:value combination
            int delimiter = headers.indexOf("##");
            String header = null;
            if (delimiter < 0) {
                header = headers;
                headers = "";
            } else {
                header = headers.substring(0, delimiter);
                headers = headers.substring(delimiter + 2);
            }
            int colon = header.indexOf(":");
            String name = header.substring(0, colon).trim();
            String value = header.substring(colon + 1).trim();
            // Check for the occurrence of this header
            ArrayList list = (ArrayList) saveHeaders.get(name.toLowerCase());
            if (list == null) {
                return ("Missing header name '" + name + "'");
            }
            boolean found = false;
            for (int i = 0, size = list.size(); i < size; i++) {
                if (value.equals((String) list.get(i))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return ("Missing header name '" + name + "' with value '" +
                    value + "'");
            }
        }

        // Everything was found successfully
        return (null);

    }


    /**
     * Return <code>true</code> if we should ignore this golden file line
     * because it is also in the ignore file.
     *
     * @param line Line from the golden file to be checked
     */
    protected boolean validateIgnore(String line) {

        for (int i = 0, size = saveIgnore.size(); i < size; i++) {
            String ignore = (String) saveIgnore.get(i);
            if (ignore.equals(line)) {
                return (true);
            }
        }
        return (false);

    }

    /**
     * Return <code>true</code> if we should ignore this golden file line
     * because it is also in the ignore file.
     *
     * @param line Line from the golden file to be checked
     */
    protected boolean validateIgnoreIfContains(String line) {

        for (int i = 0, size = saveIgnoreIfContains.size(); i < size; i++) {
            String ignoreIfContains = (String) saveIgnoreIfContains.get(i);
            if (-1 != line.indexOf(ignoreIfContains)) {
                return (true);
            }
        }
        return (false);

    }

    /**
     * Validate the returned response message against what we expected.
     * Return <code>null</code> for no problems, or an error message.
     *
     * @param message The returned response message
     */
    protected String validateMessage(String message) {

        if (this.message == null) {
            return (null);
        } else if (this.message.equals(message)) {
            return (null);
        } else {
            return ("Expected message='" + this.message + "', got message='" +
                message + "'");
        }

    }


    /**
     * Validate the returned status code against what we expected.  Return
     * <code>null</code> for no problems, or an error message.
     *
     * @param status The returned status code
     */
    protected String validateStatus(int status) {

        if (this.status == 0) {
            return (null);
        }
        if (this.status == status) {
            return (null);
        } else {
            return ("Expected status=" + this.status + ", got status=" +
                status);
        }

    }


}
