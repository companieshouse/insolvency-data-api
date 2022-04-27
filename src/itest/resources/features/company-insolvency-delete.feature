Feature: Delete company insolvency information


  Scenario: Delete company insolvency information successfully

    Given Insolvency data api service is running
    And the insolvency information exists for "CH3634545"
    When I send DELETE request with company number "CH3634545"
    Then I should receive 200 status code

  Scenario: Delete company insolvency information successfully

    Given Insolvency data api service is running
    And insolvency information is not added to database
    When I send DELETE request with company number "CH3634545"
    Then I should receive 404 status code
