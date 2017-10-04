package com.kabam.kabam.Layer;

import com.layer.sdk.exceptions.LayerException;

/**
 * Created by SmrtAsian on 11/21/15.
 */
public interface LayerCallbacks {

    public void onLayerConnected();
    public void onLayerDisconnected();
    public void onLayerConnectionError(LayerException e);

    //Layer authentication callbacks
    public void onUserAuthenticated(String id);
    public void onUserAuthenticatedError(LayerException e);
    public void onUserDeauthenticated();


}
