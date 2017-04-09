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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by hyeryungpark on 4/7/17.
 */

public class DetailFragment extends Fragment {

    public static final String PARCEL_ON_ARG = "PARCEL_ON_ARG";
    public static final String EXTRA_ON_INTENT = "EXTRA_ON_INTENT";

    private final String TAG = DetailFragment.class.getSimpleName();

//    @BindView(R2.id.post_title_text) TextView postTitle;
//    @BindView(R2.id.post_image) ImageView postImage;
//    @BindView(R2.id.listview_comments) ListView mListView;
//    @BindView(R2.id.recyclerview_comments_empty) TextView mEmptyView;
//    @BindView(R2.id.media_control) ImageButton mediaButton;

    TextView postTitle;
    ImageView postImage;
    ListView mListView;
    TextView mEmptyView;
    ImageButton mediaButton;

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

        postTitle = (TextView) rootView.findViewById(R.id.post_title_text);
        postImage = (ImageView) rootView.findViewById(R.id.post_image);
        mListView = (ListView) rootView.findViewById(R.id.listview_comments);
        mEmptyView= (TextView) rootView.findViewById(R.id.recyclerview_comments_empty);
        mediaButton= (ImageButton) rootView.findViewById(R.id.media_control);

//        ButterKnife.bind(getActivity());


        Intent intent = getActivity().getIntent();
        if (null != intent) {
            if (intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT) != null) {
                Bundle bundle = intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT);
                if (bundle.getParcelable(DetailFragment.PARCEL_ON_ARG) != null) {
                    mFragPost = bundle.getParcelable(DetailFragment.PARCEL_ON_ARG);
                    mSubrdd = mFragPost.getPostSubreddit();
                    mPostId = mFragPost.getPostId();
                } else {
                    // Nothing to-do for now.
                }
            }
        } else {
            if (getArguments() != null) {
                Bundle args = getArguments();
                if (args != null) {
                    mFragPost = args.getParcelable(DetailFragment.PARCEL_ON_ARG);
                    if (mFragPost!=null) {
                        mSubrdd = mFragPost.getPostSubreddit();
                        mPostId = mFragPost.getPostId();
                    }
                }
            }
        }

        // Default post that hopefully never needs to be used, but safegaurd the Detail view
        if (mSubrdd==null ) mSubrdd = "todayilearned";
        if (mPostId == null) mPostId = "63bx3b";

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        ArrayList<String> comments;

        try {

            comments = ((FetchDetailAsyncTask) new FetchDetailAsyncTask().execute(mSubrdd, mPostId)).get();

            if (null!=comments){
                mCommentAdapter = new ArrayAdapter<>(getActivity(), R.layout.one_comment);
            }
            for (String c: comments){
                mCommentAdapter.add(c);
            }

            mListView.setAdapter(mCommentAdapter);
            if (mFragPost!=null) {
                postTitle.setText(mFragPost.getPostTitleLarge());
                if (mFragPost.getThumburl()!=null) {
                    Picasso.with(getActivity()).load(mFragPost.getThumburl()).into(postImage);
                }
            }
        } catch (InterruptedException | ExecutionException e){
            Log.e(TAG, "" + e);
        }

        super.onActivityCreated(savedInstanceState);
    }
}
