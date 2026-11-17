package org.chenile.trigger.test;

import org.chenile.trigger.test.service.OrderListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = "org.chenile.configuration")
@PropertySource("classpath:org/chenile/trigger/test/TestTrigger.properties")
public class TriggerTestApplication {
  @Bean
  public OrderListener orderListener() {
    return new OrderListener();
  }
}
