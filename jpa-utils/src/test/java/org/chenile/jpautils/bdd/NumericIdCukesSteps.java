package org.chenile.jpautils.bdd;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import bdd.jpa.numeric.NumericIdBddApplication;
import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.HeaderUtils;
import org.chenile.jpautils.test.TestEntity;
import org.chenile.jpautils.test.TestEntityRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NumericIdBddApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:jpautils-bdd;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@CucumberContextConfiguration
public class NumericIdCukesSteps {
    private final TestEntityRepository stringRepository;
    private final BddLongEntityRepository longRepository;
    private TestEntity stringEntity;
    private BddLongEntity longEntity;

    public NumericIdCukesSteps(TestEntityRepository stringRepository, BddLongEntityRepository longRepository) {
        this.stringRepository = stringRepository;
        this.longRepository = longRepository;
    }

    @Before
    public void reset() {
        ContextContainer.CONTEXT_CONTAINER.clear();
        stringRepository.deleteAll();
        longRepository.deleteAll();
    }

    @After
    public void clearContext() {
        ContextContainer.CONTEXT_CONTAINER.clear();
    }

    @Given("a Chenile request with ID {string}, tenant {string}, and user {string}")
    public void aChenileRequest(String requestId, String tenant, String user) {
        ContextContainer.CONTEXT_CONTAINER.setRequestId(requestId);
        ContextContainer.CONTEXT_CONTAINER.setTenant(tenant);
        ContextContainer.CONTEXT_CONTAINER.put(HeaderUtils.AUTH_USER_KEY, user);
        ContextContainer.CONTEXT_CONTAINER.setTestMode("true");
    }

    @When("I persist a legacy String entity and a generated Long entity")
    public void persistBothEntityTypes() {
        stringEntity = stringRepository.save(new TestEntity());
        longEntity = longRepository.save(new BddLongEntity());
    }

    @Then("the legacy entity has String ID {string}")
    public void legacyEntityHasStringId(String id) {
        Assertions.assertEquals(id, stringEntity.getId());
    }

    @Then("the numeric entity has Long ID {long}")
    public void numericEntityHasLongId(long id) {
        Assertions.assertEquals(id, longEntity.getId());
    }

    @Then("both entities can be retrieved using their native IDs")
    public void entitiesCanBeRetrievedUsingNativeIds() {
        Assertions.assertTrue(stringRepository.findById(stringEntity.getId()).isPresent());
        Assertions.assertTrue(longRepository.findById(longEntity.getId()).isPresent());
    }

    @Then("both entities contain tenant {string}, user {string}, and test mode")
    public void entitiesContainAuditMetadata(String tenant, String user) {
        Assertions.assertEquals(tenant, stringEntity.tenant);
        Assertions.assertEquals(tenant, longEntity.tenant);
        Assertions.assertEquals(user, stringEntity.createdBy);
        Assertions.assertEquals(user, longEntity.createdBy);
        Assertions.assertTrue(stringEntity.testEntity);
        Assertions.assertTrue(longEntity.testEntity);
        Assertions.assertNotNull(stringEntity.getCreatedTime());
        Assertions.assertNotNull(longEntity.getCreatedTime());
    }
}
