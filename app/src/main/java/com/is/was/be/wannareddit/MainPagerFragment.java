package com.is.was.be.wannareddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.is.was.be.wannareddit.service.FetchPostAsyncTask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by hyeryungpark on 4/4/17.
 */

public class MainPagerFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = MainPagerFragment.class.getSimpleName();
    private RecyclerView mRecylerView;

    private PostAdapter mPostAdapter;

    public MainPagerFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainPagerFragment newInstance(int sectionNumber, String category) {
        MainPagerFragment fragment = new MainPagerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        TextView emptyView = (TextView) rootView.findViewById(R.id.recyclerview_post_empty);

        mPostAdapter = new PostAdapter(
                getActivity(),
                new PostAdapter.PostAdapterOnClickHandler() {
                    @Override
                    public void handleOnClick(String subname, String postId, PostAdapter.PostViewHolder passedIn) {
                        ((OnPostItemSelectedListener)getActivity()).OnPostItemClick(subname, postId);
                    }
                },
                emptyView);

        mRecylerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_post);
        mRecylerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecylerView.setAdapter(mPostAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        // Fetch data using AsyncTask
        ArrayList<MainPost> posts = null;
        try {
            posts = ((FetchPostAsyncTask) new FetchPostAsyncTask().execute("todayilearned")).get();
        } catch (InterruptedException | ExecutionException e){
            Log.e(TAG, "" + e);
        }
        mPostAdapter.setPostList(posts);

        super.onActivityCreated(savedInstanceState);
    }

    public interface OnPostItemSelectedListener{
        /*
            DetailFragment Callback when an item is selected
         */
        public void OnPostItemClick(String subname, String postId);

    }
}