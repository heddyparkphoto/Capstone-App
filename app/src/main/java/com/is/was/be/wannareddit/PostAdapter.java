package com.is.was.be.wannareddit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

        //non-view fields
        String subname;
        String postId;

        public PostViewHolder(View view) {
            super(view);

            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            postTextView = (TextView) view.findViewById(R.id.post_title);

            // Is there a way to set non-view values, the subname and postId?

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
    public int getItemCount() {
        Log.d(TAG, "how???");
        return 0;
    }

    // MainPagerFragement sets this after AsyncTask returns before view binding is called
    public void setPostList(ArrayList<MainPost> arr){
        this.mPostList = arr;
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
            Biggest difference in Binding - we don't get passed-in ArrayList, instead have the 'position'
            Move to that position first!
         */

        MainPost post = null;
        if (mPostList!=null && mPostList.size() > position){
            post = mPostList.get(position);
        }
        holder.postTextView.setText(post.getPostTitleLarge());
        // image
        Picasso.with(mContext).load(post.getThumburl()).into(holder.thumbnailView);
    }

    /*
        RecyclerView holder part 2
     */
    public static interface PostAdapterOnClickHandler {
        void handleOnClick (String subname, String postId, PostViewHolder passedIn);
    }

}
