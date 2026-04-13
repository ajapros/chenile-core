package org.chenile.core.test;

import org.chenile.base.exception.ServerException;
import org.chenile.core.context.ContextContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockService {
	private final Logger logger = LoggerFactory.getLogger(MockService.class);
	public static String bar;
	public List<String> mockMethod(List<String> list) {
		list.add("actual");
		return list;
	}
	
	public String s1(String id) {
		return "mock" + id;
	}
	
	public String s2(Integer id) {
		return "mockint" + id;
	}
	
	public int s3(int id) {
		return 43 + id;
	}
	
	public boolean s4(boolean flag) {
		return !flag;
	}
	
	public String s5(int exceptionNum) {
		throw new ServerException(exceptionNum,"Error");
	}
	
	public Object s6(String eventId, Object param) {
		// ensure that for eventId = "e1" the param is of type E1
		// and for event Id  = "e2" the param is of type E2.
		// the casts below wont work if that is not the case
		if (eventId.equals("e1")) {
			E1 e1 = (E1) param;
			return e1;
		}else if (eventId.equals("e2")) {
			E2 e2 = (E2) param;
			return e2;
		}
		return null;
	}
	
	public String s7(Map<String,Object> headers) {
		return headers.get("header1").toString();
	}

	public Foo s8(Foo foo){
		MockService.bar = foo.bar;
		foo.bar = foo.bar + "mock";
		return foo;
	}

	private static boolean first = true;
	private CountDownLatch latch = new CountDownLatch(1);
	public String s9() {
		String t = ContextContainer.CONTEXT_CONTAINER.getHeader("id");
		boolean firstRequest = first;
		if(first){
			first = false;
            try {
                latch.await(2000, TimeUnit.MILLISECONDS);
				String t1 = ContextContainer.CONTEXT_CONTAINER.getHeader("id");
				if (!t.equals(t1)){
					throw new RuntimeException("Context got mutated by a second request");
				}
            } catch (InterruptedException e) {
                throw new RuntimeException("Error waiting for count down latch.");
            }
        }else{
			latch.countDown();
		}
		logger.info("At s9: first = " + firstRequest + " returning " + t);
		return t;
	}

}
