Feature: Health check API endpoint

  Scenario: Client invokes GET /healthcheck endpoint
    Given Insolvency data api service is running
    When the client invokes '/insolvency-data-api/healthcheck' endpoint
    Then the client receives status code of 200
    And the client receives a response body of '{"status":"UP"}'