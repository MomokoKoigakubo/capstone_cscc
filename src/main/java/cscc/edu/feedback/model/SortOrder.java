package cscc.edu.feedback.model;

/*
* The ORDER BY text lives here and never comes from the request. The caller can only
* pick one of these names, so there is nothing for an injected sort string to ride on.
* */
public enum SortOrder {
    NEWEST("f.submitted DESC, f.feedbackID DESC"),
    OLDEST("f.submitted ASC, f.feedbackID ASC");

    private final String orderBy;

    SortOrder(String orderBy) {
        this.orderBy = orderBy;
    }

    public String orderBy() {
        return orderBy;
    }
}
