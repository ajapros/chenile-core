package org.chenile.trigger.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "org.chenile.trigger.bdd",
    plugin = "pretty")
public class CukesTriggerTest {}
