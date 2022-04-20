Feature: Process company insolvency information

  Scenario Outline: Processing company insolvency information successfully

    Given Insolvency data api service is running
    When I send PUT request with payload "<data>" file
    Then I should receive 200 status code
    And the expected result should match "<result>" file
    And the CHS Kafka API is invoked successfully

    Examples:
      | data                             | result                                  |
      | case_type_compulsory_liquidation | case_type_compulsory_liquidation_output |
      | case_type_receivership           | case_type_receivership_output           |

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

  Scenario Outline: Retrieve company insolvency information successfully

    Given Insolvency data api service is running
    And the insolvency information exists for "<companyNumber>"
    When I send GET request with company number "<companyNumber>"
    Then I should receive 200 status code
    And the Get call response body should match "<result>" file

    Examples:
      | companyNumber | result                     |
      | CH3634545     | retrieve_by_company_number |

  Scenario Outline: Delete company insolvency information successfully

    Given Insolvency data api service is running
    And the insolvency information exists for "<companyNumber>"
    When I send DELETE request with company number "<companyNumber>"
    Then I should receive 200 status code

    Examples:
      | companyNumber |
      | CH3634545     |
