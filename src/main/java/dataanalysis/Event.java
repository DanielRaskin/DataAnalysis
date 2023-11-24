package dataanalysis;

/**
 * Event
 */
public class Event {
    private static final String FIRST_CLICK_TAG = "fclick";
    private static final String VIEW_THROUGH_PREFIX = "v";
    private static final int VIEW_THROUGH_PREFIX_LENGTH = 1;

    private String uid;
    private String tag;

    private String type;
    private boolean isFirstClick;

    public Event(String uid, String tag) {
        this.uid = uid;
        this.tag = tag;
        this.type = tag.startsWith(VIEW_THROUGH_PREFIX) ? tag.substring(VIEW_THROUGH_PREFIX_LENGTH) : tag;
        this.isFirstClick = tag.equals(FIRST_CLICK_TAG);
    }

    public String getUid() {
        return uid;
    }

    public String getTag() {
        return tag;
    }

    public String getType() {
        return type;
    }

    public boolean isFirstClick() {
        return isFirstClick;
    }
}
