package org.chenile.core.i18n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * This can be used in lieu of the default Message Source. It provides an ability to modularize 
 * messages.properties resource bundle. <br/>
 * By default, the spring message source does not load multiple messages.properties in multiple jars. It picks up 
 * the first file it could find in the class path instead of looking for multiple messages.properties files in the class path
 * (presumably in multiple jar files)
 * But this behaviour is desired to provide modularization. This uses the PathMatchingResourcePatternResolver 
 * to achieve this.
 *
 */

public class MultipleMessageSource extends ReloadableResourceBundleMessageSource {

	private static final String PROPERTIES_SUFFIX = ".properties";
	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	@Override
	protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
		List<String> filenames = new ArrayList<>(super.calculateFilenamesForLocale(basename, locale));
		Set<String> add = new LinkedHashSet<>();
		for (String filename : filenames) {
			try {
				Resource[] resources = resolver.getResources(filename + PROPERTIES_SUFFIX);
				for (Resource resource : resources) {
					String sourcePath = resource.getURL().toExternalForm().replace(PROPERTIES_SUFFIX, "");
					add.add(sourcePath);
				}
			} catch (IOException ignored) {}
		}
		try {
			Resource[] resources = resolver.getResources(basename + PROPERTIES_SUFFIX);
			for (Resource resource : resources) {
				String sourcePath = resource.getURL().toExternalForm().replace(PROPERTIES_SUFFIX, "");
				add.add(sourcePath);
			}
		} catch (IOException ignored) {}
		filenames.addAll(add);
		return filenames;
	}
}
