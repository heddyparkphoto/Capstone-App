package com.is.was.be.wannareddit.data;

import android.content.ContentProviderOperation;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hyeryungpark on 4/10/17.
 */

public class DataUtility {

    // common constants
    public final static String PERIODIC_TAG = "widget";
    public final static String SRDD_PARAM = "rdd_param";
    public final static String CATEG_PARAM = "cat_param";


    private final static String TAG = "DataUtility";

    public static ArrayList widgetJsonToContentVals(JSONObject jo, String subNm) throws JSONException {
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
                            batchOperations.add(buildBatchOperation(one, subNm));
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

    private static ContentProviderOperation buildBatchOperation(JSONObject jo, String subreddit) throws JSONException{
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                ForRedditProvider.WidgetContract.CONTENT_URI);
        try {
            String postTitle = jo.getString("title");
            if (postTitle==null || postTitle.trim().length()==0){
                postTitle = jo.getString("url");   // we'll try substitution
            }
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

    public static long convertToTimeAgo(long crUtc, long crLocal) {
//
//        long nowSeconds = System.currentTimeMillis()/1000;
//
//        Log.d(TAG, "now "+ nowSeconds + " crUtc " +crUtc + "    lapsed    " + (nowSeconds - crUtc));
//        Log.d(TAG, "local "+ nowSeconds + " crLocal " +crLocal + " lapsed " + (nowSeconds - crLocal));
//
//
//
        return 0L;
    }

    public static String convertToDateFormat(long utcMillis){

        Date date = new Date(utcMillis);
        String str = DateFormat.getDateInstance().format(date);
        Log.d(TAG, "utc "+ utcMillis + " our date " + str);

        return DateFormat.getDateInstance().format(date);

        //return "12-30-2017";
    }

    public static String getDate(long utcMillis) {
        // Create a DateFormatter object for displaying date in specified format.
        String dateFormat = "MM/dd/ HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(utcMillis*1000);
        String str = formatter.format(calendar.getTime());

        Log.d(TAG, "utc "+ utcMillis + " our date " + str);
        return formatter.format(calendar.getTime());
    }
}
