package com.is.was.be.wannareddit.data;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.is.was.be.wannareddit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by hyeryungpark on 4/10/17.
 */

public class DataUtility {

    // common constants
    public final static String PERIODIC_TAG = "widget";
    public final static String SRDD_PARAM = "rdd_param";
    public final static String CATEG_PARAM = "cat_param";
    public final static String ADD_TAG = "add";
    public static final String RECEIVER = "receiver";
    public final static String RUNNOW_TAG = "runnow";

    // DB sortOrder
    public static final String sortOrder = ListColumns.SUBREDDITNAME + " ASC ";

    // Flag value indicator used for multiple classes
    public static final int AVAIL_INT = 1;
    public static final int NOT_AVAIL_INT = -1;

    private final static String TAG = "DataUtility";

    public static ArrayList widgetJsonToContentVals(JSONObject jo) throws JSONException {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

        JSONObject one;
        boolean over18;

        try {
            if (jo != null && jo.length() != 0) {
                if (jo.getString("kind")!=null){
                    JSONObject container = jo.getJSONObject("data");
                    JSONArray dataArr = container.getJSONArray("children");
                    for (int i=0; i<dataArr.length(); i++){
                        JSONObject postJo = dataArr.getJSONObject(i);
                        if (null!=postJo){
                            one = postJo.getJSONObject("data");
                            over18 = one.getBoolean("over_18");
                            if (over18){
                                continue;
                            }
                            batchOperations.add(buildBatchOperation(one));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing failed: " + e);
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong, could not apply batch ops. " + e);
        }
        return batchOperations;
    }

    private static ContentProviderOperation buildBatchOperation(JSONObject jo) throws JSONException{
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                ForRedditProvider.WidgetContract.CONTENT_URI);
        try {
            String postTitle = jo.getString("title");
            if (postTitle==null || postTitle.trim().length()==0){
                postTitle = jo.getString("url");   // we'll try substitution
            }
            String subreddit = jo.getString("subreddit");
            String poid = jo.getString("id");
            builder.withValue(WidgetColumns.POSTID, poid);
            builder.withValue(WidgetColumns.SUBRED, subreddit);
            builder.withValue(WidgetColumns.POST, postTitle);

        } catch (JSONException e) {
            e.printStackTrace();
            throw e;
        }
        return builder.build();
    }

    public static String getDate(long utcMillis) {
        // Create a DateFormatter object for displaying date in specified format.
        String dateFormat = "MM/dd/ HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(utcMillis*1000);

        return formatter.format(calendar.getTime());
    }

    public static long getTimeFencingTimePreference(Context context){

        String tfPreference;
        SharedPreferences shared = getDefaultSharedPreferences(context);
        if (shared!=null) {
            tfPreference = shared.getString(context.getString(R.string.pref_timefence_key),
                                            context.getString(R.string.default_timefence));
        } else {
          tfPreference = context.getString(R.string.default_timefence);
        }

        return Long.parseLong(tfPreference);
    }

    public static String getSubredditPreference(Context context){

        String srPreference = "";
        SharedPreferences shared = getDefaultSharedPreferences(context);
        if (shared!=null) {
            srPreference = shared.getString(context.getString(R.string.pref_subrdd_key), "");
        }

        return srPreference;
    }
}
