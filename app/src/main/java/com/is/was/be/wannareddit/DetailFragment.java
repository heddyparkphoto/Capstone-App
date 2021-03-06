package com.is.was.be.wannareddit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.is.was.be.wannareddit.data.DataUtility;
import com.is.was.be.wannareddit.service.FetchDetailAsyncTask;
import com.is.was.be.wannareddit.service.FetchPostAsyncTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Intent.ACTION_VIEW;

/**
 * Created by hyeryungpark on 4/7/17.
 */

public class DetailFragment extends Fragment {

    public static final String PARCEL_ON_ARG = "PARCEL_ON_ARG";
    public static final String GET_POST_ARG = "GET_POST_ARG";
    public static final String EXTRA_ON_INTENT = "EXTRA_ON_INTENT";
    public static final String PARCEL_SAVED_STATE = "PARCEL_SAVED_STATE";

    private final String TAG = DetailFragment.class.getSimpleName();

    @BindView(R2.id.post_title_text) TextView postTitle;
    @BindView(R2.id.post_image) ImageView postImage;
    @BindView(R2.id.listview_comments) ListView mListView;
    @BindView(R2.id.recyclerview_comments_empty) TextView mEmptyView;
    @BindView(R2.id.media_control) ImageButton mediaButton;
    Unbinder unbinder;
    @BindString(R2.string.default_subreddit) String defaultSubname;
    @BindString(R2.string.default_postid) String defaultPostname;

    // Params needed for the details api - passed in by DetailActivity or MainActivity if Tablet
    String mSubrdd;
    String mPostId;

    private MainPost mFragPost;

    // Comment text ArrayAdapter
    ArrayAdapter<String> mCommentAdapter;

    public DetailFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        Intent intent = getActivity().getIntent();
        if (null != intent && intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT) != null) {
            Bundle bundle = intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT);
            if (bundle.getParcelable(DetailFragment.PARCEL_ON_ARG) != null) {
                mFragPost = bundle.getParcelable(DetailFragment.PARCEL_ON_ARG);
                mSubrdd = mFragPost.getPostSubreddit();
                mPostId = mFragPost.getPostId();
            }
        } else {
            if (getArguments() != null) {
                Bundle args = getArguments();
                if (args != null) {
                    if (args.getParcelable(DetailFragment.PARCEL_ON_ARG) != null) {
                        mFragPost = args.getParcelable(DetailFragment.PARCEL_ON_ARG);
                        if (mFragPost != null) {
                            mSubrdd = mFragPost.getPostSubreddit();
                            mPostId = mFragPost.getPostId();
                        }
                    } else if (args.getStringArray(DetailFragment.GET_POST_ARG) != null){
                        String[] lookupinfo = args.getStringArray(DetailFragment.GET_POST_ARG);
                        if (lookupinfo==null || lookupinfo.length < 2){
                            Log.e(TAG, "Missing data for lookup for mTwoPane mode.");
                        } else {
                            String mySubrdd = lookupinfo[0];
                            String myCat = lookupinfo[1];
                            if (Util.isOnline(getActivity())) {
                                runTwoPaneSupport(mySubrdd, myCat);
                            }
                        }
                    }
                }
            }
        }

        // Default post that hopefully never needs to be used, but safegaurd the Detail view
        if (mSubrdd==null ) mSubrdd = defaultSubname;
        if (mPostId==null) mPostId = defaultPostname;

        return rootView;
    }

    // Default (first screen) of Master/Detail mode supports initial post to be populated with the app default
    private void runTwoPaneSupport(String mysubname, String myCategory) {
        // Fetch data using AsyncTask - we'll only get one MainPost in the form of a list
        ArrayList<MainPost> posts = null;
        try {

            posts = ((FetchPostAsyncTask) new FetchPostAsyncTask().execute(mysubname, "", "", myCategory)).get();

            if (posts!=null && !posts.isEmpty()){
                mFragPost = posts.get(0);
                if (mFragPost!=null){
                    mSubrdd = mFragPost.getPostSubreddit();
                    mPostId = mFragPost.getPostId();

                    // Populate the view's in the TwoPane layout for First-launched default post
                    Activity ma = getActivity();
                    // Verify it's the MainActivity which began this fragment
                    if (ma instanceof MainActivity){

                        TextView timeView = ((MainActivity) ma).ma_timelineView;
                        TextView authorView = ((MainActivity) ma).ma_authorView;
                        TextView numberView = ((MainActivity) ma).ma_numberOfCommentsView;

                        if (timeView!=null){
                            if (mFragPost.createdUtcTime != 0L){
                                timeView.setText(DataUtility.getDate(mFragPost.createdUtcTime));
                            } else {
                                timeView.setText(Long.toString(mFragPost.createdUtcTime));
                            }
                        }

                        if (authorView!=null) {
                            authorView.setText("by " + mFragPost.author);
                        }
                        if (numberView!=null) {
                            numberView.setText(String.valueOf(mFragPost.numComments) + " Comments/Threads");
                        }
                    }
                }
            } else {
                Log.e(TAG, "Post for TwoPane didn't return.");
            }

        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "" + e);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        ArrayList<String> comments;

        try {

            if (Util.isOnline(getActivity())) {
                comments = ((FetchDetailAsyncTask) new FetchDetailAsyncTask().execute(mSubrdd, mPostId)).get();

                if (null != comments) {
                    mCommentAdapter = new ArrayAdapter<>(getActivity(), R.layout.one_comment);
                    for (String c : comments) {
                        mCommentAdapter.add(c);
                    }
                }

                mListView.setAdapter(mCommentAdapter);
                setListViewHeightBasedOnChildren(mListView);

                if (mFragPost != null) {
                    postTitle.setText(mFragPost.getPostTitleLarge());
                    if (mFragPost.getUserUrl() != null) {
                        postTitle.setTextColor(getResources().getColor(R.color.linkcolor));
                        postTitle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri userPage = Uri.parse(mFragPost.getUserUrl());
                                Intent intent = new Intent(ACTION_VIEW, userPage);
                                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(intent);
                                } else {
                                    Log.w(TAG, "No app found to open the site: " + mFragPost.getUserUrl());
                                }
                            }
                        });
                    }
                    if (mFragPost.getMedia() == DataUtility.AVAIL_INT) {
                        mediaButton.setVisibility(View.VISIBLE);
                        mediaButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri userPage = Uri.parse(mFragPost.getUserUrl());
                                Intent intent = new Intent(ACTION_VIEW, userPage);
                                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(intent);
                                } else {
                                    Log.w(TAG, "No app found to open the site: " + mFragPost.getUserUrl());
                                }
                            }
                        });
                    } else {
                        mediaButton.setVisibility(View.GONE);
                    }

                    if (mFragPost.getThumburl() != null && !mFragPost.getThumburl().isEmpty()) {
                        Picasso.with(getActivity()).load(mFragPost.getThumburl()).into(postImage);
                    } else {
                        postImage.setVisibility(View.GONE);
                    }
                }
            } else {
                if (postTitle!=null) {
                    postTitle.setText(getString(R.string.noConnectivity));
                }
            }
        } catch (InterruptedException | ExecutionException e){
            Log.e(TAG, "" + e);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(PARCEL_SAVED_STATE, mFragPost);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /*
        Credit to the chosen answer on:
        http://stackoverflow.com/questions/9690246/why-does-my-android-activity-always-start-scrolled-to-the-bottom
        Coded in fragment_detail.xml in the LinearLayout.
     */

    /* Solution to make the entire Detail fragment to scroll including the ListView
       Credit to this site's chosen answer and one suggested Fix in the user comments:
       http://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view
    */
    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, Toolbar.LayoutParams.WRAP_CONTENT));

            view.measure(View.MeasureSpec.makeMeasureSpec(desiredWidth, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
