package com.csgroup.eventsched;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 8/17/2015.
 */
public class JsonParser {



    private static final String API_ERROR = "error";
    private static final String API_ERROR_MESSAGE = "message";
    private static final String API_MESSAGE = "message";
    private static final String API_EVENTS_ARRAY = "events";
    private static final String API_EVENT = "event";
    private static final String API_MEMBERS_ARRAY = "members";
    private static final String API_COMMENTS_ARRAY = "comments";
    private static final String API_COMMENT = "comment";
    private static final String API_FREE_TIMES_ARRAY = "free_times";

    public static class JsonParserException extends Exception {
        public JsonParserException(String detailMessage) {
            super(detailMessage);
        }
    }


    /**
     * parses response from /eventsched/v1/login
     * response example:
     * {
     *  error: false
     *  id: 4
     *  name: "someone"
     *  email: "someone@gmail.com"
     *  gender: "M"
     *  key: "31b6e148cc7f2e7af07acdf0c12f83f7"
     * }
     * @param jsonStr response JSON string
     * @return User contains data about the user who logged in
     */
    public static User parseLogin(String jsonStr) throws JsonParserException {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            // if response has an error message, throw JsonParserException with that message
            checkError(jsonObject);
            int id = jsonObject.getInt(User.API_ID);
            String name = jsonObject.getString(User.API_NAME);
            String email = jsonObject.getString(User.API_EMAIL);
            String gender = jsonObject.getString(User.API_GENDER);
            String key = jsonObject.getString(User.API_KEY);

            return new User(id, name, email, gender, key);

        } catch (JSONException e) {
            // failed to parse json string
            e.printStackTrace();
            return null;
        }
    }

    /**
     * parses response from /eventsched/v1/events
     * response example:
     * {
     *   "error":false,
     *    "events":[
     *         {"id":"3","owner_id":"6","event":"meeting with supervisor",
     *           "details":"a detailed message for the even goes here....",
     *           "location":"somewhere","start_time":"1272509157","duration":"60",
     *           "last_update_time":null},
     *         {"id":"4","owner_id":"6",...}
     *    ]
     * }
     * @param jsonStr response JSON string
     * @return Event List contains data about the events
     *         for the user who made the request
     */
    public static List<Event> parseEvents(String jsonStr) throws JsonParserException {

        List<Event> eventsList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            checkError(jsonObject);

            JSONArray eventsJsonArray = jsonObject.getJSONArray(API_EVENTS_ARRAY);
            for (int i = 0; i < eventsJsonArray.length(); i++) {
                JSONObject eventJson = eventsJsonArray.getJSONObject(i);
                Event event = helperParseEvent(eventJson);
                eventsList.add(event);
            }
            return  eventsList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * parses response from /eventsched/v1/priv
     * response example:
     * {
     *   "error":false,
     *    "members":[
     *         {"id":"3","name":"someone","email":"someone@gmail.com",
     *           "gender":"M"},
     *         {"id":"4","name":"someone2",...}
     *    ]
     * }
     * @param jsonStr response JSON string
     * @return Members List contains data about members whom the current user
     *          has privileges on
     */
    public static List<Member> parsePrivileges(String jsonStr) throws JsonParserException{
        List<Member> membersList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            checkError(jsonObject);

            JSONArray membersJsonArray = jsonObject.getJSONArray(API_MEMBERS_ARRAY);
            for (int i = 0; i < membersJsonArray.length(); i++) {
                JSONObject memberJson = membersJsonArray.getJSONObject(i);

                int id = memberJson.getInt(Member.API_ID);
                String name = memberJson.getString(Member.API_NAME);
                String email = memberJson.getString(Member.API_EMAIL);
                String gender = memberJson.getString(Member.API_GENDER);

                membersList.add(
                        new Member(id, name, email, gender)
                );
            }
            return membersList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Event parseCreateEvent(String jsonStr) throws JsonParserException{
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            checkError(jsonObject);

            JSONObject eventJson = jsonObject.getJSONObject(API_EVENT);
            return helperParseEvent(eventJson);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Comment> parseComments( String jsonStr) throws JsonParserException {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            checkError(jsonObject);

            JSONArray commentsJsonArray = jsonObject.getJSONArray(API_COMMENTS_ARRAY);

            List<Comment> commentsList = new ArrayList<>();
            for (int i = 0; i < commentsJsonArray.length(); i++) {
                JSONObject commentJson = commentsJsonArray.getJSONObject(i);

                Comment comment = helperParseComment(commentJson);
                commentsList.add(comment);
            }
            return commentsList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Comment parseAddComment(String jsonStr) throws JsonParserException{
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            checkError(jsonObject);

            JSONObject commentJson = jsonObject.getJSONObject(API_COMMENT);
            return helperParseComment(commentJson);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<FreeTime> parseFreeTimes( String jsonStr) throws JsonParserException {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            checkError(jsonObject);

            JSONArray freeTimesJsonArray = jsonObject.getJSONArray(API_FREE_TIMES_ARRAY);

            List<FreeTime> freeTimesList = new ArrayList<>();
            for (int i = 0; i < freeTimesJsonArray.length(); i++) {
                JSONObject freeTimeJson = freeTimesJsonArray.getJSONObject(i);

                FreeTime freeTime = helperParseFreeTime(freeTimeJson);
                freeTimesList.add(freeTime);
            }
            return freeTimesList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }


    private static Comment helperParseComment(JSONObject commentJson) throws JSONException {
        int id = commentJson.getInt(Comment.API_ID);
        int eventId = commentJson.getInt(Comment.API_EVENT_ID);
        int authorId = commentJson.getInt(Comment.API_AUTHOR_ID);
        String content = commentJson.getString(Comment.API_CONTENT);
        String authorName = commentJson.getString(Comment.API_AUTHOR_NAME);
        Long createdAtTimestamp = commentJson.getLong(Comment.API_CREATED_AT);

        return new Comment(id, authorId, eventId, content,
                authorName, createdAtTimestamp);
    }

    private static FreeTime helperParseFreeTime(JSONObject freeTimeJson) throws JSONException {

        long startTimeStamp = freeTimeJson.getLong(FreeTime.API_START_TIMESTAMP);
        int timesFit = freeTimeJson.getInt(FreeTime.API_TIMES_FIT);

        return new FreeTime(timesFit, startTimeStamp);

    }

    private static Event helperParseEvent(JSONObject eventJson) throws JSONException{
        int id = eventJson.getInt(Event.API_ID);
        int owner_id = eventJson.getInt(Event.API_OWNER_ID);
        int duration = eventJson.getInt(Event.API_DURATION);
        String title = eventJson.getString(Event.API_TITLE);
        String details = eventJson.getString(Event.API_DETAILS);
        String location = eventJson.getString(Event.API_LOCATION);
        long start_time = eventJson.getLong(Event.API_START_TIMESTAMP);

        return new Event(id, owner_id, title, details,
                location, start_time, duration);
    }

    private static void checkError(JSONObject jsonObject) throws JSONException, JsonParserException {
        if (containsError(jsonObject)) {
            JsonParserException e = new JsonParserException(getErrorMessage(jsonObject));
            throw e;
        }
    }


    private static boolean containsError(JSONObject jsonObject) throws JSONException{
        return jsonObject.getBoolean(API_ERROR);
    }

    private static String getErrorMessage(JSONObject jsonObject) throws JSONException{
        return jsonObject.getString(API_ERROR_MESSAGE);
    }


}
