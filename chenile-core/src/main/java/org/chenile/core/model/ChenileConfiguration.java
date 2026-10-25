package org.chenile.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chenile.base.exception.ConfigurationException;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.errorcodes.ErrorCodes;
import org.chenile.core.event.EventLogger;
import org.chenile.core.interceptors.ChenileExceptionHandler;
import org.chenile.owiz.Command;
import org.springframework.context.ApplicationContext;


/**
 * The core class that contains the entire ChenileConfiguration. 
 * This contains the configuration information from chenile.properties as well as 
 * a reference to all the configured services and events in Chenile
 * @author Raja Shankar Kolluru
 *
 */
public class ChenileConfiguration {

	ApplicationContext applicationContext;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	private String baseUrl;
	private String monolithName;
	private volatile List<Command<ChenileExchange>> preProcessorCommands;
	private volatile List<Command<ChenileExchange>> postProcessorCommands;
	private volatile ChenileExceptionHandler chenileExceptionHandler;
	private String preProcessorNames;
	private String postProcessorNames;
	private String version;
	private Map<String,String> versions = new HashMap<String,String>();
	private Map<String, ChenileServiceDefinition> services = new HashMap<String, ChenileServiceDefinition>();
	private Map<String, ChenileEventDefinition> events = new HashMap<String, ChenileEventDefinition>();
	/**
	 * Leaving room for other extensions here 
	 */
	private Map<String,Map<String,?>> otherExtensions = new HashMap<String,Map<String,?>>();
	private String chenileExceptionHandlerName;
	private String eventLoggerName;
	private final Map<String, TrajectoryDefinition> trajectoryDefinitions = new HashMap<>();
	
	public String getEventLoggerName() {
		return eventLoggerName;
	}

	public void setEventLoggerName(String eventLoggerName) {
		this.eventLoggerName = eventLoggerName;
	}

	public ChenileConfiguration(String monolithName,ApplicationContext applicationContext) {
		this.monolithName = monolithName;
		this.applicationContext = applicationContext;
	}
		
    public Map<String, ChenileServiceDefinition> getServices() {
        return services;
    }

    public void setService(String serviceName, ChenileServiceDefinition service) {
        this.services.put(serviceName,service);
    }

	public String getMonolithName() {
		return monolithName;
	}

	@Deprecated
	public String getModuleName() {
		return monolithName;
	}

	public void addPreProcessors(String preProcessors) {
		this.preProcessorNames = preProcessors;
		this.preProcessorCommands = null;
	}

	public void addPostProcessors(String postProcessors) {
		this.postProcessorNames = postProcessors;
		this.postProcessorCommands = null;
	}
	
	/**
	 * The deployable-wide pre-processors configured in <code>chenile.pre.processors</code>.
	 * Resolved once, all or nothing: if any configured name cannot be resolved, a
	 * {@link ConfigurationException} is thrown and nothing is cached, so a later call
	 * fails the same way instead of silently running a partial list.
	 * Normally resolved at startup by {@link #validateInterceptors()}.
	 * @return the pre-processor commands, or null if none are configured
	 */
	public List<Command<ChenileExchange>> getPreProcessorCommands(){
		List<Command<ChenileExchange>> commands = preProcessorCommands;
		if (commands != null) return commands;
		commands = resolveCommands(preProcessorNames, "chenile.pre.processors");
		if (commands != null) preProcessorCommands = commands;
		return commands;
	}

	/**
	 * The deployable-wide post-processors configured in <code>chenile.post.processors</code>.
	 * Same all-or-nothing semantics as {@link #getPreProcessorCommands()}.
	 * @return the post-processor commands, or null if none are configured
	 */
	public List<Command<ChenileExchange>> getPostProcessorCommands(){
		List<Command<ChenileExchange>> commands = postProcessorCommands;
		if (commands != null) return commands;
		commands = resolveCommands(postProcessorNames, "chenile.post.processors");
		if (commands != null) postProcessorCommands = commands;
		return commands;
	}

	/**
	 * Resolves every deployable-wide interceptor (pre-processors, post-processors and the
	 * exception handler) so that a misspelt or mistyped bean name fails the application at
	 * startup rather than the first request. Called by
	 * {@link org.chenile.core.init.ChenileInterceptorValidator}.
	 * @throws ConfigurationException if any of them cannot be resolved
	 */
	public void validateInterceptors() {
		getPreProcessorCommands();
		getPostProcessorCommands();
		getChenileExceptionHandler();
	}

	/**
	 * Builds the complete command list in a local variable and returns it only if every
	 * name resolved. Blank entries and surrounding spaces ("a, b,") are tolerated.
	 */
	@SuppressWarnings("unchecked")
	private List<Command<ChenileExchange>> resolveCommands(String names, String property) {
		if (names == null || names.isBlank()) return null;
		List<Command<ChenileExchange>> commands = new ArrayList<>();
		for (String raw : names.split(",")) {
			String name = raw.trim();
			if (name.isEmpty()) continue;
			if (!applicationContext.containsBean(name)) {
				throw new ConfigurationException(ErrorCodes.INTERCEPTOR_NOT_FOUND.getSubError(),
						new Object[]{name, property});
			}
			Object bean = applicationContext.getBean(name);
			if (!(bean instanceof Command)) {
				throw new ConfigurationException(ErrorCodes.NOT_INSTANCE_INTERCEPTOR.getSubError(),
						new Object[]{name, property, bean.getClass().getName()});
			}
			commands.add((Command<ChenileExchange>) bean);
		}
		return commands.isEmpty() ? null : Collections.unmodifiableList(commands);
	}

	public Map<String, ChenileEventDefinition> getEvents() {
		return events;
	}

	public void addEvent(ChenileEventDefinition event) {
		this.events.put(event.getId(),event);
	}

	public Map<String,Map<String,?>> getOtherExtensions() {
		return otherExtensions;
	}

	public void setOtherExtensions(Map<String,Map<String,?>> otherExtensions) {
		this.otherExtensions = otherExtensions;
	}

	public void setChenileExceptionHandlerName(String exceptionHandlerName) {
		this.chenileExceptionHandlerName = exceptionHandlerName;
		this.chenileExceptionHandler = null;
	}
	
	
	public ChenileExceptionHandler getChenileExceptionHandler() {
		ChenileExceptionHandler handler = chenileExceptionHandler;
		if (handler != null) return handler;
		String name = chenileExceptionHandlerName;
		if (name == null || !applicationContext.containsBean(name)) {
			throw new ConfigurationException(ErrorCodes.INTERCEPTOR_NOT_FOUND.getSubError(),
					new Object[]{name, "chenile.exception.handler"});
		}
		Object bean = applicationContext.getBean(name);
		if (!(bean instanceof ChenileExceptionHandler h)) {
			throw new ConfigurationException(ErrorCodes.NOT_INSTANCE_INTERCEPTOR.getSubError(),
					new Object[]{name, "chenile.exception.handler", bean.getClass().getName()});
		}
		chenileExceptionHandler = h;
		return h;
	}

	public String getVersion() {
		if (this.version != null) return this.version;
		String moduleVersion = versions.get(monolithName + ".version");
		if (moduleVersion != null) return moduleVersion;
		String defaultVersion = versions.get("version");
		if (defaultVersion != null) return defaultVersion;
		return versions.values().stream().findFirst().orElse(null);
	}

	public String getVersion(String moduleOrServiceName) {
		if (moduleOrServiceName == null || moduleOrServiceName.isEmpty()) return null;
		return versions.get(moduleOrServiceName + ".version");
	}
	
	public void setVersion(String version) {
		this.version = version;
		if (version != null) {
			this.versions.put("version", version);
		}
	}

	public Map<String, String> getVersions() {
		return versions;
	}

	public void setVersions(Map<String, String> versions) {
		this.versions = new HashMap<String, String>(versions);
		if (this.version == null) {
			this.version = getVersion();
		}
	}

	public void addVersion(String key, String value) {
		if (key == null || value == null) return;
		this.versions.put(key, value);
		if ("version".equals(key) || (monolithName + ".version").equals(key)) {
			this.version = value;
		} else if (this.version == null) {
			this.version = value;
		}
	}

	public EventLogger getEventLogger() {
		return (EventLogger) applicationContext.getBean(this.eventLoggerName);
	}
	
	public Map<String,TrajectoryDefinition> getTrajectories(){
		return this.trajectoryDefinitions ;
	}
	
	public void addTrajectory(TrajectoryDefinition trajectoryDefinition){
		String id = trajectoryDefinition.getId();
		if (id == null)
			throw new ConfigurationException(ErrorCodes.MISSING_TRAJECTORY_ID.getSubError(), new Object[] {});
		if(trajectoryDefinitions.containsKey(trajectoryDefinition.getId())){
			TrajectoryDefinition orig = trajectoryDefinitions.get(trajectoryDefinition.getId());
			orig.merge(trajectoryDefinition);
		}else {
			this.trajectoryDefinitions.put(trajectoryDefinition.getId(),trajectoryDefinition) ;
		}
		// update the ChenileServiceDefinition with the trajectory overrides as well
		// this allows us to navigate from service definition to trajectory definition directly
		for (TrajectoryOverride to : trajectoryDefinition.getTrajectoryOverrides().values()) {
			ChenileServiceDefinition csd = getServices().get(to.getServiceId());
			if (csd == null) continue;
			csd.getTrajectoryOverrides().put(to.getTrajectoryId(), to);
		}
	}
}
