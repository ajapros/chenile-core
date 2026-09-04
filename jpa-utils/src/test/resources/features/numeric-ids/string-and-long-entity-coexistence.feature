Feature: Numeric JPA identifiers coexist with legacy String identifiers

  Scenario: Persist and retrieve String and Long Chenile entities together
    Given a Chenile request with ID "BDD-REQUEST", tenant "tenant-bdd", and user "bdd-user"
    When I persist a legacy String entity and a generated Long entity
    Then the legacy entity has String ID "TestEntity-BDD-REQUEST-0001"
    And the numeric entity has Long ID 7001
    And both entities can be retrieved using their native IDs
    And both entities contain tenant "tenant-bdd", user "bdd-user", and test mode
