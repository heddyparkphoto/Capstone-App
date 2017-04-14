package com.is.was.be.wannareddit.service;

import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.is.was.be.wannareddit.data.DataUtility;
import com.is.was.be.wannareddit.data.ForRedditProvider;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by hyeryungpark on 4/10/17.
 */

public class WannaTaskService extends GcmTaskService {
    // The literal string matches the action provided in the manifest, so only the POST_WIDGxxx gets handled
    public static final String MY_WIDGET_DATA_UPDATED = "com.is.was.be.wannareddit.MY_WIDGET_DATA_UPDATED";
    public static final String POST_WIDGET_DATA_UPDATED = "com.is.was.be.wannareddit.POST_WIDGET_DATA_UPDATED";

    private String TAG = WannaTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStringBuilder = new StringBuilder();

    public WannaTaskService() {
    }

    public WannaTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /*
        3 constructors - minimum 1 String required.
        public TaskParams(String var1, Bundle var2, List<Uri> var3) {
        this.tag = var1;
        this.extras = var2;
        this.zzbhe = var3;
    }
     */
    @Override
    public int onRunTask(TaskParams taskParams) {

        if (mContext == null) {
            mContext = this;
        }
        String r = null;
        String c = null;

        if (taskParams.getTag().equals(DataUtility.PERIODIC_TAG)){
            r = taskParams.getExtras().getString(DataUtility.SRDD_PARAM);
            c = taskParams.getExtras().getString(DataUtility.CATEG_PARAM);
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        // Base URL for the Yahoo query
//        urlStringBuilder.append("https://www.reddit.com/r/h1d2r3/new.json?limit=2");  //Non-existing subreddit

//        urlStringBuilder.append("https://www.reddit.com/r/nosleep/new.json?limit=3");   //Paring error by curiousconcept

//        urlStringBuilder.append("https://www.reddit.com/r/todayilearned/new.json?limit=3&over_18=true");   //did not filter anything

//        urlStringBuilder.append("https://www.reddit.com/r/todayilearned/hot.json?limit=5");
/*
   &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
   LET'S EXPERIMENT - home widget should change a bit more than just one category,so

        urlStringBuilder.append("https://www.reddit.com/r/").append(r).append("/").append(c).append(".json?limit=20");

  */
        /*
           try mimicing the reddit.com
            default seems to me: bringing 'hot' posts from any categories...
         */
        urlStringBuilder.append("https://www.reddit.com/hot.json?limit=25");

        Log.v(TAG, "STRING verify: "+urlStringBuilder.toString());

        String urlString;
        String getResponse;

        JSONObject jsonObject;
        JSONArray jsonArray;

        int result = GcmNetworkManager.RESULT_FAILURE;
        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;

//                Log.d(TAG, getResponse);
                jsonObject = new JSONObject(getResponse);

                ArrayList dbOperationList = DataUtility.widgetJsonToContentVals(jsonObject, r);
                if (dbOperationList == null || dbOperationList.isEmpty()) {
                    Log.w(TAG, "new insert operation empty.");
                    return GcmNetworkManager.RESULT_FAILURE;
                }
                // Refresh widget data - no need to keep old data
                mContext.getContentResolver().delete(ForRedditProvider.WidgetContract.CONTENT_URI, null, null);

                mContext.getContentResolver().applyBatch(ForRedditProvider.AUTHORITY,
                        dbOperationList);
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(TAG, "Error applying batch insert", e);
            } catch (JSONException e) {
                Log.e(TAG, "Error Response returned threw JSONException", e);
                return -1;
            } catch (Exception allex) {
                Log.e(TAG, "Exception from GcmNetworkManager "+allex);
            }
        }

        notifyWidgetProvider();

        return 0;
    }

    private void notifyWidgetProvider() {

        /*
            Adv_And_class: Widget - Notify the WidgetProvider through WidgetIntentService action
         */
        Intent intent = new Intent(POST_WIDGET_DATA_UPDATED).setPackage(mContext.getPackageName());
        mContext.sendBroadcast(intent);
    }
}