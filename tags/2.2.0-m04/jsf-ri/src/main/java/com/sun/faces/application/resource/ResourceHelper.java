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

package com.sun.faces.application.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.zip.GZIPOutputStream;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.el.ELContext;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;
import com.sun.faces.util.MessageUtils;

/**
 * <p>
 * Implementations of this class contain the knowledge for finding and serving
 * web application resources.
 * <p>
 *
 * @since 2.0
 */
public abstract class ResourceHelper {

    private static final Logger LOGGER = FacesLogger.RESOURCE.getLogger();

    /**
     * This pattern represents a version for a library.
     * Examples:
     *   1_1
     *   1_11
     *   1_11_1
     *   1_11_1_2
     */
    private static final Pattern LIBRARY_VERSION_PATTERN =
          Pattern.compile("^(\\d+)(_\\d+)+");

    /**
     * This pattern represents a version for a resource.
     * Examples:
     *   1_1.jpg
     *   1_11.323
     *   1_11_1.gif
     *   1_11_1_2.txt
     *   1_1
     *   1_11
     *   1_11_1
     *   1_11_1_2
     *
     *  The extension is optional.
     */
    private static final Pattern RESOURCE_VERSION_PATTERN =
          Pattern.compile("^((?:\\d+)(?:_\\d+)+)[\\.]?(\\w+)?");

    /**
     * Arbitrary file name to write the compressed bits to.
     */
    private static final String COMPRESSED_CONTENT_FILENAME =
          "compressed-content";

    private static final String[] EL_CONTENT_TYPES = {
          "text/css",
    };

    static {
        Arrays.sort(EL_CONTENT_TYPES);
    }


    // ---------------------------------------------------------- Public Methods


    /**
     * @return the base path in which resources will be stored
     */
    public abstract String getBaseResourcePath();


    /**
     * <p>
     * If the resource is compressable, return an InputStream to read the
     * compressed content, otherwise, call {@link #getNonCompressedInputStream(ResourceInfo, javax.faces.context.FacesContext)}
     * to return the content of the original resource.
     * </p>
     * <p>
     * Implementation Note:  If any exception occurs trying to return a stream
     * to the compressed content, log the exception, and instead try to return
     * a stream to the original content.
     * </p>
     * @param toStream the resource to obtain an InputStream to
     * @param ctx the {@link javax.faces.context.FacesContext} for the current request
     * @return an <code>InputStream</code> to the resource, or
     *  <code>null</code> if no resource is found
     * @throws IOException if an error occurs obtaining the stream
     */
    public InputStream getInputStream(ResourceInfo toStream, FacesContext ctx)
    throws IOException {

        // PENDING(edburns): this is a sub-optimal implementation choice
        // done in the interest of prototyping.  It's never a good idea 
        // to do a switch statement based on the type of an object.
        
        InputStream in = null;
        
        if (toStream instanceof ClientResourceInfo) {
            ClientResourceInfo resource = (ClientResourceInfo) toStream;
        
            in = getInputStreamFromClientInfo(resource, ctx);
            if (null == in) {
                ClientResourceInfo resourceWithoutLocalePrefix = 
                        new ClientResourceInfo(resource, false);
                in = getInputStreamFromClientInfo(resourceWithoutLocalePrefix, ctx);
                if (null != in) {
                    resource.copy(resourceWithoutLocalePrefix);
                }
            }
        
        } else {
            // PENDING(edburns): get the input stream from the facelet ResourceInfo.
        }
        return in;

    }
    
    private InputStream getInputStreamFromClientInfo(ClientResourceInfo resource,
            FacesContext ctx) throws IOException {
        InputStream in = null;
        
        if (resource.isCompressable() && clientAcceptsCompression(ctx)) {
            if (!resource.supportsEL()) {
                try {
                    String path = resource.getCompressedPath();
                    in = new BufferedInputStream(
                            new FileInputStream(path
                            + File.separatorChar
                            + COMPRESSED_CONTENT_FILENAME));
                } catch (IOException ioe) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE,
                                ioe.getMessage(),
                                ioe);
                    }
                    // return null so that the override code will try to serve
                    // the non-compressed content
                    in = null;
                }
            } else {
                InputStream temp = null;
                try {
                    // using dynamic compression here
                    temp = new BufferedInputStream(
                            new ELEvaluatingInputStream(ctx,
                                    resource,
                                    getNonCompressedInputStream(resource,
                            ctx)));
                    byte[] buf = new byte[512];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
                    OutputStream out = new GZIPOutputStream(baos);
                    for (int read = temp.read(buf); read != -1; read = temp.read(buf)) {
                        out.write(buf, 0, read);
                    }
                    out.flush();
                    out.close();
                    in = new BufferedInputStream(
                            new ByteArrayInputStream(baos.toByteArray()));
                    
                } catch (IOException ioe) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE,
                                ioe.getMessage(),
                                ioe);
                    }
                } finally {
                    if (temp != null) {
                        try {
                            temp.close();
                        } catch (IOException ioe) {
                            // ignore
                        }
                    }
                }
            }
        }
        if (in == null) {
            if (resource.supportsEL()) {
                return new BufferedInputStream(
                        new ELEvaluatingInputStream(ctx,
                                resource,
                                getNonCompressedInputStream(resource,
                        ctx)));
            } else {
                in = getNonCompressedInputStream(resource, ctx);
            }
        }
        return in;
    }


    /**
     * @param resource the resource to obtain a URL reference to
     * @param ctx the {@link FacesContext} for the current request
     * @return a URL to the specified resource, otherwise <code>null</code>
     *  if no resource is found
     */
    public abstract URL getURL(ResourceInfo resource, FacesContext ctx);


    /**
     * Search for the specified library/localPrefix combination in an
     * implementation dependent manner.
     * @param libraryName the name of the library
     * @param localePrefix the logicial identifier for a locale specific library.
     *  if no localePrefix is configured, pass <code>null</code>
     * @param ctx the {@link FacesContext} for the current request
     * @return a {@link LibraryInfo} if a matching library based off the inputs
     *  can be found, otherwise returns <code>null</code>
     */
    public abstract LibraryInfo findLibrary(String libraryName,
                                            String localePrefix,
                                            FacesContext ctx);


    /**
     * <p>
     * Search for the specified resource based in the library/localePrefix/resourceName
     * combination in an implementation dependent manner.
     * </p>
     * <p>
     * If the resource is found, and is compressable, call {@link #handleCompression(com.sun.faces.application.resource.ClientResourceInfo)}
     * to compress the content.
     * </p>
     *
     * @param library the library this resource should be a part of.  If the
     *  the resource that is being searched for isn't part of a library, then
     *  pass <code>null</code>
     * @param resourceName the name of the resource that is being searched for
     * @param localePrefix the logicial identifier for a locale specific library.
     *  if no localePrefix is configured, pass <code>null</code>
     * @param compressable <code>true</code> if the resource can be compressed
     * @param ctx the {@link javax.faces.context.FacesContext} for the current request
     * @return a {@link ResourceInfo} if a matching resource based off the inputs
     *  can be found, otherwise returns <code>null</code>
     */
    public abstract ResourceInfo findResource(LibraryInfo library,
                                              String resourceName,
                                              String localePrefix,
                                              boolean compressable,
                                              FacesContext ctx);


    /**
     * <p>
     * The default implementation of this method will call through to
     * {@link ResourceHelper#getURL(ResourceInfo, javax.faces.context.FacesContext)}
     * and leverage the URL to obtain the date information of the resource and
     * return the value of <code>URLConnection.getLastModified()</code>
     * </p>
     * @param resource the resource in question
     * @param ctx the {@link FacesContext} for the current request
     * @return the date of the resource in milliseconds (since epoch),
     *  or <code>0</code> if the date cannot be determined
     */
    public long getLastModified(ResourceInfo resource, FacesContext ctx) {
        
        URL url = getURL(resource, ctx);
        // resource may have been deleted.
        if (url == null) {
            return 0;
        }
        long ret;
        InputStream input = null;
        try {
            URLConnection con = url.openConnection();
            con.setUseCaches(false);
            con.connect();
            input = con.getInputStream();
            ret = con.getLastModified();
        } catch (IOException ioe) {
            ret = 0;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) { }
            }
        }

        return ((ret >= 0) ? ret : 0);

    }


    // ------------------------------------------------------- Protected Methods


    /**
     * If a {@link ResourceInfo} is not compressable, {@link #getInputStream(ResourceInfo, javax.faces.context.FacesContext)}
     * will call this method to return a stream to the actual resource.
     *
     * @param info  the resource to obtain an InputStream to
     * @param ctx the {@link FacesContext} for the current request
     * @return an InputStream to the resource
     * @throws IOException if an error occurs obtaining the stream
     */
    protected abstract InputStream getNonCompressedInputStream(ResourceInfo info,
                                                               FacesContext ctx)
    throws IOException;


    /**
     * <p>
     * Given a collection of path names:
     * </p>
     * <pre>
     *   1.1, scripts, images, 1.2
     * </pre>
     * <p>
     * this method will pick out the directories that represent a library or
     * resource version and return the latest version found, if any.
     * </p>
     *
     * @param resourcePaths a collection of paths (consisting of single path
     *  elements)
     * @param isResource <code>true</code> if the version being looked up
     *  is for a reource, otherwise, pass <code>false</code> if the version
     *  is a library version
     * @return the latest version or if no version can be detected, otherwise
     *  this method returns <code>null</code>
     */
    protected VersionInfo getVersion(Collection<String> resourcePaths, boolean isResource) {

        List<VersionInfo> versionedPaths = new ArrayList<VersionInfo>(resourcePaths.size());
        for (String p : resourcePaths) {
            VersionInfo vp = getVersion(p, isResource);
            if (vp != null) {
                versionedPaths.add(vp);
            }
        }
        VersionInfo version = null;
        if (!versionedPaths.isEmpty()) {
            Collections.sort(versionedPaths);
            version = versionedPaths.get(versionedPaths.size() - 1);
        }
        return version;

    }


    /**
     * Utility method to compress the content of the original resource
     * to the temporary directory specified by {@link com.sun.faces.application.resource.ClientResourceInfo#getCompressedPath()}.
     *
     * @param info the resource to be compressed
     * @return <code>true</code> if compression succeeded <em>and</em> the compressed
     *  result is smaller than the original content, otherwise <code>false</code>
     * @throws IOException if any error occur reading/writing
     */
    protected boolean compressContent(ClientResourceInfo info)
    throws IOException {

        InputStream source = null;
        OutputStream dest = null;
        try {
            URL url = info.getHelper()
                  .getURL(info, FacesContext.getCurrentInstance());
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            conn.connect();
            source = conn.getInputStream();
            byte[] buf = new byte[512];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dest = new GZIPOutputStream(baos);
            int totalRead = 0;
            for (int len = source.read(buf); len != -1; len = source.read(buf)) {
                dest.write(buf, 0, len);
                totalRead += len;
            }
            dest.flush();
            try {
                dest.close();
            } catch (IOException ignored) { }

            if (baos.size() < totalRead) {
                String outputFile = info.getCompressedPath()
                                + File.separatorChar
                                + COMPRESSED_CONTENT_FILENAME;
                dest = new FileOutputStream(outputFile);
                dest.write(baos.toByteArray());
                dest.flush();                
                return true;
            }
            return false;
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (IOException ignored) { }
            }
            if (dest != null) {
                try {
                    dest.close();
                } catch (IOException ignored) { }
            }
        }

    }


    /**
     * <p>
     * This method attempt to verify that the user agent can accept a gzip
     * encoded response by interrogating the <code>Accept-Encoding</code>
     * requester header.  If it is determined safe to send a gzip encoded
     * response, send the <code>Content-Encoding</code> header with a value
     * of <code>gzip</code>.</p>
     *
     * <p>
     * See <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html>RFC 2616, sec. 14</a>
     * for details on the accept-encoding header.
     * </p>
     *
     * <p>
     * Implementation Note:  It is safe to cast to a <code>HttpServletResponse</code>
     * as this method will only be called when handling a resource request.  Resource
     * serving is outside of the JSF and Portlet lifecycle.
     * </p>
     *
     * @param ctx the {@link FacesContext} for the current request
     * @return <code>true</code> if compressed content can be sent to the client,
     *  otherwise <code>false</code>
     */
    protected boolean clientAcceptsCompression(FacesContext ctx) {

        ExternalContext extCtx = ctx.getExternalContext();
        Object response = extCtx.getResponse();
        if (response instanceof HttpServletResponse) {
            String[] values =
                  extCtx.getRequestHeaderValuesMap().get("accept-encoding");
            boolean gzipFound = false;
            for (String value : values) {
                if (value.contains("gzip;q=0")) {
                    // gzip compression not accepted by the user-agent
                    return false;
                }
                if (value.contains("gzip")) {
                    // gzip compression explicitly listed as supported
                    // by the user agent.  Break here as we don't need to continue.
                    gzipFound = true;
                    break;
                }
                if (value.contains("*")
                      && (!value.contains("*;q=0,") && !value.endsWith("*;q=0"))) {
                    // gzip not explictly listed, but client sent *
                    // meaning gzip is implicitly acceptable
                    // keep looping to ensure we don't come across a
                    // *;q=0 value.
                    gzipFound = true;
                }
            }                                               
            
            if (gzipFound) {
                ((HttpServletResponse) response)
                      .setHeader("Content-Encoding", "gzip");
                return true;
            }
        }

        return false;

    }


    /**
     * <p>
     * Utility method to peform the necessary actions to compress content.
     * </p>
     *
     * <p>
     * Implmentation Note:  If an exception occurs while compressing the content,
     * log the IOException and rebuilt the {@link ResourceInfo} as non-compressable.
     * </p>
     *
     * @param resource the resource to compression
     * @return the ResourceInfo after compression is complete.  If compression
     *  was successful, this should be the same instance.  If compression was
     *  not successful, it will be a different instance than what was passed.
     */
    protected ClientResourceInfo handleCompression(ClientResourceInfo resource) {

        try {
            if (!resource.supportsEL() && !compressContent(resource)) {
                resource = rebuildAsNonCompressed(resource);
            }
        } catch (IOException ioe) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE,
                           ioe.getMessage(),
                           ioe);
            }
            resource = rebuildAsNonCompressed(resource);
        }
        return resource;

    }


    protected boolean resourceSupportsEL(String resourceName, String libraryName, FacesContext ctx) {

        ExternalContext extContext = ctx.getExternalContext();
        String contentType = extContext.getMimeType(resourceName);
        boolean result = (contentType != null
                   && (Arrays.binarySearch(EL_CONTENT_TYPES, contentType) >= 0)) ||
                (null != resourceName && null != libraryName && 
                "javax.faces".equals(libraryName) && "jsf.js".equals(resourceName));
        return result;

    }
    
    /**
     * @param s input String
     * @return the String without a leading slash if it has one.
     */
    protected String trimLeadingSlash(String s) {

        if (s.charAt(0) == '/') {
            return s.substring(1);
        } else {
            return s;
        }

    }

    // --------------------------------------------------------- Private Methods


    private ClientResourceInfo rebuildAsNonCompressed(ClientResourceInfo resource) {

        LibraryInfo library = resource.getLibraryInfo();
        ClientResourceInfo ret;
        if (library != null) {
            ret = new ClientResourceInfo(resource.library,
                                   resource.name,
                                   resource.version,
                                   false,
                                   resource.supportsEL,
                                   resource.isDevStage,
                                   resource.cacheTimestamp);
        } else {
            ret = new ClientResourceInfo(resource.name,
                                   resource.version,
                                   resource.localePrefix,
                                   this,
                                   false,
                                   resource.supportsEL,
                                   resource.isDevStage,
                                   resource.cacheTimestamp);
        }
        return ret;

    }

    /**
     * @param pathElement the path element to verify
     * @param isResource <code>true</code> if the version being looked up
     *  is for a reource, otherwise, pass <code>false</code> if the version
     *  is a library version
     * @return <code>true</code> if this path element represents a version
     *  (i.e. matches {@link #LIBRARY_VERSION_PATTERN}), otherwise
     *  returns <code>false</code>
     */
    private VersionInfo getVersion(String pathElement, boolean isResource) {

        String[] pathElements = Util.split(pathElement, "/");
        String path = pathElements[pathElements.length - 1];

        String extension = null;
        if (isResource) {
            Matcher m = RESOURCE_VERSION_PATTERN.matcher(path);
            return ((m.matches())
                    ? new VersionInfo(m.group(1), m.group(2))
                    : null);
        } else {
            return ((LIBRARY_VERSION_PATTERN.matcher(path).matches())
                    ? new VersionInfo(path, extension)
                    : null);
        }

    }


    // ---------------------------------------------------------- Nested Classes


    private static final class ELEvaluatingInputStream extends InputStream {

        // Premature optimization is the root of all evil.  Blah blah.
        private List<Integer> buf = new ArrayList<Integer>(1024);
        private boolean failedExpressionTest = false;
        private boolean writingExpression = false;
        private InputStream inner;
        private ClientResourceInfo info;
        private FacesContext ctx;
        private boolean expressionEvaluated;

        // ---------------------------------------------------- Constructors


        public ELEvaluatingInputStream(FacesContext ctx,
                                       ClientResourceInfo info,
                                       InputStream inner) {

            this.inner = inner;
            this.info = info;
            this.ctx = ctx;

        }


        // ------------------------------------------------ Methods from InputStream


        @Override
        public int read() throws IOException {
            int i;
            char c;

            if (failedExpressionTest) {
                i = nextRead;
                nextRead = -1;
                failedExpressionTest = false;
            } else if (writingExpression) {
                if (0 < buf.size()) {
                    i = buf.remove(0);
                } else {
                    writingExpression = false;
                    i = inner.read();
                }
            } else {
                // Read a character.
                i = inner.read();
                c = (char) i;
                // If it *might* be an expression...
                if (c == '#') {
                    // read another character.
                    i = inner.read();
                    c = (char) i;
                    // If it's '{', assume we have an expression.
                    if (c == '{') {
                        // read it into the buffer, and evaluate it into the
                        // same buffer.
                        readExpressionIntoBufferAndEvaluateIntoBuffer();
                        // set the flag so that we need to return content
                        // from the buffer.
                        writingExpression = true;
                        // Make sure to swallow the '{'.
                        i = this.read();
                    } else {
                        // It's not an expression, we need to return '#',
                        i = (int) '#';
                        // then return whatever we just read, on the
                        // *next* read;
                        nextRead = (int) c;
                        failedExpressionTest = true;
                    }
                }
            }

            return i;
        }


        private int nextRead = -1;


        private void readExpressionIntoBufferAndEvaluateIntoBuffer()
              throws IOException {
            int i;
            char c;
            do {
                i = inner.read();
                c = (char) i;
                if (c == '}') {
                    evaluateExpressionIntoBuffer();
                } else {
                    buf.add(i);
                }
            } while (c != '}' && i != -1);
        }

        /*
        * At this point, we know that getBuf() returns a List<Integer>
        * that contains the bytes of the expression.
        * Turn it into a String, turn the String into a ValueExpression,
        * evaluate it, store the toString() of it in
        * expressionResult;
        */
        private void evaluateExpressionIntoBuffer() {
            char chars[] = new char[buf.size()];
            for (int i = 0, len = buf.size(); i < len; i++) {
                chars[i] = (char) (int) buf.get(i);
            }
            String expressionBody = new String(chars);
            int colon;
            // If this expression contains a ":"
            if (-1 != (colon = expressionBody.indexOf(":"))) {
                // Make sure it contains only one ":"
                if (!isPropertyValid(expressionBody)) {
                    String message =
                          MessageUtils
                                .getExceptionMessageString(MessageUtils.INVALID_RESOURCE_FORMAT_COLON_ERROR,
                                                           expressionBody);
                    throw new ELException(message);
                }
                String[] parts = Util.split(expressionBody, ":");
                if (null == parts[0] || null == parts[1]) {
                    String message =
                          MessageUtils
                                .getExceptionMessageString(MessageUtils.INVALID_RESOURCE_FORMAT_NO_LIBRARY_NAME_ERROR,
                                                           expressionBody);
                    throw new ELException(message);

                }
                try {
                    int mark = parts[0].indexOf("[") + 2;
                    char quoteMark = parts[0].charAt(mark - 1);
                    parts[0] = parts[0].substring(mark, colon);
                    if (parts[0].equals("this")) {
                        parts[0] = info.getLibraryInfo().getName();
                        mark = parts[1].indexOf("]") - 1;
                        parts[1] = parts[1].substring(0, mark);
                        expressionBody = "resource[" + quoteMark + parts[0] +
                                         ":" + parts[1] + quoteMark + "]";
                    }
                }
                catch (Exception e) {
                    String message =
                          MessageUtils
                                .getExceptionMessageString(MessageUtils.INVALID_RESOURCE_FORMAT_ERROR,
                                                           expressionBody);
                    throw new ELException(message);

                }
            }
            ELContext elContext = ctx.getELContext();
            expressionEvaluated = true;
            ValueExpression ve =
                  ctx.getApplication().getExpressionFactory().
                        createValueExpression(elContext, "#{" + expressionBody +
                                                         "}", String.class);
            Object value = ve.getValue(elContext);
            String expressionResult = ((value != null) ? value.toString() : "");
            buf.clear();
            for (int i = 0, len = expressionResult.length(); i < len; i++) {
                buf.add((int) expressionResult.charAt(i));
            }
        }


        @Override
        public void close() throws IOException {

            if (!expressionEvaluated) {
                info.disableEL();
            }
            inner.close();
            super.close();

        }

        
        private boolean isPropertyValid(String property) {
            int idx = property.indexOf(':');
            return (property.indexOf(':', idx + 1) == -1);
        }

    } // END ELEvaluatingInputStream

}
