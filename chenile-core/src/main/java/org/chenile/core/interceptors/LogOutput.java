package org.chenile.core.interceptors;

import org.chenile.base.response.GenericResponse;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.HeaderUtils;
import org.chenile.core.context.LogRecord;
import org.chenile.core.external.ExternalApiDirection;
import org.chenile.core.external.ExternalApiPublisher;
import org.chenile.core.external.ExternalApiLogSupport;
import org.chenile.core.external.ExternalApiMetadata;
import org.chenile.core.external.ExternalApiProperties;
import org.chenile.core.model.LogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Logs the output from the response object. This is required if the service is asynchronous i.e. its
 * response is not emitted back but needs to be used to keep track of success and failure by
 * logging the response into some medium (such as files, queues, topics etc.) <br/>
 * The original entry point (that constructed the ChenileExchange) can give us a callback to invoke
 * or can ask us to log into a file. That can then be used to transmit the status to the original
 * caller or displayed in a UI.<br/>
 */
public class LogOutput extends BaseChenileInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(LogOutput.class);
	private static final String LOG_START_TIME = "x-chenile-external-log-start-time";

	@Autowired(required = false)
	private ExternalApiPublisher externalApiPublisher;
	@Autowired(required = false)
	private ExternalApiLogSupport externalApiLogSupport;
	@Autowired(required = false)
	private ExternalApiProperties externalApiProperties;

	@Override
	protected void doPreProcessing(ChenileExchange exchange) {
		exchange.setHeader(LOG_START_TIME, String.valueOf(System.currentTimeMillis()));
	}

	@Override
	protected void doPostProcessing(ChenileExchange chenileExchange) {
		LogRecord record = makeLogRecord(chenileExchange);
		chenileExchange.getHeaders().remove(LOG_START_TIME);
		// execute registered loggers
		LogWriter logWriter = chenileExchange.getLogWriter();
		if (logWriter != null) {
			logWriter.write(record);
		}
		publishExternalApiRecord(chenileExchange, record);
	}

	private LogRecord makeLogRecord(ChenileExchange exchange) {
		LogRecord record = new LogRecord();
		try {
			GenericResponse<?> resp = (GenericResponse<?>) exchange.getResponse();
			record.success = resp.isSuccess();
			record.response = resp;
			record.responseMessages = resp.getErrors();
		}catch(Exception e){
			e.printStackTrace();
		}

		record.serviceName = exchange.getServiceDefinition().getName();
		record.moduleName = exchange.getServiceDefinition().getModuleName();
		record.operationName = exchange.getOperationDefinition().getName();
		record.direction = LogRecord.Direction.INBOUND;
		record.protocol = exchange.getHeader(HeaderUtils.ENTRY_POINT, String.class);
		record.httpStatusCode = exchange.getHttpResponseStatusCode() == 0 ? null : exchange.getHttpResponseStatusCode();
		record.durationMillis = durationMillis(exchange);
		record.timestamp = System.currentTimeMillis();

		record.request = exchange.getBody();
		record.originalSource = exchange.getHeader(HeaderUtils.ENTRY_POINT,String.class);
		record.originalSourceReference = exchange.getOriginalSourceReference();
		record.exception = exchange.getException();
		if (exchange.getException() != null) {
			record.errorCode = String.valueOf(exchange.getException().getErrorNum());
			record.errorMessage = exchange.getException().getMessage();
		}
		copyParamHeaders(record,exchange);
		return record;
	}

	private void publishExternalApiRecord(ChenileExchange exchange, LogRecord record) {
		if (externalApiPublisher == null || externalApiLogSupport == null
				|| externalApiProperties == null || !externalApiProperties.isEnabled()) {
			return;
		}
		ExternalApiMetadata metadata = ExternalApiMetadata.from(exchange);
		if (metadata == null) {
			return;
		}
		externalApiLogSupport.applyCommon(record, ExternalApiDirection.INBOUND,
				metadata.system(), metadata.operation(), exchange.getHeaders());
		record.requestPayload = externalApiLogSupport.payload(exchange.getBody());
		record.responsePayload = externalApiLogSupport.payload(exchange.getResponse());
		externalApiLogSupport.applyError(record, exchange.getException());
		try {
			externalApiPublisher.publish(record);
		} catch (Exception e) {
			logger.warn("Unable to publish inbound external API log record", e);
		}
	}

	private void copyParamHeaders(LogRecord record, ChenileExchange exchange) {
		exchange.getOperationDefinition().getParams().forEach(
				(pd) -> {
					Object obj = exchange.getHeader(pd.getName());
					if (obj != null){
						record.headers.put(pd.getName(),obj);
					}
				}
		);
	}

	@Override
	protected boolean bypassInterception(ChenileExchange exchange) {
        return exchange.getLogWriter() == null && ExternalApiMetadata.from(exchange) == null;
    }

	private Long durationMillis(ChenileExchange exchange) {
		Object start = exchange.getHeader(LOG_START_TIME);
		if (start instanceof Long startTime) {
			return System.currentTimeMillis() - startTime;
		}
		if (start instanceof String startTime && !startTime.isBlank()) {
			try {
				return System.currentTimeMillis() - Long.parseLong(startTime);
			} catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}
}
