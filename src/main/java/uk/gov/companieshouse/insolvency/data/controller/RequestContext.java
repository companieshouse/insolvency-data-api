package uk.gov.companieshouse.insolvency.data.controller;

public class RequestContext {

    public static final String CONTEXT_ID = "contextId";

    private static final ThreadLocal<String> id = new ThreadLocal<String>();

    public static String getId() { return id.get(); }

    public static void setId(String correlationId) { id.set(correlationId); }

}
