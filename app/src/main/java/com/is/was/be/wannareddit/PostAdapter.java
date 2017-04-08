package com.is.was.be.wannareddit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hyeryungpark on 4/4/17.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>  {

    final private static String TAG = "PostAdapter";
    final private Context mContext;

    private final PostAdapterOnClickHandler mClickHandler;
    private final TextView mEmptyTextView;
    ArrayList<MainPost> mPostList;

    public PostAdapter(Context context, PostAdapterOnClickHandler handler, TextView emptyView) {
        mContext = context;
        mClickHandler = handler;
        mEmptyTextView = emptyView;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView thumbnailView;
        TextView postTextView;

        //non-view fields, required to fetch detail json request
        String subname;
        String postId;

        public PostViewHolder(View view) {
            super(view);

            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            postTextView = (TextView) view.findViewById(R.id.post_title);
            subname = "";
            postId = "";

            // responsible for click event
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.handleOnClick(subname, postId, this);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        if (mPostList!=null){
            return mPostList.size();
        } else {
            return 0;
        }
    }

    // MainPagerFragement sets this after AsyncTask returns before view binding is called
    public void setPostList(ArrayList<MainPost> arr){

        this.mPostList = arr;
        if (getItemCount()==0){
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (parent instanceof RecyclerView) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_post, parent, false); // Also context from the ViewGroup

            view.setFocusable(true);

            PostViewHolder holder = new PostViewHolder(view);
            return holder;
        } else {
            throw new RuntimeException("Error ** Not bound to RecyclerView Selection!!");
        }
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        /*
            Weirdest problem I have caused at first - the postiton would only be 0
            displaying a single item ever...
            From stackoverflow googling - answers to 2 same problems were - one_post.xml
            outermost Layout was the LinearLayout height was the 'match_parent'
            should be wrap_content or other dimen but never the match_parent ...
            I guess the LayoutManager that we set on the RecyclerView at first, knows it does not
            need to layout any more children than just once !!! what a weird but clear
            management !!! most efficient :)
            I'm glad I tried their answers - learned that solution sometimes is much easier
            than the problem.. :)
         */

        MainPost post = null;
        if (mPostList!=null && mPostList.size() > position){
            post = mPostList.get(position);
        }
        holder.postTextView.setText(post.getPostTitleLarge());
        // image
        Picasso.with(mContext).load(post.getThumburl()).into(holder.thumbnailView);
        holder.postId = post.getPostId();
        holder.subname = post.getPostSubreddit();
    }

    /*
        RecyclerView holder
     */
    public static interface PostAdapterOnClickHandler {
        void handleOnClick (String subname, String postId, PostViewHolder passedIn);
    }

}
