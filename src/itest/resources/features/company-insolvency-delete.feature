Feature: Delete company insolvency information


  Scenario: Delete company insolvency information successfully

    Given Insolvency data api service is running
    And the CHS Kafka API is reachable
    And the insolvency information exists for "CH3634545"
    When I send DELETE request with company number "CH3634545"
    Then I should receive 200 status code
    And the CHS Kafka API is invoked successfully with event "deleted"

  Scenario: Delete company insolvency information without setting eric headers unsuccessful

    Given Insolvency data api service is running
    And the insolvency information exists for "CH3634545"
    When I send DELETE request with company number "CH3634545" without setting eric headers
    Then I should receive 401 status code

  Scenario: Delete company insolvency information unsuccessfully

    Given Insolvency data api service is running
    And the insolvency information exists for "CH3634545"
    When I send DELETE request with company number "CH1234567"
    Then I should receive 404 status code
    And the CHS Kafka API is not invoked

  Scenario: Processing delete company insolvency without 'x-request-id' key in the header

    Given Insolvency data api service is running
    When I send DELETE request without x-request-id key in the header
    Then I should receive 401 status code
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
    Then I should receive 503 status code
    And the CHS Kafka API is invoked successfully with event "deleted"
    And the company insolvency with company number "CH3634545" still exists in the database

