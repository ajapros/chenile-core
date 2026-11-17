Feature: Raise Chenile events through the trigger service
  Every source (the scheduler, file watch, pub/sub, an API or code) raises an event through the
  TriggerService. Without process management, DirectTriggerService raises it at once.

  Scenario: A trigger raises the event with the standard headers
    When code triggers "orderPlaced" for order "o-1" from source "CRON" with key "k-1"
    Then the listener received 1 delivery of order "o-1"
    And the delivery has header "x-chenile-event-source" equal to "CRON"
    And the delivery has the accepted trigger id in header "x-chenile-trigger-id"
    And the trigger status is "DONE"

  Scenario: A duplicate key is ignored
    When code triggers "orderPlaced" for order "o-2" from source "CRON" with key "k-2"
    And code triggers "orderPlaced" for order "o-2" from source "CRON" with key "k-2"
    Then the listener received 1 delivery of order "o-2"
    And both triggers were accepted with the same id

  Scenario: Triggers without a key are never deduplicated
    When code triggers "orderPlaced" for order "o-3" from source "API" without a key
    And code triggers "orderPlaced" for order "o-3" from source "API" without a key
    Then the listener received 2 deliveries of order "o-3"

  Scenario: Each tenant has its own keys
    When tenant "t1" triggers "orderPlaced" for order "o-4" with key "k-4"
    And tenant "t2" triggers "orderPlaced" for order "o-4" with key "k-4"
    Then the listener received 2 deliveries of order "o-4"
    And the deliveries carry tenants "t1,t2"

  Scenario: A caller cannot choose the trigger id
    When code triggers "orderPlaced" for order "o-5" with header "x-chenile-trigger-id" set to "chosen"
    Then the delivery has the accepted trigger id in header "x-chenile-trigger-id"

  Scenario: Headers must start with x-
    When code triggers "orderPlaced" for order "o-6" with header "tenant" set to "t1"
    Then the trigger is refused with "Trigger headers must start with x-"
    And the listener received 0 deliveries of order "o-6"

  Scenario: An event without subscribers fails
    When code triggers "nobodyListens" for order "o-7" from source "CRON" with key "k-7"
    Then the trigger fails with "No event subscriber configured for nobodyListens"

  Scenario: A failed trigger is reported and can be retried with the same key
    When code triggers "orderRejected" for order "o-8" from source "CRON" with key "k-8"
    Then the trigger fails with "Trigger subscriber failed"
    And the status of the delivered trigger is "FAILED" with error "Order o-8 was rejected"
    When code triggers "orderRejected" for order "o-8" from source "CRON" with key "k-8"
    Then the listener received 2 deliveries of order "o-8"
