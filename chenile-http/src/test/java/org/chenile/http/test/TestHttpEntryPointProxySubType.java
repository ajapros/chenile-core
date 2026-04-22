package org.chenile.http.test;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.EntryPointSubType;
import org.chenile.core.context.HeaderUtils;
import org.chenile.core.entrypoint.ChenileEntryPoint;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.MimeType;
import org.chenile.core.model.OperationDefinition;
import org.chenile.http.Constants;
import org.chenile.http.handler.HttpEntryPoint;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;

public class TestHttpEntryPointProxySubType {

	@Test
	public void handleRequestKeepsProxySubtypeWhileSettingHttpEntryPoint() throws Exception {
		OperationDefinition operationDefinition = new OperationDefinition();
		operationDefinition.setUrl("/increment");
		operationDefinition.setProduces(MimeType.JSON);
		operationDefinition.setSuccessHttpStatus(200);
		ChenileServiceDefinition serviceDefinition = new ChenileServiceDefinition();
		RecordingEntryPoint chenileEntryPoint = new RecordingEntryPoint();
		HttpEntryPoint httpEntryPoint = new HttpEntryPoint(serviceDefinition, operationDefinition, chenileEntryPoint);
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/increment");
		request.addHeader(HeaderUtils.ENTRY_POINT_SUB_TYPE, EntryPointSubType.REMOTE_PROXY.name());
		MockHttpServletResponse response = new MockHttpServletResponse();

		httpEntryPoint.handleRequest(request, response);

		assertEquals(Constants.HTTP_ENTRY_POINT, chenileEntryPoint.exchange.getHeader(HeaderUtils.ENTRY_POINT));
		assertEquals(EntryPointSubType.REMOTE_PROXY, chenileEntryPoint.exchange.getEntryPointSubType());
	}

	private static class RecordingEntryPoint extends ChenileEntryPoint {
		private ChenileExchange exchange;

		@Override
		public void execute(ChenileExchange exchange) {
			this.exchange = exchange;
			exchange.setResponse("ok");
			exchange.setHttpResponseStatusCode(200);
		}
	}
}
