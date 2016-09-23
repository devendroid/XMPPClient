package com.devs.xmppclient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by ${Deven} on ${4/1/16}.
 */
public class XMPPConnectorIService extends IntentService {

    private static final String TAG = "XMPPConnectorIService";

    public static XMPPConnector xmppConnector;
    private static ConnectivityManager cm;

    public XMPPConnectorIService() {
        super(TAG);
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public XMPPConnectorIService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "====onHandleIntent");

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(isNetworkConnected()) {
            String uname = intent.getStringExtra("username");
            String psw = intent.getStringExtra("password");
            Log.i(TAG, "====user in service "+uname);

            xmppConnector = XMPPConnector.getInstance(getApplication(),uname, psw);
            xmppConnector.connectNLogin(TAG);
        }
        else {
            Log.i(TAG, "====Not connected to network");
        }
    }

    public static boolean isNetworkConnected() {
        return cm.getActiveNetworkInfo() != null;
    }
}
