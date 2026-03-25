package org.chenile.multids.bdd.defaulttenant;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/defaulttenant",
        glue = {
                "classpath:org/chenile/multids/bdd/defaulttenant",
                "classpath:org/chenile/cucumber/rest"
        },
        plugin = {"pretty"}
)
public class DefaultTenantCukesRestTest {
}
