package com.kabam.kabam;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.layer.sdk.query.Predicate;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.kabam.kabam.Adapters.ConversationQueryAdapter;
import com.kabam.kabam.Adapters.QueryAdapter;
import com.kabam.kabam.Layer.LayerImpl;
import com.layer.sdk.messaging.Conversation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Alex on 11/19/15.
 */
public class ClassDetail extends FragmentBase implements ConversationQueryAdapter.ConversationClickHandler {

    private Class selectedClass;
    private EventQueryAdapter classEvents;

    private ConversationQueryAdapter mConversationsAdapter;
    private View view;
    private Boolean enrolled = false;
    private ListView classDetailList;
    private View enrollButton, eventButton, addEventButton, chatButton, addChatButton;

    private void notLoggedIn() {
        enrollButton.setVisibility(View.GONE);
        eventButton.setVisibility(View.GONE);
        addEventButton.setVisibility(View.GONE);
        chatButton.setVisibility(View.GONE);
        addChatButton.setVisibility(View.GONE);
        classDetailList.setVisibility(View.GONE);
    }

    private void notEnrolled() {
        enrollButton.setVisibility(View.VISIBLE);
        eventButton.setVisibility(View.GONE);
        addEventButton.setVisibility(View.GONE);
        chatButton.setVisibility(View.GONE);
        addChatButton.setVisibility(View.GONE);
        classDetailList.setVisibility(View.GONE);
    }

    private void enrolled() {
        enrollButton.setVisibility(View.GONE);
        classDetailList.setVisibility(View.VISIBLE);
        eventButton.setVisibility(View.VISIBLE);
        addEventButton.setVisibility(View.VISIBLE);
        chatButton.setVisibility(View.VISIBLE);
        addChatButton.setVisibility(View.VISIBLE);
    }

    private void refreshButtons() {
        if (ParseUser.getCurrentUser() != null) {
            if (enrolled) {
                enrolled();
            } else {
                notEnrolled();
                enrollButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ParseUtilities.enrollStudentInClass(selectedClass);
                        ((TextView)getView().findViewById(R.id.classEnrolled)).setText(selectedClass.getEnrollCount());
                        enrolled = true;
                        refreshButtons();
                    }
                });
            }
        } else {
            notLoggedIn();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.class_detail, container, false);

        ParseUtilities.updateAllConversations();

        LayerImpl.initialize(this.getActivity().getApplicationContext());
        LayerImpl.setContext(this);
        ParseUtilities.updateAllEvents();

        if (LayerImpl.isAuthenticated()){
            Log.d("Message", "User is already authenicated");
        } else {
            //User is logged into Parse, so start the Layer Authentication process
            LayerImpl.authenticateUser();
        }

        classDetailList = (ListView) view.findViewById(R.id.classDetailList);
        enrollButton = view.findViewById(R.id.enrollButton);
        eventButton = view.findViewById(R.id.eventButton);
        addEventButton = view.findViewById(R.id.addEventButton);
        chatButton = view.findViewById(R.id.chatButton);
        addChatButton = view.findViewById(R.id.addChatButton);

        if (getArguments() != null) {
            String classID = getArguments().getString("class");
            selectedClass = ParseUtilities.getClass(classID);
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }

        if (ParseUser.getCurrentUser() != null) {
            ParseRelation<Class> classes = ParseUser.getCurrentUser().getRelation("enrolled");
            ParseQuery<Class> query = classes.getQuery();
            query.whereEqualTo("objectId", selectedClass.getObjectId());
            query.findInBackground(new FindCallback<Class>() {
                @Override
                public void done(List<Class> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0)
                            enrolled = true;
                    }
                    refreshButtons();
                }
            });
        }

        if (selectedClass != null) {
            RecyclerView rView = (RecyclerView)view.findViewById(R.id.chatView);
            rView.setVisibility(View.GONE);

            final ListView lView = (ListView)view.findViewById(R.id.classDetailList);
            lView.setVisibility(View.VISIBLE);
            lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String eventObjectId = ((EventQueryAdapter) lView.getAdapter()).getItem(position).getObjectId();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString("event", eventObjectId);
                    bundle.putString("class", selectedClass.getObjectId());
                    EventDetails eventDetails = new EventDetails();
                    eventDetails.setArguments(bundle);
                    ft.replace(R.id.fragmentContainer, eventDetails);
                    ft.addToBackStack("event detail");
                    ft.commit();
                }
            });

            ((TextView) view.findViewById(R.id.className)).setText(selectedClass.getClassTitle());
            ((TextView)view.findViewById(R.id.classEnrolled)).setText(selectedClass.getEnrollCount());
            classEvents = new EventQueryAdapter(getActivity(), selectedClass);
            classDetailList.setAdapter(classEvents);
        }

        view.findViewById(R.id.eventButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecyclerView rView = (RecyclerView)getView().findViewById(R.id.chatView);
                rView.setVisibility(View.GONE);

                if (classEvents != null) {
                    ListView lView = (ListView)getView().findViewById(R.id.classDetailList);
                    lView.setVisibility(View.VISIBLE);

                    ((ListView)getView().findViewById(R.id.classDetailList)).setAdapter(classEvents);
                }
            }
        });

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedClass != null) {
                    String selectedClassObjectID = selectedClass.getObjectId();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString("class", selectedClassObjectID);
                    AddEvent addEvent = new AddEvent();
                    addEvent.setArguments(bundle);
                    ft.replace(R.id.fragmentContainer, addEvent);
                    ft.addToBackStack("add event");
                    ft.commit();
                }
            }
        });

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView lView = (ListView) getView().findViewById(R.id.classDetailList);
                lView.setVisibility(View.GONE);

                RecyclerView rView = (RecyclerView) getView().findViewById(R.id.chatView);
                rView.setVisibility(View.VISIBLE);
            }
        });

        addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedClass != null) {
                    String selectedClassObjectID = selectedClass.getObjectId();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString("class", selectedClassObjectID);
                    Message message = new Message();
                    message.setArguments(bundle);
                    ft.replace(R.id.fragmentContainer, message);
                    ft.addToBackStack("add chat");
                    ft.commit();
                }
            }
        });

        return view;
    }

    public void onResume(){
        super.onResume();
        refreshButtons();

        if (LayerImpl.isAuthenticated()) {
            setupConversationView();
        }
    }

    public void onUserAuthenticated(String id) {
        Log.d("Activity", "User authenticated: " + id);
        setupConversationView();
    }

    //Set up the Query Adapter that will drive the RecyclerView on the conversations_screen
    private void setupConversationView() {

        Log.d("Activity", "Setting conversation view");

        //Grab the Recycler View and list all conversation objects in a vertical list
        final RecyclerView conversationsView = (RecyclerView) view.findViewById(R.id.chatView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        conversationsView.setLayoutManager(layoutManager);

        //The Query Adapter drives the recycler view, and calls back to this activity when the user
        // taps on a Conversation
        ParseQuery<ParseObject> conversationsForClass = new ParseQuery<>("Conversation");
        conversationsForClass.whereEqualTo("class", selectedClass);
        final List<Predicate> conversationIds = new LinkedList<>();

        conversationsForClass.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        Predicate predicate = new Predicate(Conversation.Property.ID, Predicate.Operator.EQUAL_TO, ((ParseObject)objects.get(i)).getString("conversationId"));
                        conversationIds.add(predicate);
                    }

                    if (conversationIds.size() == 0) {
                        RecyclerView rView = (RecyclerView) getView().findViewById(R.id.chatView);
                        rView.setVisibility(View.GONE);
                    } else {
                        mConversationsAdapter = new ConversationQueryAdapter(getActivity().getApplicationContext(), LayerImpl.getLayerClient(), ClassDetail.this, new QueryAdapter.Callback() {
                            @Override
                            public void onItemInserted() {
                                Log.d("Activity", "Conversation Adapter, new conversation inserted");
                            }
                        }, conversationIds);

                        //Attach the Query Adapter to the Recycler View
                        conversationsView.setAdapter(mConversationsAdapter);

                        //Execute the Query
                        mConversationsAdapter.refresh();
                    }
                }
            }
        });

//        mConversationsAdapter = new ConversationQueryAdapter(getActivity().getApplicationContext(), LayerImpl.getLayerClient(), ClassDetail.this, new QueryAdapter.Callback() {
//            @Override
//            public void onItemInserted() {
//                Log.d("Activity", "Conversation Adapter, new conversation inserted");
//            }
//        });

//        //Attach the Query Adapter to the Recycler View
//        conversationsView.setAdapter(mConversationsAdapter);
//
//        //Execute the Query
//        mConversationsAdapter.refresh();
    }


    //Callback from the Query Adapter. When the user taps a Conversation, grab its ID and start
    // a MessageActivity to display all the messages

    public void onConversationClick(Conversation conversation) {
        Log.d("Activity", "Conversation clicked: " + conversation.getId());

        //If the Conversation is valid, start the MessageActivity and pass in the Conversation ID
        if (conversation != null && conversation.getId() != null && !conversation.isDeleted()) {

            Bundle bundle = new Bundle();
            bundle.putString("conversation-id", conversation.getId().toString());

            FragmentTransaction ft = this.getActivity().getSupportFragmentManager().beginTransaction();
            Message temp = new Message();
            temp.setArguments(bundle);
            ft.replace(R.id.fragmentContainer, temp);
            ft.addToBackStack("message_screen");
            ft.commit();

            /*
            Intent intent = new Intent(ConversationsActivity.this, MessageActivity.class);
            intent.putExtra("conversation-id", conversation.getId());
            startActivity(intent);
            */


        }
    }

    //You can handle long clicks as well (such as displaying metadata or deleting a conversation)
    public boolean onConversationLongClick(Conversation conversation) {
        return false;
    }

    //Once the user is fully deauthetnicated (all Messaging activity is synced and deleted), we
    // allow another user to login
    public void onUserDeauthenticated() {
        this.getActivity().getSupportFragmentManager().popBackStack();
//        Intent intent = new Intent(ConversationsActivity.this, LoginActivity.class);
//        startActivity(intent);
    }
}
