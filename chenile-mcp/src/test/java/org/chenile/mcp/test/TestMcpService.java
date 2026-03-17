package org.chenile.mcp.test;

public class TestMcpService {

    public String simple(SimplePayload payload) {
        return "simple:" + payload.value();
    }

    public String foo(String id, String eventId, Object eventPayload) {
        if (eventPayload instanceof E1Payload e1Payload) {
            return eventId + ":E1:" + e1Payload.value();
        }
        if (eventPayload instanceof E2Payload e2Payload) {
            return eventId + ":E2:" + e2Payload.value();
        }
        return eventId + ":" + id + ":" + String.valueOf(eventPayload);
    }
}
