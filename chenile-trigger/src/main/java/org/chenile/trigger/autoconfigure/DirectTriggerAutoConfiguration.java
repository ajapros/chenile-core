package org.chenile.trigger.autoconfigure;

import java.time.Duration;
import org.chenile.core.event.EventProcessor;
import org.chenile.trigger.DirectTriggerService;
import org.chenile.trigger.TriggerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Supplies {@link DirectTriggerService} when the application has no other {@link TriggerService}.
 * Process management's trigger-service defines its durable one, which then wins.
 *
 * <p>Properties: {@code chenile.trigger.dedupe.retention}, an ISO-8601 duration (default PT24H), and {@code
 * chenile.trigger.dedupe.max-entries} (default 100000).
 */
@AutoConfiguration
public class DirectTriggerAutoConfiguration {
  @Bean
  @ConditionalOnBean(EventProcessor.class)
  @ConditionalOnMissingBean(TriggerService.class)
  public DirectTriggerService triggerService(
      EventProcessor eventProcessor,
      @Value("${chenile.trigger.dedupe.retention:PT24H}") String retention,
      @Value("${chenile.trigger.dedupe.max-entries:100000}") int maxEntries) {
    return new DirectTriggerService(
        eventProcessor, Duration.parse(retention), maxEntries);
  }
}
