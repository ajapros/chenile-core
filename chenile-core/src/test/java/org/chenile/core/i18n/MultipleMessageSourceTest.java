package org.chenile.core.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MultipleMessageSourceTest {

	@TempDir
	Path tempDir;

	@Test
	void resolvesMessagesFromDependencyJarBaseBundle() throws IOException {
		Path jarFile = tempDir.resolve("dependency-messages.jar");
		try (OutputStream outputStream = Files.newOutputStream(jarFile);
				JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
			jarOutputStream.putNextEntry(new JarEntry("messages.properties"));
			jarOutputStream.write("E700=Loaded from dependency jar for {0}\n".getBytes(StandardCharsets.UTF_8));
			jarOutputStream.closeEntry();
		}

		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try (URLClassLoader classLoader =
				new URLClassLoader(new URL[] { jarFile.toUri().toURL() }, originalClassLoader)) {
			Thread.currentThread().setContextClassLoader(classLoader);

			MultipleMessageSource messageSource = new MultipleMessageSource();
			messageSource.setBasename("classpath*:messages");

			String message = messageSource.getMessage("E700", new Object[] { "unknown-query" }, Locale.ENGLISH);
			assertEquals("Loaded from dependency jar for unknown-query", message);
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}
}
