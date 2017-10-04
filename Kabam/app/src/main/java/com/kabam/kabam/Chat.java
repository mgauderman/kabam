package com.kabam.kabam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kabam.kabam.Adapters.ConversationQueryAdapter;
import com.kabam.kabam.Adapters.QueryAdapter;
import com.kabam.kabam.Layer.LayerImpl;
import com.layer.sdk.messaging.Conversation;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by SmrtAsian on 11/21/15.
 */
public class Chat extends FragmentBase implements ConversationQueryAdapter.ConversationClickHandler{

    private ConversationQueryAdapter mConversationsAdapter;
    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);

        Button chatButton = (Button) view.findViewById(R.id.newConversation);
        if (chatButton != null){
            chatButton.setOnClickListener(this);
        }

        return view;
    }

    public void onResume(){
        super.onResume();

        if (!LayerImpl.isAuthenticated()) {


            //Everything is set up, so start populating the Conversation list
        } else {

            Log.d("Activity", "Starting conversation view");
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
        RecyclerView conversationsView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        conversationsView.setLayoutManager(layoutManager);

        //The Query Adapter drives the recycler view, and calls back to this activity when the user
        // taps on a Conversation
        mConversationsAdapter = new ConversationQueryAdapter(this.getActivity().getApplicationContext(), LayerImpl.getLayerClient(), this, new QueryAdapter.Callback() {
            @Override
            public void onItemInserted() {
                Log.d("Activity", "Conversation Adapter, new conversation inserted");
            }
        });

        //Attach the Query Adapter to the Recycler View
        conversationsView.setAdapter(mConversationsAdapter);

        //Execute the Query
        mConversationsAdapter.refresh();
    }

    //Callback from the Query Adapter. When the user taps a Conversation, grab its ID and start
    // a MessageActivity to display all the messages
    public void onConversationClick(Conversation conversation) {
        Log.d("Activity", "Conversation clicked: " + conversation.getId());

        //If the Conversation is valid, start the MessageActivity and pass in the Conversation ID
        if (conversation != null && conversation.getId() != null && !conversation.isDeleted()) {

            Bundle bundle=new Bundle();
            bundle.putString("conversation-id", conversation.getId().toString());
            bundle.putString("class", getArguments().getString("class"));

            FragmentTransaction ft = this.getActivity().getSupportFragmentManager().beginTransaction();
            Message temp = new Message();
            temp.setArguments(bundle);
            ft.replace(R.id.fragmentContainer, temp);
            ft.addToBackStack("message_screen");
            ft.commit();
        }
    }

    //You can handle long clicks as well (such as displaying metadata or deleting a conversation)
    public boolean onConversationLongClick(Conversation conversation) {
        return false;
    }

    //Handle the buttons on the conversation_screen
    public void onClick(View v) {

        switch (v.getId()) {

//            case R.id.logout:
//                Log.d("Activity", "Logout button pressed");
//                logoutUser();
//                break;

            case R.id.newConversation:
                Log.d("Activity", "New conversation button pressed");
                createConversation();
                break;
        }
    }

    //When the user creates a new Conversation, we start the MessageActivity (but we don't bother
    // passing in a Conversation ID)
    private void createConversation() {

        FragmentTransaction ft = this.getActivity().getSupportFragmentManager().beginTransaction();
        Message temp = new Message();
        ft.replace(R.id.fragmentContainer, temp);
        ft.addToBackStack("message_screen");
        ft.commit();

//        Intent intent = new Intent(ConversationsActivity.this, MessageActivity.class);
//        startActivity(intent);
    }

    //Once the user is fully deauthetnicated (all Messaging activity is synced and deleted), we
    // allow another user to login
    public void onUserDeauthenticated() {
        this.getActivity().getSupportFragmentManager().popBackStack();
//        Intent intent = new Intent(ConversationsActivity.this, LoginActivity.class);
//        startActivity(intent);
    }


}
