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

package javax.faces.component.visit;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * <p class="changed_added_2_0">A context object that is used to hold 
 * state relating to a component tree visit.</p>
 *
 * RELEASE_PENDING (edburns, rogerk) documentation
 *
 * <p>Component tree visits are initiated by calling
 * {@link UIComponent#visitTree UIComponent.visitTree()}, 
 * at which point both a {@link VisitContext} and a {@link VisitCallback} 
 * must be provided.
 *
 * @see UIComponent#visitTree UIComponent.visitTree()
 * @see VisitCallback
 *
 * @since 2.0
 */
abstract public class VisitContext {

    // Design notes: The VisitContext contract could be defined
    // as an interface.  However, there is the potential that we
    // may need to add new methods in the future, so leaving as 
    // an abstract class in order to have room to grow.
    // 
    // Since we are an abstract class rather than an interface,
    // we could provide implementations of of some of the simpler
    // methods (eg. getFacesContext() and getHints()) to avoid 
    // duplicating this code in VisitContext implementations.
    // However, doing so would mean that "wrapping" VisitContext
    // implementations would be forced to pick up such implementations,
    // so going with a pure contract (no implementation).

    /**
     * <p>This unmodifiable Collection is returned by 
     * getIdsToVisit()and getSubtreeIdsToVisit() in cases where all ids
     * should be visited.</p>
     * <p>To simplify logic for visitTree() implementations, this Collection
     * always return {@code false} for {@code isEmpty}.  All other methods 
     * throw {@code UnsupportedOperationException}.</p>
     */
    // Note: We cannot use Collections.emptyList() as that returns
    // a shared instance - we want to unique instance to allow for
    // identity tests.
    static public final Collection<String> ALL_IDS = 
        new AbstractCollection<String>() {

            @Override
            public Iterator<String> iterator() {
                throw new UnsupportedOperationException(
                    "VisitContext.ALL_IDS does not support this operation");
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException(
                    "VisitContext.ALL_IDS does not support this operation");
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };

    /**
     * <p>Returns the FacesContext for the current request.</p>
     */
    abstract public FacesContext getFacesContext();

    /**
     * <p>
     * Returns the ids of the components to visit.
     * </p>
     * <p>
     * In the case of a full tree visit, this method returns the
     * ALL_IDS collection.  Otherwise, if a partial visit is beign
     * performed, returns a modifiable collection containing the
     * client ids of the components that should be visited.
     * </p>
     * @return {@code VisitContext.ALL_IDS}, or a modifiable 
     * Collection of client ids.
     */
    abstract public Collection<String> getIdsToVisit();

    /**
     * <p>
     * Given a NamingContainer component, returns the client ids of 
     * any components underneath the NamingContainer that should be
     * visited.
     * </p>
     * <p>
     * This method is called by NamingContainer visitTree() implementations 
     * to determine whether the NamingContainer contains components to be 
     * visited.  In the case where no such components exist, the 
     * NamingContainer can short-circuit the tree visit and avoid 
     * descending into child subtrees.
     * </p>
     * <p>
     * In addition, iterating components such as UIData may be able to
     * use the returned ids to determine which iterated states (ie. rows)
     * need to be visited.  This allows the visit traversal to be
     * contstrained such only those rows that contain visit targets
     * need to be traversed.
     * </p>
     * @param component a NamingContainer component
     * @return an unmodifiable Collection containing the client ids of 
     *   any components underneath the NamingContainer that are known to be
     *   targets of the tree visit.  If no such components exist, returns 
     *   an empty Collection.  If all components underneath the 
     *   NamingContainer should be visited, returns the
     *   {@code VisitContext.ALL_IDS} collection.
     * @throws IllegalArgumentException if {@code component} is not
     *  an instance of NamingContainer
     */
    abstract public Collection<String> getSubtreeIdsToVisit(UIComponent component);

    /**
     * <p>Called by {@link UIComponent#visitTree UIComponent.visitTree()}
     * to visit a single component.</p>
     *
     * @param component the component to visit
     * @param callback the VisitCallback to call
     * @return a VisitResult value that indicates whether to continue
     *   visiting the component's subtree, skip visiting the component's
     *   subtree or abort the visit altogether.
     */
    abstract public VisitResult invokeVisitCallback(UIComponent component, 
                                                    VisitCallback callback); 

    /**
     * <p>Returns hints that influence the behavior of the tree visit.</p>
     *
     * <p>Interested parties, such as 
     * {@link UIComponent#visitTree UIComponent.visitTree()} implementations,
     * may check to see whether a particular hint is present by calling
     * {@code VisitContext.getHints().contains()}, passing in one of the
     * hints defined by {@link VisitHint}.
     *   
     * @return a non-empty, unmodifiable collection of VisitHints
     */
    abstract public Set<VisitHint> getHints();


    /**
     * <p>Creates a VisitContext instance for use with 
     * {@link UIComponent#visitTree UIComponent.visitTree()}.</p>
     *
     * @param context the FacesContext for the current request
     * @param ids the client ids of the components to visit.  If null,
     *   all components will be visited.
     * @param hints the VisitHints to apply to the visit
     * @return a VisitContext instance that is initialized with the 
     *   specified ids and hints.
     */
    public static VisitContext createVisitContext(FacesContext context,
                                                  Collection<String> ids,
                                                  Set<VisitHint> hints) {

        VisitContextFactory factory = (VisitContextFactory)
                FactoryFinder.getFactory(FactoryFinder.VISIT_CONTEXT_FACTORY);
        return factory.getVisitContext(context, ids, hints);

    }

    /**
     * <p>Creates a VisitContext instance for use with 
     * {@link UIComponent#visitTree UIComponent.visitTree()}.
     * This method can be used to obtain a VisitContext instance
     * when all components should be visited with the default
     * visit hints.</p>
     * @param context the FacesContext for the current request
     * @return a VisitContext instance
     */
    public static VisitContext createVisitContext(FacesContext context) {

        return createVisitContext(context, null, null);
        
    }
    
}
