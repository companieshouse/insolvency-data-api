package uk.gov.companieshouse.insolvency.data.logging;


//@Component
//@Order(value = HIGHEST_PRECEDENCE)
public class RequestLoggingFilter { // extends OncePerRequestFilter implements RequestLogger {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
//
//    @Override
//    protected void doFilterInternal(@Nonnull HttpServletRequest request,
//            @Nonnull HttpServletResponse response,
//            @Nonnull FilterChain filterChain) throws ServletException, IOException {
//        logStartRequestProcessing(request, LOGGER);
//        DataMapHolder.initialise(Optional
//                .ofNullable(request.getHeader(REQUEST_ID.value()))
//                .orElse(UUID.randomUUID().toString()));
//        try {
//            filterChain.doFilter(request, response);
//        } catch (ServiceUnavailableException | DocumentNotFoundException | MethodNotAllowedException ex) {
//            LOGGER.info("Recoverable exception: %s".formatted(Arrays.toString(ex.getStackTrace())),
//                    DataMapHolder.getLogMap());
//            throw ex;
//        } catch (Exception ex) {
//            LOGGER.error(ex.getMessage(), ex, DataMapHolder.getLogMap());
//            throw ex;
//        } finally {
//            logEndRequestProcessing(request, response, LOGGER);
//            DataMapHolder.clear();
//        }
//    }
}
