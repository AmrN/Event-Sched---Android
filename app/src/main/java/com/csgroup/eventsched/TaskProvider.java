package com.csgroup.eventsched;

import android.os.AsyncTask;

/**
 * Created by amr on 9/14/15.
 */
public interface TaskProvider<T extends AsyncTask<Void,?,?>> {
    T getTask(int num);
}
