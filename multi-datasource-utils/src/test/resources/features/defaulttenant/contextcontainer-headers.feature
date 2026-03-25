Feature: ContextContainer header population

  Scenario: Request headers are copied into ContextContainer
    Given dummy
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant-1"
    And I construct a REST request with header "x-chenile-region-id" and value "region-1"
    And I construct a REST request with header "x-chenile-uid" and value "user-1"
    And I construct a REST request with header "x-chenile-eid" and value "emp-1"
    And I construct a REST request with header "x-chenile-auth-user" and value "auth-1"
    And I construct a REST request with header "x-chenile-gid" and value "group-1"
    And I construct a REST request with header "x-chenile-apt" and value "app-1"
    And I construct a REST request with header "user-agent" and value "JUnit"
    And I construct a REST request with header "x-batchId" and value "batch-1"
    And I construct a REST request with header "x-chenile-deviceid" and value "device-1"
    And I construct a REST request with header "x-chenile-tenanttype" and value "type-1"
    When I GET a REST request to URL "/test/headers"
    Then the http status code is 200
    And success is true
    And the REST response key "tenant" is "tenant-1"
    And the REST response key "region" is "region-1"
    And the REST response key "userId" is "user-1"
    And the REST response key "employeeId" is "emp-1"
    And the REST response key "authUser" is "auth-1"
    And the REST response key "groupId" is "group-1"
    And the REST response key "appType" is "app-1"
    And the REST response key "batchId" is "batch-1"
    And the REST response key "deviceId" is "device-1"
    And the REST response key "tenantType" is "type-1"

  Scenario: Missing headers default to empty strings
    Given dummy
    When I GET a REST request to URL "/test/headers"
    Then the http status code is 200
    And success is true
    And the REST response key "tenant" is ""
    And the REST response key "region" is ""
    And the REST response key "userId" is ""
    And the REST response key "employeeId" is ""
    And the REST response key "authUser" is ""
    And the REST response key "groupId" is ""
    And the REST response key "appType" is ""
