package org.chenile.multids.bdd.nodefaulttenant;

import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.chenile.multids.SpringConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpringConfig.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("nodefaulttenant")
public class NoDefaultTenantCukesSteps {
    @Given("dummy")
    public void dummy() {
    }
}
