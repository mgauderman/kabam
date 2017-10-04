package com.kabam.kabam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.parse.SaveCallback;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by tanke_000 on 11/20/2015.
 */
public class AddEvent extends Fragment {
    //TIME HERE
    private Time t;


    private Class selectedClass;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_event, container, false);

        if (getArguments() != null) {
            if (getArguments().getString("class") != null) {
                selectedClass = ParseUtilities.getClass(getArguments().getString("class"));
                ((TextView)view.findViewById(R.id.addEventClass)).setText(selectedClass.getClassTitle());
                ((TextView)view.findViewById(R.id.addEventNumEnrolled)).setText(selectedClass.getEnrollCount());
            }
        }

        final RadioButton isAssignment = (RadioButton)view.findViewById(R.id.addEventIsAssignment);

        EditText dueDateText = (EditText)view.findViewById(R.id.addEventDueDate);
        dueDateText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasfocus) {
                if (hasfocus) {
                    DialogFragment newFragment = new DateDialog(getView());
                    newFragment.show(getActivity().getFragmentManager(), "date_picker");
                }
            }

        });

        view.findViewById(R.id.addEventSubmitButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (selectedClass != null) {
                    String title = ((EditText) getView().findViewById(R.id.addEventEventName)).getText().toString();
                    if (title.length() > 0) {
                        boolean assignment = isAssignment.isChecked();

                        TimePicker timePicker = (TimePicker)getView().findViewById(R.id.addEventTimePicker);
                        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                        Date date = null;
                        Calendar calendar = Calendar.getInstance();
                        try {
                            date = df.parse(((EditText) getView().findViewById(R.id.addEventDueDate)).getText().toString());
                            date.setHours(timePicker.getCurrentHour()-8);//wtf why does this make it work
                            date.setMinutes(timePicker.getCurrentMinute());
                            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                            calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                            Log.d("Kevin:", timePicker.getCurrentHour()+":"+timePicker.getCurrentMinute()+"ReceivedHour="+date.getHours());
                        //    date.setTime(calendar.getTimeInMillis());
                        } catch (ParseException e) {
                            Log.d("Exception Adding Event", e.getMessage());
                        }

                        if (date != null) {
                            String description = ((EditText) getView().findViewById(R.id.addEventDescription)).getText().toString();
                            if (description.length() > 0) {
                                String location = ((EditText) getView().findViewById(R.id.addEventLocation)).getText().toString();

                                if (location.length() > 0) {
                                    final Dialog progressDialog = ProgressDialog.show(getActivity(), "", "Adding Event...", true);

                                    Event event = new Event();
                                    event.put("time", date);
                                    event.put("assignment", assignment);
                                    event.put("description", description);
                                    event.put("location", location);
                                    event.put("title", title);
                                    event.put("class", selectedClass);
                                    event.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(com.parse.ParseException e) {
                                            progressDialog.dismiss();
                                            if (e == null) {
                                                getActivity().getSupportFragmentManager().popBackStack();
                                            } else {
                                                displayErrorMessage("There was an error trying to add your event. Please try again later!");
                                                getActivity().getSupportFragmentManager().popBackStack();
                                            }
                                        }
                                    });
                                    ParseUtilities.updateAllEvents();
                                } else {
                                    displayErrorMessage("Please enter a location for your event!");
                                }
                            } else {
                                displayErrorMessage("Please enter a description for your event!");
                            }
                        } else {
                            displayErrorMessage("Please select a date for your event!");
                        }
                    } else {
                        displayErrorMessage("Please enter a title for your event!");
                    }
                } else {
                    displayErrorMessage("There was an error trying to add your event. Please try again later!");
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
        return view;
    }

    private void displayErrorMessage(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Add Event Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
