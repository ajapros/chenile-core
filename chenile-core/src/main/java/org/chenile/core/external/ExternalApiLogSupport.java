package org.chenile.core.external;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.core.context.HeaderUtils;
import org.chenile.core.context.LogRecord;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ExternalApiLogSupport {
    private static final String TRUNCATED_SUFFIX = "...[truncated]";

    private final ExternalApiProperties properties;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public ExternalApiLogSupport(ExternalApiProperties properties) {
        this.properties = properties;
    }

    public void applyCommon(LogRecord record, ExternalApiDirection direction,
                            String system, String operation, Map<String, ?> headers) {
        record.direction = direction == ExternalApiDirection.INBOUND
                ? LogRecord.Direction.INBOUND : LogRecord.Direction.OUTBOUND;
        record.external = true;
        record.externalSystem = system;
        record.externalOperation = operation;
        record.timestamp = System.currentTimeMillis();
        record.requestId = header(headers, HeaderUtils.REQUEST_ID);
        record.correlationId = header(headers, HeaderUtils.CORRELATION_ID);
        if (record.correlationId == null) {
            record.correlationId = record.requestId;
        }
        copyHeaders(record, headers);
    }

    public String payload(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            if (payload instanceof String s) {
                return truncate(s);
            }
            return truncate(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            return truncate(String.valueOf(payload));
        }
    }

    public void applyError(LogRecord record, Throwable throwable) {
        if (throwable == null) {
            return;
        }
        record.errorMessage = throwable.getMessage();
        if (throwable instanceof ErrorNumException errorNumException) {
            record.errorCode = String.valueOf(errorNumException.getErrorNum());
        }
    }

    private void copyHeaders(LogRecord record, Map<String, ?> headers) {
        if (headers == null) {
            return;
        }
        headers.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            record.headers.put(key, properties.isMaskedHeader(key) ? "****" : value);
        });
    }

    private String header(Map<String, ?> headers, String key) {
        if (headers == null || headers.get(key) == null) {
            return null;
        }
        return String.valueOf(headers.get(key));
    }

    private String truncate(String value) {
        int maxBytes = properties.getMaxPayloadBytes();
        if (maxBytes <= 0 || value == null) {
            return value;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }
        return new String(bytes, 0, maxBytes, StandardCharsets.UTF_8) + TRUNCATED_SUFFIX;
    }
}
