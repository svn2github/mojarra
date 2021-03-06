/*
 * $Id: UIInput.java,v 1.96.8.1 2008/04/21 20:31:24 edburns Exp $
 */

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

package javax.faces.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.MethodBinding;
import javax.faces.event.ExceptionEvent;
import javax.faces.event.ExceptionEventContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * <p><span class="changed_modified_2_0"><strong>UIInput</strong></span>
 * is a {@link UIComponent} that represents a component that both
 * displays output to the user (like {@link UIOutput} components do) and
 * processes request parameters on the subsequent request that need to
 * be decoded.  There are no restrictions on the data type of the local
 * value, or the object referenced by the value binding expression (if
 * any); however, individual {@link javax.faces.render.Renderer}s will
 * generally impose restrictions on the type of data they know how to
 * display.</p> <p/> <p>During the <em>Apply Request Values</em> phase
 * of the request processing lifecycle, the decoded value of this
 * component, usually but not necessarily a String, must be stored - but
 * not yet converted - using <code>setSubmittedValue()</code>.  If the
 * component wishes to indicate that no particular value was submitted,
 * it can either do nothing, or set the submitted value to
 * <code>null</code>.</p> <p></p> <p>By default, during the <em>Process
 * Validators</em> phase of the request processing lifecycle, the
 * submitted value will be converted to a typesafe object, and, if
 * validation succeeds, stored as a local value using
 * <code>setValue()</code>.  However, if the <code>immediate</code>
 * property is set to <code>true</code>, this processing will occur
 * instead at the end of the <em>Apply Request Values</em> phase.  </p>
 * <p>During the <em>Render Response</em> phase of the request
 * processing lifecycle, conversion for output occurs as for {@link
 * UIOutput}.</p> <p/> <p>When the <code>validate()</code> method of
 * this {@link UIInput} detects that a value change has actually
 * occurred, and that all validations have been successfully passed, it
 * will queue a {@link ValueChangeEvent}.  Later on, the
 * <code>broadcast()</code> method will ensure that this event is
 * broadcast to all interested listeners.  This event will be delivered
 * by default in the <em>Process Validators</em> phase, but can be
 * delivered instead during <em>Apply Request Values</em> if the
 * <code>immediate</code> property is set to <code>true</code>.</p> <p/>
 * <p>By default, the <code>rendererType</code> property must be set to
 * "<code>Text</code>".  This value can be changed by calling the
 * <code>setRendererType()</code> method.</p>
 */

public class UIInput extends UIOutput implements EditableValueHolder, 
    ClientBehaviorHolder {

    // ------------------------------------------------------ Manifest Constants


    /**
     * <p>The standard component type for this component.</p>
     */
    public static final String COMPONENT_TYPE = "javax.faces.Input";


    /**
     * <p>The standard component family for this component.</p>
     */
    public static final String COMPONENT_FAMILY = "javax.faces.Input";


    /**
     * <p>The message identifier of the
     * {@link javax.faces.application.FacesMessage} to be created if
     * a conversion error occurs, and neither the page author nor
     * the {@link ConverterException} provides a message.</p>
     */
    public static final String CONVERSION_MESSAGE_ID =
         "javax.faces.component.UIInput.CONVERSION";


    /**
     * <p>The message identifier of the
     * {@link javax.faces.application.FacesMessage} to be created if
     * a required check fails.</p>
     */
    public static final String REQUIRED_MESSAGE_ID =
         "javax.faces.component.UIInput.REQUIRED";

    /**
     * <p>The message identifier of the
     * {@link javax.faces.application.FacesMessage} to be created if
     * a model update error occurs, and the thrown exception has
     * no message.</p>
     */
    public static final String UPDATE_MESSAGE_ID =
         "javax.faces.component.UIInput.UPDATE";
    private static final Validator[] EMPTY_VALIDATOR = new Validator[0];

    // The default client event name for UIInput components
    private static final String DEFAULT_CLIENT_EVENT_NAME = "valueChange";

    // The set of client events for UIInput components.
    private static final Set<String> CLIENT_EVENT_NAMES;

    private Boolean emptyStringIsNull;

    // ------------------------------------------------------------ Constructors


    /**
     * <p>Create a new {@link UIInput} instance with default property
     * values.</p>
     */
    public UIInput() {

        super();
        setRendererType("javax.faces.Text");

    }

    // -------------------------------------------------------------- Properties


    public String getFamily() {

        return (COMPONENT_FAMILY);

    }


    /**
     * <p>The submittedValue value of this {@link UIInput} component.</p>
     */
    private Object submittedValue = null;


    /**
     * <p>Return the submittedValue value of this {@link UIInput} component.
     * This method should only be used by the <code>decode()</code> and
     * <code>validate()</code> method of this component, or
     * its corresponding {@link Renderer}.</p>
     */
    public Object getSubmittedValue() {

        return (this.submittedValue);

    }


    /**
     * <p>Set the submittedValue value of this {@link UIInput} component.
     * This method should only be used by the <code>decode()</code> and
     * <code>validate()</code> method of this component, or
     * its corresponding {@link Renderer}.</p>
     *
     * @param submittedValue The new submitted value
     */
    public void setSubmittedValue(Object submittedValue) {

        this.submittedValue = submittedValue;

    }

    public void setValue(Object value) {
        super.setValue(value);
        // Mark the local value as set.
        setLocalValueSet(true);
    }

    /**
     * <p>Convenience method to reset this component's value to the
     * un-initialized state.  This method does the following:</p>
     * <p/>
     * <p>Call {@link #setValue} passing <code>null</code>.</p>
     * <p/>
     * <p>Call {@link #setSubmittedValue} passing <code>null</code>.</p>
     * <p/>
     * <p>Call {@link #setLocalValueSet} passing <code>false</code>.</p>
     * <p/>
     * <p>Call {@link #setValid} passing <code>true</code>.</p>
     * <p/>
     * <p>Upon return from this call if the instance had a
     * <code>ValueBinding</code> associated with it for the "value"
     * property, this binding is evaluated when {@link
     * UIOutput#getValue} is called.  Otherwise, <code>null</code> is
     * returned from <code>getValue()</code>.</p>
     */

    public void resetValue() {
        this.setValue(null);
        this.setSubmittedValue(null);
        this.setLocalValueSet(false);
        this.setValid(true);
    }

    /**
     * <p>The "localValueSet" state for this component.
     */
    private boolean localValueSet;

    /**
     * Return the "local value set" state for this component.
     * Calls to <code>setValue()</code> automatically reset
     * this property to <code>true</code>.
     */
    public boolean isLocalValueSet() {
        return localValueSet;
    }

    /**
     * Sets the "local value set" state for this component.
     */
    public void setLocalValueSet(boolean localValueSet) {
        this.localValueSet = localValueSet;
    }


    /**
     * <p>The "required field" state for this component.</p>
     */
    private Boolean required;

    /**
     * <p>Return the "required field" state for this component.</p>
     */
    public boolean isRequired() {

        if (required != null) {
            return (this.required);
        }
        ValueExpression ve = getValueExpression("required");
        if (ve != null) {
            try {
                return (Boolean.TRUE.equals(ve.getValue(getFacesContext().getELContext())));
            }
            catch (ELException e) {
                throw new FacesException(e);
            }
        } else {
            return (false);
        }

    }

    private String requiredMessage;

    /**
     * <p>If there has been a call to {@link #setRequiredMessage} on this
     * instance, return the message.  Otherwise, call {@link #getValueExpression}
     * passing the key "requiredMessage", get the result of the expression, and return it.
     * Any {@link ELException}s thrown during the call to <code>getValue()</code>
     * must be wrapped in a {@link FacesException} and rethrown.
     */

    public String getRequiredMessage() {
        if (requiredMessage != null) {
            return requiredMessage;
        }

        ValueExpression ve = getValueExpression("requiredMessage");
        if (ve != null) {
            try {
                return ((String) ve.getValue(getFacesContext().getELContext()));
            }
            catch (ELException e) {
                throw new FacesException(e);
            }
        } else {
            return (null);
        }

    }

    /**
     * <p>Override any {@link ValueExpression} set for the "requiredMessage"
     * with the literal argument provided to this method.  Subsequent calls
     * to {@link #getRequiredMessage} will return this value;</p>
     *
     * @param message the literal message value to be displayed in the event
     *                the user hasn't supplied a value and one is required.
     */

    public void setRequiredMessage(String message) {
        requiredMessage = message;
    }

    private String converterMessage;

    /**
     * <p>If there has been a call to {@link #setConverterMessage} on this
     * instance, return the message.  Otherwise, call {@link #getValueExpression}
     * passing the key "converterMessage", get the result of the expression, and return it.
     * Any {@link ELException}s thrown during the call to <code>getValue()</code>
     * must be wrapped in a {@link FacesException} and rethrown.
     */

    public String getConverterMessage() {
        if (converterMessage != null) {
            return converterMessage;
        }

        ValueExpression ve = getValueExpression("converterMessage");
        if (ve != null) {
            try {
                return ((String) ve.getValue(getFacesContext().getELContext()));
            }
            catch (ELException e) {
                throw new FacesException(e);
            }
        } else {
            return (null);
        }

    }

    /**
     * <p>Override any {@link ValueExpression} set for the "converterMessage"
     * with the literal argument provided to this method.  Subsequent calls
     * to {@link #getConverterMessage} will return this value;</p>
     *
     * @param message the literal message value to be displayed in the event
     *                conversion fails.
     */

    public void setConverterMessage(String message) {
        converterMessage = message;
    }

    private String validatorMessage;

    /**
     * <p>If there has been a call to {@link #setValidatorMessage} on this
     * instance, return the message.  Otherwise, call {@link #getValueExpression}
     * passing the key "validatorMessage", get the result of the expression, and return it.
     * Any {@link ELException}s thrown during the call to <code>getValue()</code>
     * must be wrapped in a {@link FacesException} and rethrown.
     */

    public String getValidatorMessage() {
        if (validatorMessage != null) {
            return validatorMessage;
        }

        ValueExpression ve = getValueExpression("validatorMessage");
        if (ve != null) {
            try {
                return ((String) ve.getValue(getFacesContext().getELContext()));
            }
            catch (ELException e) {
                throw new FacesException(e);
            }
        } else {
            return (null);
        }

    }

    /**
     * <p>Override any {@link ValueExpression} set for the "validatorMessage"
     * with the literal argument provided to this method.  Subsequent calls
     * to {@link #getValidatorMessage} will return this value;</p>
     *
     * @param message the literal message value to be displayed in the event
     *                validation fails.
     */

    public void setValidatorMessage(String message) {
        validatorMessage = message;
    }

    private boolean valid = true;


    public boolean isValid() {

        return (this.valid);

    }


    public void setValid(boolean valid) {

        this.valid = valid;

    }


    /**
     * <p>Set the "required field" state for this component.</p>
     *
     * @param required The new "required field" state
     */
    public void setRequired(boolean required) {

        this.required = required;

    }

    /**
     * <p>The immediate flag.</p>
     */
    private Boolean immediate;


    public boolean isImmediate() {

        if (this.immediate != null) {
            return (this.immediate);
        }
        ValueExpression ve = getValueExpression("immediate");
        if (ve != null) {
            try {
                return (Boolean.TRUE.equals(ve.getValue(getFacesContext().getELContext())));
            }
            catch (ELException e) {
                throw new FacesException(e);
            }

        } else {
            return (false);
        }

    }


    public void setImmediate(boolean immediate) {

        this.immediate = immediate;

    }


    /**
     * <p>Return a <code>MethodBinding</code> pointing at a
     * method that will be called during <em>Process Validations</em>
     * phase of the request processing lifecycle, to validate the current
     * value of this component.</p>
     *
     * @deprecated {@link #getValidators} should be used instead.
     */
    public MethodBinding getValidator() {
        MethodBinding result = null;

        Validator[] curValidators = getValidators();
        // go through our lisetners list and find the one and only
        // MethodBindingValidator instance, if present.
        if (null != curValidators) {
            for (int i = 0; i < curValidators.length; i++) {
                // We are guaranteed to have at most one instance of
                // MethodBindingValidator in the curValidators list.
                if (MethodBindingValidator.class ==
                     curValidators[i].getClass()) {
                    result = ((MethodBindingValidator) curValidators[i]).
                         getWrapped();
                    break;
                }
            }
        }
        return result;

    }


    /**
     * <p>Set a <code>MethodBinding</code> pointing at a
     * method that will be called during <em>Process Validations</em>
     * phase of the request processing lifecycle, to validate the current
     * value of this component.</p>
     * <p/>
     * <p>Any method referenced by such an expression must be public, with
     * a return type of <code>void</code>, and accept parameters of type
     * {@link FacesContext}, {@link UIComponent}, and <code>Object</code>.</p>
     *
     * @param validatorBinding The new <code>MethodBinding</code> instance
     * @deprecated Use {@link #addValidator} instead, obtaining the
     *             argument {@link Validator} by creating an instance of {@link
     *             javax.faces.validator.MethodExpressionValidator}.
     */
    public void setValidator(MethodBinding validatorBinding) {
        Validator[] curValidators = getValidators();
        // see if we need to null-out, or replace an existing validator
        if (null != curValidators) {
            for (int i = 0; i < curValidators.length; i++) {
                // if we want to remove the validatorBinding
                if (null == validatorBinding) {
                    // We are guaranteed to have at most one instance of
                    // MethodBindingValidator in the curValidators
                    // list.
                    if (MethodBindingValidator.class ==
                         curValidators[i].getClass()) {
                        removeValidator(curValidators[i]);
                        return;
                    }
                }
                // if we want to replace the validatorBinding
                else //noinspection ObjectEquality
                    if (validatorBinding == curValidators[i]) {
                    removeValidator(curValidators[i]);
                    break;
                }
            }
        }
        addValidator(new MethodBindingValidator(validatorBinding));

    }

    public MethodBinding getValueChangeListener() {
        MethodBinding result = null;

        ValueChangeListener[] curListeners = getValueChangeListeners();
        // go through our lisetners list and find the one and only
        // MethodBindingValueChangeListener instance, if present.
        if (null != curListeners) {
            for (int i = 0; i < curListeners.length; i++) {
                // We are guaranteed to have at most one instance of
                // MethodBindingValueChangeListener in the curListeners list.
                if (MethodBindingValueChangeListener.class ==
                     curListeners[i].getClass()) {
                    result = ((MethodBindingValueChangeListener) curListeners[i]).
                         getWrapped();
                    break;
                }
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #addValueChangeListener} instead, obtaining the
     *             argument {@link ValueChangeListener} by creating an instance of {@link
     *             javax.faces.event.MethodExpressionValueChangeListener}.
     */
    public void setValueChangeListener(MethodBinding valueChangeListener) {

        ValueChangeListener[] curListeners = getValueChangeListeners();
        // see if we need to null-out, or replace an existing listener
        if (null != curListeners) {
            for (int i = 0; i < curListeners.length; i++) {
                // if we want to remove the valueChangeListener
                if (null == valueChangeListener) {
                    // We are guaranteed to have at most one instance of
                    // MethodBindingValueChangeListener in the curListeners
                    // list.
                    if (MethodBindingValueChangeListener.class ==
                         curListeners[i].getClass()) {
                        removeFacesListener(curListeners[i]);
                        return;
                    }
                }
                // if we want to replace the valueChangeListener
                else //noinspection ObjectEquality
                    if (valueChangeListener == curListeners[i]) {
                    removeFacesListener(curListeners[i]);
                    break;
                }
            }
        }
        addValueChangeListener(new MethodBindingValueChangeListener(valueChangeListener));
    }

    // ----------------------------------------------------- UIComponent Methods

    /**
     * <p>Specialized decode behavior on top of that provided by the
     * superclass.  In addition to the standard
     * <code>processDecodes</code> behavior inherited from {@link
     * UIComponentBase}, calls <code>validate()</code> if the the
     * <code>immediate</code> property is true; if the component is
     * invalid afterwards or a <code>RuntimeException</code> is thrown,
     * calls {@link FacesContext#renderResponse}.  </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    public void processDecodes(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        super.processDecodes(context);

        if (isImmediate()) {
            executeValidate(context);
        }
    }

    /**
     * <p>In addition to the standard <code>processValidators</code> behavior
     * inherited from {@link UIComponentBase}, calls <code>validate()</code>
     * if the <code>immediate</code> property is false (which is the
     * default);  if the component is invalid afterwards, calls
     * {@link FacesContext#renderResponse}.
     * If a <code>RuntimeException</code> is thrown during
     * validation processing, calls {@link FacesContext#renderResponse}
     * and re-throw the exception.
     * </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    public void processValidators(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        super.processValidators(context);
        if (!isImmediate()) {
            executeValidate(context);
        }
    }

    /**
     * <p>In addition to the standard <code>processUpdates</code> behavior
     * inherited from {@link UIComponentBase}, calls
     * <code>updateModel()</code>.
     * If the component is invalid afterwards, calls
     * {@link FacesContext#renderResponse}.
     * If a <code>RuntimeException</code> is thrown during
     * update processing, calls {@link FacesContext#renderResponse}
     * and re-throw the exception.
     * </p>
     *
     * @throws NullPointerException {@inheritDoc}
     */
    public void processUpdates(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        super.processUpdates(context);

        try {
            updateModel(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            context.renderResponse();
        }
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    public void decode(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Force validity back to "true"
        setValid(true);
        super.decode(context);
    }

    /**
     * <p><span class="changed_modified_2_0">Perform</span>
     * the following algorithm to update the model data
     * associated with this {@link UIInput}, if any, as appropriate.</p>
     * <ul>
     * <li>If the <code>valid</code> property of this component is
     * <code>false</code>, take no further action.</li>
     * <li>If the <code>localValueSet</code> property of this component is
     * <code>false</code>, take no further action.</li>
     * <li>If no {@link ValueExpression} for <code>value</code> exists,
     * take no further action.</li>
     * <li>Call <code>setValue()</code> method of the {@link ValueExpression}
     * to update the value that the {@link ValueExpression} points at.</li>
     * <li>If the <code>setValue()</code> method returns successfully:
     * <ul>
     * <li>Clear the local value of this {@link UIInput}.</li>
     * <li>Set the <code>localValueSet</code> property of this
     * {@link UIInput} to false.</li>
     * </ul></li>
     * <li>If the <code>setValue()</code> method throws an Exception:
     * <ul>
     * <li class="changed_modified_2_0">Enqueue an error message.  Create a
     * {@link FacesMessage} with the id {@link #UPDATE_MESSAGE_ID}.  Create a
     * {@link UpdateModelException}, passing the <code>FacesMessage</code> and
     * the caught exception to the constructor.  Create an
     * {@link ExceptionEventContext}, passing the <code>FacesContext</code>,
     * the <code>UpdateModelException</code>, this component instance, and
     * {@link PhaseId#UPDATE_MODEL_VALUES} to its constructor.  Call
     * {@link FacesContext#getExceptionHandler} and then call
     * {@link ExceptionHandler#processEvent}, passing the
     * <code>ExceptionEventContext</code>.
     * </li>
     * <li>Set the <code>valid</code> property of this {@link UIInput}
     * to <code>false</code>.</li>
     * </ul></li>
     * The exception must not be re-thrown.  This enables tree traversal
     * to continue for this lifecycle phase, as in all the other lifecycle
     * phases.
     * </ul>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @throws NullPointerException if <code>context</code>
     *                              is <code>null</code>
     */
    public void updateModel(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (!isValid() || !isLocalValueSet()) {
            return;
        }
        ValueExpression ve = getValueExpression("value");
        if (ve != null) {
            Throwable caught = null;
            FacesMessage message = null;
            try {
                ve.setValue(context.getELContext(), getLocalValue());
                setValue(null);
                setLocalValueSet(false);
            } catch (ELException e) {
                caught = e;
                String messageStr = e.getMessage();
                Throwable result = e.getCause();
                while (null != result &&
                     result.getClass().isAssignableFrom(ELException.class)) {
                    messageStr = result.getMessage();
                    result = result.getCause();
                }
                if (null == messageStr) {
                    message =
                         MessageFactory.getMessage(context, UPDATE_MESSAGE_ID,
                              MessageFactory.getLabel(
                                   context, this));
                } else {
                    message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                               messageStr,
                                               messageStr);
                }
                setValid(false);
            } catch (Exception e) {
                caught = e;
                message =
                     MessageFactory.getMessage(context, UPDATE_MESSAGE_ID,
                          MessageFactory.getLabel(
                               context, this));
                setValid(false);
            }
            if (caught != null) {
                assert(message != null);
                @SuppressWarnings({"ThrowableInstanceNeverThrown"})
                UpdateModelException toQueue =
                      new UpdateModelException(message, caught);
                ExceptionHandler exHandler = context.getExceptionHandler();
                ExceptionEventContext eventContext =
                      new ExceptionEventContext(context,
                                                toQueue,
                                                this,
                                                PhaseId.UPDATE_MODEL_VALUES);
                exHandler.processEvent(new ExceptionEvent(eventContext));
            }

        }
    }

    // ------------------------------------------------------ Validation Methods


    /**
     * <p><span class="changed_modified_2_0">Perform</span> the
     * following algorithm to validate the local value of this {@link
     * UIInput}.</p>

     * <ul>

     * <li>Retrieve the submitted value with {@link #getSubmittedValue}.
     * If this returns <code>null</code>, exit without further
     * processing.  (This indicates that no value was submitted for this
     * component.)</li>

     * <p/>

     * <li><span class="changed_modified_2_0">If the
     * <code>javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL</code>
     * context parameter value is <code>true</code> (ignoring case), and
     * <code>getSubmittedValue()</code> returns a zero-length
     * <code>String</code> call <code>{@link #setSubmittedValue}</code>,
     * passing <code>null</code> as the argument and continue processing
     * using <code>null</code> as the current submitted
     * value.</code></span></li>

     * <p/>

     * <li> Convert the submitted value into a "local value" of the
     * appropriate data type by calling {@link #getConvertedValue}.</li>
     * <p/>
     * <li>Validate the property by calling {@link #validateValue}.</li>
     * <p/>
     * <li>If the <code>valid</code> property of this component is still
     * <code>true</code>, retrieve the previous value of the component
     * (with <code>getValue()</code>), store the new local value using
     * <code>setValue()</code>, and reset the submitted value to
     * null.  If the local value is different from
     * the previous value of this component, fire a
     * {@link ValueChangeEvent} to be broadcast to all interested
     * listeners.</li>
     * </ul>
     * <p/>
     * <p>Application components implementing {@link UIInput} that wish to
     * perform validation with logic embedded in the component should perform
     * their own correctness checks, and then call the
     * <code>super.validate()</code> method to perform the standard
     * processing described above.</p>
     *
     * @param context The {@link FacesContext} for the current request
     * @throws NullPointerException if <code>context</code>
     *                              is null
     */
    public void validate(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Submitted value == null means "the component was not submitted
        // at all".  
        Object submittedValue = getSubmittedValue();
        if (submittedValue == null) {
            return;
        }

        // If non-null, an instanceof String, and we're configured to treat
        // zero-length Strings as null:
        //   call setSubmittedValue(null)
        if ((considerEmptyStringNull(context)
             && submittedValue instanceof String 
             && ((String) submittedValue).length() == 0)) {
            setSubmittedValue(null);
            submittedValue = null;
        }

        Object newValue = null;

        try {
            newValue = getConvertedValue(context, submittedValue);
        }
        catch (ConverterException ce) {
            addConversionErrorMessage(context, ce);
            setValid(false);
        }

        validateValue(context, newValue);

        // If our value is valid, store the new value, erase the
        // "submitted" value, and emit a ValueChangeEvent if appropriate
        if (isValid()) {
            Object previous = getValue();
            setValue(newValue);
            setSubmittedValue(null);
            if (compareValues(previous, newValue)) {
                queueEvent(new ValueChangeEvent(this, previous, newValue));
            }
        }

    }

    /**
     * <p>Convert the submitted value into a "local value" of the
     * appropriate data type, if necessary.  Employ the following
     * algorithm to do so:</p>
     * <ul>
     * <li>If a <code>Renderer</code> is present, call
     * <code>getConvertedValue()</code> to convert the submitted
     * value.</li>
     * <li>If no <code>Renderer</code> is present, and the submitted
     * value is a String, locate a {@link Converter} as follows:
     * <ul>
     * <li>If <code>getConverter()</code> returns a non-null {@link Converter},
     * use that instance.</li>
     * <li>Otherwise, if a value binding for <code>value</code> exists,
     * call <code>getType()</code> on it.
     * <ul>
     * <li>If this call returns <code>null</code>, assume the output
     * type is <code>String</code> and perform no conversion.</li>
     * <li>Otherwise, call
     * <code>Application.createConverter(Class)</code>
     * to locate any registered {@link Converter} capable of
     * converting data values of the specified type.</li>
     * </ul>
     * </li>
     * </ul>
     * <li>If a {@link Converter} instance was located, call its
     * <code>getAsObject()</code> method to perform the conversion.
     * If conversion fails:
     * <ul>
     * <li>Enqueue an appropriate error message by calling the
     * <code>addMessage()</code> method on the
     * <code>FacesContext</code>.</li>
     * <li>Set the <code>valid</code> property
     * on this component to <code>false</code> </li>
     * </ul></li>
     * <li>Otherwise, use the submitted value without any conversion</li>
     * </ul>
     * </li>
     * <p/>
     * </p>
     * <p/>
     * <p>This method can be overridden by subclasses for more specific
     * behavior.</p>
     */


    protected Object getConvertedValue(FacesContext context,
                                       Object newSubmittedValue) throws ConverterException {
        Renderer renderer = getRenderer(context);
        Object newValue;

        if (renderer != null) {
            newValue = renderer.getConvertedValue(context, this,
                 newSubmittedValue);
        } else if (newSubmittedValue instanceof String) {
            // If there's no Renderer, and we've got a String,
            // run it through the Converter (if any)
            Converter converter = getConverterWithType(context);
            if (converter != null) {
                newValue = converter.getAsObject(context, this,
                     (String) newSubmittedValue);
            } else {
                newValue = newSubmittedValue;
            }
        } else {
            newValue = newSubmittedValue;
        }
        return newValue;
    }

    /**
     * <p>Set the "valid" property according to the below algorithm.</p>
     * <p/>
     * <ul>
     * <p/>
     * <li>If the <code>valid</code> property on this component is still
     * <code>true</code>, and the <code>required</code> property is also
     * true, ensure that the local value is not empty (where "empty" is
     * defined as <code>null</code> or a zero-length String.  If the local
     * value is empty:
     * <ul>
     * <li>Enqueue an appropriate error message by calling the
     * <code>addMessage()</code> method on the <code>FacesContext</code>
     * instance for the current request.  If the {@link #getRequiredMessage}
     * returns non-<code>null</code>, use the value as the <code>summary</code>
     * and <code>detail</code> in the {@link FacesMessage} that
     * is enqueued on the <code>FacesContext</code>, otherwise
     * use the message for the {@link #REQUIRED_MESSAGE_ID}.
     * </li>
     * <li>Set the <code>valid</code> property on this component to
     * <code>false</code>.</li>
     * </ul></li>
     * <li>If the <code>valid</code> property on this component is still
     * <code>true</code>, and the local value is not empty, call the
     * <code>validate()</code> method of each {@link Validator}
     * registered for this {@link UIInput}, followed by the method
     * pointed at by the <code>validatorBinding</code> property (if any).
     * If any of these validators or the method throws a
     * {@link ValidatorException}, catch the exception, add
     * its message (if any) to the {@link FacesContext}, and set
     * the <code>valid</code> property of this component to false.</li>
     * <p/>
     * </ul>
     */

    protected void validateValue(FacesContext context, Object newValue) {
        // If our value is valid, enforce the required property if present
        if (isValid() && isRequired() && isEmpty(newValue)) {
            String requiredMessageStr = getRequiredMessage();
            FacesMessage message;
            if (null != requiredMessageStr) {
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                           requiredMessageStr,
                                           requiredMessageStr);
            } else {
                message =
                     MessageFactory.getMessage(context, REQUIRED_MESSAGE_ID,
                          MessageFactory.getLabel(
                               context, this));
            }
            context.addMessage(getClientId(context), message);
            setValid(false);
        }

        // If our value is valid and not empty, call all validators
        if (isValid() && !isEmpty(newValue)) {
            if (this.validators != null) {
                for (Validator validator : this.validators) {
                    try {
                        validator.validate(context, this, newValue);
                    }
                    catch (ValidatorException ve) {
                        // If the validator throws an exception, we're
                        // invalid, and we need to add a message
                        setValid(false);
                        FacesMessage message;
                        String validatorMessageString = getValidatorMessage();

                        if (null != validatorMessageString) {
                            message =
                                  new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                   validatorMessageString,
                                                   validatorMessageString);
                            message.setSeverity(FacesMessage.SEVERITY_ERROR);
                        } else {
                            message = ve.getFacesMessage();
                        }
                        if (message != null) {
                            context.addMessage(getClientId(context), message);
                        }
                    }
                }
            }
        }
    }


    /**
     * <p>Return <code>true</code> if the new value is different from the
     * previous value.</p>
     *
     * @param previous old value of this component (if any)
     * @param value    new value of this component (if any)
     */
    protected boolean compareValues(Object previous, Object value) {

        if (previous == null) {
            return (value != null);
        } else if (value == null) {
            return (true);
        } else {
            return (!(previous.equals(value)));
        }

    }


    /**
     * Executes validation logic.
     */
    private void executeValidate(FacesContext context) {
        try {
            validate(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            context.renderResponse();
        }
    }

    private static boolean isEmpty(Object value) {

        if (value == null) {
            return (true);
        } else if ((value instanceof String) &&
             (((String) value).length() < 1)) {
            return (true);
        } else if (value.getClass().isArray()) {
            if (0 == java.lang.reflect.Array.getLength(value)) {
                return (true);
            }
        } else if (value instanceof List) {
            if (((List) value).isEmpty()) {
                return (true);
            }
        }
        return (false);
    }


    /**
     * <p>The set of {@link Validator}s associated with this
     * <code>UIComponent</code>.</p>
     */
    List<Validator> validators = null;


    /**
     * <p>Add a {@link Validator} instance to the set associated with
     * this {@link UIInput}.</p>
     *
     * @param validator The {@link Validator} to add
     * @throws NullPointerException if <code>validator</code>
     *                              is null
     */
    public void addValidator(Validator validator) {

        if (validator == null) {
            throw new NullPointerException();
        }
        if (validators == null) {
            //noinspection CollectionWithoutInitialCapacity
            validators = new ArrayList<Validator>();
        }
        validators.add(validator);

    }
    

    /**
     * <p>Return the set of registered {@link Validator}s for this
     * {@link UIInput} instance.  If there are no registered validators,
     * a zero-length array is returned.</p>
     */
    public Validator[] getValidators() {

        if (validators == null) {
            return EMPTY_VALIDATOR;
        } else {
            return (validators.toArray(new Validator[validators.size()]));
        }

    }


    /**
     * <p>Remove a {@link Validator} instance from the set associated with
     * this {@link UIInput}, if it was previously associated.
     * Otherwise, do nothing.</p>
     *
     * @param validator The {@link Validator} to remove
     */
    public void removeValidator(Validator validator) {

        if (validators != null) {
            validators.remove(validator);
        }

    }

    // ------------------------------------------------ Event Processing Methods


    /**
     * <p>Add a new {@link ValueChangeListener} to the set of listeners
     * interested in being notified when {@link ValueChangeEvent}s occur.</p>
     *
     * @param listener The {@link ValueChangeListener} to be added
     * @throws NullPointerException if <code>listener</code>
     *                              is <code>null</code>
     */
    public void addValueChangeListener(ValueChangeListener listener) {

        addFacesListener(listener);

    }


    /**
     * <p>Return the set of registered {@link ValueChangeListener}s for this
     * {@link UIInput} instance.  If there are no registered listeners,
     * a zero-length array is returned.</p>
     */
    public ValueChangeListener[] getValueChangeListeners() {

        return (ValueChangeListener[]) getFacesListeners(ValueChangeListener.class);
    }


    /**
     * <p>Remove an existing {@link ValueChangeListener} (if any) from the
     * set of listeners interested in being notified when
     * {@link ValueChangeEvent}s occur.</p>
     *
     * @param listener The {@link ValueChangeListener} to be removed
     * @throws NullPointerException if <code>listener</code>
     *                              is <code>null</code>
     */
    public void removeValueChangeListener(ValueChangeListener listener) {

        removeFacesListener(listener);

    }

    // ----------------------------------------------------- StateHolder Methods


    private Object[] values;

    public Object saveState(FacesContext context) {

        if (values == null) {
            values = new Object[10];
        }

        values[0] = super.saveState(context);
        values[1] = localValueSet;
        values[2] = required;
        values[3] = requiredMessage;
        values[4] = converterMessage;
        values[5] = validatorMessage;
        values[6] = valid;
        values[7] = immediate;
        values[8] = saveAttachedState(context, validators);
        values[9] = emptyStringIsNull;
        return (values);

    }


    public void restoreState(FacesContext context, Object state) {

        values = (Object[]) state;
        super.restoreState(context, values[0]);
        localValueSet = (Boolean) values[1];
        required = (Boolean) values[2];
        requiredMessage = ((String) values[3]);
        converterMessage = ((String) values[4]);
        validatorMessage = ((String) values[5]);
        valid = (Boolean) values[6];
        immediate = (Boolean) values[7];
        List<Validator> restoredValidators;
        Iterator<Validator> iter;

        if (null != (restoredValidators = TypedCollections.dynamicallyCastList((List)
             restoreAttachedState(context, values[8]), Validator.class))) {
            // if there were some validators registered prior to this
            // method being invoked, merge them with the list to be
            // restored.
            if (null != validators) {
                iter = restoredValidators.iterator();
                while (iter.hasNext()) {
                    validators.add(iter.next());
                }
            } else {
                validators = restoredValidators;
            }
        }

        emptyStringIsNull = (Boolean) values[9];

    }

    /**
     * <p class="changed_added_2_0">Returns the set of client event
     * names supported by <code>UIInput</code> components.  The 
     * <code>Set</code> contains a single item: <code>"valueChange"</code>.</p>
     */
    @Override
    public Set<String> getClientEventNames() {
        return CLIENT_EVENT_NAMES;
    }

    /**
     * <p class="changed_added_2_0">Returns the default client event name 
     * for <code>UIInput</code> components: <code>"valueChange"</code>.</p>
     */
    @Override
     public String getDefaultClientEventName() {
         return DEFAULT_CLIENT_EVENT_NAME;
     }

    static {
        Set<String> eventNames = new HashSet<String>(1, 1.0f);
        eventNames.add(DEFAULT_CLIENT_EVENT_NAME);
        CLIENT_EVENT_NAMES = Collections.unmodifiableSet(eventNames);
    }

    private Converter getConverterWithType(FacesContext context) {
        Converter converter = getConverter();
        if (converter != null) {
            return converter;
        }

        ValueExpression valueExpression = getValueExpression("value");
        if (valueExpression == null) {
            return null;
        }

        Class converterType;
        try {
            converterType = valueExpression.getType(context.getELContext());
        }
        catch (ELException e) {
            throw new FacesException(e);
        }

        // if converterType is null, String, or Object, assume
        // no conversion is needed
        if (converterType == null ||
             converterType == String.class ||
             converterType == Object.class) {
            return null;
        }

        // if getType returns a type for which we support a default
        // conversion, acquire an appropriate converter instance.
        try {
            Application application = context.getApplication();
            return application.createConverter(converterType);
        } catch (Exception e) {
            return (null);
        }
    }

    private void addConversionErrorMessage(FacesContext context,
                                           ConverterException ce) {
        FacesMessage message;
        String converterMessageString = getConverterMessage();
        if (null != converterMessageString) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                       converterMessageString,
                                       converterMessageString);
        } else {
            message = ce.getFacesMessage();
            if (message == null) {
                message = MessageFactory.getMessage(context,
                     CONVERSION_MESSAGE_ID);                
                if (message.getDetail() == null) {
                    message.setDetail(ce.getMessage());
                }
            }
        }

        context.addMessage(getClientId(context), message);
    }


    private boolean considerEmptyStringNull(FacesContext ctx) {

        if (emptyStringIsNull == null) {
            String val = ctx.getExternalContext()
              .getInitParameter("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL");
            emptyStringIsNull = Boolean.valueOf(val);
        }

        return emptyStringIsNull;
        
    }

}
