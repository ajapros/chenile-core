package org.chenile.core.external;

import org.chenile.core.context.HeaderUtils;
import org.chenile.core.context.LogRecord;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExternalApiLogSupportTest {

    @Test
    public void applyCommonMasksHeadersAndSetsCorrelation() {
        ExternalApiLogSupport support = new ExternalApiLogSupport(
                new ExternalApiProperties(true, "in", "out", 1024, "Authorization"));
        LogRecord record = new LogRecord();

        support.applyCommon(record, ExternalApiDirection.INBOUND, "partner", "create",
                Map.of(HeaderUtils.REQUEST_ID, "req-1", "Authorization", "secret", "x-visible", "ok"));

        assertEquals(LogRecord.Direction.INBOUND, record.direction);
        assertTrue(record.external);
        assertEquals("partner", record.externalSystem);
        assertEquals("create", record.externalOperation);
        assertEquals("req-1", record.requestId);
        assertEquals("req-1", record.correlationId);
        assertEquals("****", record.headers.get("Authorization"));
        assertEquals("ok", record.headers.get("x-visible"));
    }

    @Test
    public void payloadIsTruncatedWhenItExceedsConfiguredSize() {
        ExternalApiLogSupport support = new ExternalApiLogSupport(
                new ExternalApiProperties(true, "in", "out", 4, ""));

        assertEquals("abcd...[truncated]", support.payload("abcdef"));
    }
}
