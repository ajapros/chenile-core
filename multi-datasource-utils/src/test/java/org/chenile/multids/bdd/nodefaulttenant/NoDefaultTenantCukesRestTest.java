package org.chenile.multids.bdd.nodefaulttenant;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/nodefaulttenant",
        glue = {
                "classpath:org/chenile/multids/bdd/nodefaulttenant",
                "classpath:org/chenile/cucumber/rest"
        },
        plugin = {"pretty"}
)
public class NoDefaultTenantCukesRestTest {
}
