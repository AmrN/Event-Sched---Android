package com.csgroup.eventsched;

import android.os.AsyncTask;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by amr on 9/14/15.
 */
public class PeriodicAsyncTask<T extends AsyncTask<Void,?,?>> {
    private Timer timer;
    private TimerTask doAsynchronousTask;
    private Handler handler;
    private boolean isRunning = false;
    private TaskProvider<T> taskProvider;

    public PeriodicAsyncTask(TaskProvider<T> taskProvider) {
        this.taskProvider = taskProvider;
    }

    public void start(int interval) {
        if (!isRunning) {
            handler = new Handler();
            timer = new Timer();
            final Runnable runnable = new Runnable() {
                public void run() {
                    try {

                        taskProvider.getTask(0).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            doAsynchronousTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(runnable);
                }
            };

            timer.schedule(doAsynchronousTask, 0, interval);
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning) {
            if (timer != null) {
                timer.cancel();
            }
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            if (doAsynchronousTask != null) {
                doAsynchronousTask.cancel();
            }
            isRunning = false;
        }
    }
}
