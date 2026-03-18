package org.chenile.core.init;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This ensures that all ChenileInitializers are executed in a particular sequence.
 * The initializers are discovered dynamically and executed in the order specified by them.
 */
public class ExecuteChenileInitializers implements SmartInitializingSingleton {

    /**
     * Spring will automatically inject ALL beans from ALL jars
     * that implement {@link ChenileInitializer}, sorted by their @Order.
     */

    @Autowired
    private List<ChenileInitializer> initializers;

    @Override
    public void afterSingletonsInstantiated() {
        for (ChenileInitializer initializer : initializers) {
            initializer.performInit();
        }
    }
}
