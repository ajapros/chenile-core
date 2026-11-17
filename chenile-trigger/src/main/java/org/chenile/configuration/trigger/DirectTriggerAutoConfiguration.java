package org.chenile.configuration.trigger;

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
 *
 * <p>It is registered as an auto-configuration (META-INF/spring/...AutoConfiguration.imports), so the
 * condition is evaluated after the application's own beans. {@code @SpringBootApplication}'s scan skips
 * auto-configuration classes, so living under org.chenile.configuration does not make it a scanned bean.
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
