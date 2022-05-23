Feature: Process company insolvency information

  Scenario Outline: Processing company insolvency information successfully

    Given Insolvency data api service is running
    When I send PUT request with payload "<data>" file
    Then I should receive 200 status code
    And the expected result should match "<result>" file
    And the CHS Kafka API is invoked successfully with event "changed"

    Examples:
      | data                             | result                                  |
      | case_type_compulsory_liquidation | case_type_compulsory_liquidation_output |
      | case_type_receivership           | case_type_receivership_output           |

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

