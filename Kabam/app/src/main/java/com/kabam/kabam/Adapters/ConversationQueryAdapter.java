package com.kabam.kabam.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import com.kabam.kabam.Layer.LayerImpl;
import com.kabam.kabam.MainActivity;
import com.kabam.kabam.ParseUtilities;
import com.kabam.kabam.R;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.CompoundPredicate;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.parse.Parse;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.Set;

/**
 * Created by SmrtAsian on 11/21/15.
 */
public class ConversationQueryAdapter extends QueryAdapter<Conversation, ConversationQueryAdapter.ViewHolder>{

    //Inflates the view associated with each Conversation object returned by the Query
    private final LayoutInflater mInflater;
    //Handle the callbacks when the Conversation item is actually clicked. In this case, the
    // ConversationsActivity class implements the ConversationClickHandler
    private final ConversationClickHandler mConversationClickHandler;
    public static interface ConversationClickHandler {
        public void onConversationClick(Conversation conversation);

        public boolean onConversationLongClick(Conversation conversation);
    }

    //The fields in the ViewHolder reflect the conversation_item view
    public static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        //For each Conversation item in the RecyclerView list, we show the participants, time,
        // contents of the last message, and have a reference to the conversation so when it is
        // clicked we can start the MessageActivity
        public TextView participants;
        public TextView time;
        public TextView lastMsgContent;
        public Conversation conversation;
        public final ConversationClickHandler conversationClickHandler;

        //Registers the click listener callback handler
        public ViewHolder(View itemView, ConversationClickHandler conversationClickHandler) {
            super(itemView);
            this.conversationClickHandler = conversationClickHandler;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        //Execute the callback when the conversation is clicked
        public void onClick(View v) {
            conversationClickHandler.onConversationClick(conversation);
        }

        //Execute the callback when the conversation is long-clicked
        public boolean onLongClick(View v) {
            return conversationClickHandler.onConversationLongClick(conversation);
        }
    }

    //Constructor for the ConversationQueryAdapter
    //Sorts all conversations by last message received (ie, downloaded to the device)
    public ConversationQueryAdapter(Context context, LayerClient client, ConversationClickHandler conversationClickHandler, Callback callback, List<Predicate> conversationIds) {
        // existing items predicate
        super(client, Query.builder(Conversation.class).predicate(new CompoundPredicate(CompoundPredicate.Type.OR, conversationIds))
                    .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                    .build(), callback);

        //Sets the LayoutInflator and Click callback handler
        mInflater = LayoutInflater.from(context);
        mConversationClickHandler = conversationClickHandler;
    }

    public ConversationQueryAdapter(Context context, LayerClient client, ConversationClickHandler conversationClickHandler, Callback callback) {
        // existing items predicate
        super(client, Query.builder(Conversation.class)
                .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                .build(), callback);

        //Sets the LayoutInflator and Click callback handler
        mInflater = LayoutInflater.from(context);
        mConversationClickHandler = conversationClickHandler;
    }

    //When a new Conversation is created (ie, either locally, or by another user), a new ViewHolder is created
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        //The conversation_item is just an example view you can use to display each Conversation in a list
        View itemView = mInflater.inflate(R.layout.chat_item, null);

        //Tie the view elements to the fields in the actual view after it has been created
        ViewHolder holder = new ViewHolder(itemView, mConversationClickHandler);
        holder.participants = (TextView) itemView.findViewById(R.id.participants);
        holder.time = (TextView) itemView.findViewById(R.id.time);
        holder.lastMsgContent = (TextView) itemView.findViewById(R.id.message);

        return holder;
    }

    //After the ViewHolder is created, we need to populate the fields with information from the Conversation
    public void onBindViewHolder(ViewHolder viewHolder, Conversation conversation) {
        if (conversation == null) {
            // If the item no longer exists, the ID probably migrated.
            refresh();
            return;
        }

        //Log.d("WTF", "binding conversation: " + conversation.getId() + " with participants: " + conversation.getParticipants().toString());

        //Set the Conversation (so when this item is clicked, we can start a MessageActivity and
        // show all the messages associated with it)
        viewHolder.conversation = conversation;

        //Go through all the User IDs in the Conversation and find the matching human readable
        // handles from Parse
        String title = null;
        if (ParseUtilities.getConversation(conversation.getId().toString()) != null)
            title = ParseUtilities.getConversation(conversation.getId().toString()).getString("title");

        viewHolder.participants.setText(title);

        //Grab the last message in the conversation and show it in the format "sender: last message content"
        Message message = conversation.getLastMessage();
        if (message != null) {
            viewHolder.lastMsgContent.setText(ParseUtilities.getName(message.getSender().getUserId()) + ": " + LayerImpl.getMessageText(message));
        } else {
            viewHolder.lastMsgContent.setText("");
        }

        //Draw the date the last message was received (downloaded from the server)
        viewHolder.time.setText(LayerImpl.getReceivedAtTime(message));
    }

    //This example app only has one kind of view type, but you could support different TYPES of
    // Conversations if you were so inclined
    public int getItemViewType(int i) {
        return 1;
    }
}
