package com.csgroup.eventsched;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.csgroup.eventsched.Event.EventHash;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventsFragment extends Fragment {

    private ListView mEventsListView;
    private SimpleAdapter mAdapter;

    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);
        this.mEventsListView = (ListView) rootView.findViewById(R.id.listview_events);
        mEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> eventHash = (HashMap<String, String>) mAdapter.getItem(position);
                Event ev = EventHash.getEventFromHash(eventHash);

                Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                intent.putExtra("event", ev);
                startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        GetEventsTask task = new GetEventsTask();
        task.execute();
    }

    private void updateEvents(List<Event> events) {

        List<HashMap<String, String>> listValues = new ArrayList<>();
        for (Event ev : events) {
            HashMap<String, String> eventHashMap =
                    EventHash.getHashFromEvent(ev);
            listValues.add(eventHashMap);
        }

        String [] from = new String[] {
                EventHash.KEY_TITLE,
                 EventHash.KEY_DATE,
                EventHash.KEY_TIME
        };


        int [] to = new int[] {R.id.tvEventTitle, R.id.tvEventDate, R.id.tvEventTime};

        mAdapter = new SimpleAdapter(getActivity(), listValues,
                R.layout.list_item_event, from, to);

        mEventsListView.setAdapter(mAdapter);

    }

    private class GetEventsTask  extends AsyncTask<Void, Void, String> {
        private String LOG_TAG = GetEventsTask.class.getSimpleName();
        private String ROUTE = "events";

        @Override
        protected String doInBackground(Void... params) {
            HTTPManager httpManager = new HTTPManager();
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization",
                    new PreferencesManager(getActivity(), null)
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
                        updateEvents(eventsList);
                    }

                } catch (JsonParser.JsonParserException e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    }


}
