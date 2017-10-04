package com.kabam.kabam;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Ayush on 11/19/15.
 */
public class ClassQueryAdapter extends ParseQueryAdapter<Class> {

    public ClassQueryAdapter(Context context, final String searchText) {
        super(context, new ParseQueryAdapter.QueryFactory<Class>() {
            public ParseQuery<Class> create() {
                ParseQuery query = new ParseQuery("Class");
                query.orderByDescending("title");
                if (searchText != null) {
                    query.whereContains("title", searchText);
                }
                return query;
            }
        });
    }

    @Override
    public View getItemView(Class object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.class_list_item, null);
        }

        super.getItemView(object, v, parent);

        ((TextView) v.findViewById(R.id.title)).setText(object.getClassTitle());
        ((TextView) v.findViewById(R.id.enrolled)).setText(object.getEnrollCount());

        return v;
    }
}
