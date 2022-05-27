package uk.gov.companieshouse.insolvency.data.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.DocumentGoneException;
import uk.gov.companieshouse.insolvency.data.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;

@ControllerAdvice
public class ExceptionHandlerConfig {
    // The correlation identifier from the header
    private static final String X_REQUEST_ID_HEADER = "x-request-id";
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
    public ResponseEntity<Object> handleException(Exception ex,
                                                  WebRequest request) {
        String errMsg = "Unexpected exception";
        HashMap<String, Object> data = buildExceptionResponse(errMsg);
        logger.errorContext(request.getHeader(X_REQUEST_ID_HEADER), errMsg, ex, data);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(data);
    }

    /**
     * DocumentGoneException exception handler.
     * Thrown when the requested document could not be found in the DB in place of status code 404.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {DocumentGoneException.class})
    public ResponseEntity<Object> handleDocumentGoneException(Exception ex,
                                                              WebRequest request) {
        String errMsg = "Resource gone";
        HashMap<String, Object> data = buildExceptionResponse(errMsg);
        logger.errorContext(request.getHeader(X_REQUEST_ID_HEADER), errMsg, ex, data);

        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(data);
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
            HttpMessageNotReadableException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception ex,
                                                            WebRequest request) {
        String errMsg = "Bad request";
        HashMap<String, Object> data = buildExceptionResponse(errMsg);
        logger.errorContext(request.getHeader(X_REQUEST_ID_HEADER), errMsg, ex, data);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(data);
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
        String errMsg = "Unable to process the request, method not allowed";
        HashMap<String, Object> data = buildExceptionResponse(errMsg);
        logger.errorContext(request.getHeader(X_REQUEST_ID_HEADER), errMsg, ex, data);

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(data);
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
        String errMsg = "Service unavailable";
        HashMap<String, Object> data = buildExceptionResponse(errMsg);
        logger.errorContext(request.getHeader(X_REQUEST_ID_HEADER), errMsg, ex, data);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(data);
    }

    private HashMap<String, Object> buildExceptionResponse(String errMsg) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", errMsg);
        return response;
    }
}
