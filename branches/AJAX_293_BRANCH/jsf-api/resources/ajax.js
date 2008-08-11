/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Copyright 2004 The Apache Software Foundation
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

/**
 * Register with OpenAjax
 */
if (typeof OpenAjax != "undefined" &&
    typeof OpenAjax.hub.registerLibrary != "undefined") {
    OpenAjax.hub.registerLibrary("javax", "www.sun.com", "1.0", null);
}

/**
 * Create our top level namespace - javax.faces.Ajax
 */
if (javax == null || typeof javax == "undefined") {
    var javax = new Object();
}
if (javax.faces == null || typeof javax.faces == "undefined") {
    javax["faces"] = new Object();
}
if (javax.faces.Ajax == null || typeof javax.faces.Ajax == "undefined") {
    javax.faces["Ajax"] = new Object();
}

/**
 * <p><span class="changed_added_2_0">Collect</span> and encode
 * state for input controls associated with the specified
 * <code>form</code> element. 
 *
 * @param form The <code>form</code> element whose contained
 * <code>input</code> controls will be collected and encoded.
 * Only successful controls will be collected and encoded in 
 * accordance with: <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.2">
 * Section 17.13.2 of the HTML Specification</a>.
 * @return The encoded state for the specified form's input controls.
 * @function viewState
 */
javax.faces.Ajax.viewState = function(form) {

    var viewState = javax.faces.Ajax.AjaxEngine.serializeForm(form);
    return viewState;

}

/**
 * <p><span class="changed_added_2_0">Send</span> an Ajax request
 * to the server.  This function must:
 * <ul>
 * <li>Capture the element that triggered this Ajax request
 * (from the <code>element</code> argument, also known as the
 * <code>source</code> element.</li>
 * <li>Determine the <code>source</code> element's <code>form</code>
 * element.</li>
 * <li>Get the <code>form</code> view state by calling 
 * <code>javax.faces.Ajax.viewState</code> passing the 
 * <code>form</code> element as the argument.</li> 
 * <li>Determine additional arguments from the <code>options</code>
 * argument.</li>
 * <li>Set up post data arguments for the Ajax request.  The following
 * name/value pairs must be passed as post data:
 * <ul>
 * <li>The name and value of the <code>source</code> element that 
 * triggered this request;</li>
 * <li><code>javax.faces.partial.ajax</code> with the value 
 * <code>true</code></li>
 * </ul>
 * </li>
 * <li>Optionally, the following arguments can be sent as post
 * data: 
 * <ul>
 * <li><code>javax.faces.partial.execute</code> with the value
 * <code>true</code>.</li> 
 * <li><code>javax.faces.partial.render</code> with the value
 * <code>true</code>.</li> 
 * <li>basic <code>event</code> information:
 * <ul>
 * <li><code>target</code> - the ID of the element that triggered the event.</li>
 * <li><code>captured</code> - the ID of the element that captured the event.</li>
 * <li><code>type</code> - the type of event (ex: onkeypress)</li>
 * </ul>
 * </li>
 * <li>mouse and keyboard <code>event</code> information:
 * <ul>
 * <li><code>alt</code> - <code>true</code> if ALT key was pressed.</li>
 * <li><code>ctrl</code> - <code>true</code> if CTRL key was pressed.</li>
 * <li><code>shift</code> - <code>true</code> if SHIFT key was pressed. </li>
 * <li><code>meta</code> - <code>true</code> if META key was pressed. </li>
 * <li><code>right</code> - <code>true</code> if right mouse button  
 * was pressed. </li>
 * <li><code>left</code> - <code>true</code> if left mouse button  
 * was pressed. </li>
 * <li><code>keycode</code> - the key code. 
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>  
 *
 * @param element The DOM element that triggered this Ajax request.
 * @param options The set of available options that can be sent as
 * request parameters to control client and/or server side 
 * request processing. This argument is optional. 
 * @param event The DOM event that triggered this Ajax request.  This
 * argument is optional.
 */
javax.faces.Ajax.ajaxRequest = function(element, options, event) {

    // Capture the element that triggered this Ajax request.
    var source = element;

    var utils = new javax.faces.Ajax.Utils();
    var form = utils.getForm(source); 
    var viewState = javax.faces.Ajax.viewState(form);

    // Set up additional arguments to be used in the request..
    var args = new Object();
    if (options.execute) {
        args["javax.faces.partial.execute"] = utils.toArray(options.execute,',').join(','); 
        options.execute = null;
        delete options.execute;
    }
    if (options.render) {
        args["javax.faces.partial.render"] = utils.toArray(options.render,',').join(','); 
        options.render = null;
        delete options.render;
    }

    utils.extend(args, options);
    args["javax.faces.partial.ajax"] = "true";
    args["method"] = "POST";
    args["url"] = form.action;
    // add source
    var action = utils.$(source);
    if (action && action.form) {
        args[action.name] = action.value || 'x';
    } else {
        args[source] = source;
    }

    var ajaxEngine = new javax.faces.Ajax.AjaxEngine();
    ajaxEngine.setupArguments(args);
    ajaxEngine.queryString = viewState;
    ajaxEngine.sendRequest();
} 

/**
 * <p><span class="changed_added_2_0">Receive</span> an Ajax response
 * from the server.  This function must evaluate the markup returned 
 * in the <code>responseXML</code> object and update the <code>DOM</code>
 * as follows:
 * <ul>
 * <li>Determine if the entire <code>DOM</code> should be replaced, or
 * if only specified sections (known as partial rendering) should be
 * updated.  The entire <code>DOM</code>must be replaced if a 
 * <code>render</code> element identifier is 
 * <code>javax.faces.viewRoot</code>.</li> 
 * <li>If partial <code>DOM</code> update is required, replace the 
 * <code>DOM</code> markup whose identifier matches the corresponding
 * <code>render</code> identifier.</li>
 * <li>Capture the view state sent in the response and insert it into
 * the <code>DOM</code> as a <code>hidden input</code> field with the
 * identifier <code>javax.faces.viewState</code>.  Look for all the
 * <code>form</code> elements in the <code>DOM</code>, and for each
 * <code>form</code> element, determine if a <code>javax.faces.viewState</code>
 * field exists.  If it does, replace it with the view state from the 
 * response.  If it does not exist, create a <code>hidden input</code>
 * field with the identifier <code>javax.faces.viewState</code> and 
 * insert it as a child element of the <code>form</code> elements.</li> 
 * </ul>
 *
 * @param request The <code>XMLHttpRequest</code> instance that 
 * contains the status code and response message from the server.
 */ 
javax.faces.Ajax.ajaxResponse = function(request) {

    var utils = new javax.faces.Ajax.Utils();
    var xmlReq = request.xmlReq;

    var xml = xmlReq.responseXML;
    var id, content, markup, str;

    var components = xml.getElementsByTagName('components')[0];
    var render = components.getElementsByTagName('render');

    for (var i = 0; i < render.length; i++) {
        id = render[i].getAttribute('id');
        // join the CDATA sections in the markup
        markup = '';
        for (var j = 0; j < render[i].firstChild.childNodes.length; j++) {
            content = render[i].firstChild.childNodes[j];
            markup += content.text || content.data;
        }
        str = utils.stripScripts(markup);
        var src = str;
         
        // If our special render all markup is present..
        if (-1 != id.indexOf("javax.faces.ViewRoot")) {
            // if src contains <html>, trim the <html> and </html>, if present.
            //   if src contains <head>
            //      extract the contents of <head> and replace current document's
            //      <head> with the contents.
            //   if src contains <body>
            //      extract the contents of <body> and replace the current
            //      document's <body> with the contents.
            //   if src does not contain <body>
            //      replace the current document's <body> with the contents.
            var
                htmlStartEx = new RegExp("< *html.*>", "gi"),
                htmlEndEx = new RegExp("< */ *html.*>", "gi"),
                headStartEx = new RegExp("< *head.*>", "gi"),
                headEndEx = new RegExp("< */ *head.*>", "gi"),
                bodyStartEx = new RegExp("< *body.*>", "gi"),
                bodyEndEx = new RegExp("< */ *body.*>", "gi"),
                htmlStart, htmlEnd, headStart, headEnd, bodyStart, bodyEnd;
            var srcHead = null, srcBody = null;
            // find the current document's "body" element
            var docBody = document.getElementsByTagName("body")[0];
            // if src contains <html>
            if (null != (htmlStart = htmlStartEx.exec(src))) {
                // if src contains </html>
                if (null != (htmlEnd = htmlEndEx.exec(src))) {
                    src = src.substring(htmlStartEx.lastIndex, htmlEnd.index);
                } else {
                    src = src.substring(htmlStartEx.lastIndex);
                }
            }
            // if src contains <head>
            if (null != (headStart = headStartEx.exec(src))) {
                // if src contains </head>
                if (null != (headEnd = headEndEx.exec(src))) {
                    srcHead = src.substring(headStartEx.lastIndex,
                        headEnd.index);
                } else {
                    srcHead = src.substring(headStartEx.lastIndex);
                }
                // find the "head" element
                var docHead = document.getElementsByTagName("head")[0];
                if (docHead) {
                    utils.elementReplace(docHead, "head", srcHead);
                }
            }       
            // if src contains <body>
            if (null != (bodyStart = bodyStartEx.exec(src))) {
                // if src contains </body>
                if (null != (bodyEnd = bodyEndEx.exec(src))) {
                    srcBody = src.substring(bodyStartEx.lastIndex,
                        bodyEnd.index);
                } else {
                    srcBody = src.substring(bodyStartEx.lastIndex);
                }
                result = utils.elementReplace(docBody, "body", srcBody);
            }
            if (!srcBody) {
                result = utils.elementReplace(docBody, "body", src);
            }
        
        } else {
            var d = utils.$(id);
            if (!d) {
                alert(id + " not found");
            }
            var parent = d.parentNode;
            var temp = document.createElement('div');
            var result = null;
            temp.id = d.id;
            temp.innerHTML = utils.trim(str);

            result = temp.firstChild;
            parent.replaceChild(temp.firstChild,d);
        }
    }

    // Now set the view state from the server into the DOM
    // If there are multiple forms, make sure they all have a 
    // viewState hidden field.

    var state = state || xml.getElementsByTagName('state')[0].firstChild;
    if (state) {
        var stateElem = utils.$("javax.faces.ViewState");
        if (stateElem) {
            stateElem.value = state.text || state.data;
        }
        var numForms = document.forms.length;
        var field;
        for (var i = 0; i < numForms; i++) {
            field = document.forms[i].elements["javax.faces.ViewState"];
            if (typeof field == 'undefined') {
                field = document.createElement("input");
                field.type = "hidden";
                field.name = "javax.faces.ViewState";
                document.forms[i].appendChild(field);
            }
            field.value = state.text || state.data;
        }
    }
}

