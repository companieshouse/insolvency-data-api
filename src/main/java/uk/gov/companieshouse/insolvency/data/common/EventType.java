package uk.gov.companieshouse.insolvency.data.common;

import java.util.Arrays;
import java.util.Optional;

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

    public static Optional<EventType> getEventType(String value) {
        return Arrays.stream(EventType.values()).filter(
                event -> event.getEvent().equals(value)).findFirst();
    }

}
