Feature: Multi-tenant datasource routing without a configured default tenant

  Scenario: Missing tenant header returns an error when no default tenant is configured
    Given dummy
    When I GET a REST request to URL "/test/items"
    Then the http status code is 500
    And success is false
    And the top level code is 500

  Scenario: Tenant specific data is still returned for tenant1
    Given dummy
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I GET a REST request to URL "/test/items"
    Then the http status code is 200
    And success is true
    And the REST response key "items" collection has an item with keys and values:
      | key   | value    |
      | name  | t1-item-a|
      | tenant| tenant1  |
    And the REST response key "items" collection has an item with keys and values:
      | key   | value    |
      | name  | t1-item-b|
      | tenant| tenant1  |

  Scenario: Unknown tenant header returns an error when no default tenant is configured
    Given dummy
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant-does-not-exist"
    And I GET a REST request to URL "/test/items"
    Then the http status code is 500
    And success is false
    And the top level code is 500
