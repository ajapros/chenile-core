package org.chenile.utils.test.tenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.utils.tenancy.TenantSpecificResourceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;



public class TestTenancy {
    @Test
    public void testIfURLCorrectForAbc(){
        String tenantSpecificPath = "org/chenile/test/%{tenantId}/test-resource.json";
        URL url = TenantSpecificResourceLoader.getResource(tenantSpecificPath, "abc");
        assertNotNull(url);
        assertTrue(url.getFile().contains("org/chenile/test/abc/test-resource.json"));
    }

    @Test
    public void testIfURLCorrectForOthers(){
        String tenantSpecificPath = "org/chenile/test/%{tenantId}/test-resource.json";
        URL url = TenantSpecificResourceLoader.getResource(tenantSpecificPath, "def");
        assertNotNull(url);
        assertTrue(url.getFile().contains("org/chenile/test/test-resource.json"));
    }

    ObjectMapper objectMapper = new ObjectMapper();
    private static class JsonValue {
        public String forTenant;
    }

    @Test
    public void testIfResourceFetchedForAbc() throws Exception{
        String tenantSpecificPath = "org/chenile/test/%{tenantId}/test-resource.json";
        JsonValue val = objectMapper.readValue(
                TenantSpecificResourceLoader.getResourceAsStream(tenantSpecificPath, "abc"),
                JsonValue.class
                );
        assertNotNull(val);
        assertEquals("abc",val.forTenant);
    }

    @Test
    public void testIfResourceFetchedForNonAbc() throws Exception{
        String tenantSpecificPath = "org/chenile/test/%{tenantId}/test-resource.json";
        JsonValue val = objectMapper.readValue(
                TenantSpecificResourceLoader.getResourceAsStream(tenantSpecificPath, "def"),
                JsonValue.class
        );
        assertNotNull(val);
        assertEquals("all",val.forTenant);
    }

    @Test
    public void testIfResourceStreamWorksFromJar(@TempDir Path tempDir) throws Exception {
        Path jarPath = tempDir.resolve("resources.jar");
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarPath))) {
            addJarEntry(jarOutputStream, "org/chenile/test/abc/test-resource.json",
                    "{\"forTenant\":\"abc-jar\"}");
            addJarEntry(jarOutputStream, "org/chenile/test/test-resource.json",
                    "{\"forTenant\":\"all-jar\"}");
        }

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader jarClassLoader = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, null)) {
            Thread.currentThread().setContextClassLoader(jarClassLoader);
            String tenantSpecificPath = "org/chenile/test/%{tenantId}/test-resource.json";

            try (InputStream inputStream = TenantSpecificResourceLoader.getResourceAsStream(tenantSpecificPath, "abc")) {
                JsonValue val = objectMapper.readValue(inputStream, JsonValue.class);
                assertEquals("abc-jar", val.forTenant);
            }

            try (InputStream inputStream = TenantSpecificResourceLoader.getResourceAsStream(tenantSpecificPath, "def")) {
                JsonValue val = objectMapper.readValue(inputStream, JsonValue.class);
                assertEquals("all-jar", val.forTenant);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private void addJarEntry(JarOutputStream jarOutputStream, String name, String value) throws Exception {
        jarOutputStream.putNextEntry(new JarEntry(name));
        jarOutputStream.write(value.getBytes());
        jarOutputStream.closeEntry();
    }
}
