package org.chenile.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.chenile.core.event.EventProcessor;
import org.chenile.configuration.trigger.DirectTriggerAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DirectTriggerAutoConfigurationTest {
  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(DirectTriggerAutoConfiguration.class))
          // A ready-made singleton: as a bean definition, Spring would try to fill the @Autowired
          // fields the mock inherits from EventProcessor.
          .withInitializer(
              context ->
                  context
                      .getBeanFactory()
                      .registerSingleton("chenileEventProcessor", mock(EventProcessor.class)));

  @Test
  void suppliesTheDirectServiceByDefault() {
    runner.run(context -> assertThat(context).hasSingleBean(DirectTriggerService.class));
  }

  @Test
  void stepsAsideForAnotherTriggerService() {
    runner
        .withBean("durableTriggerService", TriggerService.class, () -> mock(TriggerService.class))
        .run(
            context -> {
              assertThat(context).hasSingleBean(TriggerService.class);
              assertThat(context).doesNotHaveBean(DirectTriggerService.class);
            });
  }
}
