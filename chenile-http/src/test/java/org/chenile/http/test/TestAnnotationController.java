package org.chenile.http.test;

import org.chenile.core.annotation.ExternalApi;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestChenileHttp.class)
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
public class TestAnnotationController {

   @Autowired TestUtil testUtil;
   @Autowired ChenileConfiguration chenileConfiguration;
    
   @Test @Order(1)
    public void testService() throws Exception {
	  testUtil.testService("/c/getOne/");
    }

   @Test @Order(2)
    public void testPostService() throws Exception {
	   	testUtil.testPostService("/c/save");
    }

   @Test @Order(3)
   public void testException() throws Exception {
	   	testUtil.testException("/c/throw-exception");
   }

    @Test @Order(4)
    public void testPostProcessInterceptionException() throws Exception {
        testUtil.testPostProcessInterceptionException("/c/save");
    }

    @Test @Order(5)
    public void testPreProcessInterceptionException() throws Exception {
        testUtil.testPreProcessInterceptionException("/c/save");
    }
     
   @Test @Order(6)
   public void testWarning() throws Exception {
	   	testUtil.testWarning("/c/throw-warning");
   }

    @Test @Order(7)
    public void testMultipleExceptions() throws Exception {
        testUtil.testMultipleExceptions("/c/throw-multiple-exceptions");
    }

    @Test @Order(8)
    public void testDoubleInterceptorException() throws Exception {
        testUtil.testDoubleInterceptorException("/c/ping");
    }

	@Test @Order(9)
	public void testAnnotationDefaultVersionProperty() {
		ChenileServiceDefinition service = chenileConfiguration.getServices().get("jsonController");
		org.junit.Assert.assertEquals("jsonController", service.getServiceModule());
		org.junit.Assert.assertEquals("testcase-json-controller", service.getVersion());
	}

	@Test @Order(10)
	public void testAnnotationExplicitVersionProperty() {
		ChenileServiceDefinition service = chenileConfiguration.getServices().get("capacityService");
		assertEquals("custom-capacity", service.getServiceModule());
		assertEquals("testcase-capacity-service", service.getVersion());
	}

	@Test @Order(11)
	public void testExternalApiAnnotationIsRegisteredOnServiceAndOperation() {
		ChenileServiceDefinition service = chenileConfiguration.getServices().get("jsonController");
		ExternalApi serviceExternalApi = service.getExtensionAsAnnotation(ExternalApi.class);
		assertNotNull(serviceExternalApi);
		assertEquals("json-client", serviceExternalApi.system());

		OperationDefinition saveOperation = service.getOperations().stream()
				.filter(operation -> "save".equals(operation.getName()))
				.findFirst()
				.orElseThrow();
		ExternalApi operationExternalApi = saveOperation.getExtensionAsAnnotation(ExternalApi.class);
		assertNotNull(operationExternalApi);
		assertEquals("json-client", operationExternalApi.system());
		assertEquals("save-json", operationExternalApi.operation());
	}
}
