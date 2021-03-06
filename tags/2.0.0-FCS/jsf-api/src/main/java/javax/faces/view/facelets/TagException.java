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
 * This file incorporates work covered by the following copyright and
 * permission notice:
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

package javax.faces.view.facelets;

/**
 * <p class="changed_added_2_0">An Exception caused by a {@link Tag}</p>
 * 
 * @since 2.0
 */
public final class TagException extends FaceletException {

    private static final long serialVersionUID = 1L;

    /**
     * <p class="changed_added_2_0">Wrap the argument <code>tag</code>
     * so the exception can reference its information.</p>
     * @param tag the <code>Tag</code> that caused this exception.
     */
    public TagException(Tag tag) {
        super(tag.toString());
    }

    /**
     * <p class="changed_added_2_0">Wrap the argument <code>tag</code>
     * so the exception can reference its information.</p>
     * @param tag the <code>Tag</code> that caused this exception.
     * @param message a message describing the exception
     */
    public TagException(Tag tag, String message) {
        super(tag + " " + message);
    }

    /**
     * <p class="changed_added_2_0">Wrap the argument <code>tag</code>
     * so the exception can reference its information.</p>
     * @param tag the <code>Tag</code> that caused this exception.
     * @param cause the root cause for this exception.
     */
    public TagException(Tag tag, Throwable cause) {
        super(tag.toString(), cause);
    }

    /**
     * <p class="changed_added_2_0">Wrap the argument <code>tag</code>
     * so the exception can reference its information.</p>
     * @param tag the <code>Tag</code> that caused this exception.
     * @param message a message describing the exception
     * @param cause the root cause for this exception.
     */
    public TagException(Tag tag, String message, Throwable cause) {
        super(tag + " " + message, cause);
    }

}
