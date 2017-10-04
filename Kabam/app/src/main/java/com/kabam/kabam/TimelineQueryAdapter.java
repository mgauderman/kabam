package com.kabam.kabam;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.text.DateFormat;

/**
 * Created by Ayush on 11/22/15.
 */
public class TimelineQueryAdapter extends ParseQueryAdapter<Event> {

    public TimelineQueryAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<Event>() {
            public ParseQuery<Event> create() {
                ParseQuery query = ParseQuery.getQuery("Event");
                query.addDescendingOrder("time");

                ParseQuery user_classes = ParseUser.getCurrentUser().getRelation("enrolled").getQuery();
                query.whereMatchesQuery("class", user_classes);

                return query;
            }
        });

    }

    @Override
    public View getItemView(Event object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.class_list_item, null);
        }

        super.getItemView(object, v, parent);

        ((TextView) v.findViewById(R.id.title)).setText(object.getEventTitle());

        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        ((TextView) v.findViewById(R.id.enrolled)).setText(df.format(object.getTime()));

        return v;
    }
}
