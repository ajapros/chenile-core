package org.chenile.core.event;

import java.util.*;

import org.chenile.base.exception.ServerException;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.EventLog;
import org.chenile.core.entrypoint.ChenileEntryPoint;
import org.chenile.core.errorcodes.ErrorCodes;
import org.chenile.core.interceptors.ChenileExceptionHandler;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileEventDefinition;
import org.chenile.core.model.SubscriberVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * A sequential way of processing an event within the same thread. Sends the event payload to
 * each of the event subscribers in a loop. This can be used if a simple SEDA pipeline needs
 * to be established in the same thread within a single transaction. An example of this is
 * logging threads which need to do a bunch of things before a user is considered to be logged in.
 * <p>This should not be used if parallelism is desired. </p>
 * <p>This is a very handy way of doing testing. It is also internally used by all Chenile subscribers
 * at the entry point. Chenile file watcher, scheduler etc. use this class internally.</p>
 */
public class EventProcessor {
	Logger logger = LoggerFactory.getLogger(ChenileExceptionHandler.class);

	@Autowired  @Qualifier("chenileServiceConfiguration") ChenileConfiguration chenileServiceConfiguration;
	@Autowired  ChenileEntryPoint chenileEntryPoint;

	
	public EventProcessor() {
	}

	public void handleEvent(String eventId, Object eventPayload) {
		handleEvent(eventId, eventPayload, new HashMap<>());
	}

	public List<ChenileExchange> handleEvent(String eventId, Object eventPayload, Map<String,String> headers) {
		List<ChenileExchange> resExcahngeList = new ArrayList<>();
		ChenileEventDefinition ced = chenileServiceConfiguration.getEvents().get(eventId);
		if (ced == null) {
			//throw new ServerException(ErrorCodes.UNKNOWN_EVENT.getSubError(), new Object[]{eventId});
			logger.warn("No event listener found for event {}",eventId);
			return resExcahngeList;
		}

		Set<SubscriberVO> subscribers = ced.getEventSubscribers();
		if(subscribers == null || subscribers.isEmpty()) {
			System.err.println("Subscribers is null");
			return resExcahngeList;
		}
		for(SubscriberVO subscriber: subscribers) {
			ChenileExchange chenileExchange = new ChenileExchange();
			chenileExchange.setServiceDefinition(subscriber.serviceDefinition);
			chenileExchange.setOperationDefinition(subscriber.operationDefinition);
			if(!headers.isEmpty()){
				chenileExchange.getHeaders().putAll(headers);
			}

			setHeaders(chenileExchange);
			chenileExchange.setBody(eventPayload);
			chenileEntryPoint.execute(chenileExchange);
			resExcahngeList.add(chenileExchange);
		}
		return resExcahngeList;
	}



	public void handleEventAsync(String eventId, Object eventPayload) {

	}

	public void handleEvent(ChenileEventDefinition ced, ChenileExchange chenileExchange) {
		Set<SubscriberVO> subscribers = ced.getEventSubscribers();
		if(subscribers == null || subscribers.isEmpty()) return;
		for(SubscriberVO subscriber: subscribers) {
			ChenileExchange exchange = new ChenileExchange(chenileExchange);
			exchange.setServiceDefinition(subscriber.serviceDefinition);
			exchange.setOperationDefinition(subscriber.operationDefinition);
			chenileEntryPoint.execute(exchange);
		}
	}

	/**
	 * An Extension point to allow subclasses to set the headers in the Chenile Exchange prior to
	 * invoking it. These headers would be app specific and hence this class needs to be subclassed 
	 * by the actual application for the purpose of setting headers.
	 * @param chenileExchange
	 */
	protected void setHeaders(ChenileExchange chenileExchange) {
		
		
	}
}
