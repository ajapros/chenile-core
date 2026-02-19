package org.chenile.multids;

import org.chenile.core.context.ContextContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TestApplication.class, TestDataInitializerConfig.class})
class MultiTenantRoutingDataSourceTest {

    @Autowired
    private ContextContainer contextContainer;

    @Autowired
    private WidgetRepository widgetRepository;

    @AfterEach
    void clearTenant() {
        contextContainer.setTenant("");
    }

    @Test
    void routesToDefaultTenantWhenNoTenantIsSet() {
        contextContainer.setTenant("");
        List<Widget> widgets = widgetRepository.findAll();
        assertThat(widgets).extracting(Widget::getName)
                .containsExactlyInAnyOrder("t1-widget-a", "t1-widget-b");
    }

    @Test
    void routesToTenant1() {
        contextContainer.setTenant("tenant1");
        List<Widget> widgets = widgetRepository.findAll();
        assertThat(widgets).extracting(Widget::getName)
                .containsExactlyInAnyOrder("t1-widget-a", "t1-widget-b");
    }

    @Test
    void routesToTenant2() {
        contextContainer.setTenant("tenant2");
        List<Widget> widgets = widgetRepository.findAll();
        assertThat(widgets).extracting(Widget::getName)
                .containsExactlyInAnyOrder("t2-widget-a", "t2-widget-b");
    }

    @Test
    void writesAreIsolatedPerTenant() {
        contextContainer.setTenant("tenant2");
        Widget widget = new Widget();
        widget.setName("t2-widget-c");
        widgetRepository.save(widget);

        contextContainer.setTenant("tenant2");
        assertThat(widgetRepository.findAll()).extracting(Widget::getName)
                .contains("t2-widget-c");

        contextContainer.setTenant("tenant1");
        assertThat(widgetRepository.findAll()).extracting(Widget::getName)
                .doesNotContain("t2-widget-c");
    }
}
