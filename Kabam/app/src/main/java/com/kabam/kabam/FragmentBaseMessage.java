package com.kabam.kabam;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kabam.kabam.Layer.LayerCallbacks;
import com.kabam.kabam.Layer.LayerImpl;
import com.layer.sdk.exceptions.LayerException;

/**
 * Created by SmrtAsian on 11/21/15.
 */
public class FragmentBaseMessage extends Fragment implements LayerCallbacks, View.OnClickListener{

    public Activity activity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LayerImpl.initialize(this.getActivity().getApplicationContext());
        LayerImpl.setContext(this);

        if (LayerImpl.isAuthenticated()){
            Log.d("Message", "User is already authenicated");
        } else {
            //User is logged into Parse, so start the Layer Authentication process
            LayerImpl.authenticateUser();
        }

        View view = inflater.inflate(R.layout.message_screen, container, false);
        activity = this.getActivity();

        return view;
    }

    public void onResume(){
        super.onResume();

        LayerImpl.setContext(this);
    }

    //Handler to put up an alert dialog
    protected void showAlert(String heading, String body){
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(body)
                .setTitle(heading);

        // Get the AlertDialog from create() and then show() it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Handler to return the text contained in an EditText object
    protected String getTextAsString(EditText view){

        if(view != null && view.getText() != null)
            return view.getText().toString();

        return "";
    }

    //Layer connection callbacks
    public void onLayerConnected(){}
    public void onLayerDisconnected(){}
    public void onLayerConnectionError(LayerException e){}

    //Layer authentication callbacks
    public void onUserAuthenticated(String id){}
    public void onUserAuthenticatedError(LayerException e){}
    public void onUserDeauthenticated(){}

    //OnClickListener callback
    public void onClick(View v) {}


    //Keyboard showing/hidden callbacks
    protected void onShowKeyboard(int keyboardHeight) {}
    protected void onHideKeyboard() {}

    //A helper class that adds several Views to a LinearLayout with automatic wrapping, so that
    // items in the views array don't get cut off. In other words, turns this:
    //
    //  |item1 item2 item3 it|
    //
    // into this:
    //
    //  |item1 item2 item3  |
    //  |item4 item5 item6  |
    //
    protected void populateViewWithWrapping(LinearLayout linearLayout, View[] views, Context context) {

        Display display = this.getActivity().getWindowManager().getDefaultDisplay();
        linearLayout.removeAllViews();

        Point size = new Point();
        display.getSize(size);
        int maxWidth = size.x;

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params;
        LinearLayout newLL = new LinearLayout(context);
        newLL.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        newLL.setGravity(Gravity.LEFT);
        newLL.setOrientation(LinearLayout.HORIZONTAL);

        int widthSoFar = 0;

        for (View view : views) {
            LinearLayout LL = new LinearLayout(context);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LL.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            LL.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (view == null)
                continue;

            view.measure(0, 0);
            params = new LinearLayout.LayoutParams(view.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(5, 0, 5, 0);

            LL.addView(view, params);
            LL.measure(0, 0);
            widthSoFar += view.getMeasuredWidth();
            if (widthSoFar >= maxWidth) {
                linearLayout.addView(newLL);

                newLL = new LinearLayout(context);
                newLL.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                newLL.setOrientation(LinearLayout.HORIZONTAL);
                newLL.setGravity(Gravity.LEFT);
                params = new LinearLayout.LayoutParams(LL.getMeasuredWidth(), LL.getMeasuredHeight());
                newLL.addView(LL, params);
                widthSoFar = LL.getMeasuredWidth();
            } else {
                newLL.addView(LL);
            }
        }
        linearLayout.addView(newLL);
    }

    //Detect when the keyboard is showing or not
    //Used to adjust the view in the MessageActivity when the Keyboard changes the view size
    private boolean mKeyboardListenersAttached = false;
    private ViewGroup targetView;

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = targetView.getRootView().getHeight() - targetView.getHeight();
            int contentViewTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

            if(heightDiff <= contentViewTop){
                onHideKeyboard();
            } else {
                int keyboardHeight = heightDiff - contentViewTop;
                onShowKeyboard(keyboardHeight);
            }
        }
    };

    protected void attachKeyboardListeners(ViewGroup group) {
        if (mKeyboardListenersAttached) {
            return;
        }

        targetView = group;
        if(targetView != null) {
            targetView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

            mKeyboardListenersAttached = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mKeyboardListenersAttached) {
            targetView.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardLayoutListener);
        }
    }

}
