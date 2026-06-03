package org.chenile.core.external;

import org.chenile.core.context.LogRecord;

public interface ExternalApiPublisher {
    void publish(LogRecord record);
}
