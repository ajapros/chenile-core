package org.chenile.core.init;

import org.springframework.core.Ordered;

public interface ChenileInitializer extends Ordered {
    public void performInit();
    @Override
    default int getOrder() {
        // Default to the end of the line
        return Ordered.LOWEST_PRECEDENCE;
    }
}
