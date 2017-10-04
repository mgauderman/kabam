package com.kabam.kabam;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.Set;

/**
 * Created by Ayush on 11/19/15.
 */
@ParseClassName("Class")
public class Class extends ParseObject {


    public Class() {

    }

    public String getClassTitle() {
        return getString("title");
    }

    public String getEnrollCount() {
        int enrolled = (getNumber("enrolled") == null) ? 0 : getNumber("enrolled").intValue();
        return enrolled + ((enrolled > 1) ? " Students" : " Student") + " Enrolled";
    }

    public String getLocation() {
        return getString("location");
    }

    public Date getTime() {
        return getDate("time");
    }

    public String getDescription() {
        return getString("description");
    }

    public Set<Event> getEvents() {
        return (Set<Event>)get("events");
    }

    public Set<Post> getPosts() {
        return (Set<Post>)get("posts");
    }

    public void enrollStudent(ParseUser student) {
        put("enrolled", getNumber("enrolled").intValue() + 1);
    }

}
