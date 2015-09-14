package com.csgroup.eventsched;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsFragment extends Fragment implements TaskProvider {

    private ListView commentsListView;
    private SimpleAdapter adapter;
    private Button btnCommentSend;
    private EditText etCommentbody;
    private EventDetailsActivity mActivity;

    private List<Comment> commentsList;
    private List<Comment> oldCommentsList = new ArrayList<>();

    private PeriodicAsyncTask<GetCommentsTask> periodicCommentsRefresh;

    public CommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (EventDetailsActivity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (periodicCommentsRefresh == null) {
            periodicCommentsRefresh =
                    new PeriodicAsyncTask<>(this);
        }
        periodicCommentsRefresh.start(5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        periodicCommentsRefresh.stop();
    }

    @Override
    public AsyncTask<Void, ?, ?> getTask(int num) {
        return new GetCommentsTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comments, container, false);
        commentsListView = (ListView) rootView.findViewById(R.id.listview_comments);


        btnCommentSend = (Button) rootView.findViewById(R.id.btnCommentSend);
        btnCommentSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentBody = etCommentbody.getText().toString();
                if (commentBody.isEmpty()) {
                    Toast.makeText(mActivity, "Please Enter a Comment", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                AddCommentTask addCommentTask = new AddCommentTask();
                addCommentTask.execute(new String[]{commentBody});
            }
        });
        etCommentbody = (EditText) rootView.findViewById(R.id.etCommentBody);


        return rootView;
    }

    private void updateComments() {

        if (commentsList.size() != oldCommentsList.size()) {
            List<HashMap<String, String>> listValues = new ArrayList<>();
            for (Comment comment : commentsList) {
                HashMap<String, String> commentHashMap = new HashMap<>();
                commentHashMap.put("author_name", comment.getAuthorName());
                commentHashMap.put("created_at", comment.getDateTimeString());
                commentHashMap.put("content", comment.getContent());

                listValues.add(commentHashMap);
            }

            String[] from = new String[]{
                    "author_name", "created_at", "content"
            };
            int[] to = new int[]{
                    R.id.tvAuthorName, R.id.tvDate, R.id.tvCommentBody
            };

            adapter = new SimpleAdapter(getActivity(), listValues,
                    R.layout.list_item_comment, from, to);

            commentsListView.setAdapter(adapter);
            setListViewHeightBasedOnChildren(commentsListView);

            oldCommentsList.clear();
            oldCommentsList.addAll(commentsList);
        }
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    private class GetCommentsTask extends AsyncTask<Void, Void, String> {
        private final String QUERY_EVENT_ID = "event_id";

        private final String LOG_TAG = GetCommentsTask.class.getSimpleName();
        private final String ROUTE = "comments";

        // constructor to solve instantiation problem in PeriodicAsyncTask
        public GetCommentsTask() {}

        @Override
        protected String doInBackground(Void... params) {
            HTTPManager httpManager = new HTTPManager();
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization",
                    new PreferencesManager(getActivity(), null)
                            .getApiKey());

            HashMap<String, String> queryParamsMap = new HashMap<>();
            String eventIdStr = Integer.toString(mActivity.getEvent().getId());
            queryParamsMap.put(QUERY_EVENT_ID, eventIdStr);

            String jsonResponse = httpManager.get(ROUTE, header, queryParamsMap);
            Log.v(LOG_TAG, "GetComments jsonResponse: " + jsonResponse);
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            if (jsonStr != null) {
                try {
                    List<Comment> commentsList = JsonParser.parseComments(jsonStr);

                    if (commentsList != null) {
                        CommentsFragment.this.commentsList = commentsList;
                        CommentsFragment.this.updateComments();
                    }

                } catch (JsonParser.JsonParserException e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class AddCommentTask extends AsyncTask<String, Void, String> {
        private final String PAYLOAD_EVENT_ID = "event_id";
        private final String PAYLOAD_CONTENT = "content";
        private final String LOG_TAG = AddCommentTask.class.getSimpleName();
        private final String ROUTE = "comments";

        @Override
        protected String doInBackground(String... params) {
            String content = params[0];

            HTTPManager httpManager = new HTTPManager();
            HashMap<String, String> header = new HashMap<>();
            header.put("Authorization",
                    new PreferencesManager(getActivity(), null)
                            .getApiKey());

            HashMap<String, String> payload = new HashMap<>();
            String eventIdStr = Integer.toString(mActivity.getEvent().getId());

            payload.put(PAYLOAD_EVENT_ID, eventIdStr);
            payload.put(PAYLOAD_CONTENT, content);

            String jsonResponse = httpManager.post(ROUTE, header, payload);
            Log.v(LOG_TAG, "AddComment jsonResponse: " + jsonResponse);

            return jsonResponse;

        }

        @Override
        protected void onPostExecute(String jsonStr) {
            if (jsonStr != null) {
                try {
                    Comment comment = JsonParser.parseAddComment(jsonStr);
                    if (comment != null) {
                        Toast.makeText(mActivity, "Comment Posted", Toast.LENGTH_SHORT)
                                .show();
                        CommentsFragment.this.commentsList.add(0, comment);
                        CommentsFragment.this.updateComments();
                        etCommentbody.setText("");
                    }

                } catch (JsonParser.JsonParserException e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    }


}
