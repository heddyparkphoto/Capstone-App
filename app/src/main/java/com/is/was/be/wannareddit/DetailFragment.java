package com.is.was.be.wannareddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.is.was.be.wannareddit.service.FetchDetailAsyncTask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hyeryungpark on 4/7/17.
 */

public class DetailFragment extends Fragment {

    private final String TAG = DetailFragment.class.getSimpleName();

    @BindView(R2.id.post_title_text) TextView postTitle;
    @BindView(R2.id.post_image) ImageView postImage;
    @BindView(R2.id.listview_comments)
    ListView mListView;
    @BindView(R2.id.recyclerview_comments_empty) TextView mEmptyView;
    @BindView(R2.id.media_control) ImageButton mediaButton;

    // Params needed for the details api - passed in by DetailActivity or MainActivity if Tablet
    String mSubrdd;
    String mPostId;

    public final static String KEY_SUBRDDT = "KEY_SUBRDDT";
    public final static String KEY_POSTID = "KEY_POSTID";

    // Comment text ArrayAdapter
    ArrayAdapter<String> mCommentAdapter;

    public DetailFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(getActivity());

        // Find parameters need: subreddit name and the post's id

        Intent intent = getActivity().getIntent();

        if (null != intent) {
            if (intent.getStringExtra(KEY_SUBRDDT)!=null){
                mSubrdd = intent.getStringExtra(KEY_SUBRDDT);
            }
            if (intent.getStringExtra(KEY_POSTID)!=null){
                mPostId = intent.getStringExtra(KEY_POSTID);
            }
        }
        // ^^^^^^^^^^^^^ FIX CONSTANTS LATER!!! ~~~~~~~~~~~~~~~~
        if (getArguments()!= null){
            Bundle args = getArguments();
            if (args != null){
                mSubrdd = args.getString(DetailFragment.KEY_SUBRDDT);
            }
            if (args != null){
                mPostId = args.getString(DetailFragment.KEY_POSTID);
            }
        }
        if (mSubrdd==null ) mSubrdd = "todayilearned";
        if (mPostId == null) mPostId = "63bx3b";

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        ArrayList<String> comments;

        try {
//            comments = ((FetchDetailAsyncTask) new FetchDetailAsyncTask().execute("todayilearned", "63bx3b")).get();
            comments = ((FetchDetailAsyncTask) new FetchDetailAsyncTask().execute(mSubrdd, mPostId)).get();

            for (String c: comments){
                mCommentAdapter.add(c);
            }

            mListView.setAdapter(mCommentAdapter);

        } catch (InterruptedException | ExecutionException e){
            Log.e(TAG, "" + e);
        }

        super.onActivityCreated(savedInstanceState);
    }
}
