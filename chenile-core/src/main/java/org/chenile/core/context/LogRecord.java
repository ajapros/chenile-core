package org.chenile.core.context;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.base.response.GenericResponse;
import org.chenile.base.response.ResponseMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the version of the {@link GenericResponse} for asynchronous processing. <br/>
 * In case, the medium of invocation is asynchronous and the output cannot be captured then this
 * record must be logged in a file or queue or topic somewhere so that a retry mechanism
 * can be implemented using a workflow.<br/>
 */
public class LogRecord {
    public enum Direction {
        INBOUND, OUTBOUND
    }
    public boolean success;
    public List<ResponseMessage> responseMessages;
    public String serviceName;
    public String operationName;
    public String moduleName;
    public Map<String,Object> headers = new HashMap<>();
    public Object request;
    public GenericResponse<?> response;
    /**
     * Indicates what is the original Source of this request. This will be the same as the
     * entry point of this request.
     */
    public String originalSource;
    /**
     * Indicates what is the reference in the original source. This is required for tracing the
     * request back to where it originally got created. (for example file1: line 23 or
     * topic name etc.)
     */
    public String originalSourceReference;
    public ErrorNumException exception;
    public Direction direction;
    public boolean external;
    public String externalSystem;
    public String externalOperation;
    public String protocol;
    public String target;
    public String httpMethod;
    public Integer httpStatusCode;
    public Long durationMillis;
    public Long timestamp;
    public String requestId;
    public String correlationId;
    public String requestPayload;
    public String responsePayload;
    public String errorCode;
    public String errorMessage;
}
