package com.csgroup.eventsched;

/**
 * Created by amr on 9/18/15.
 */
public class FreeTime {

    public static final String API_START_TIMESTAMP = "start_time";
    public static final String API_TIMES_FIT = "times_fit";


    private long startTimeStamp;
    private int timesFit;
    private DateManager dateManager;


    public FreeTime(int timesFit, long startTimeStamp) {
        this.timesFit = timesFit;
        this.startTimeStamp = startTimeStamp;
        dateManager = new DateManager(startTimeStamp);
    }

    public String getTimeString() {
        return dateManager.getReadableDayDateTimeString();
    }

    public int getRepetitionsCount() {
        return timesFit;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }
}
