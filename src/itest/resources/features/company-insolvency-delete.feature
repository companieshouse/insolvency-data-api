Feature: Delete company insolvency information


  Scenario: Delete company insolvency information successfully

    Given Insolvency data api service is running
    And the CHS Kafka API is reachable
    And the insolvency information exists for "CH3634545"
    When I send DELETE request with company number "CH3634545"
    Then I should receive 200 status code
    And the company insolvency with company number "CH3634545" is deleted from the database
    And the CHS Kafka API is invoked with event "deleted"

  Scenario: Delete company insolvency information unsuccessfully when stale delta

    Given Insolvency data api service is running
    And the CHS Kafka API is reachable
    And the insolvency information exists for "CH3634545"
    When I send DELETE request with company number "CH3634545" and delta_at "20181010175532456123"
    Then I should receive 409 status code
    And the company insolvency with company number "CH3634545" still exists in the database
    And the CHS Kafka API is not invoked

  Scenario: Delete company insolvency information without setting eric headers unsuccessful

    Given Insolvency data api service is running
    And the insolvency information exists for "CH3634545"
    When I send DELETE request with company number "CH3634545" without setting eric headers
    Then I should receive 401 status code
    And the company insolvency with company number "CH3634545" still exists in the database
    And the CHS Kafka API is not invoked

  Scenario: Delete company insolvency information successfully with already absent data

    Given Insolvency data api service is running
    And the CHS Kafka API is reachable
    And the insolvency information exists for "CH3634545"
    When I send DELETE request with company number "CH1234567"
    Then I should receive 200 status code
    And the CHS Kafka API is invoked with event "deleted"

  Scenario: Processing delete company insolvency without 'x-request-id' key in the header

    Given Insolvency data api service is running
    And the insolvency information exists for "CH3634545"
    When I send DELETE request without x-request-id key in the header
    Then I should receive 401 status code
    And the company insolvency with company number "CH3634545" still exists in the database
    And the CHS Kafka API is not invoked

  Scenario: Processing delete company insolvency while database is down

    Given Insolvency data api service is running
    And the insolvency information exists for "CH3634545"
    And the insolvency database is down
    When I send DELETE request with company number "CH3634545"
    Then I should receive 502 status code
    And the CHS Kafka API is not invoked

  Scenario: Processing delete company insolvency when kafka-api is not available

    Given Insolvency data api service is running
    And the insolvency information exists for "CH3634545"
    And CHS kafka API service is unavailable
    When I send DELETE request with company number "CH3634545"
    Then I should receive 502 status code
    And the company insolvency with company number "CH3634545" is deleted from the database
    And the CHS Kafka API is invoked with event "deleted"

