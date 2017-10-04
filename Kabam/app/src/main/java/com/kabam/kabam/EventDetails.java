package com.kabam.kabam;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.Parse;

import java.text.DateFormat;
import java.util.TimeZone;

/**
 * Created by Ayush on 11/22/15.
 */
public class EventDetails extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_details, container, false);

        if (getArguments() == null || getArguments().getString("event") == null || getArguments().getString("class") == null)
            getActivity().getFragmentManager().popBackStack();

        Event event = ParseUtilities.getEvent(getArguments().getString("event"));
        ((TextView) view.findViewById(R.id.homeworkName)).setText(event.getEventTitle());
        ((TextView) view.findViewById(R.id.className)).setText(ParseUtilities.getClass(getArguments().getString("class")).getClassTitle());

        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        ((TextView) view.findViewById(R.id.eventDetailsDate)).setText("Date: " + df.format(event.getTime()));

        df = DateFormat.getTimeInstance(DateFormat.SHORT);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        ((TextView) view.findViewById(R.id.eventDetailsTime)).setText("Time: " + df.format(event.getTime()));

        ((TextView) view.findViewById(R.id.eventDetailsDescription)).setText("Description: " + event.getDescription());

        return view;
    }
}
