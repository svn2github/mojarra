/*
 * $Id: UISelectOneTestCase.java,v 1.6 2003/03/13 01:12:41 craigmcc Exp $
 */

/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.faces.component;


import java.util.Iterator;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangedEvent;
import javax.faces.mock.MockFacesContext;
import javax.faces.validator.Validator;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import javax.faces.mock.MockFacesContext;
import javax.faces.context.FacesContext;


/**
 * <p>Test case for the <strong>javax.faces.UISelectOne</strong>
 * concrete class.</p>
 */

public class UISelectOneTestCase extends UISelectBaseTestCase {


    // ----------------------------------------------------- Instance Variables

    // ----------------------------------------------------- Class Variables

    // ---------------------------------------------------------- Constructors

    public UISelectBase newUISelectBaseSubclass() {
	return new UISelectOne();
    }

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UISelectOneTestCase(String name) {
        super(name);
    }


    // -------------------------------------------------- Overall Test Methods

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {

        component = new UISelectOne();
        component.setComponentId("test");
        attributes = new String[0];
        rendererType = "Menu";

    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {

        return (new TestSuite(UISelectOneTestCase.class));

    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {

        component = null;

    }


    // ------------------------------------------------ Individual Test Methods


    /**
     * [3.1.1] Component Type.
     */
    public void testComponentType() {

        assertEquals("componentType", UISelectOne.TYPE,
                     component.getComponentType());

    }


    /**
     * [3.1.7] Attribute/Property Transparency
     */
    public void testAttributePropertyTransparency() {

        super.testAttributePropertyTransparency();
        UISelectOne selectOne = (UISelectOne) component;

        assertNull("selectedValue1", selectOne.getSelectedValue());
        assertNull("selectedValue2", selectOne.getValue());
        selectOne.setSelectedValue("foo");
        assertEquals("selectedValue3", "foo", selectOne.getSelectedValue());
        assertEquals("selectedValue4", "foo",
                     (String) selectOne.getValue());
        selectOne.setValue("bar");
        assertEquals("selectedValue5", "bar", selectOne.getSelectedValue());
        assertEquals("selectedValue6", "bar",
                     (String) selectOne.getValue());
        selectOne.setValue(null);
        assertNull("selectedValue7", selectOne.getSelectedValue());
        assertNull("selectedValue8", selectOne.getValue());

    }


    public void testSelectItemNoIds() {
        /* PENDING - cannot run this without initializing RenderKit
	doSelectItemNoIds();
        */
    }

    public void testSelectItemNoIdsCrazyOrder() {
        /* PENDING - cannot run this without initializing RenderKit
	doSelectItemNoIdsCrazyOrder();
        */
    }

    public void testSelectItemNoIdsExtraNonNamingContainerRootChild() {
        /* PENDING - cannot run this without initializing RenderKit
	doSelectItemNoIdsExtraNonNamingContainerRootChild();
        */
    }

    public void testSelectItemNoIdsExtraNamingContainerRootChild() {
        /* PENDING - cannot run this without initializing RenderKit
	doSelectItemNoIdsExtraNamingContainerRootChild();
        */
    }

    public void testSelectItemWithNamedRoot() {
        /* PENDING - cannot run this without initializing RenderKit
	doSelectItemWithNamedRoot();
        */
    }

    public void testSelectItemWithNamedSelectBase() {
        /* PENDING - cannot run this without initializing RenderKit
	doSelectItemWithNamedSelectBase();
        */
    }

    public void testSelectItemWithNamedSelectBaseExtraNamingContainerRootChild() {
        /* PENDING - cannot run this without initializing RenderKit
	doSelectItemWithNamedSelectBaseExtraNamingContainerRootChild();
        */
    }

    public void testDuplicateSelectItemIdsInNamedParent() {
	doDuplicateSelectItemIdsInNamedParent();
    }

    public void testDuplicateSelectItemIdsInUnnamedParent() {
	doDuplicateSelectItemIdsInUnnamedParent();
    }

    public void testDuplicateSelectBaseIds() {
	doDuplicateSelectBaseIds();
    }

    public void testFireValueChangeEvents() {

        MockFacesContext facesContext = new MockFacesContext();

        // case 1: previous value null, new value is null;
        // make sure ValueChangedEvent is not fired if new value is same
        // as the old value.
        UISelectOne selectOne = (UISelectOne) component;
        selectOne.setPrevious(selectOne.currentValue(facesContext));
        selectOne.setValue(null);
        selectOne.validate(facesContext);
        // ValueChangedEvent should not be fired in this case since the value
        // didn't change.
        Iterator eventsItr = facesContext.getFacesEvents();
        assertTrue(!(eventsItr.hasNext()));

        // case 2: previous value null, new value is "one";
        // make sure ValueChangedEvent is fired if new value is different
        // from the old value.
        Object selectedValue = "one";
        selectOne.setPrevious(selectOne.currentValue(facesContext));
        selectOne.setValue(selectedValue);
        selectOne.validate(facesContext);

        // ValueChangedEvent should be fired in this case since the value
        // changed
        eventsItr = facesContext.getFacesEvents();
        assertTrue((eventsItr.hasNext()));
        Object eventObj = eventsItr.next();
        // make sure it is an instance of ValueChangedEvent
        assertTrue(eventObj instanceof ValueChangedEvent);

        // case 3: previous value "one"
        // new value is "one"
        // create a new FacesContext make sure we don't have any events
        // queued from previous test case.
        facesContext = new MockFacesContext();
        selectOne.setPrevious(selectOne.currentValue(facesContext));
        Object selectedValue2 = "one";
        selectOne.setValue(selectedValue2);

        selectOne.validate(facesContext);
        // make sure ValueChangedEvent was not fired.
        eventsItr = facesContext.getFacesEvents();
        assertTrue(!(eventsItr.hasNext()));

    }
}
