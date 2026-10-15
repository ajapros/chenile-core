package org.chenile.http.test;

import java.util.Map;
import java.util.List;
import org.chenile.core.model.OperationDefinition;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import org.chenile.http.handler.HttpEntryPoint;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("unittest")
public class TestExtractPathVarsInHttpEntryPoint {
	
	@Test public void testPathVars1() {
		Map<String, Object> m = HttpEntryPoint.extractPathVariables("/xxx/{id}", "/xxx/abc");
		assertMapContains(m,"id", "abc");
	}
	
	@Test public void testPathVars2() {
		Map<String, Object> m = HttpEntryPoint.extractPathVariables("/xxx/{id}/{name}", "/xxx/abc/def");
		assertMapContains(m,"id", "abc");
		assertMapContains(m,"name", "def");
	}
	
	@Test public void testPathVars3() {
		Map<String, Object> m = HttpEntryPoint.extractPathVariables("/xxx/{id}/{name}/", "/xxx/abc/def/");
		assertMapContains(m,"id", "abc");
		assertMapContains(m,"name", "def");
	}
	
	@Test public void testPathVars4() {
		Map<String, Object> m = HttpEntryPoint.extractPathVariables("/xxx/{id}/{name}/", "/xxx/abc/def");
		assertMapContains(m,"id", "abc");
		assertMapContains(m,"name", "def");
	}

	@Test public void test_if_prefix_added_to_pathInfo(){
		String url = "/order/{id}/{eventId}";
		String pathInfo = "/api/foo/bar/order/123/add-to-cart";
		Map<String,Object> m = HttpEntryPoint.extractPathVariables(url,pathInfo);
		assertMapContains(m,"id", "123");
		assertMapContains(m,"eventId", "add-to-cart");
	}

    @Test public void aliasesUseTheMatchedPatternAndSpringExtractedVariables() {
        OperationDefinition operation = new OperationDefinition();
        operation.setUrls(List.of("/customers/{id}", "/customers/{id}/details"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers/123/details");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/customers/{id}/details");
        assertMapContains(HttpEntryPoint.getHeaders(operation, request), "id", "123");

        // Spring MVC variables take precedence, including decoded values and class-level prefixes.
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "spring-value"));
        assertMapContains(HttpEntryPoint.getHeaders(operation, request), "id", "spring-value");
        request.removeAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        request.removeAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        request.setContextPath("/app");
        request.setRequestURI("/app/customers/123/details");
        assertMapContains(HttpEntryPoint.getHeaders(operation, request), "id", "123");

        // Transport-neutral operations have no HTTP pattern to extract.
        Assert.assertNotNull(HttpEntryPoint.getHeaders(new OperationDefinition(), new MockHttpServletRequest()));
    }

	private void assertMapContains(Map<String, Object> m, String key, String value) {
		Assert.assertNotNull(m);
		Assert.assertTrue(m.containsKey(key));
		Assert.assertEquals(value, m.get(key));
	}
}
