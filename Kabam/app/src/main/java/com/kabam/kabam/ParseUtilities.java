package com.kabam.kabam;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Ayush on 11/21/15.
 */
public class ParseUtilities {

    private static HashMap<String, ParseUser> allUsers;
    private static HashMap<String, Class> allClasses;
    private static HashMap<String, Event> allEvents;
    private static HashMap<String, ParseObject> allConversations;

    public static void updateAllUsers() {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.setLimit(1000);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> results, ParseException e) {
                if (e == null) {
                    allUsers = new HashMap<>();
                    for (int i = 0; i < results.size(); i++) {
                        allUsers.put(results.get(i).getObjectId(), results.get(i));
                    }
                }
            }
        });
    }

    public static boolean isGhost(String id) {
        if (allUsers.get(id) == null) return true;
        return allUsers.get(id).getBoolean("ghost");
    }

    public static Set getUserList() {
        return allUsers.keySet();
    }

    public static void updateAllClasses() {
        ParseQuery<Class> classQuery = ParseQuery.getQuery("Class");
        classQuery.setLimit(1000);
        classQuery.findInBackground(new FindCallback<Class>() {
            @Override
            public void done(List<Class> results, ParseException e) {
                if (e == null) {
                    allClasses = new HashMap<>();
                    for (int i = 0; i < results.size(); i++) {
                        allClasses.put(results.get(i).getObjectId(), results.get(i));
                    }
                }
            }
        });
    }

    public static Event getEvent(String eventId) {
        return allEvents.get(eventId);
    }

    public static void updateAllEvents() {
        ParseQuery<Event> classQuery = ParseQuery.getQuery("Event");
        classQuery.setLimit(1000);
        classQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> results, ParseException e) {
                if (e == null) {
                    allEvents = new HashMap<>();
                    for (int i = 0; i < results.size(); i++) {
                        allEvents.put(results.get(i).getObjectId(), results.get(i));
                    }
                }
            }
        });
    }

    // use allConversations.get(<layerID>).get("title");
    public static void updateAllConversations() {
        ParseQuery<ParseObject> classQuery = ParseQuery.getQuery("Conversation");
        classQuery.setLimit(1000);
        classQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> results, ParseException e) {
                if (e == null) {
                    allConversations = new HashMap<>();
                    for (int i = 0; i < results.size(); i++) {
                        allConversations.put(results.get(i).getString("conversationId"), results.get(i));
                    }
                }
            }
        });
    }

    public static String getName(String userID) {
        return allUsers.get(userID).get("first_name") + " " + allUsers.get(userID).get("last_name");
    }

    public static ParseUser getUser(String userID) {
        return allUsers.get(userID);
    }

    public static Class getClass(String classID) {
        return allClasses.get(classID);
    }

    public static ParseObject getConversation(String layerID) {
        return allConversations.get(layerID);
    }

    public static void addConversationToParse(String conversationId, String title, String classId) {
        ParseObject conversation = new ParseObject("Conversation");
        conversation.put("conversationId", conversationId);
        conversation.put("title", title);
        conversation.put("class", getClass(classId));
        conversation.saveInBackground();
    }

    public static void enrollStudentInClass(Class currentClass) {
        ParseRelation<Class> enrolled = ParseUser.getCurrentUser().getRelation("enrolled");
        enrolled.add(currentClass);
        currentClass.increment("enrolled");
        ParseUser.getCurrentUser().saveInBackground();
        currentClass.saveInBackground();
    }
}
