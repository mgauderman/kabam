package com.kabam.kabam;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.Set;

/**
 * Created by Ayush on 11/21/15.
 */
@ParseClassName("Event")
public class Event extends ParseObject {

    public Event() {

    }

    public String getEventTitle() {
        return getString("title");
    }

    public Date getTime() {
        return getDate("time");
    }

    public String getLocation() {
        return getString("location");
    }

    public String getDescription() {
        return getString("description");
    }

    public Class getEventClass() {
        return (Class)get("class");
    }

    public Set<ParseUser> getAttendingUsers() {
        return (Set<ParseUser>)get("attending");
    }
}
