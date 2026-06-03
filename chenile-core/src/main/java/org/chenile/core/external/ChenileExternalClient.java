package org.chenile.core.external;

import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderUtils;
import org.chenile.core.context.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

public class ChenileExternalClient {
    private static final Logger logger = LoggerFactory.getLogger(ChenileExternalClient.class);

    private final RestTemplate restTemplate;
    private final ExternalApiPublisher publisher;
    private final ExternalApiProperties properties;
    private final ExternalApiLogSupport logSupport;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public ChenileExternalClient(RestTemplate restTemplate,
                                 ExternalApiPublisher publisher,
                                 ExternalApiProperties properties,
                                 ExternalApiLogSupport logSupport) {
        this.restTemplate = restTemplate;
        this.publisher = publisher;
        this.properties = properties;
        this.logSupport = logSupport;
    }

    public <T> T exchange(ExternalApiRequest<T> request) {
        HttpHeaders headers = headers(request.headers);
        HttpEntity<Object> entity = new HttpEntity<>(request.body, headers);
        LogRecord record = baseRecord(request, headers);
        long start = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    request.url, request.method, entity, String.class);
            record.durationMillis = System.currentTimeMillis() - start;
            record.httpStatusCode = response.getStatusCode().value();
            record.success = response.getStatusCode().is2xxSuccessful();
            record.responsePayload = logSupport.payload(response.getBody());
            publish(record);
            return convert(response.getBody(), request.responseType);
        } catch (RestClientResponseException e) {
            record.durationMillis = System.currentTimeMillis() - start;
            record.httpStatusCode = e.getStatusCode().value();
            record.success = false;
            record.responsePayload = logSupport.payload(e.getResponseBodyAsString());
            logSupport.applyError(record, e);
            publish(record);
            throw e;
        } catch (RestClientException e) {
            record.durationMillis = System.currentTimeMillis() - start;
            record.success = false;
            logSupport.applyError(record, e);
            publish(record);
            throw e;
        }
    }

    private LogRecord baseRecord(ExternalApiRequest<?> request, HttpHeaders headers) {
        LogRecord record = new LogRecord();
        logSupport.applyCommon(record, ExternalApiDirection.OUTBOUND,
                request.system, request.operation, headers.toSingleValueMap());
        record.protocol = "HTTP";
        record.target = request.url;
        record.httpMethod = request.method.name();
        record.requestPayload = logSupport.payload(request.body);
        record.request = request.body;
        return record;
    }

    private HttpHeaders headers(Map<String, Object> requestHeaders) {
        HttpHeaders headers = new HttpHeaders();
        if (requestHeaders != null) {
            requestHeaders.forEach((key, value) -> {
                if (value != null) {
                    headers.add(key, String.valueOf(value));
                }
            });
        }
        propagate(headers, HeaderUtils.REQUEST_ID, ContextContainer.getInstance().getRequestId());
        propagate(headers, HeaderUtils.CORRELATION_ID, ContextContainer.getInstance().get(HeaderUtils.CORRELATION_ID));
        return headers;
    }

    private void propagate(HttpHeaders headers, String key, String value) {
        if (value != null && !value.isBlank() && !headers.containsHeader(key)) {
            headers.add(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(String body, Class<T> responseType) {
        if (responseType == null || responseType == Void.class || body == null || body.isBlank()) {
            return null;
        }
        if (responseType == String.class) {
            return (T) body;
        }
        try {
            return objectMapper.readValue(body, responseType);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize external API response", e);
        }
    }

    private void publish(LogRecord record) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            publisher.publish(record);
        } catch (Exception e) {
            logger.warn("Unable to publish outbound external API log record", e);
        }
    }
}
