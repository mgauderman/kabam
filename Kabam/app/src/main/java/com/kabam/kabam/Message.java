package com.kabam.kabam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kabam.kabam.Adapters.MessageQueryAdapter;
import com.kabam.kabam.Adapters.QueryAdapter;
import com.kabam.kabam.Layer.LayerImpl;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.ConversationOptions;
import com.layer.sdk.messaging.MessagePart;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by SmrtAsian on 11/21/15.
 */
public class Message extends FragmentBaseMessage implements MessageQueryAdapter.MessageClickHandler {

    //The owning conversation
    private Conversation mConversation;

    //The Query Adapter that grabs all Messages and displays them based on their Position
    private MessageQueryAdapter mMessagesAdapter;

    //Once a Conversation is started (ie, the first message is sent) disallow adding/removing
    // of participants (this is an implementation choice, you can always choose to allow Participants
    // to add or remove users at any point if you choose)
    private Button mAddUserButton;

    //Layout view of current participants
    private LinearLayout mParticipantsList;

    //This is the actual view that contains all the messages
    private RecyclerView mMessagesView;

    //When starting a new Conversation, we keep a list of all target participants. The Conversation
    // is only created when the first message is sent
    private ArrayList<String> mTargetParticipants;

    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = super.onCreateView(inflater, container, savedInstanceState);

        //View containing all messages in the target Conversastion
        mMessagesView = (RecyclerView)view.findViewById(R.id.mRecyclerView);

        //Check to see when the locally Authenticated user is trying to send a message
        Button sendButton = (Button)view.findViewById(R.id.sendButton);
        if(sendButton != null)
            sendButton.setOnClickListener(this);

        //If this is a new conversation, we will want to allow the user to add his/her friends
        mAddUserButton = (Button)view.findViewById(R.id.addParticipants);
        if(mAddUserButton != null){

            TextView textView = (TextView)view.findViewById(R.id.chatName);
            textView.setVisibility(View.GONE);

            mAddUserButton.setOnClickListener(this);
        }


        //A view containing a list of all the Participants in the Conversation (not including the
        // locally authenticated user)
        mParticipantsList = (LinearLayout)view.findViewById(R.id.participantList);

        //If the soft keyboard changes the size of the mMessagesView, we want to force the scroll to
        // the bottom of the view so the latest message is always displayed
        attachKeyboardListeners(mMessagesView);

        return view;
    }

    //Checks the state of the LayerClient and whether this is an existing Conversation or a new
    // Conversation
    public void onResume() {
        super.onResume();


        //Now check to see if this is a new Conversation, or if the Activity needs to render an
        // existing Conversation

        if (getArguments() != null){
            String conversationID = getArguments().getString("conversation-id");

            if (conversationID != null) {
                Uri conversationURI = Uri.parse(conversationID);
                if (conversationURI != null)
                    mConversation = LayerImpl.getLayerClient().getConversation(conversationURI);
            }
        }


        //This is an existing Conversation, display the messages, otherwise, allow the user to
        // add/remove participants and create a new Conversation
        if (mConversation != null)
            setupMessagesView();
        else
            createNewConversationView();

    }

    //Existing Conversation, so render the messages in the RecyclerView
    private void setupMessagesView() {

        Log.d("Activity", "Conversation exists, setting up view");

        //Hide the "add users" button
        hideAddParticipantsButton();

        //Create the appropriate RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        mMessagesView.setLayoutManager(layoutManager);

        //And attach it to the appropriate QueryAdapter, which will automatically update the view
        // when a new Message is added to the Conversation
        createMessagesAdapter();

        //Grab all the Participants and add them at the top of the screen (the "To:" field)
        populateToField(mConversation.getParticipants());
    }

    //Takes a String Array of user IDs, finds the display name, and adds them to the "To:" field
    // at the top of the Messages screen
    private void populateToField(List<String> participantIds){
        //We will not include the Authenticated user in the "To:" field, since they know they are
        // already part of the Conversation
        TextView[] participantList = new TextView[participantIds.size()-1];
        int idx = 0;
        for(String id : participantIds){
            if(!id.equals(LayerImpl.getLayerClient().getAuthenticatedUserId()) && !ParseUtilities.isGhost(id)) {

                //Create a new stylized text view
                TextView tv = new TextView(this.getContext());
                tv.setText(ParseUtilities.getName(id));
                tv.setTextSize(16);
                tv.setPadding(5, 5, 5, 5);
                tv.setBackgroundColor(Color.LTGRAY);
                participantList[idx] = tv;

                idx++;
            }
        }

        //Uses the helper function to make sure all participant names are appropriately displayed
        // and not cut off due to size constraints
        populateViewWithWrapping(mParticipantsList, participantList, this.getContext());
    }

    //If a Conversation ID was not passed into this Activity, we assume that a new Conversation is
    // being created
    private void createNewConversationView() {

        Log.d("Activity", "Creating a new Conversation");

        //Create the appropriate RecyclerView which will be attached to the QueryController when it
        // is created (after the first message is sent and the Conversation is actually created)
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        mMessagesView.setLayoutManager(layoutManager);
    }

    //This is called when there is a valid Conversation to attach the RecyclerView to the appropriate
    // QueryAdapter. Whenever a new Message is sent to the Conversation, the RecyclerView will be updated
    private void createMessagesAdapter(){

        //The Query Adapter drives the RecyclerView, and handles all the heavy lifting of checking
        // for new Messages, and updating the RecyclerView
        mMessagesAdapter = new MessageQueryAdapter(this.getActivity().getApplicationContext(), LayerImpl.getLayerClient(), mMessagesView, mConversation, this, new QueryAdapter.Callback() {

            public void onItemInserted() {
                //When a new item is inserted into the RecyclerView, scroll to the bottom so the
                // most recent Message is always displayed
                mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);

            }
        });
        mMessagesView.setAdapter(mMessagesAdapter);

        //Execute the Query
        mMessagesAdapter.refresh();

        //Start by scrolling to the bottom (newest Message)
        mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
    }

    //You can choose to present additional options when a Message is tapped
    public void onMessageClick(com.layer.sdk.messaging.Message message) {

    }

    //You can choose to present additional options when a Message is long tapped
    public boolean onMessageLongClick(com.layer.sdk.messaging.Message message) {
        return false;
    }

    //Handle the sendButtona nd Add/Remove Participants button (if displayed)
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.sendButton:
                Log.d("Activity", "Send button pressed");

                InputMethodManager imm = (InputMethodManager)getService();
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                sendMessage();
                break;

            case R.id.addParticipants:
                Log.d("Activity", "Add participant button pressed");
                showParticipantPicker();
                break;
        }
    }

    private Object getService(){
        return this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    //The Authenticated User is actually sending a Message to this Conversation
    private void sendMessage(){

        //First Check to see if we have a valid Conversation object
        if(mConversation == null) {
            //Make sure there are valid participants. Since the Authenticated user will always be
            // included in a new Conversation, we check to see if there is more than one target participant
            if(mTargetParticipants.size() > 1) {

                //Create a new conversation, and tie it to the QueryAdapter
                mConversation = LayerImpl.getLayerClient().newConversation(mTargetParticipants);

                createMessagesAdapter();

                EditText title = (EditText) view.findViewById(R.id.chatTitle);
                String titleText = getTextAsString(title);

                ParseUtilities.addConversationToParse(mConversation.getId().toString(), titleText, getArguments().getString("class"));

                //Once the Conversation object is created, we don't allow changing the Participant List
                // Note: this is an implementation choice. It is always possible to add/remove participants
                // after a Conversation has been created
                hideAddParticipantsButton();

                ParseUtilities.updateAllConversations();
            } else {
                showAlert("Send Message Error","You need to specify at least one participant before sending a message.");
                return;
            }
        }

        //Grab the user's input
        final EditText input = (EditText)view.findViewById(R.id.textInput);
        final String text = getTextAsString(input);


        //If the input is valid, create a new Message and send it to the Conversation
        if(mConversation != null && text != null && text.length() > 0){
            MessagePart part = LayerImpl.getLayerClient().newMessagePart(text);
            com.layer.sdk.messaging.Message msg = LayerImpl.getLayerClient().newMessage(part);
            mConversation.send(msg);

            input.setText("");
        } else {
            showAlert("Send Message Error","You cannot send an empty message.");
        }
    }

    //Shows a list of all users that can be added to the Conversation
    private void showParticipantPicker() {
        Class selectedClass = null;
        if (getArguments() != null) {
            if (getArguments().getString("class") != null) {
                selectedClass = ParseUtilities.getClass(getArguments().getString("class"));
            }
        }

        //Update user list from Parse
        final HashMap<String, ParseUser> users = new HashMap<>();
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();

        ParseQuery<Class> classQuery = ParseQuery.getQuery("Class");
        classQuery.whereEqualTo("objectId", selectedClass.getObjectId());

        userQuery.whereMatchesQuery("enrolled", classQuery);

        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> results, ParseException e) {
                if (e == null) {
                    //Create a new Dialog Box
                    AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getContext());
                    helpBuilder.setTitle("Select Participants");
                    helpBuilder.setMessage("Add or remove participants from this conversation:\n");

                    //The Linear Layout View that will hold all the CheckBox views
                    LinearLayout checkboxList = new LinearLayout(getContext());
                    checkboxList.setOrientation(LinearLayout.VERTICAL);

                    //Grab a list of all friends
                    //A Map of the CheckBox with the human readable username and the Parse Object ID
                    final HashMap<CheckBox, String> allUsers = new HashMap<>();

                    //Create the list of participants if it hasn't been instantiated
                    if(mTargetParticipants == null)
                        mTargetParticipants = new ArrayList<>();

                    //Go through each friend and create a Checkbox with a human readable name mapped to the
                    // Object ID
                    String ghostId = null;
                    for (int i = 0; i < results.size(); i++){
                        if ( !results.get(i).getUsername().equals(ParseUser.getCurrentUser().getUsername()) && !ParseUtilities.isGhost(results.get(i).getObjectId()) ) {
                            String friendId = results.get(i).getObjectId();
                            //Log.d("BEN", ParseUser.getCurrentUser().getUsername());

                            CheckBox friend = new CheckBox(getContext());
                            friend.setText(ParseUtilities.getName(friendId));

                            //If this user is already selected, mark the checkbox
                            if(mTargetParticipants.contains(friendId))
                                friend.setChecked(true);

                            checkboxList.addView(friend);

                            allUsers.put(friend, friendId);
                        } else if (ParseUtilities.isGhost(results.get(i).getObjectId())) {
                            ghostId = results.get(i).getObjectId();
                        }
                    }

                    //Add the list of CheckBoxes to the Alert Dialog
                    helpBuilder.setView(checkboxList);

                    //When the user is done adding/removing participants, update the list of target users
                    final String finalGhostId = ghostId;
                    helpBuilder.setPositiveButton("Done",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing but close the dialog

                                    //Reset the target user list, and rebuild it based on which checkboxes are selected
                                    mTargetParticipants.clear();
                                    mTargetParticipants.add(LayerImpl.getLayerClient().getAuthenticatedUserId());

                                    Set checkboxes = allUsers.keySet();
                                    Iterator checkItr = checkboxes.iterator();
                                    while(checkItr.hasNext()){
                                        CheckBox currCheck = (CheckBox)checkItr.next();
                                        if(currCheck != null && currCheck.isChecked()){
                                            String friendID = allUsers.get(currCheck);
                                            mTargetParticipants.add(friendID);
                                        }
                                    }

                                    if (finalGhostId != null)
                                        mTargetParticipants.add(finalGhostId);


                                    Log.d("Activity", "Current participants: " + mTargetParticipants.toString());

                                    //Draw the list of target users
                                    populateToField(mTargetParticipants);
                                }
                            });


                    // Create and show the dialog box with list of all participants
                    AlertDialog helpDialog = helpBuilder.create();
                    helpDialog.show();
                }
            }
        });



    }

    //When a Conversation has Messages, we disable the ability to Add/Remove participants
    private void hideAddParticipantsButton(){
        if(mAddUserButton != null) {

            TextView textView = (TextView)view.findViewById(R.id.chatName);
            if (ParseUtilities.getConversation(mConversation.getId().toString()) != null){
                String chatName = ParseUtilities.getConversation(mConversation.getId().toString()).getString("title");
                textView.setText(chatName);
                textView.setVisibility(View.VISIBLE);
            }

            EditText editText = (EditText) view.findViewById(R.id.chatTitle);
            editText.setVisibility(View.GONE);


            mAddUserButton.setVisibility(View.GONE);
        }
    }

    //When the RecyclerView changes size because of the Soft Keyboard, force scroll to the bottom
    // in order to always show the latest message
    protected void onShowKeyboard(int keyboardHeight) {
        mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
    }
    protected void onHideKeyboard() {
        mMessagesView.smoothScrollToPosition(Integer.MAX_VALUE);
    }
}
