package uk.gov.companieshouse.insolvency.data.common;

public enum EventType {

    DELETED("deleted"),
    CHANGED("changed");

    private final String event;

    EventType(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}
