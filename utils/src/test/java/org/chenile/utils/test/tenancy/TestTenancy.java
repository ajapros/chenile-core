package org.chenile.utils.test.tenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.utils.tenancy.TenantSpecificResourceLoader;
import org.junit.jupiter.api.Test;

import java.net.URL;

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
}