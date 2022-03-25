Feature: Process company insolvency information

  Scenario Outline: Processing company insolvency information successfully

    Given Insolvency data api service is running
    When I send PUT request with payload "<data>"
    Then I should receive 200 status code
    And the expected result should match "<result>"

    Examples:
      | data                             | result                                  |
      | case_type_compulsory_liquidation | case_type_compulsory_liquidation_output |
      | case_type_receivership           | case_type_receivership_output           |