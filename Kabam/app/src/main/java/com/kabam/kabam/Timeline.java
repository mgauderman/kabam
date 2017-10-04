package com.kabam.kabam;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Ayush on 11/22/15.
 */
public class Timeline extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.classes, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new TimelineQueryAdapter(getActivity()));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Event event = ((TimelineQueryAdapter)getListAdapter()).getItem(position);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("event", event.getObjectId());
        bundle.putString("class", event.getEventClass().getObjectId());
        EventDetails eventDetails = new EventDetails();
        eventDetails.setArguments(bundle);
        ft.replace(R.id.fragmentContainer, eventDetails);
        ft.addToBackStack("event detail");
        ft.commit();
    }
}
