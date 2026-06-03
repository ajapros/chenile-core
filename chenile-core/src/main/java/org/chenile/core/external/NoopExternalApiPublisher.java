package org.chenile.core.external;

import org.chenile.core.context.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopExternalApiPublisher implements ExternalApiPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopExternalApiPublisher.class);

    @Override
    public void publish(LogRecord record) {
        if (record == null) {
            LOGGER.info("External API publish skipped. No LogRecord supplied.");
            return;
        }
        LOGGER.info("External API publish skipped by noop publisher. direction={}, service={}, operation={}, externalSystem={}, status={}, durationMillis={}, requestId={}, correlationId={}",
                record.direction, record.serviceName, record.operationName, record.externalSystem,
                record.httpStatusCode, record.durationMillis, record.requestId, record.correlationId);
        LOGGER.debug("External API record details: protocol={}, method={}, target={}, success={}, errorCode={}, errorMessage={}, requestPayload={}, responsePayload={}",
                record.protocol, record.httpMethod, record.target, record.success, record.errorCode,
                record.errorMessage, record.requestPayload, record.responsePayload);
    }
}
