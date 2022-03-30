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
        ex.printStackTrace();
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

    private String generateShortCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 8);
    }
}
