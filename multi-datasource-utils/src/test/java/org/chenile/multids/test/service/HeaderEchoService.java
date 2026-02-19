package org.chenile.multids.test.service;

import org.chenile.core.context.ContextContainer;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service("headerEchoService")
public class HeaderEchoService {

    public Map<String, String> headers() {
        ContextContainer contextContainer = ContextContainer.CONTEXT_CONTAINER;
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("tenant", contextContainer.getTenant());
        payload.put("region", contextContainer.getRegion());
        payload.put("userId", contextContainer.getUser());
        payload.put("employeeId", contextContainer.getEmployeeId());
        payload.put("authUser", contextContainer.getAuthUser());
        payload.put("groupId", contextContainer.getGroupId());
        payload.put("appType", contextContainer.getAppType());
        payload.put("userAgent", contextContainer.getUserAgent());
        payload.put("batchId", contextContainer.get("x-batchId"));
        payload.put("deviceId", contextContainer.get("x-chenile-deviceid"));
        payload.put("tenantType", contextContainer.getTenantType());
        return payload;
    }
}
