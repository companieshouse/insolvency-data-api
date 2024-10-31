Feature: Process company insolvency information

  Scenario Outline: Processing company insolvency information successfully

    Given Insolvency data api service is running
    And the CHS Kafka API is reachable
    When I send PUT request with payload "<data>" file
    Then I should receive 200 status code
    And the expected result should match "<result>" file
    And the CHS Kafka API is invoked with event "changed"

    Examples:
      | data                             | result                                  |
      | case_type_compulsory_liquidation | case_type_compulsory_liquidation_output |
      | case_type_receivership           | case_type_receivership_output           |

  Scenario Outline: Update existing company insolvency by unsetting the status field

    Given Insolvency data api service is running
    And the CHS Kafka API is reachable
    And the insolvency information exists for "<companyNumber>"
    When I send PUT request with payload "<data>" file
    Then I should receive 200 status code
    And the expected result should match "<result>" file
    And the CHS Kafka API is invoked with event "changed"

    Examples:
      | companyNumber  | data                                       | result                                            |
      | CH5324324      | case_type_compulsory_liquidation_no_status | case_type_compulsory_liquidation_no_status_output |

  Scenario Outline: Processing company insolvency information no eric headers

    Given Insolvency data api service is running
    When I send PUT request with payload "<data>" file without eric headers
    Then I should receive 401 status code

    Examples:
      | data                             |
      | case_type_compulsory_liquidation |

  Scenario Outline: Retrieve company insolvency information successfully

    Given Insolvency data api service is running
    And the insolvency information exists for "<companyNumber>"
    When I send GET request with company number "<companyNumber>"
    Then I should receive 200 status code
    And the Get call response body should match "<result>" file

    Examples:
      | companyNumber | result                     |
      | CH3634545     | retrieve_by_company_number |

  Scenario: Return a 404 when document doesn't exist

    Given Insolvency data api service is running
    When I send GET request with company number "CH1234567"
    Then I should receive 404 status code

