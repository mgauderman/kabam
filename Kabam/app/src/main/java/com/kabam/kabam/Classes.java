package com.kabam.kabam;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

/**
 * Created by Ayush on 11/19/15.
 */
public class Classes extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.classes, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String searchText = null;
        if (getArguments() != null)
            searchText = getArguments().getString("search");

        ParseUtilities.updateAllUsers();
        ParseUtilities.updateAllClasses();

        setListAdapter(new ClassQueryAdapter(getActivity(), searchText));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String selectedClassObjectID = ((ClassQueryAdapter)getListAdapter()).getItem(position).getObjectId();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("class", selectedClassObjectID);
        ClassDetail classDetail = new ClassDetail();
        classDetail.setArguments(bundle);
        ft.replace(R.id.fragmentContainer, classDetail);
        ft.addToBackStack("class detail");
        ft.commit();
    }
}
