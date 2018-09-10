package org.example.listener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import org.apache.jackrabbit.commons.webdav.EventUtil;
import org.hippoecm.hst.site.HstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event Listener for Jcr tags.
 * 
 * assessment 2.6 
 * It prints out details to the log file about events that occur on nodes under /content/ in the tree. 
 * 
 * @author Erbil Kaplan
 * 
 */
public class JCREventListener implements EventListener {
	
	private static final Logger log = LoggerFactory.getLogger(JCREventListener.class);
	private static final String EVENT_LISTENER_PATH = "/content";
	private static final boolean IS_DEEP = true;
	private static final boolean NO_LOCAL = false;
    private static final int eventTypes = Event.NODE_ADDED | Event.NODE_MOVED | Event.NODE_REMOVED | Event.PERSIST
			| Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED;

	private Repository repository = HstServices.getComponentManager().getComponent(Repository.class.getName());
	private ObservationManager observationManager;
	private Session session;
    
	public JCREventListener(){
    	try {
			session = repository.login();
			observationManager = session.getWorkspace().getObservationManager();
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
		}
    }
    
    
    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
	@Override
	public void onEvent(final EventIterator it) {
		try {
			log.info("onEvent method triggered!!!!!!");
			while (it.hasNext()) {
				Event event = it.nextEvent();
				log.info("new event: path={}, userID={}, event={}", event.getPath(),
						event.getUserID(), EventUtil.getEventName(event.getType()));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
    /**
     * Register this observer with the JCR event listeners
     *
     * @throws RepositoryException if repository exception occurred
     */
    @PostConstruct
    public void startListening() throws RepositoryException {
        log.debug("Constructing an observer for JCR events...");
        observationManager.addEventListener(this, eventTypes, EVENT_LISTENER_PATH, IS_DEEP, null, null, NO_LOCAL);
    }
    
	/**
     * Remove listener and logout of the session
     *
     * @throws RepositoryException if repository exception occurred
     */
    @PreDestroy
    public void stopListening() throws RepositoryException {
        try {
            log.debug("Destroying an observer for JCR events...");
            if (observationManager != null) {
            	observationManager.removeEventListener(this);
            }
        } finally {
			if (session != null) {
				session.logout();
			}
        }
    }
    
    /**
     * just for debugging..
     * prints all event listeners to the log, which registered to the same observationManager before
     * 
     */
    public void printAllRegisteredListeners() {
		EventListenerIterator registeredEventListeners;
		try {
			registeredEventListeners = observationManager.getRegisteredEventListeners();
			while (registeredEventListeners.hasNext()) {
				Object object = (Object) registeredEventListeners.next();
				log.info("event=" + object);
			}
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
		}
	}
    
}