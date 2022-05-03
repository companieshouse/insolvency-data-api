package uk.gov.companieshouse.insolvency.data.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
     * Runtime exception handler. Acts as the catch-all scenario.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        logger.error(String.format("Unexpected exception, response code: %s",
                HttpStatus.INTERNAL_SERVER_ERROR), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Unable to process the request.");
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * IllegalArgumentException exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<Object> handleNotFoundException(Exception ex, WebRequest request) {
        logger.error(String.format("Resource not found, response code: %s",
                HttpStatus.NOT_FOUND), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Resource not found.");
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    /**
     * BadRequestException exception handler.
     * Thrown when data is given in the wrong format.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {BadRequestException.class, DateTimeParseException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception ex, WebRequest request) {
        logger.error(String.format("Bad request, response code: %s", HttpStatus.BAD_REQUEST), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Bad request.");
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    /**
     * MethodNotAllowedException exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {MethodNotAllowedException.class})
    public ResponseEntity<Object> handleMethodNotAllowedException(Exception ex,
                                                                  WebRequest request) {
        logger.error(String.format("Unable to process the request, response code: %s",
                HttpStatus.METHOD_NOT_ALLOWED), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Unable to process the request.");
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * ServiceUnavailableException exception handler.
     * To be thrown when there are connection issues.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {ServiceUnavailableException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(Exception ex,
                                                                    WebRequest request) {
        logger.error(String.format("Service unavailable, response code: %s",
                HttpStatus.SERVICE_UNAVAILABLE), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("message", "Service unavailable.");
        request.setAttribute("javax.servlet.error.exception", ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
