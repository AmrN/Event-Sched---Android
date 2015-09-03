package com.csgroup.eventsched;

/**
 * Created by pc on 8/24/2015.
 */
public class Comment {
    public static final String API_ID = "id";
    public static final String API_AUTHOR_ID = "author_id";
    public static final String API_EVENT_ID = "event_id";
    public static final String API_CONTENT = "content";
    public static final String API_CREATED_AT = "created_at";
    public static final String API_AUTHOR_NAME = "author_name";

    private int id;
    private int authorID;
    private int eventID;
    private String content;
    private String authorName;
    private Long createdAtTimestamp;
    private DateManager mDateManager;

    public Comment(int id, int authorID, int eventID, String content, String authorName, Long createdAtTimestamp) {
        this.id = id;
        this.authorID = authorID;
        this.eventID = eventID;
        this.content = content;
        this.authorName = authorName;
        this.createdAtTimestamp = createdAtTimestamp;
        mDateManager = new DateManager(this.createdAtTimestamp);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuthorID() {
        return authorID;
    }

    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreatedAtTimestamp() {
        return createdAtTimestamp;
    }

    public void setCreatedAtTimestamp(Long createdAtTimestamp) {
        this.createdAtTimestamp = createdAtTimestamp;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getDateString() {
        return mDateManager.getReadableDateString();
    }

    public String getTimeString() {
        return mDateManager.getReadableTimeString();
    }

    public String getDateTimeString() {
        return mDateManager.getReadableDateTimeString();
    }
}
