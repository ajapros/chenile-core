package org.chenile.core.external;

import org.springframework.http.HttpMethod;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExternalApiRequest<T> {
    public String system;
    public String operation;
    public String url;
    public HttpMethod method = HttpMethod.GET;
    public Map<String, Object> headers = new LinkedHashMap<>();
    public Object body;
    public Class<T> responseType;

    public static <T> ExternalApiRequest<T> of(String system, String operation, String url,
                                               HttpMethod method, Object body, Class<T> responseType) {
        ExternalApiRequest<T> request = new ExternalApiRequest<>();
        request.system = system;
        request.operation = operation;
        request.url = url;
        request.method = method;
        request.body = body;
        request.responseType = responseType;
        return request;
    }
}
