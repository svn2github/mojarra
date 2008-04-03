<%--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 
 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.
 
 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"
 
 Contributor(s):
 
 If you wish your version of this file to be governed by only the CDDL or
 only the GPL Version 2, indicate your decision by adding "[Contributor]
 elects to include this software in this distribution under the [CDDL or GPL
 Version 2] license."  If you don't indicate a single choice of license, a
 recipient has the option to distribute your version of this file under
 either the CDDL, the GPL Version 2 or to extend the choice of license to
 its licensees as provided above.  However, if you add GPL Version 2 code
 and therefore, elected the GPL Version 2 license, then the option applies
 only if the new code is made subject to such option by the copyright
 holder.
--%>

<%@ include file="header.inc"%>
<f:view>
    <h:form>
        <risb:menu>
            <risb:menuItem id="jsfsites" value="JSF Sites">
                <risb:menuItem value="JSF RI Homepage" url="https://javaserverfaces.dev.java.net" />
                <risb:menuItem value="Glassfish" url="https://glassfish.dev.java.net" />
                <risb:menuItem id="subprojects" value="Sub Projects">
                    <risb:menuItem value="JSFTemplating" url="https://jsftemplating.dev.java.net" />
                    <risb:menuItem value="Facelets" url="https://facelets.dev.java.net" />
                </risb:menuItem>
            </risb:menuItem>
            <risb:menuItem id="devblogs" value="Developer Blogs">
                <risb:menuItem value="Ed Burns' Blog" url="http://weblogs.java.net/blog/edburns/" />
                <risb:menuItem value="Roger Kitain's Blog" url="http://weblogs.java.net/blog/rogerk/" />
                <risb:menuItem value="Ryan Lubke's Blog" url="http://blogs.sun.com/rlubke/" />
                <risb:menuItem value="Jacob Hookom's Blog #1" url="http://weblogs.java.net/blog/jhook/" />
                <risb:menuItem value="Jacob Hookom's Blog #2" url="http://hookom.blogspot.com" />
                <risb:menuItem value="Jason Lee's Blog" url="http://blogs.steeplesoft.com/javaserver-faces" />
            </risb:menuItem>
            <risb:menuItem value="Throw Away">
                <risb:menuItem>
                    <h:commandLink value="Home" action="home" />
                </risb:menuItem>
            </risb:menuItem>
        </risb:menu>
    </h:form>
</f:view>
<%@ include file="footer.inc"%>
