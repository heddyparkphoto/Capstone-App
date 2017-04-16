package com.is.was.be.wannareddit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by hyeryungpark on 4/15/17.
 */

public class Util {

    /*
    Check the Connectivity for the Context passed in
 */
    public static boolean isOnline(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return (networkInfo!=null && networkInfo.isConnectedOrConnecting());
    }

}
