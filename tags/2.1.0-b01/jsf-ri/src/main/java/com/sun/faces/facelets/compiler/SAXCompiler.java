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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2005-2007 The Apache Software Foundation
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

package com.sun.faces.facelets.compiler;

import com.sun.faces.RIConstants;
import com.sun.faces.facelets.tag.TagAttributeImpl;
import com.sun.faces.facelets.tag.TagAttributesImpl;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.faces.view.Location;
import javax.faces.view.facelets.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiler implementation that uses SAX
 * 
 * @see {@link com.sun.faces.facelets.compiler.Compiler}
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class SAXCompiler extends Compiler {
    
    private final static Pattern XmlDeclaration = Pattern.compile("^<\\?xml.+?version=['\"](.+?)['\"](.+?encoding=['\"]((.+?))['\"])?.*?\\?>");

    private static class CompilationHandler extends DefaultHandler implements
            LexicalHandler {

        protected final String alias;

        protected boolean inDocument = false;

        protected Locator locator;

        protected final CompilationManager unit;
        
        public CompilationHandler(CompilationManager unit, String alias) {
            this.unit = unit;
            this.alias = alias;
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {
            if (this.inDocument) {
                this.unit.writeText(new String(ch, start, length));
            }
        }

        public void comment(char[] ch, int start, int length)
                throws SAXException {
            if (this.inDocument) {
                this.unit.writeComment(new String(ch, start, length));
            }
        }

        protected TagAttributes createAttributes(Attributes attrs) {
            int len = attrs.getLength();
            TagAttribute[] ta = new TagAttribute[len];
            for (int i = 0; i < len; i++) {
                ta[i] = new TagAttributeImpl(this.createLocation(),
                        attrs.getURI(i), attrs.getLocalName(i), attrs
                                .getQName(i), attrs.getValue(i));
            }
            return new TagAttributesImpl(ta);
        }

        protected Location createLocation() {
            return new Location(this.alias, this.locator.getLineNumber(),
                    this.locator.getColumnNumber());
        }

        public void endCDATA() throws SAXException {
            if (this.inDocument) {
                this.unit.writeInstruction("]]>");
            }
        }

        public void endDocument() throws SAXException {
            super.endDocument();
        }

        public void endDTD() throws SAXException {
            this.inDocument = true;
        }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            this.unit.popTag();
        }

        public void endEntity(String name) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            this.unit.popNamespace(prefix);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            if (this.locator != null) {
            throw new SAXException("Error Traced[line: "
                    + this.locator.getLineNumber() + "] " + e.getMessage());
            } else {
                throw e;
            }
        }

        public void ignorableWhitespace(char[] ch, int start, int length)
                throws SAXException {
            if (this.inDocument) {
                this.unit.writeWhitespace(new String(ch, start, length));
            }
        }

        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException {
            String dtd = "com/sun/faces/xhtml/default.dtd";
            /*if ("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(publicId)) {
                dtd = "xhtml1-transitional.dtd";
            } else if (systemId != null && systemId.startsWith("file:/")) {
                return new InputSource(systemId);
            }*/
            URL url = this.getClass().getClassLoader().getResource(dtd);
            return new InputSource(url.toString());
        }

        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        public void startCDATA() throws SAXException {
            if (this.inDocument) {
                this.unit.writeInstruction("<![CDATA[");
            }
        }

        public void startDocument() throws SAXException {
            this.inDocument = true;
        }

        public void startDTD(String name, String publicId, String systemId)
                throws SAXException {
            if (this.inDocument) {
                // If we're in an ajax request, this is unnecessary and bugged
                // RELEASE_PENDING - this is a hack, and should probably not be here -
                // but the alternative is to somehow figure out how *not* to escape the "<!"
                // within the cdata of the ajax response.  Putting the PENDING in here to
                // remind me to have rlubke take a look.  But I'm stumped.
                StringBuffer sb = new StringBuffer(64);
                sb.append("<!DOCTYPE ").append(name);
                if (publicId != null) {
                    sb.append(" PUBLIC \"").append(publicId).append("\"");
                    if (systemId != null) {
                        sb.append(" \"").append(systemId).append("\"");
                    }
                } else if (systemId != null) {
                    sb.append(" SYSTEM \"").append(systemId).append("\"");
                }
                sb.append(">\n");
                this.unit.writeInstruction(sb.toString());
            }
            this.inDocument = false;
        }

        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            this.unit.pushTag(new Tag(this.createLocation(), uri, localName,
                    qName, this.createAttributes(attributes)));
        }

        public void startEntity(String name) throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            this.unit.pushNamespace(prefix, uri);
        }

        public void processingInstruction(String target, String data)
                throws SAXException {
            if (this.inDocument) {
                StringBuffer sb = new StringBuffer(64);
                sb.append("<?").append(target).append(' ').append(data).append(
                        "?>\n");
                this.unit.writeInstruction(sb.toString());
            }
        }
    }


    private static class MetadataCompilationHandler extends CompilationHandler {

        private static final String METADATA_HANDLER = "metadata";
        private boolean processingMetadata = false;
        private boolean metadataProcessed = false;


        // -------------------------------------------------------- Constructors


        public MetadataCompilationHandler(CompilationManager unit, String alias) {

            super(unit, alias);

        }


        // ------------------------------------- Methods from CompilationHandler


        @Override
        public void characters(char[] ch, int start, int length)
        throws SAXException {
            // no-op
        }

        @Override
        public void comment(char[] ch, int start, int length)
        throws SAXException {
            // no-op
        }

        @Override
        public void endCDATA() throws SAXException {
            // no-op
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
           // no-op
        }

        @Override
        public void startCDATA() throws SAXException {
            // no-op
        }

        @Override
        public void startDTD(String name, String publicId, String systemId)
        throws SAXException {
            // no-op
        }

        @Override
        public void startEntity(String name) throws SAXException {
            // no-op
        }

        @Override
        public void processingInstruction(String target, String data)
        throws SAXException {
            // no-op
        }



        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {

            if (!metadataProcessed) {
                if (!processingMetadata && RIConstants.CORE_NAMESPACE.equals(uri)) {
                    if (METADATA_HANDLER.equals(localName)) {
                        processingMetadata = true;
                    }
                }
                if (processingMetadata) {
                    super.startElement(uri, localName, qName, attributes);
                }
            }

        }

        @Override
        public void endElement(String uri, String localName, String qName)
        throws SAXException {

            if (!metadataProcessed) {
                if (processingMetadata) {
                    super.endElement(uri, localName, qName);
                }
                if (processingMetadata && RIConstants.CORE_NAMESPACE.equals(uri)) {
                    if (METADATA_HANDLER.equals(localName)) {
                        processingMetadata = false;
                        metadataProcessed = true;
                    }
                }
            }

        }

    }

    public SAXCompiler() {
        super();
    }

    public FaceletHandler doCompile(URL src, String alias) throws IOException {

        CompilationManager mgr = new CompilationManager(alias, this);
        CompilationHandler handler = new CompilationHandler(mgr, alias);
        return doCompile(mgr, handler, src, alias);

    }

    public FaceletHandler doMetadataCompile(URL src, String alias)
    throws IOException {

        CompilationManager mgr = new CompilationManager("metadata/" + alias, this);
        CompilationHandler handler = new MetadataCompilationHandler(mgr, alias);
        return doCompile(mgr, handler, src, alias);
    }

    protected FaceletHandler doCompile(CompilationManager mngr,
                                       CompilationHandler handler,
                                       URL src,
                                       String alias)
    throws IOException {

        InputStream is = null;
        String encoding = "UTF-8";
        try {
            is = new BufferedInputStream(src.openStream(), 1024);
            encoding = writeXmlDecl(is, mngr);
            SAXParser parser = this.createSAXParser(handler);
            parser.parse(is, handler);
        } catch (SAXException e) {
            throw new FaceletException("Error Parsing " + alias + ": "
                    + e.getMessage(), e.getCause());
        } catch (ParserConfigurationException e) {
            throw new FaceletException("Error Configuring Parser " + alias
                    + ": " + e.getMessage(), e.getCause());
        } catch (FaceletException e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
        }
        FaceletHandler result = new EncodingHandler(mngr.createFaceletHandler(), encoding,
                mngr.getCompilationMessageHolder());
        mngr.setCompilationMessageHolder(null);

        return result;

    }

    protected static String writeXmlDecl(InputStream is, CompilationManager mngr)
            throws IOException {
        is.mark(128);
        String encoding = "UTF-8";
        try {
            byte[] b = new byte[128];
            if (is.read(b) > 0) {
                String r = new String(b);
                Matcher m = XmlDeclaration.matcher(r);
                if (m.find()) {
                    mngr.writeInstruction(m.group(0) + "\n");
                    if (m.group(3) != null) {
                        encoding = m.group(3);
                    }
                }
            }
        } finally {
            is.reset();
        }
        return encoding;
    }

    private SAXParser createSAXParser(CompilationHandler handler)
            throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespace-prefixes",
                true);
        factory.setFeature("http://xml.org/sax/features/validation", this
                .isValidating());
        factory.setValidating(this.isValidating());
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                handler);
        reader.setErrorHandler(handler);
        reader.setEntityResolver(handler);
        return parser;
    }

}
