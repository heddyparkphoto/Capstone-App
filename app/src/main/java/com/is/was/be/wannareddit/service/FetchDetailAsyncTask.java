package com.is.was.be.wannareddit.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
        final String COMMENTS_BASE_STRING = "https://www.reddit.com/r/";
        final String COMMENTS = "comments";
        final String ENDING_BASE_STRING = ".json";

        StringBuilder urlStringBuilder = new StringBuilder(COMMENTS_BASE_STRING);
//        urlStringBuilder.append("https://www.reddit.com/r/todayilearned/comments/63bx3b.json");
        urlStringBuilder.append(subreddit).append("/").append(COMMENTS).append("/").append(postId).append(ENDING_BASE_STRING);

        Log.d(TAG, "VERIFY: "+urlStringBuilder.toString());

        String returnStr = null;
        String getResponse;

        JSONObject jsonObject = null;
        JSONArray jsonArray = null;

        URL url;
        try {
            url = new URL(urlStringBuilder.toString());
            getResponse = fetchData(urlStringBuilder.toString());
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

        JSONObject one;

        if (jo.length() != 2){
            Log.e(TAG, "Unexpected response.  Cannot find details for the post.");
            return null;
        }

        try {
            JSONObject postObj = (JSONObject)jo.get(1);

            if (postObj.getJSONObject("data")!=null){
                JSONObject pOb = postObj.getJSONObject("data");
                JSONArray dataArr = pOb.getJSONArray("children");

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

//                            JSONObject comment = commentObj.getJSONObject("body");
//                            String aComment = commentObj.getJSONObject("body").toString();
                            String aComment = commentObj.getString("body");
//                            if (aComment!=null){
//                                aComment = aCommentObj.toString();
//                            }
                            aComment += " created utc " + commentObj.getLong("created_utc");
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
