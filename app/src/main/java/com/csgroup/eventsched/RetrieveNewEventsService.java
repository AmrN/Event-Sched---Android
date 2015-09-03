package com.csgroup.eventsched;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class RetrieveNewEventsService extends Service {
    public static boolean isRunning = false;
    private boolean retrievedFirst = false;
    private Timer timer;
    private TimerTask doAsynchronousTask;
    private Handler handler;
    private Runnable runnable = new Runnable() {
        public void run() {
            try {
                FetchNewEvents fetchNewEvents = new FetchNewEvents();

                fetchNewEvents.execute();
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
        }
    };

    public RetrieveNewEventsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Toast.makeText(this, "service oncreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.runService();
        return START_STICKY;
    }

    private void runService() {
        if (!isRunning) {
//            Toast.makeText(this, "Starting service", Toast.LENGTH_SHORT).show();
            isRunning = true;
            callAsynchronousTask();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

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
//        Toast.makeText(this, "service onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private void callAsynchronousTask() {
        handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        };

        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 5000 ms
    }

    private class FetchNewEvents extends AsyncTask<Void, Void, String> {
        private final String LOG_TAG = FetchNewEvents.class.getSimpleName();
        private final String ROUTE = "events";
        @Override
        protected String doInBackground(Void... params) {
            HTTPManager httpManager = new HTTPManager();
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization",
                    new PreferencesManager(RetrieveNewEventsService.this, null)
                            .getApiKey());

            String jsonResponse = httpManager.get(ROUTE, header, null);
            Log.v(LOG_TAG, "GetEvents jsonResponse: " + jsonResponse);

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                try {
                    List<Event> eventsList = JsonParser.parseEvents(jsonResponse);
                    if (eventsList != null) {
                        List<Event> newEvents = checkNewEvents(eventsList);

                        // if there are new events and this is not the first time to fetch events,
                        // notify the user about the new events
                        if (!newEvents.isEmpty() && retrievedFirst) {
                            notifyNewEvents(newEvents);
                        }
                        else {
                            retrievedFirst = true;
                        }

                        // store events ids in prefs
                        PreferencesManager pm = new PreferencesManager(
                                RetrieveNewEventsService.this, null);
                        pm.setEventsIds(eventsList);
                    }

                } catch (JsonParser.JsonParserException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }

        private List<Event> checkNewEvents(List<Event> eventsList) {
            PreferencesManager preferencesManager = new PreferencesManager(
                    RetrieveNewEventsService.this, null);
            Set<Integer> idSet = preferencesManager.getEventsIds();


            List<Event> newEvents = new ArrayList<>();

            // if nothing was stored before in prefs
            if (idSet == null) {
                newEvents.addAll(eventsList);
            }
            else {
                for (Event ev : eventsList) {
                    if (!idSet.contains(ev.getId())) {
                        newEvents.add(ev);
                    }
                }
            }
           return newEvents;
        }

        private void notifyNewEvents(List<Event> newEvents) {
            Context context = RetrieveNewEventsService.this;
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);



            int userId = Integer.parseInt(
                    new PreferencesManager(context, null).getUserId());

            for (Event ev : newEvents) {
                if (ev.getOwner_id() != userId) {
                    Intent intent = new Intent(context, EventDetailsActivity.class);
                    intent.putExtra("event", ev);

                    // to navigate back to parent activity from the notification's activity
//                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                    stackBuilder.addParentStack(EventDetailsActivity.class);
//
//                    stackBuilder.addNextIntent(intent);

//                    PendingIntent pIntent = stackBuilder.getPendingIntent(ev.getId(), 0);
                    PendingIntent pIntent = PendingIntent.getActivity(context, ev.getId(), intent, 0);
                    Notification notification = new NotificationCompat.Builder(context)
                            .setContentTitle(ev.getTitle())
                            .setContentText(ev.getDetails())
                            .setSmallIcon(R.mipmap.calendar)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .build();

                    notificationManager.notify(ev.getId(), notification);
                }
            }
        }
    }
}
