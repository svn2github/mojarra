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


package com.sun.faces.facelets.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Jacob Hookom
 * @author Roland Huss
 * @author Ales Justin (ales.justin@jboss.org)
 */
public final class Classpath {

    /**
     *
     */
    public Classpath() {
        super();
    }

    public static URL[] search(String prefix, String suffix)
          throws IOException {
        return search(Thread.currentThread().getContextClassLoader(), prefix,
                      suffix);
    }

    public static URL[] search(ClassLoader cl, String prefix, String suffix)
          throws IOException {
        Enumeration[] e = new Enumeration[]{
              cl.getResources(prefix),
              cl.getResources(prefix + "MANIFEST.MF")
        };
        Set all = new LinkedHashSet();
        URL url;
        URLConnection conn;
        JarFile jarFile;
        for (int i = 0, s = e.length; i < s; ++i) {
            while (e[i].hasMoreElements()) {
                url = (URL) e[i].nextElement();
                conn = url.openConnection();
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                if (conn instanceof JarURLConnection) {
                    jarFile = ((JarURLConnection) conn).getJarFile();
                } else {
                    jarFile = getAlternativeJarFile(url);
                }
                if (jarFile != null) {
                    searchJar(cl, all, jarFile, prefix, suffix);
                } else {
                    boolean searchDone =
                          searchDir(all, new File(URLDecoder.decode(url.getFile(), "UTF-8")), suffix);
                    if (!searchDone) {
                        searchFromURL(all, prefix, suffix, url);
                    }
                }
            }
        }
        URL[] urlArray = (URL[]) all.toArray(new URL[all.size()]);
        return urlArray;
    }

    private static boolean searchDir(Set result, File file, String suffix)
          throws IOException {
        if (file.exists() && file.isDirectory()) {
            File[] fc = file.listFiles();
            String path;
            URL src;
            // protect against Windows JDK bugs for listFiles -
            // if it's null (even though it shouldn't be) return false
            if (fc == null) return false;

            for (int i = 0; i < fc.length; i++) {
                path = fc[i].getAbsolutePath();
                if (fc[i].isDirectory()) {
                    searchDir(result, fc[i], suffix);
                } else if (path.endsWith(suffix)) {
                    // result.add(new URL("file:/" + path));
                    result.add(fc[i].toURL());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Search from URL. Fall back on prefix tokens if not able to read from
     * original url param.
     *
     * @param result the result urls
     * @param prefix the current prefix
     * @param suffix the suffix to match
     * @param url    the current url to start search
     *
     * @throws IOException for any error
     */
    private static void searchFromURL(Set result, String prefix, String suffix,
                                      URL url) throws IOException {
        boolean done = false;
        InputStream is = getInputStream(url);
        if (is != null) {
            ZipInputStream zis;
            if (is instanceof ZipInputStream) {
                zis = (ZipInputStream) is;
            } else {
                zis = new ZipInputStream(is);
            }
            try {
                ZipEntry entry = zis.getNextEntry();
                // initial entry should not be null
                // if we assume this is some inner jar
                done = (entry != null);
                while (entry != null) {
                    String entryName = entry.getName();
                    if (entryName.endsWith(suffix)) {
                        String urlString = url.toExternalForm();
                        result.add(new URL(urlString + entryName));
                    }
                    entry = zis.getNextEntry();
                }
            } finally {
                zis.close();
            }
        }
        if (!done && prefix.length() > 0) {
            // we add '/' at the end since join adds it as well
            String urlString = url.toExternalForm() + "/";
            String[] split = prefix.split("/");
            prefix = join(split, true);
            String end = join(split, false);
            int p = urlString.lastIndexOf(end);
            url = new URL(urlString.substring(0, p));
            searchFromURL(result, prefix, suffix, url);
        }
    }

    /**
     * Join tokens, exlude last if param equals true.
     *
     * @param tokens      the tokens
     * @param excludeLast do we exclude last token
     *
     * @return joined tokens
     */
    private static String join(String[] tokens, boolean excludeLast) {
        StringBuffer join = new StringBuffer();
        for (int i = 0; i < tokens.length - (excludeLast ? 1 : 0); i++) {
            join.append(tokens[i]).append("/");
        }
        return join.toString();
    }

    /**
     * Open input stream from url. Ignore any errors.
     *
     * @param url the url to open
     *
     * @return input stream or null if not possible
     */
    private static InputStream getInputStream(URL url) {
        try {
            return url.openStream();
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * For URLs to JARs that do not use JarURLConnection - allowed by the servlet
     * spec - attempt to produce a JarFile object all the same. Known servlet
     * engines that function like this include Weblogic and OC4J. This is not a
     * full solution, since an unpacked WAR or EAR will not have JAR "files" as
     * such.
     */
    private static JarFile getAlternativeJarFile(URL url) throws IOException {
        String urlFile = url.getFile();
        // Trim off any suffix - which is prefixed by "!/" on Weblogic
        int separatorIndex = urlFile.indexOf("!/");

        // OK, didn't find that. Try the less safe "!", used on OC4J
        if (separatorIndex == -1) {
            separatorIndex = urlFile.indexOf('!');
        }

        if (separatorIndex != -1) {
            String jarFileUrl = urlFile.substring(0, separatorIndex);
            // And trim off any "file:" prefix.
            if (jarFileUrl.startsWith("file:")) {
                jarFileUrl = jarFileUrl.substring("file:".length());
                jarFileUrl = URLDecoder.decode(jarFileUrl, "UTF-8");
            }
            return new JarFile(jarFileUrl);
        }
        return null;
    }

    private static void searchJar(ClassLoader cl, Set result, JarFile file,
                                  String prefix, String suffix)
          throws IOException {
        Enumeration e = file.entries();
        JarEntry entry;
        String name;
        while (e.hasMoreElements()) {
            try {
                entry = (JarEntry) e.nextElement();
            } catch (Throwable t) {
                continue;
            }
            name = entry.getName();
            if (name.startsWith(prefix) && name.endsWith(suffix)) {
                Enumeration e2 = cl.getResources(name);
                while (e2.hasMoreElements()) {
					result.add(e2.nextElement());
				}
			}
		}
	}

}
