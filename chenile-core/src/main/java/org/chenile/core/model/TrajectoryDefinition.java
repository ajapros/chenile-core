package org.chenile.core.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.chenile.core.service.HealthChecker;

/**
 * The trajectory definition details. Trajectories allow experimentation. They may also serve
 * other purposes like overriding services for certain geographies, tenants etc. These overrides
 * customize the services for a specific set of users. Trajectories are highly modular. A new
 * trajectory can be defined/used in a new module. Once the module is removed, the trajectory
 * vanishes from Chenile!
 * <p>A trajectory can also be an experimental (A-B test) path that provides
 * selected users with alternate services. These paths may invoke experimental services. Loggers will
 * log the trajectories. This will allow us to determine if the experimental services are
 * more effective than the actual services. </p>
 * <p> Even health checks can be customized for a particular trajectory </p>
 */
public class TrajectoryDefinition {
	/**
	 * This is a special ID used for the MAINSTREAM requests. All requests, by default, belong to this
	 * unless specified otherwise.
	 */
	public static final String MAINSTREAM = "_MAINSTREAM_";
	private String id;
	/** 
	 * a map between the service name and the service reference to use for this trajectory.
	 * This effects only those services that are configured. Other services will stay unaffected.
	 */
	//private Map<String,String> serviceToServiceReferenceId = new HashMap<>();
	//private Map<String,Object> serviceToServiceReference = new HashMap<>();
	private Map<String,TrajectoryOverride> trajectoryOverrides = new HashMap<>();

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Trajectories can contain additional attributes that can be used by the Trajectory consumers in
	 * their own way. Chenile is opaque to how this is used but provides a way to store them here.
	 */
	private Map<String,Object> attributes = new LinkedHashMap<>();

	public void setTrajectoryOverrides(Map<String, TrajectoryOverride> trajectoryOverrides) {
		this.trajectoryOverrides = trajectoryOverrides;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
	
	public void merge(TrajectoryDefinition trajectoryDefinition) {
		trajectoryOverrides.putAll(trajectoryDefinition.getTrajectoryOverrides());	
	}
	
	public Object getServiceReference(String serviceId) {
		TrajectoryOverride to = trajectoryOverrides.get(serviceId);
		if (to == null) return null;
		return to.getNewServiceReference();
	}
	
	public HealthChecker getHealthCheckerReference(String serviceId) {
		TrajectoryOverride to = trajectoryOverrides.get(serviceId);
		if (to == null) return null;
		return to.getNewHealthCheckerReference();
	}
	
	public String getServiceReferenceId(String serviceId) {
		TrajectoryOverride to = trajectoryOverrides.get(serviceId);
		if (to == null) return null;
		return to.getNewServiceReferenceId();
	}
	
	public String getHealthCheckerReferenceId(String serviceId) {
		TrajectoryOverride to = trajectoryOverrides.get(serviceId);
		if (to == null) return null;
		return to.getNewHealthCheckerReferenceId();
	}

	public Map<String,TrajectoryOverride> getTrajectoryOverrides() {
		return trajectoryOverrides;
	}

}
