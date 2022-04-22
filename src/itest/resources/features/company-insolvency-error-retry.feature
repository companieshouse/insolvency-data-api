Feature: Error and retry scenarios for company insolvency

  Scenario Outline: Processing company insolvency information unsuccessfully

    Given Insolvency data api service is running
    When I send PUT request with raw payload "<data>" file
    Then I should receive <response_code> status code
    And the CHS Kafka API is not invoked
    And nothing is persisted in the database

    Examples:
      | data                                             | response_code |
      | bad_request_payload                              | 400           |
      | internal_server_error_payload                    | 500           |

  Scenario Outline: Processing company insolvency information unsuccessfully but saves to database

    Given Insolvency data api service is running
    When CHS kafka API service is unavailable
    And I send PUT request with payload "<data>" file
    Then I should receive 503 status code
    And the expected result should match "<result>" file
    And the CHS Kafka API is invoked successfully

    Examples:
      | data                             | result |
      | case_type_compulsory_liquidation | case_type_compulsory_liquidation_output |

  Scenario: Processing company insolvency information while database is down

    Given Insolvency data api service is running
    And the insolvency database is down
    When I send PUT request with payload "case_type_compulsory_liquidation" file
    Then I should receive 503 status code
    And the CHS Kafka API is not invoked
