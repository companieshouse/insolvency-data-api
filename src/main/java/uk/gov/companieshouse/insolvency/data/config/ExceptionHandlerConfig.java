package uk.gov.companieshouse.insolvency.data.config;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.insolvency.data.exceptions.BadGatewayException;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.ConflictException;
import uk.gov.companieshouse.insolvency.data.exceptions.DocumentNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@ControllerAdvice
public class ExceptionHandlerConfig {

    // The correlation identifier from the header
    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private void populateResponseBody(Map<String, Object> responseBody, String correlationId) {
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", String.format("Exception occurred while processing the API"
                + " request with Correlation ID: %s", correlationId));
    }

    private void errorLogException(Exception ex) {
        LOGGER.error("Exception occurred while processing the API request", ex);
    }

    private Map<String, Object> responseAndLogBuilderHandler(Exception ex, WebRequest request) {
        var correlationId = request.getHeader(X_REQUEST_ID_HEADER);

        if (StringUtils.isEmpty(correlationId)) {
            correlationId = generateShortCorrelationId();
        }
        Map<String, Object> responseBody = new LinkedHashMap<>();
        populateResponseBody(responseBody, correlationId);
        errorLogException(ex);

        return responseBody;
    }

    /**
     * Runtime exception handler. Acts as the catch-all scenario.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DocumentNotFoundException exception handler. Thrown when the requested document could not be found in the DB in
     * place of status code 404.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {DocumentNotFoundException.class})
    public ResponseEntity<Object> handleDocumentNotFoundException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request), HttpStatus.NOT_FOUND);
    }

    /**
     * BadRequestException exception handler. Thrown when data is given in the wrong format.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {BadRequestException.class, DateTimeParseException.class,
            HttpMessageNotReadableException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(value = {BadGatewayException.class})
    public ResponseEntity<Object> handleBadGatewayException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(value = {ConflictException.class})
    public ResponseEntity<Object> handleConflictException(Exception ex,
            WebRequest request) {
        return new ResponseEntity<>(responseAndLogBuilderHandler(ex, request), HttpStatus.CONFLICT);
    }

    private String generateShortCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
