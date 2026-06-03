package org.chenile.core.external;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ExternalApiProperties {
    private final boolean enabled;
    private final String inboundTopic;
    private final String outboundTopic;
    private final int maxPayloadBytes;
    private final Set<String> maskedHeaders;

    public ExternalApiProperties(boolean enabled, String inboundTopic, String outboundTopic,
                                 int maxPayloadBytes, String maskedHeaders) {
        this.enabled = enabled;
        this.inboundTopic = inboundTopic;
        this.outboundTopic = outboundTopic;
        this.maxPayloadBytes = maxPayloadBytes;
        this.maskedHeaders = new HashSet<>();
        if (maskedHeaders != null && !maskedHeaders.isBlank()) {
            Arrays.stream(maskedHeaders.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .forEach(this.maskedHeaders::add);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String topic(ExternalApiDirection direction) {
        return direction == ExternalApiDirection.INBOUND ? inboundTopic : outboundTopic;
    }

    public int getMaxPayloadBytes() {
        return maxPayloadBytes;
    }

    public boolean isMaskedHeader(String header) {
        return header != null && maskedHeaders.contains(header.toLowerCase(Locale.ROOT));
    }
}
