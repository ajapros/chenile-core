package org.chenile.utils.tenancy;

import org.chenile.base.exception.ServerException;
import org.chenile.core.context.ContextContainer;
import org.chenile.utils.str.StrSubstitutor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * A class that looks for a specific resource (using {@link #tenantSpecificPath}) from the class path.
 * If the tenant specific resource is not found then a generic path ({@link #genericPath} is used.
 * The resource is then opened and the name and URL are returned by calling {@link #obtainFileName(String, String)}
 * and {@link #obtainURL(String, String)}
 * Variable %{name} and %{tenantId} can be used for finding the name of the resource in both the generic and tenant 
 * specific paths.
 * 
 * 
 * 
 * @author Raja Shankar Kolluru
 *
 */
public class TenantSpecificResourceLoader {

	public static URL getResource(String resourceTemplate){
		return getResource(resourceTemplate, ContextContainer.getInstance().getTenant());
	}

	public static InputStream getResourceAsStream(String resourceTemplate){
		return getResourceAsStream(resourceTemplate, ContextContainer.getInstance().getTenant());
	}
	/**
	 * Looks for a tenant specific resource. If tenant specific resource is not present, looks for a generic
	 * resource by skipping the tenant specific part. <br/>
	 * For example, consider a resource template "org/chenile/%{tenantId}/a.json". Let us say we have a tenant "abc".
	 * This returns org/chenile/abc/a.json if it is present. Else it will return org/chenile/a.json if that is
	 * present. Else it will return null.
	 *
	 * @param resourceTemplate - resource name with tenantId in it of the form stated above.
	 * @return the input stream of the discovered resource. null if absent.
	 */
	public static InputStream getResourceAsStream(String resourceTemplate, String tenantId) {
		URL url = getResource(resourceTemplate,tenantId);
		if (url == null) return null;
		try {
			return new FileInputStream(url.getFile());
		}catch(Exception e){ return null;}
	}
	/**
	 * Returns a tenant specific resource. If tenant specific resource is not present, return a generic
	 * resource by skipping the tenant specific part. <br/>
	 * For example, consider a resource template "org/chenile/%{tenantId}/a.json". Let us say we have a tenant "abc".
	 * This returns org/chenile/abc/a.json if it is present. Else it will return org/chanile/a.json if that is
	 * present. Else it will return null.
	 *
	 * @param resourceTemplate - resource name with tenantId in it of the form stated above.
	 * @return the URL of the discovered resource or null if not available.
	 */
	public static URL getResource(String resourceTemplate, String tenantId){
        String genericPath = resourceTemplate.replace("/%{tenantId}/","/");
		return new TenantSpecificResourceLoader(resourceTemplate,genericPath).obtainURL("",tenantId);
	}
	public String delimiter = "%";
	protected String tenantSpecificPath;
	protected String genericPath;
	
	public TenantSpecificResourceLoader(String tenantSpecificPath, String genericPath) {
		this.tenantSpecificPath = tenantSpecificPath;
		this.genericPath = genericPath;
	}

	protected Map<Key,CachedValue> templateStore = new HashMap<Key, CachedValue>();
	protected static final String GENERIC_TENANT_NAME = "__generic__";

	public URL obtainURL(String name, String tenantId) {
		CachedValue value = obtainValue(name,tenantId);
		return value.url;
	}
	
	public String obtainFileName(String name,String tenantId) {
		CachedValue value = obtainValue(name,tenantId);
		return value.fileName;
	}
	
	protected CachedValue obtainValue(String name, String tenantId) {
		Key key = new Key();
		key.name = name;
		key.tenantId = tenantId;
		if (templateStore.containsKey(key)) {
			CachedValue value = templateStore.get(key);
			return value;
		}else {
			try {
				CachedValue value = lookup(key);
				templateStore.put(key,value);
				return value;
			}catch(Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}			
		}
	}

	protected CachedValue lookup(Key key) throws Exception {
		Map<String,String> valueMap = new HashMap<String, String>();
		valueMap.put("tenantId", key.tenantId);
		valueMap.put("name", key.name);
		// first see if we can find a specific file for the tenant
		String filename = StrSubstitutor.replaceNamedKeysInTemplate(tenantSpecificPath, valueMap,delimiter);
		URL res = this.getClass().getClassLoader().getResource(filename);
		if (res == null) {
			// try with the generic key to see if the template exists.
			Key genericKey = new Key();
			genericKey.name = key.name;
			genericKey.tenantId = GENERIC_TENANT_NAME;
			
			CachedValue genericValue = templateStore.get(genericKey);
			if (genericValue != null) return genericValue;
			
			filename = StrSubstitutor.replaceNamedKeysInTemplate(genericPath, valueMap,delimiter);
			res = this.getClass().getClassLoader().getResource(filename);
			if (res == null)
				throw new ServerException(601, 
						"Class " + getClass().getName() + ": Unable to find a default template for " + key.name);
			genericValue = populateValue(filename,res);
			templateStore.put(genericKey, genericValue);
			templateStore.put(key,genericValue);
			return genericValue;
		} 
		CachedValue value = populateValue(filename,res);
		templateStore.put(key, value);
		return value;
	}
	
	/**
	 * Override this to store cached values that have information specific to the particular sub class 
	 * By default the Cached Value stores URL and filename. But the specific sub class might choose to
	 * cache additional resources. 
	 * @param filename the filename to look for
	 * @param url - URL
	 * @return the cached value
	 * @throws Exception if there is a problem
	 */
	protected CachedValue populateValue(String filename, URL url) throws Exception{
		return new CachedValue(url,filename);
	}

}