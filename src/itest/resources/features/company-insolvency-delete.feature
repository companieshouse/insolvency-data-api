Feature: Delete company insolvency information


  Scenario Outline: Delete company insolvency information successfully

    Given Insolvency data api service is running
    And the insolvency information exists for "<companyNumber>"
    When I send DELETE request with company number "<companyNumber>"
    Then I should receive 200 status code

    Examples:
      | companyNumber |
      | CH3634545     |