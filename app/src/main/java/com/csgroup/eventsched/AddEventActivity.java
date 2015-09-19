package com.csgroup.eventsched;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;


public class AddEventActivity extends MenuActivity {

    public String timeFromStr, timeToStr, dateFromStr, dateToStr;

    private Button btnFilterOptions, btnCreate, btnCancel, btnRefreshSuggestions;
    private EditText etEventTitle, etEventLocation, etEventDetails,
            etDurationHours, etDurationMinutes;
    private ViewGroup containerMembers;
    private Spinner spinTime;
    private List<Member> mMembers;

    private List<Member> mSelectedMembers;
//    private Long mSelectedTimeStamp = null;
    private Long mSelectedUnixTimeStamp;
    private String eventTitle, eventLocation, eventDetails;
    private int durationHours, durationMinutes, durationTotalMinutes;

    private List<Long> freeTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        btnFilterOptions = (Button) findViewById(R.id.btnFilterOptions);
        btnFilterOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterDialog filterDialog = new FilterDialog(AddEventActivity.this);
                filterDialog.show();
            }
        });

        btnCreate = (Button) findViewById(R.id.btnEventCreate);
        btnCancel = (Button) findViewById(R.id.btnEventCancel);
        btnRefreshSuggestions = (Button) findViewById(R.id.btnRefreshSuggestions);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    CreateEventTask createEventTask = new CreateEventTask();
                    createEventTask.execute();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnRefreshSuggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSuggestions();
            }
        });

        etEventTitle = (EditText) findViewById(R.id.etEventTitle);
        etEventLocation = (EditText) findViewById(R.id.etEventLocation);
        etEventDetails = (EditText) findViewById(R.id.etEventDetails);
        etDurationHours = (EditText) findViewById(R.id.etDurationHours);
        etDurationMinutes = (EditText) findViewById(R.id.etDurationMinutes);

        spinTime = (Spinner) findViewById(R.id.spinTime);
        // TODO Remove this
//        this.tempFillSpinner();

        containerMembers = (ViewGroup) findViewById(R.id.containerMembers);
        containerMembers.setVisibility(View.GONE);

        mSelectedMembers = new ArrayList<>();
        GetPrivilegesTask getPrivilegesTask = new GetPrivilegesTask();
        getPrivilegesTask.execute();
    }

    public void refreshSuggestions() {
        GregorianCalendar cal = new GregorianCalendar();
        long currentTimeMillis = System.currentTimeMillis();
        cal.setTimeInMillis(currentTimeMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date dateFrom, dateTo;
        if (dateFromStr != null && !dateFromStr.isEmpty()) {
            try {
                dateFrom = formatter.parse(dateFromStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            dateFrom = cal.getTime();
            dateFromStr = formatter.format(dateFrom);
        }

        if (dateToStr != null && !dateToStr.isEmpty()) {
            try {
                dateTo = formatter.parse(dateToStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 7);
            dateTo = cal.getTime();
            dateToStr = formatter.format(dateTo);
        }

        if (timeFromStr == null || timeFromStr.isEmpty()) {
            timeFromStr = "00:00:00";
        }

        if (timeToStr == null || timeToStr.isEmpty()) {
            timeToStr = "23:59:00";
        }



        calcDuration();
        if (durationTotalMinutes <= 0) {
            Toast.makeText(this, "Please Choose a suitable duration", Toast.LENGTH_SHORT).show();
            return;
        }


        RefreshTimesTask refreshTimesTask = new RefreshTimesTask();
        refreshTimesTask.execute();

    }

    private boolean validateFields() {
        eventTitle = etEventTitle.getText().toString();
        if (eventTitle.isEmpty()) {
            Toast.makeText(this, "Please Fill in Title", Toast.LENGTH_SHORT).show();
            return false;
        }

        eventLocation = etEventLocation.getText().toString();
        if (eventLocation.isEmpty()) {
            Toast.makeText(this, "Please Fill in Location", Toast.LENGTH_SHORT).show();
            return false;
        }

        eventDetails = etEventDetails.getText().toString();
        if (eventDetails.isEmpty()) {
            Toast.makeText(this, "Please Fill in Event Details", Toast.LENGTH_SHORT).show();
            return false;
        }

        calcDuration();

        if (durationTotalMinutes <= 0) {
            Toast.makeText(this, "Please Choose a suitable duration", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mSelectedUnixTimeStamp == null) {
            Toast.makeText(this, "Please Choose a suitable Time", Toast.LENGTH_SHORT).show();
            return false;
        }

        // TODO fix this after dynamic fill
//        mSelectedUnixTimeStamp = mSelectedTimeStamp;
        return true;
    }

    private void calcDuration() {
        String durationHoursStr = etDurationHours.getText().toString();
        durationHours = durationHoursStr.isEmpty() ? 0 : Integer.parseInt(durationHoursStr);

        String durationMinutesStr = etDurationMinutes.getText().toString();
        durationMinutes = durationMinutesStr.isEmpty() ? 0 : Integer.parseInt(durationMinutesStr);

        durationTotalMinutes = durationMinutes + durationHours * 60;
    }

    private void fillFreeTimesSpinner(List<FreeTime> freeTimesList) {
//        this.freeTimes = freeTimesList;
        this.freeTimes = new ArrayList<>();
        DateManager dateManager = new DateManager();

        ArrayList<String> arraySpinner = new ArrayList<>();
        for (FreeTime freeTime : freeTimesList) {
//            arraySpinner.add(freeTime.getTimeString());
//            if (arraySpinner.size() > 15) {
//                break;
//            }
            for (int i = 0; i < freeTime.getRepetitionsCount() && i < 4; i++) {
                Long timestamp = freeTime.getStartTimeStamp() + i * durationTotalMinutes * 60;
                this.freeTimes.add(timestamp);
                dateManager.setTimeStamp(timestamp);
                arraySpinner.add(dateManager.getReadableDayDateTimeString());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arraySpinner);
        spinTime.setAdapter(adapter);
        spinTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DateManager dateManager = new DateManager();

                AddEventActivity.this.mSelectedUnixTimeStamp =
                        AddEventActivity.this.freeTimes.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private void displayMembersList() {
        mSelectedMembers = new ArrayList<>();

        if (mMembers.size() == 0) {
            containerMembers.setVisibility(View.GONE);
        }
        else {
            containerMembers.setVisibility(View.VISIBLE);
            MembersAdapter adapter = new MembersAdapter(this, R.layout.list_item_member, mMembers);
            ListView membersListView = (ListView) findViewById(R.id.listview_members);
            membersListView.setAdapter(adapter);
        }
    }



    private class MembersAdapter extends ArrayAdapter<Member> {
        private ArrayList<MemberHolder> mMembersList;
        private final String LOG_TAG = MembersAdapter.class.getSimpleName();

        public MembersAdapter(Context context, int resource, List<Member> members) {
            super(context, resource, members);
            mMembersList = new ArrayList<>();
            for (Member m : members) {
                mMembersList.add(new MemberHolder(m, false));
            }

        }

        private class MemberHolder {
            Member member;
            boolean isChecked;

            public MemberHolder(Member member, boolean isChecked) {
                this.member = member;
                this.isChecked = isChecked;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            CheckBox cbMember = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_member, null, false);

                cbMember = (CheckBox) convertView.findViewById(R.id.cbMember);
                convertView.setTag(cbMember);
                cbMember.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cbMember = (CheckBox) v;
                        MemberHolder memberHolder = (MemberHolder) cbMember.getTag();
                        if (cbMember.isChecked()) {
                            mSelectedMembers.add(memberHolder.member);
                            memberHolder.isChecked = true;
                        } else {
                            mSelectedMembers.remove(memberHolder.member);
                            memberHolder.isChecked = false;
                        }
                        Log.v(LOG_TAG, "Selected Members: " +  mSelectedMembers.toString());
                    }
                });
            }
            else {
                cbMember = (CheckBox) convertView.getTag();
            }

            MemberHolder memberHolder = this.mMembersList.get(position);
            Member member = memberHolder.member;
            cbMember.setText(member.getName() + " (" + member.getEmail() + ")");
            cbMember.setTag(memberHolder);
            cbMember.setChecked(memberHolder.isChecked);

            return convertView;
        }
    }

    private class RefreshTimesTask extends AsyncTask<Void, Void, String> {
        private final String LOG_TAG = RefreshTimesTask.class.getSimpleName();
        private final String ROUTE = "freetimes";

        private final String QUERY_MEMBERS = "members";
        private final String QUERY_DATE_START = "date_start";
        private final String QUERY_DATE_END = "date_end";
        private final String QUERY_TIME_START = "time_start";
        private final String QUERY_TIME_END = "time_end";
        private final String QUERY_DURATION = "duration";


        @Override
        protected String doInBackground(Void... params) {
            HTTPManager httpManager = new HTTPManager();
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization",
                    new PreferencesManager(AddEventActivity.this, null)
                            .getApiKey());

            String membersStr = constructMembersString();
            HashMap<String, String> queryParams = new HashMap<>();
            if (!membersStr.isEmpty()) {
                queryParams.put(QUERY_MEMBERS, membersStr);
            }
            queryParams.put(QUERY_DATE_START, dateFromStr);
            queryParams.put(QUERY_DATE_END, dateToStr);
            queryParams.put(QUERY_TIME_START, timeFromStr);
            queryParams.put(QUERY_TIME_END, timeToStr);
            queryParams.put(QUERY_DURATION, Integer.toString(durationTotalMinutes));
//            if (!membersStr.isEmpty()) {
//                queryParams.put(PAYLOAD_MEMBERS, membersStr);
//            }

            String jsonResponse = httpManager.get(ROUTE, header, queryParams);
            Log.v(LOG_TAG, "RefreshTimes jsonResponse: " + jsonResponse);

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                try {
                    List<FreeTime> freeTimesList = JsonParser.parseFreeTimes(jsonResponse);
                    if (freeTimesList != null) {
                        fillFreeTimesSpinner(freeTimesList);
                        Toast.makeText(AddEventActivity.this,
                                "Suggested times refreshed", Toast.LENGTH_SHORT).show();
                    }

                } catch (JsonParser.JsonParserException e) {
                    Toast.makeText(AddEventActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class GetPrivilegesTask extends AsyncTask<Void, Void, String> {
        private final String LOG_TAG = GetPrivilegesTask.class.getSimpleName();
        private final String ROUTE = "priv";

        @Override
        protected String doInBackground(Void... params) {
            HTTPManager httpManager = new HTTPManager();
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization",
                    new PreferencesManager(AddEventActivity.this, null)
                            .getApiKey());

            String jsonResponse = httpManager.get(ROUTE, header, null);
            Log.v(LOG_TAG, "GetPrivileges jsonResponse: " + jsonResponse);

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                try {
                    List<Member> membersList = JsonParser.parsePrivileges(jsonResponse);
                    if (membersList != null) {
                        AddEventActivity.this.mMembers = membersList;
                        AddEventActivity.this.displayMembersList();
                    }

                } catch (JsonParser.JsonParserException e) {
                    Toast.makeText(AddEventActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    }

    private class CreateEventTask extends AsyncTask<Void, Void, String> {
        private final String LOG_TAG = CreateEventTask.class.getSimpleName();
        private final String ROUTE = "events";

        private final String PAYLOAD_TITLE = "title";
        private final String PAYLOAD_START_TIME = "start_time";
        private final String PAYLOAD_DURATION = "duration";
        private final String PAYLOAD_LOCATION = "location";
        private final String PAYLOAD_DETAILS = "details";
        private final String PAYLOAD_MEMBERS = "members";

        @Override
        protected String doInBackground(Void... params) {
            HTTPManager httpManager = new HTTPManager();
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization",
                    new PreferencesManager(AddEventActivity.this, null)
                            .getApiKey());

            String membersStr = constructMembersString();

            HashMap<String, String> payload = new HashMap<>();
            payload.put(PAYLOAD_TITLE, eventTitle);
            payload.put(PAYLOAD_START_TIME, mSelectedUnixTimeStamp.toString());
            payload.put(PAYLOAD_DURATION, Integer.toString(durationTotalMinutes));
            payload.put(PAYLOAD_LOCATION, eventLocation);
            payload.put(PAYLOAD_DETAILS, eventDetails);
            if (!membersStr.isEmpty()) {
                payload.put(PAYLOAD_MEMBERS, membersStr);
            }

            String jsonResponse = httpManager.post(ROUTE, header, payload);
            Log.v(LOG_TAG, "CreateEvent jsonResponse: " + jsonResponse);

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                try {
                    Event event = JsonParser.parseCreateEvent(jsonResponse);
                    if (event != null) {
                        Toast.makeText(AddEventActivity.this, "Event Created Successfully!", Toast.LENGTH_LONG)
                                .show();

                        Intent intent = new Intent(AddEventActivity.this,
                                EventDetailsActivity.class);
                        intent.putExtra("event", event);
                        startActivity(intent);

                        finish();
                    }

                } catch (JsonParser.JsonParserException e) {
                    Toast.makeText(AddEventActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }

    }

    private String constructMembersString() {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (Member member : mSelectedMembers) {
            if (first) {
                first = false;
            }
            else {
                buffer.append(',');
            }

            buffer.append(member.getId());
        }
        return buffer.toString();

    }
}
