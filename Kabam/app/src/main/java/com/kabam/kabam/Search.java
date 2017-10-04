package com.kabam.kabam;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by SmrtAsian on 11/20/15.
 */
public class Search extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.search, container, false);

        Button searchButton = (Button)view.findViewById(R.id.searchButton);
        final EditText searchField = (EditText)view.findViewById(R.id.searchField);



        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchField.getText().length() > 0) { //if search field is not empty

                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getService();
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    Log.d("Message", "Search field was not empty");
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString("search", searchField.getText().toString());
                    Classes classes = new Classes();
                    classes.setArguments(bundle);
                    ft.replace(R.id.fragmentContainer, classes);
                    ft.addToBackStack("classes");
                    ft.commit();
                }
                else{ //search field cannot be empty
                    Log.d("Message", "Search field was empty");
                    displayErrorMessage("Please enter something to search for!");

                }
            }
        });
        return view;
    }

    private Object getService(){
        return this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void displayErrorMessage(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Search Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
