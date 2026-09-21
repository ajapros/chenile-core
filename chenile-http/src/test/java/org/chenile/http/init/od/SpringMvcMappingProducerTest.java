package org.chenile.http.init.od;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.MimeType;
import org.chenile.core.model.OperationDefinition;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SpringMvcMappingProducerTest {
	private final SpringMvcMappingProducer producer = new SpringMvcMappingProducer(new StaticApplicationContext());

	@Test
	public void resolvesComposedGetMapping() throws Exception {
		OperationDefinition operation = produce("get");

		assertEquals("/get", operation.getUrl());
		assertEquals(HTTPMethod.GET, operation.getHttpMethod());
	}

	@Test
	public void resolvesEachComposedMappingAnnotation() throws Exception {
		assertMapping("get", "/get", HTTPMethod.GET);
		assertMapping("postMapping", "/post-mapping", HTTPMethod.POST);
		assertMapping("put", "/put", HTTPMethod.PUT);
		assertMapping("patch", "/patch", HTTPMethod.PATCH);
		assertMapping("delete", "/delete", HTTPMethod.DELETE);
	}

	@Test
	public void resolvesDirectRequestMapping() throws Exception {
		OperationDefinition operation = produce("post");

		assertEquals("/post", operation.getUrl());
		assertEquals(HTTPMethod.POST, operation.getHttpMethod());
		assertEquals(MimeType.JSON, operation.getConsumes());
		assertEquals(MimeType.JSON, operation.getProduces());
	}

	@Test
	public void rejectsMultipleHttpMethods() throws Exception {
		assertRejected("multipleMethods", "exactly one HTTP method");
	}

	@Test
	public void rejectsMultipleUrls() throws Exception {
		assertRejected("multipleUrls", "one URL");
	}

	private OperationDefinition produce(String methodName) throws Exception {
		Method method = TestMappings.class.getDeclaredMethod(methodName, HttpServletRequest.class);
		assertTrue(producer.supports(method));
		ChenileServiceDefinition service = new ChenileServiceDefinition();
		service.setOperations(new ArrayList<>());
		producer.produceOperationDefinition(service, method);
		return service.getOperations().getFirst();
	}

	private void assertRejected(String methodName, String expectedMessage) throws Exception {
		try {
			produce(methodName);
			fail("Expected mapping to be rejected");
		} catch (IllegalArgumentException exception) {
			assertTrue(exception.getMessage().contains(expectedMessage));
		}
	}

	private void assertMapping(String methodName, String url, HTTPMethod httpMethod) throws Exception {
		OperationDefinition operation = produce(methodName);
		assertEquals(url, operation.getUrl());
		assertEquals(httpMethod, operation.getHttpMethod());
	}

	private static class TestMappings {
		@GetMapping("/get")
		String get(HttpServletRequest request) {
			return "get";
		}

		@RequestMapping(path = "/post", method = RequestMethod.POST,
				consumes = "application/json", produces = "application/json")
		String post(HttpServletRequest request) {
			return "post";
		}

		@PostMapping("/post-mapping")
		String postMapping(HttpServletRequest request) {
			return "postMapping";
		}

		@PutMapping("/put")
		String put(HttpServletRequest request) {
			return "put";
		}

		@PatchMapping("/patch")
		String patch(HttpServletRequest request) {
			return "patch";
		}

		@DeleteMapping("/delete")
		String delete(HttpServletRequest request) {
			return "delete";
		}

		@RequestMapping(path = "/multiple-methods", method = {RequestMethod.GET, RequestMethod.POST})
		String multipleMethods(HttpServletRequest request) {
			return "multipleMethods";
		}

		@RequestMapping(path = {"/first", "/second"}, method = RequestMethod.GET)
		String multipleUrls(HttpServletRequest request) {
			return "multipleUrls";
		}
	}
}
