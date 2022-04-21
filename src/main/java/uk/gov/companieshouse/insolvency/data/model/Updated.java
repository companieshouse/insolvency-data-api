package uk.gov.companieshouse.insolvency.data.model;

public class Updated {

    private String at;

    private String by;

    private String type;

    /**
     * Default constructor.
     */
    public Updated() {
    }

    /**
     * Instantiate company insolvency updated data.
     * @param at timestamp for the delta change
     * @param by updated by
     * @param type the delta type
     */
    public Updated(String at, String by, String type) {
        this.at = at;
        this.by = by;
        this.type = type;
    }

    public String getAt() {
        return at;
    }

    public String getBy() {
        return by;
    }

    public String getType() {
        return type;
    }
}
