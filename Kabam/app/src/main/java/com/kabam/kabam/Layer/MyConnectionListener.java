package com.kabam.kabam.Layer;

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerConnectionListener;

/**
 * Created by SmrtAsian on 11/21/15.
 */
public class MyConnectionListener implements LayerConnectionListener{

    private LayerCallbacks mCurrentContext;

    @Override
    public void onConnectionConnected(LayerClient client) {
        if (mCurrentContext != null){
            mCurrentContext.onLayerConnected();
        }
    }

    @Override
    public void onConnectionDisconnected(LayerClient arg0) {
        if(mCurrentContext != null)
            mCurrentContext.onLayerDisconnected();
    }

    @Override
    public void onConnectionError(LayerClient arg0, LayerException e) {
        if(mCurrentContext != null)
            mCurrentContext.onLayerConnectionError(e);
    }

    //Helper function to keep track of the current Activity (set whenever an Activity resumes)
    public void setActiveContext(LayerCallbacks context){
        mCurrentContext = context;
    }

}
