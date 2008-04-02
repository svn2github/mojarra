/*
 * $Id: Tree.java,v 1.5 2002/09/20 00:24:13 craigmcc Exp $
 */

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.faces.tree;


import java.io.Serializable;
import javax.faces.component.UIComponent;
import javax.faces.render.RenderKit;


/**
 * <p><strong>Tree</strong> is a container for a tree of {@link UIComponent}
 * nodes rooted at a root node, along with associated metadata about those
 * {@link UIComponent}s.  Trees have unique identifiers, which associate
 * them with metadata information that can be acquired from external sources,
 * by means of a {@link TreeFactory} instance.  Tree identifiers must be
 * unique within the scope of a web application.</p>
 */

public abstract class Tree implements Serializable {


    /**
     * <p>Return the render kit identifier of the {@link RenderKit} instance
     * (if any) associated with this <code>Tree</code>.</p>
     */
    public abstract String getRenderKitId();


    /**
     * <p>Set the render kit identifier of the  {@link RenderKit} instance
     * (if any) associated with this <code>Tree</code>.</p>
     *
     * @param renderKit The new {@link RenderKit} identifier, or
     *  <code>null</code> to disassociate this tree with any specific
     *  {@link RenderKit} instance
     */
    public abstract void setRenderKitId(String renderKitId);


    /**
     * <p>Return the root node of the component tree associated with
     * this <code>Tree</code>, or <code>null</code> if this component
     * tree has no nodes.</p>
     */
    public abstract UIComponent getRoot();


    /**
     * <p>Return the tree identifier for this <code>Tree</code>.</p>
     */
    public abstract String getTreeId();


}
