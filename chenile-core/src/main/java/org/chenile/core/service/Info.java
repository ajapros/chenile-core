package org.chenile.core.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A DTO that provides a condensed version of services along with the operations that they support
 */
public class Info {
	public String version;
	public Map<String,String> versions = new LinkedHashMap<>();
	public String moduleName;
	public static class ServiceInfo{
		public String name;
		public ServiceInfo() {}
		public ServiceInfo(String name) { this.name = name;}
		public List<OperationInfo> operations = new ArrayList<>();
	}
	public static class OperationInfo {
		public String name;
		public String url;
		public String method;
	}
	public List<ServiceInfo> services = new ArrayList<>();
}
