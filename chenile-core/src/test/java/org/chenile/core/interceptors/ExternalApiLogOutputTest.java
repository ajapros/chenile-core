package org.chenile.core.interceptors;

import org.chenile.base.response.GenericResponse;
import org.chenile.core.annotation.ExternalApi;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.HeaderUtils;
import org.chenile.core.context.LogRecord;
import org.chenile.core.external.ExternalApiPublisher;
import org.chenile.core.external.ExternalApiLogSupport;
import org.chenile.core.external.ExternalApiProperties;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.owiz.Command;
import org.chenile.owiz.impl.ChainContext;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExternalApiLogOutputTest {

    @Test
    public void publishesInboundRecordOnlyWhenOperationIsMarkedExternal() throws Exception {
        CapturingPublisher publisher = new CapturingPublisher();
        LogOutput logOutput = logOutput(publisher);
        ChenileExchange exchange = exchange(true);
        exchange.setBody("{\"id\":\"42\"}");
        exchange.setHeader(HeaderUtils.REQUEST_ID, "req-1");
        exchange.setHeader(HeaderUtils.ENTRY_POINT, "HTTP");

        logOutput.execute(exchange);

        assertEquals(1, publisher.records.size());
        LogRecord record = publisher.records.getFirst();
        assertEquals(LogRecord.Direction.INBOUND, record.direction);
        assertEquals("client-app", record.externalSystem);
        assertEquals("createOrder", record.externalOperation);
        assertEquals("HTTP", record.protocol);
        assertEquals("req-1", record.requestId);
        assertEquals("{\"id\":\"42\"}", record.requestPayload);
    }

    @Test
    public void skipsPubSubWhenOperationIsNotMarkedExternal() throws Exception {
        CapturingPublisher publisher = new CapturingPublisher();
        LogOutput logOutput = logOutput(publisher);

        logOutput.execute(exchange(false));

        assertEquals(0, publisher.records.size());
    }

    private LogOutput logOutput(CapturingPublisher publisher) {
        ExternalApiProperties properties = new ExternalApiProperties(true, "in", "out", 65536, "");
        LogOutput logOutput = new LogOutput();
        ReflectionTestUtils.setField(logOutput, "externalApiPublisher", publisher);
        ReflectionTestUtils.setField(logOutput, "externalApiProperties", properties);
        ReflectionTestUtils.setField(logOutput, "externalApiLogSupport", new ExternalApiLogSupport(properties));
        return logOutput;
    }

    private ChenileExchange exchange(boolean external) throws Exception {
        ChenileServiceDefinition serviceDefinition = new ChenileServiceDefinition();
        serviceDefinition.setName("orderService");
        serviceDefinition.setModuleName("orders");
        OperationDefinition operationDefinition = new OperationDefinition();
        operationDefinition.setName("createOrder");
        if (external) {
            Method method = MarkedController.class.getMethod("createOrder");
            operationDefinition.putExtensionAsAnnotation(ExternalApi.class, method.getAnnotation(ExternalApi.class));
        }
        ChenileExchange exchange = new ChenileExchange();
        exchange.setServiceDefinition(serviceDefinition);
        exchange.setOperationDefinition(operationDefinition);
        List<Command<ChenileExchange>> commands = List.of(context -> context.setResponse(new GenericResponse<>("ok")));
        exchange.setChainContext(new ChainContext<>(commands, exchange));
        return exchange;
    }

    private static class CapturingPublisher implements ExternalApiPublisher {
        final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }
    }

    private static class MarkedController {
        @ExternalApi(system = "client-app", operation = "createOrder")
        public void createOrder() {
        }
    }
}
