package org.chenile.core.external;

import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderUtils;
import org.chenile.core.context.LogRecord;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class ChenileExternalClientTest {
    private final ContextContainer contextContainer = ContextContainer.getInstance();

    @After
    public void clearContext() {
        contextContainer.clear();
    }

    @Test
    public void publishesOutboundSuccessRecordForThirdPartyCall() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        CapturingPublisher publisher = new CapturingPublisher();
        ChenileExternalClient client = client(restTemplate, publisher);
        contextContainer.setRequestId("req-1");
        contextContainer.put(HeaderUtils.CORRELATION_ID, "corr-1");

        server.expect(requestTo("https://partner.example/orders/42"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HeaderUtils.REQUEST_ID, "req-1"))
                .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));

        String response = client.exchange(ExternalApiRequest.of("partner", "getOrder",
                "https://partner.example/orders/42", HttpMethod.GET, null, String.class));

        server.verify();
        assertEquals("{\"status\":\"ok\"}", response);
        LogRecord record = publisher.records.getFirst();
        assertEquals(LogRecord.Direction.OUTBOUND, record.direction);
        assertEquals("partner", record.externalSystem);
        assertEquals("getOrder", record.externalOperation);
        assertEquals("https://partner.example/orders/42", record.target);
        assertEquals("GET", record.httpMethod);
        assertEquals(Integer.valueOf(200), record.httpStatusCode);
        assertEquals("req-1", record.requestId);
        assertEquals("corr-1", record.correlationId);
    }

    @Test
    public void publishesOutboundErrorRecordForThirdPartyCall() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        CapturingPublisher publisher = new CapturingPublisher();
        ChenileExternalClient client = client(restTemplate, publisher);

        server.expect(requestTo("https://partner.example/orders"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest().body("{\"error\":\"bad\"}").contentType(MediaType.APPLICATION_JSON));

        assertThrows(RestClientResponseException.class, () -> client.exchange(ExternalApiRequest.of(
                "partner", "createOrder", "https://partner.example/orders", HttpMethod.POST,
                "{\"id\":\"42\"}", String.class)));

        server.verify();
        LogRecord record = publisher.records.getFirst();
        assertEquals(LogRecord.Direction.OUTBOUND, record.direction);
        assertEquals(Boolean.FALSE, record.success);
        assertEquals(Integer.valueOf(400), record.httpStatusCode);
        assertEquals("{\"error\":\"bad\"}", record.responsePayload);
    }

    private ChenileExternalClient client(RestTemplate restTemplate, ExternalApiPublisher publisher) {
        ExternalApiProperties properties = new ExternalApiProperties(true, "in", "out", 65536, "Authorization");
        return new ChenileExternalClient(restTemplate, publisher, properties, new ExternalApiLogSupport(properties));
    }

    private static class CapturingPublisher implements ExternalApiPublisher {
        final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }
    }
}
