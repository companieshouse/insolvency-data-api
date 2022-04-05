package uk.gov.companieshouse.insolvency.data.config;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;

@ControllerAdvice
public class ExceptionHandlerConfig {

    private final Logger logger;

    @Autowired
    public ExceptionHandlerConfig(Logger logger) {
        this.logger = logger;
    }

    /**
     * Runtime exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        String correlationId = generateShortCorrelationId();
        logger.error(String.format("Unexpected exception, correlationId: %s", correlationId), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Unable to process the request.");
        responseBody.put("correlationId", correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Runtime exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<Object> handleNotFoundException(Exception ex, WebRequest request) {
        String correlationId = generateShortCorrelationId();
        logger.error(String.format("Unexpected exception, correlationId: %s", correlationId), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Resource not found.");
        responseBody.put("correlationId", correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity(responseBody, HttpStatus.NOT_FOUND);
    }

    /**
     * Runtime exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {BadRequestException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception ex, WebRequest request) {
        String correlationId = generateShortCorrelationId();
        logger.error(String.format("Unexpected exception, correlationId: %s", correlationId), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Bad request.");
        responseBody.put("correlationId", correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity(responseBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * Runtime exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {MethodNotAllowedException.class})
    public ResponseEntity<Object> handleMethodNotAllowedException(Exception ex, WebRequest request) {
        String correlationId = generateShortCorrelationId();
        logger.error(String.format("Unexpected exception, correlationId: %s", correlationId), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Unable to process the request.");
        responseBody.put("correlationId", correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity(responseBody, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Runtime exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {ServiceUnavailableException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(Exception ex, WebRequest request) {
        String correlationId = generateShortCorrelationId();
        logger.error(String.format("Unexpected exception, correlationId: %s", correlationId), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Service unavailable.");
        responseBody.put("correlationId", correlationId);
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity(responseBody, HttpStatus.SERVICE_UNAVAILABLE);
    }


    private String generateShortCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 8);
    }
}
