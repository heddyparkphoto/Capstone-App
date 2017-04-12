package com.is.was.be.wannareddit.service;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.is.was.be.wannareddit.data.DataUtility;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * Created by hyeryungpark on 4/7/17.
 */

public class FetchDetailAsyncTask extends AsyncTask <String, Void, ArrayList<String>>{

    private final static String TAG = "FetchDetailAT";
    public static final int MAX_NUM_COMMENTS = 20;

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStringBuilder = new StringBuilder();

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    protected ArrayList<String> doInBackground(String... args) {

        String subreddit = args[0];
        String postId = args[1];

        // completed url example: "https://www.reddit.com/r/todayilearned/comments/63bx3b.json"
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("www.reddit.com")
                .appendPath("r")
                .appendPath(subreddit)
                .appendPath("comments")
                .appendPath(postId.concat(".json"));

        Log.d(TAG, "new 2 VERIFY: "+ builder.build().toString());

        String getResponse;

        JSONObject jsonObject = null;
        JSONArray jsonArray = null;

        try {
            getResponse = fetchData(builder.build().toString());
            jsonArray = new JSONArray(getResponse);

        } catch (MalformedURLException e){
            Log.e(TAG, ""+e);
        } catch (IOException e){
            Log.e(TAG, ""+e);
        } catch (JSONException e){
            Log.e(TAG, ""+e);
        }

        return parseDetailData(jsonArray);
    }


    public static ArrayList<String> parseDetailData(JSONArray jo){
        ArrayList<String> returnList = null;

        if (jo.length() != 2){
            Log.e(TAG, "Unexpected response.  Cannot find details for the post.");
            return null;
        }

        try {
            JSONObject postObj = (JSONObject)jo.get(1);

            if (postObj.getJSONObject("data")!=null){
                JSONObject dOb = postObj.getJSONObject("data");
                JSONArray dataArr = dOb.getJSONArray("children");

                if (dataArr!=null) {
                    int total = dataArr.length();
                    if (total > MAX_NUM_COMMENTS){
                        total = MAX_NUM_COMMENTS;
                    }

                    returnList = new ArrayList<> ();

                    for (int i = 0; i < total; i++) {
                        JSONObject postJo = dataArr.getJSONObject(i);
                        if (null != postJo) {
                            String kindType = postJo.getString("kind");
                            if (!"t1".equalsIgnoreCase(kindType)) {
                                continue;
                            }

                            JSONObject commentObj = postJo.getJSONObject("data");
                            String aComment = commentObj.getString("body");
                            // Later in the view, |created_utc| value will be tokenized and becomes the time line of this comment

                            long crUtc = commentObj.getLong("created_utc");
                            long crLocal = commentObj.getLong("created");

//                            DataUtility.convertToTimeAgo(crUtc, crLocal);
//                            DataUtility.convertToDateFormat(crUtc);
                            String dateStr = DataUtility.getDate(crUtc);

                            aComment += " | created " + dateStr + " |";



                            returnList.add(aComment);
                        }
                    }
                }
            }

        } catch (JSONException ex){
            Log.e(TAG, ""+ ex);
        } finally {
            return returnList;
        }
    }
}
