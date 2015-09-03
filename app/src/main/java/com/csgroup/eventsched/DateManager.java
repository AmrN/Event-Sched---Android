package com.csgroup.eventsched;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * Created by pc on 8/21/2015.
 */
public class DateManager {
    private GregorianCalendar mCalendar;
    private long mTimeStamp;

    public DateManager(long unixTimeStamp) {
        mCalendar = new GregorianCalendar();
        this.setTimeStamp(unixTimeStamp);
    }

    public DateManager() {
        mCalendar = new GregorianCalendar();
    }

    // get normal timestamp from Unix timestamp
    // unix timestamp is in seconds
    // normal timestamp is in milliseconds
    public static long fromUnixTimeStamp(long unixTimeStamp) {
        return unixTimeStamp * 1000;
    }

    public static long toUnixTimeStamp(long timeStamp) {
        return timeStamp / 1000;
    }

    public void setTimeStamp(long unixTimeStamp) {
        mTimeStamp = this.fromUnixTimeStamp(unixTimeStamp);
        mCalendar.setTimeInMillis(mTimeStamp);
    }

    public String getReadableDateString() {
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        return dateFormat.format(this.mCalendar.getTimeInMillis());
    }

    public String getReadableTimeString() {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(this.mCalendar.getTimeInMillis());
    }

    public String getReadableDateTimeString() {
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        return dateFormat.format(this.mCalendar.getTimeInMillis());
    }

    public String getREadableDayDateTimeString() {
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, hh:mm a");
        return dateFormat.format(this.mCalendar.getTimeInMillis());
    }
}
