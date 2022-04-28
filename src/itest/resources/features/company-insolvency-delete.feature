Feature: Delete company insolvency information


  Scenario Outline: Delete company insolvency information successfully

    Given Insolvency data api service is running
    And the insolvency information exists for "<company_number_created>"
    When I send DELETE request with company number "<company_number_deleted>"
    Then I should receive <response_code> status code

    Examples:
      | company_number_created             | company_number_deleted | response_code |
      | CH3634545                          | CH3634545              | 200           |
      | CH3634545                          | CH1234567              | 404           |
