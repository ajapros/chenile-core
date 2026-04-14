package org.chenile.core.test;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.base.exception.ServerException;
import org.chenile.base.response.GenericResponse;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.ChenileExchangeBuilder;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderUtils;
import org.chenile.core.entrypoint.ChenileEntryPoint;
import org.chenile.core.event.EventProcessor;
import org.chenile.core.service.HealthCheckInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestConfig.class)
@ActiveProfiles("unittest")
public class TestChenileCore {

	@Autowired private EventProcessor eventProcessor;
	@Autowired private ChenileEntryPoint chenileEntryPoint;
	@Autowired private ChenileExchangeBuilder chenileExchangeBuilder;
	@Autowired ContextContainer contextContainer;
	
	private ChenileExchange makeExchange(String serviceName,String operationName) {
		return chenileExchangeBuilder.makeExchange(serviceName, operationName,null);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testInterception() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","mockMethod");
		exchange.setBody(new ArrayList<String>());
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		List<String> expected = new ArrayList<String>();
		expected.add("first");expected.add("second");expected.add("third");expected.add("fourth"); expected.add("fifth");
		expected.add("actual");
		expected.add("sixth");expected.add("seventh");expected.add("eighth");expected.add("ninth"); expected.add("tenth");
		assertEquals("interception order calling order does not match expected",expected, data);
	}
	
	
	@SuppressWarnings("unchecked")
	@Test public void testServiceInvokerWithHeaderStringArgument() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s1");
		exchange.setHeader("id","myid");
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value","mockmyid",data);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testServiceInvokerWithHeaderIntArgument() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s2");
		exchange.setHeader("id",123);
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value","mockint123",data);
	}

	@Test public void testValidateCopyHeadersforXP() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s2");
		exchange.setHeader("x-p-id","123");
		chenileEntryPoint.execute(exchange);
		ErrorNumException ene = exchange.getException();
		assertEquals(ene.getErrorNum(), HttpStatus.FORBIDDEN.value());
	}

	@Test public void testValidateCopyHeadersforXClears() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s2");
		exchange.setHeader("x-id","123");

		chenileEntryPoint.execute(exchange);
		assertNull("context container must not contain x-id on return", contextContainer.get("x-id"));
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testServiceInvokerWithHeaderIntArgumentPassedAsString() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s2");
		exchange.setHeader("id","123");
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value","mockint123",data);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testServiceInvokerWithHeaderIntArgumentPassedAndIntReturned() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s3");
		exchange.setHeader("id",7);
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value",50,data);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testServiceInvokerWithHeaderStringArgumentPassedForPrimitiveIntAndIntReturned() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s3");
		exchange.setHeader("id","7");
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value",50,data);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testServiceInvokerWithHeaderBooleanArgumentPassedAndBooleanReturned() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s4");
		exchange.setHeader("flag",true);
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value",false,data);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testServiceInvokerWithHeaderStringArgumentPassedForPrimitiveBooleanAndBooleanReturned() throws Exception{
		ChenileExchange exchange = makeExchange("mockService","s4");
		exchange.setHeader("flag","true");
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value",false,data);
	}
	
	@Test public void testServiceInvokerWithExceptionHandled() {
		int exceptionNum = 1089;
		ChenileExchange exchange = makeExchange("mockService","s5");
		exchange.setHeader("exceptionNum",exceptionNum);
		
		chenileEntryPoint.execute(exchange);
		if (exchange.getException() == null) {
			fail("Exception hidden by Chenile Entry Point");
		}
		RuntimeException e = exchange.getException();
		assertEquals("Exception returned must be of type ServerException", 
				ServerException.class,e.getClass());
		ServerException e1 = (ServerException)e;
		assertEquals("Sub error code of exception does not match",exceptionNum,e1.getSubErrorNum());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testBodyTypeSelector() {
		ChenileExchange exchange = makeExchange("mockService","s6");
		
		exchange.setHeader("eventId","e1");
		exchange.setBody("{\"s1\": \"xyz\"}");
		
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertTrue("Response from mockService.s6(e1) must be castable to E1",data instanceof E1);
		E1 e1 = (E1)data;
		assertEquals("xyz",e1.getS1());
		
		exchange = makeExchange("mockService","s6");
		exchange.setHeader("eventId","e2");
		exchange.setBody("{\"s2\": \"abc\"}");
		
		chenileEntryPoint.execute(exchange);
		data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertTrue("Response from mockService.s6(e2) must be castable to E2",data instanceof E2);
		E2 e2 = (E2)data;
		assertEquals("abc",e2.getS2());
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testHeaders() {
		ChenileExchange exchange = makeExchange("mockService","s7");
		exchange.setHeader("header1","some_stuff");
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value","some_stuff",data);
	}

	@SuppressWarnings("unchecked")
	@Test public void testContextContainerIsolationAcrossThreads() throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		try {
			Callable<String> request1 = () -> executeContextAwareRequest("tenant-a", ready, start);
			Callable<String> request2 = () -> executeContextAwareRequest("tenant-b", ready, start);

			Future<String> future1 = executorService.submit(request1);
			Future<String> future2 = executorService.submit(request2);

			assertTrue("Both tasks must be ready before starting", ready.await(5, TimeUnit.SECONDS));
			start.countDown();

			assertEquals("tenant-a", future1.get(5, TimeUnit.SECONDS));
			assertEquals("tenant-b", future2.get(5, TimeUnit.SECONDS));
		} finally {
			executorService.shutdownNow();
		}
	}

	private String executeContextAwareRequest(String headerValue, CountDownLatch ready, CountDownLatch start) throws Exception {
		ChenileExchange exchange = makeExchange("mockService","s9");
		exchange.setHeader("x-id", headerValue);
		ready.countDown();
		assertTrue("Timed out waiting to start concurrent request", start.await(5, TimeUnit.SECONDS));
		chenileEntryPoint.execute(exchange);
		assertNull("Context container must be cleared after request completion", contextContainer.get("x-id"));
		return ((GenericResponse<String>) exchange.getResponse()).getData();
	}

	@Test public void testContextContainerSnapshotRestore() {
		contextContainer.clear();
		contextContainer.setTenant("tenant-a");
		contextContainer.setRequestId("req-a");
		ContextContainer.putExtension("trace", "original");
		ContextContainer.ContextSnapshot snapshot = contextContainer.snapshot();

		contextContainer.setTenant("tenant-b");
		contextContainer.setRequestId("req-b");
		ContextContainer.putExtension("trace", "updated");

		contextContainer.restore(snapshot);

		assertEquals("tenant-a", contextContainer.getTenant());
		assertEquals("req-a", contextContainer.getRequestId());
		assertEquals("original", ContextContainer.getExtension("trace"));
		contextContainer.clear();
	}

	@Test public void testContextContainerSnapshotRestoreNoContext() {
		contextContainer.clear();
		ContextContainer.ContextSnapshot snapshot = contextContainer.snapshot();

		contextContainer.setTenant("tenant-a");
		contextContainer.setRequestId("req-a");

		contextContainer.restore(snapshot);

		assertNull("Restoring a missing context must clear thread local state", contextContainer.get(HeaderUtils.TENANT_ID_KEY));
		assertNull("Restoring a missing context must clear request id", contextContainer.getRequestId());
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testMockingAbility() {
		String testId = "testID";
		ChenileExchange exchange = makeExchange("mockService","s1");
		exchange.setInvokeMock(true);
		exchange.setHeader("id",testId);
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value","mockmock" + testId,data);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testTrajectoryRouting() {
		String testId = "testID";
		ChenileExchange exchange = makeExchange("mockService","s1");
		exchange.setHeader("x-chenile-trajectory-id","t1");
		exchange.setHeader("id",testId);
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value","t1mock" + testId,data);
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testHealthChecker() {
		ChenileExchange exchange = makeExchange("infoService","healthCheck");
		exchange.setHeader("service", "mockService");
		chenileEntryPoint.execute(exchange);
		Object data = ((GenericResponse<Object>)exchange.getResponse()).getData();
		HealthCheckInfo hci = (HealthCheckInfo)data;
		assertEquals("Response message for health check did not match", MockHealthChecker.HEALTH_CHECK_MESSAGE,hci.message);
		assertTrue("Response message for health check is not returning healthy",hci.healthy);
	}

	@SuppressWarnings("unchecked")
	@Test public void testSimpleBody(){
		ChenileExchange exchange = makeExchange("mockService","s8");
		exchange.setBody("{\"bar\": \"value\"}");
		chenileEntryPoint.execute(exchange);
		Foo data = (Foo)((GenericResponse<Object>)exchange.getResponse()).getData();
		assertEquals("Expected does not match the actual return value","valuemock",data.bar);
		assertEquals("Expected does not match the actual return value","value",MockService.bar);
	}

	@Test public void testEvent(){
		Foo foo = new Foo();
		foo.bar = "baz";
		eventProcessor.handleEvent("foo",foo);
		assertEquals("Expected does not match the actual return value","baz",MockService.bar);
	}

	/**
	 * Test an event that does not have an explicit event definition.
	 */
	@Test public void testUndefinedEvent(){
		Foo foo = new Foo();
		foo.bar = "event1-test";
		eventProcessor.handleEvent("event1",foo);
		assertEquals("Expected does not match the actual return value","event1-test",MockService.bar);
	}
}
