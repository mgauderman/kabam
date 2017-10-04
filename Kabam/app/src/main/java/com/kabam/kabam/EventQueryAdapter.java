package com.kabam.kabam;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.text.DateFormat;
import java.util.TimeZone;

/**
 * Created by Ayush on 11/22/15.
 */
public class EventQueryAdapter extends ParseQueryAdapter<Event> {

    public EventQueryAdapter(Context context, final Class searchClass) {
        super(context, new ParseQueryAdapter.QueryFactory<Event>() {
            public ParseQuery<Event> create() {
                ParseQuery query = new ParseQuery("Event");
                if (searchClass != null) {
                    query.whereEqualTo("class", searchClass);
                }
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

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        ((TextView) v.findViewById(R.id.enrolled)).setText(df.format(object.getTime()));

        return v;
    }
}