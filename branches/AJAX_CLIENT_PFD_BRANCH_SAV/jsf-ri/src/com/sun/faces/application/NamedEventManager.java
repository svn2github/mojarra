/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.faces.application;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.context.FacesContext;
import javax.faces.event.AfterAddToParentEvent;
import javax.faces.event.AfterAddToViewEvent;
import javax.faces.event.BeforeRenderEvent;
import javax.faces.event.SystemEvent;

/**
 * Note: New, relevant spec'd ComponentSystemEvents must be added to the constructor
 */
public class NamedEventManager {

    private Map<String, Class<? extends SystemEvent>> namedEvents =
            new ConcurrentHashMap<String, Class<? extends SystemEvent>>();
    private Map<String, Set<Class<? extends SystemEvent>>> duplicateNames =
             new ConcurrentHashMap<String, Set<Class<? extends SystemEvent>>>();

    public NamedEventManager() {
        namedEvents.put("javax.faces.event.beforeRender", BeforeRenderEvent.class);
        namedEvents.put("javax.faces.event.afterAddToParent", AfterAddToParentEvent.class);
        namedEvents.put("javax.faces.event.afterAddToView", AfterAddToViewEvent.class);
        namedEvents.put("beforeRender", BeforeRenderEvent.class);
        namedEvents.put("afterAddToParent", AfterAddToParentEvent.class);
        namedEvents.put("afterAddToView", AfterAddToViewEvent.class);
    }

    public void addNamedEvent(String name, Class<? extends SystemEvent> event) {
        namedEvents.put(name, event);
    }

    public Class<? extends SystemEvent> getNamedEvent(String name) {
        String foo = namedEvents.toString();
        Class<? extends SystemEvent> namedEvent = namedEvents.get(name);
        return namedEvent;
    }

    public void addDuplicateName(String name, Class<? extends SystemEvent> event) {
        Class<? extends SystemEvent> registeredEvent = namedEvents.remove(name);
        Set<Class<? extends SystemEvent>> events = duplicateNames.get(name);
        if (events == null) {
            events = new HashSet<Class<? extends SystemEvent>>();
            duplicateNames.put(name, events);
        }
        events.add(event);
        if (registeredEvent != null) {
            events.add(registeredEvent);
        }
    }

    public boolean isDuplicateNamedEvent(String name) {
        return (namedEvents.get(name) != null) || (duplicateNames.get(name) != null);
    }
}
