package com.csgroup.eventsched;

/**
 * Created by pc on 8/17/2015.
 */
public class User extends Member {
    public static final String API_KEY = "key";


    private String key;

    public User(int id, String name, String email, String gender, String key) {
        super(id, name, email, gender);
        this.key = key;
    }


    public String getApiKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        String res = super.toString()
                + ", key: " + this.key;
        return res;
    }

}
