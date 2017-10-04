package com.kabam.kabam.Layer;

import android.os.AsyncTask;
import android.util.Log;

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerAuthenticationListener;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by SmrtAsian on 11/21/15.
 */
public class MyAuthenticationListener implements LayerAuthenticationListener{

    private LayerCallbacks mCurrentContext;

    @Override
    public void onAuthenticated(LayerClient client, String userName) {
        if(mCurrentContext != null)
            mCurrentContext.onUserAuthenticated(userName);
    }

    @Override
    public void onAuthenticationChallenge(final LayerClient client, String nonce) {

        //Validate the current Parse user
        if(ParseUser.getCurrentUser() == null || ParseUser.getCurrentUser().getUsername() == null){
            onAuthenticationError(client, new LayerException(LayerException.Type.USER_NOT_FOUND, "Invalid Parse User ID"));
            return;
        }

        ParseUser user = ParseUser.getCurrentUser();
        String userID = user.getObjectId();

        //Print out the nonce for validation purposes (useful for debugging)
        Log.d("Authentication", "User ID: " + userID + " with nonce: " + nonce);

        // Make a request to your Parse Cloud Function to acquire a Layer identityToken
        HashMap<String, Object> params = new HashMap<>();
        params.put("userID", userID);
        params.put("nonce", nonce);

        ParseCloud.callFunctionInBackground("generateToken", params, new FunctionCallback<String>() {
            public void done(String token, ParseException e) {
                if (e == null) {
                    //Once you have a Valid Identity Token, return it to the Layer SDK which will
                    // complete the Authentication process
                    Log.d("Authentication", "Identity token: " + token);
                    client.answerAuthenticationChallenge(token);
                } else {
                    Log.d("Authentication", "Parse Cloud function failed to be called to generate token with error: " + e.getMessage());
                }
            }
        });

    }

    @Override
    public void onAuthenticationError(LayerClient layerClient, LayerException e) {
        if(mCurrentContext != null)
            mCurrentContext.onUserAuthenticatedError(e);
    }

    @Override
    public void onDeauthenticated(LayerClient client) {
        if(mCurrentContext != null)
            mCurrentContext.onUserDeauthenticated();
    }

    public void setActiveContext(LayerCallbacks context){
        mCurrentContext = context;
    }
}