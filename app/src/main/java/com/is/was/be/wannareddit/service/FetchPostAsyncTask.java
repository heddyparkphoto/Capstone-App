package com.is.was.be.wannareddit.service;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.is.was.be.wannareddit.MainPost;
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
 * Created by hyeryungpark on 4/5/17.
 */

public class FetchPostAsyncTask extends AsyncTask<String, Void, ArrayList<MainPost>> {
    private final static String TAG = "FetchPostAsyncTask";

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
    protected ArrayList<MainPost> doInBackground(String... args) {

        String subreddit = args[0];
        String category = args[1];

        // completed url example: "https://www.reddit.com/r/todayilearned/hot.json?limit=25"
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("www.reddit.com")
                .appendPath("r")
                .appendPath(subreddit)
                .appendPath(category.concat(".json"))
                .appendQueryParameter("limit", "25");

        Log.d(TAG, "VERIFY: "+ builder.build().toString());

        String getResponse;

        JSONObject jsonObject = null;
        JSONArray jsonArray;

        try {
            getResponse = fetchData(builder.build().toString());
            jsonObject = new JSONObject(getResponse);

        } catch (MalformedURLException e){
            Log.e(TAG, ""+e);
        } catch (IOException e){
            Log.e(TAG, ""+e);
        } catch (JSONException e){
            Log.e(TAG, ""+e);
        }

        return parsePostData(jsonObject);
    }


    public static ArrayList<MainPost> parsePostData(JSONObject jo){
        ArrayList<MainPost> returnList = null;

        JSONObject one;
        boolean over18;

        try {
            if (jo.getString("kind")!=null){
                JSONObject container = jo.getJSONObject("data");
                JSONArray dataArr = container.getJSONArray("children");
                if (dataArr!=null) {
                    int total = dataArr.length();

                    returnList = new ArrayList<> ();

                    for (int i = 0; i < total; i++) {
                        JSONObject postJo = dataArr.getJSONObject(i);
                        if (null != postJo) {
                            one = postJo.getJSONObject("data");
                            over18 = one.getBoolean("over_18");
                            if (over18) {
                                continue;
                            }
                            MainPost po = new MainPost();
                            po.setPostId(one.getString("id"));
                            po.setPostSubreddit(one.getString("subreddit"));
                            po.setAuthor(one.getString("author"));
                            po.setPostTitleLarge(one.getString("title"));
                            po.setCreatedUtcTime(one.getLong("created_utc"));
                            po.setNumComments(one.getInt("num_comments"));
                            po.setThumburl(one.getString("thumbnail"));
                            po.setUserUrl(one.getString("url"));

                            returnList.add(po);
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
