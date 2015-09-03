package com.csgroup.eventsched;

/**
 * Created by pc on 8/24/2015.
 */
public class Member {
    public static final String API_NAME = "name";
    public static final String API_EMAIL = "email";
    public static final String API_GENDER = "gender";
    public static final String API_ID = "id";


    private int id;
    private String name;
    private String email;
    private String gender;


    public Member(int id, String name, String email, String gender) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        String res = "name: " + this.name
                + ", email: " + this.email
                + ", gender: " + this.gender;
        return res;
    }
}
