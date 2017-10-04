package com.kabam.kabam;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Ayush on 11/21/15.
 */
@ParseClassName("Post")
public class Post extends ParseObject {

    public Post() {

    }

    public String getMessage() {
        return getString("message");
    }

    public ParseUser getAuthor() {
        return (ParseUser)get("author");
    }

    public Class getPostClass() {
        return (Class)get("class");
    }
}
