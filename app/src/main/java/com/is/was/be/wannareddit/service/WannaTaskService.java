package com.is.was.be.wannareddit.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.is.was.be.wannareddit.SubredditActivity;
import com.is.was.be.wannareddit.data.DataUtility;
import com.is.was.be.wannareddit.data.ForRedditProvider;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static com.is.was.be.wannareddit.data.ListColumns.SUBREDDITNAME;

/**
 * Created by hyeryungpark on 4/10/17.
 */

public class WannaTaskService extends GcmTaskService {
    // The literal string matches the action provided in the manifest, so only the POST_WIDGET_DATA_UPDATED gets handled
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

    @Override
    public int onRunTask(TaskParams taskParams) {

        if (mContext == null) {
            mContext = this;
        }

        String category = null;     // Currently not used, but maybe in the near future

        String taskName = taskParams.getTag();

        if (taskName.equals(DataUtility.PERIODIC_TAG) || taskName.equals(DataUtility.RUNNOW_TAG)) {
            category = taskParams.getExtras().getString(DataUtility.CATEG_PARAM);

            StringBuilder urlStringBuilder = new StringBuilder();

            /*
               Widget on the Home displays the same content as default reddit.com
               posts that are 'hot' from any categories are requested.
             */
            urlStringBuilder.append("https://www.reddit.com/hot.json?limit=25");

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

                    jsonObject = new JSONObject(getResponse);

                    ArrayList dbOperationList = DataUtility.widgetJsonToContentVals(jsonObject);
                    if (dbOperationList == null || dbOperationList.isEmpty()) {
                        Log.w(TAG, "Problem during Widget db insert operation.");
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
                    Log.e(TAG, "Exception from GcmNetworkManager " + allex);
                }
            }

            notifyWidgetProvider();

        }  else if (taskName.equalsIgnoreCase(DataUtility.ADD_TAG)){

            String newname = taskParams.getExtras().getString(DataUtility.SRDD_PARAM);

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("www.reddit.com")
                    .appendPath("r")
                    .appendPath(newname.concat(".json"))
                    .appendQueryParameter("limit", "1");

            String urlString;
            String getResponse;

            JSONObject jsonObject;
            JSONArray jsonArray;

            int result = GcmNetworkManager.RESULT_FAILURE;
            try {
                getResponse = fetchData(builder.build().toString());
                result = GcmNetworkManager.RESULT_SUCCESS;

                jsonObject = new JSONObject(getResponse);

                if (jsonObject != null && jsonObject.length() != 0) {
                    if (jsonObject.getString("kind") != null) {
                        JSONObject container = jsonObject.getJSONObject("data");
                        JSONArray dataArr = container.getJSONArray("children");
                        if (null!=dataArr && dataArr.length() > 0){

                            ContentValues cv = new ContentValues(1);
                            cv.put(SUBREDDITNAME, newname);
                            Uri uri = mContext.getContentResolver().insert(
                                    ForRedditProvider.MainContract.CONTENT_URI, cv);
                        } else {
                            Log.i(TAG, "Response was empty for Searched Subreddit Name.");
                            return SubredditActivity.UNKNOWN_SUBREDDIT;
                        }
                    }
                } else {
                    //ADD failed, Log and return Failure code to the WannaIntentService
                    Log.i(TAG, "Validating failed.  No response from the site.");
                    return SubredditActivity.NO_RESPONSE;
                }
            } catch (MalformedURLException e){
                Log.e(TAG, ""+e);
            } catch (IOException e){
                Log.e(TAG, ""+e);
                return SubredditActivity.NO_RESPONSE;
            } catch (JSONException e){
                Log.e(TAG, ""+e);
                return SubredditActivity.UNKNOWN_SUBREDDIT;
            }
        }

        return 0;
    }

    private void notifyWidgetProvider() {

        /*
            Notify the WidgetProvider through WidgetIntentService action
         */
        Intent intent = new Intent(POST_WIDGET_DATA_UPDATED).setPackage(mContext.getPackageName());
        mContext.sendBroadcast(intent);
    }

}
