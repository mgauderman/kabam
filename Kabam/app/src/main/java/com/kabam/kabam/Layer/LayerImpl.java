package com.kabam.kabam.Layer;

import android.content.Context;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by SmrtAsian on 11/21/15.
 */
public class LayerImpl {
    private static String LayerAppID = "layer:///apps/staging/3b922f38-8ff9-11e5-a092-a26de4023d09";

    private static LayerClient mLayerClient;

    private static MyConnectionListener connectionListener;
    private static MyAuthenticationListener authenticationListener;


    public static boolean hasValidAppID(){
        if(LayerAppID.equals("LAYER_APP_ID")){
            return false;
        }

        return true;
    }

    //Called when any Activity is created to make sure the LayerClient is created, the callbacks
    // are registered, and the LayerClient is connected
    public static void initialize(Context context){
        if(mLayerClient == null && hasValidAppID()){

            LayerClient.setLoggingEnabled(context, true);
            mLayerClient = LayerClient.newInstance(context.getApplicationContext(), LayerAppID);
        }

        if(mLayerClient != null) {
            if (connectionListener == null) {
                connectionListener = new MyConnectionListener();
                mLayerClient.registerConnectionListener(connectionListener);
            }
            if (authenticationListener == null) {
                authenticationListener = new MyAuthenticationListener();
                mLayerClient.registerAuthenticationListener(authenticationListener);
            }

            if (!mLayerClient.isConnected())
                mLayerClient.connect();
        }
    }

    //Connects to the Layer service
    public static void connectClient(){
        if(mLayerClient != null)
            mLayerClient.connect();
    }

    //Starts the Authentication process. The actual User registration happens in the
    // MyAuthenticationListener callbacks
    public static void authenticateUser(){
        if(mLayerClient != null)
            mLayerClient.authenticate();
    }

    //Returns true if the LayerClient exists and is connected to the web service
    public static boolean isConnected(){

        if(mLayerClient != null)
            return mLayerClient.isConnected();

        return false;
    }

    //Returns true if the LayerClient exists and a user has been authenticated successfully
    public static boolean isAuthenticated(){

        if(mLayerClient != null)
            return mLayerClient.isAuthenticated();

        return false;
    }

    //When an Activity comes to the foreground (onResume), we want that Activity to handle any
    // callbacks
    public static void setContext(LayerCallbacks callbacks){

        if(connectionListener != null)
            connectionListener.setActiveContext(callbacks);


        if(authenticationListener != null)
            authenticationListener.setActiveContext(callbacks);
    }

    //Returns the App ID set by the developer
    public static String getLayerAppID(){

        return LayerAppID;
    }

    //Returns the actual LayerClient object
    public static LayerClient getLayerClient(){

        return mLayerClient;
    }

    //A helper function that takes a Message Object and returns the String contents of its data.
    // In this implementation, we make the assumption that all messages are text, but this could
    // be changed to handle any other file types (images, audio, JSON, etc)
    public static String getMessageText(Message msg){

        //The message content that will be returned
        String msgContent = "";
        if(msg != null){

            //Iterate through all the messasge parts, if the mime type indicates it is text, decode
            // it and add it to the string that will be returned
            List<MessagePart> parts = msg.getMessageParts();
            for(MessagePart part : parts){
                try {

                    if(part.getMimeType().equals("text/plain"))
                        msgContent += new String(part.getData(), "UTF-8") + "\n";

                } catch(UnsupportedEncodingException e){

                }
            }
        }

        return msgContent;
    }

    //A helper function that takes a Message object and returns a String representation of the
    // ReceivedAt time (the local time the message was downloaded to the device)
    public static String getReceivedAtTime(Message msg){
        String dateString = "";
        if(msg != null && msg.getReceivedAt() != null) {
            SimpleDateFormat format = new SimpleDateFormat("M/dd hh:mm:ss");
            dateString = format.format(msg.getReceivedAt());
        }
        return dateString;
    }

}
