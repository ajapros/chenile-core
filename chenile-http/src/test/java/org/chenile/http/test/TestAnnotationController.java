package org.chenile.http.test;

import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


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
		org.junit.Assert.assertEquals("custom-capacity", service.getServiceModule());
		org.junit.Assert.assertEquals("testcase-capacity-service", service.getVersion());
		org.junit.Assert.assertEquals("vehicle-capacity", service.getBluePrintName());
		org.junit.Assert.assertEquals("logistics", service.getAdditionalAttributes().get("domain"));
		org.junit.Assert.assertEquals("capacity-planning", service.getAdditionalAttributes().get("mode"));
	}
}
