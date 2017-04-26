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

import butterknife.BindView;
import butterknife.ButterKnife;

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
        @BindView(R2.id.thumbnail) ImageView thumbnailView;
        @BindView(R2.id.post_title) TextView postTextView;
        /*
            non-view fields, postId and subname are required to fetch detail json request
            other fields are used for Detail UI so that json parsing for the Detail can be
            focused on Comments
         */
        String postId;
        String subname;
        String author;
        long createdUtcTime;
        int numComments;
        String postTitleLarge;
        String thumburl;
        String userUrl;
        int videoFlag;

        public PostViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            subname = "";
            postId = "";
            author = "";
            createdUtcTime = 1400000L;
            numComments=0;
            postTitleLarge = "";
            thumburl = "";
            userUrl = "";
            videoFlag = 1;

            // responsible for click event
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
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
            String message = mContext.getString(R.string.noPostDataText);
            if (!Util.isOnline(mContext)){
                message = mContext.getString(R.string.noConnectivity);
            }
            mEmptyTextView.setText(message);
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (parent instanceof RecyclerView) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_post, parent, false);

            view.setFocusable(true);

            PostViewHolder holder = new PostViewHolder(view);
            return holder;
        } else {
            throw new RuntimeException("Error ** Not bound to RecyclerView Selection!!");
        }
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {

        MainPost post = null;
        if (mPostList!=null && mPostList.size() > position){
            post = mPostList.get(position);
        }
        holder.postTextView.setText(post.getPostTitleLarge());
        if (post.getThumburl()!=null && !post.getThumburl().isEmpty()) {
            // image
            Picasso.with(mContext).load(post.getThumburl()).into(holder.thumbnailView);
        } else {
            holder.thumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);  // Smaller scale set for the placeholder image
        }
        holder.postId = post.getPostId();
        holder.subname = post.getPostSubreddit();
        holder.author = post.getAuthor();
        holder.createdUtcTime = post.getCreatedUtcTime();
        holder.numComments = post.getNumComments();
        holder.postTitleLarge = post.getPostTitleLarge();
        holder.thumburl = post.getThumburl();
        holder.userUrl = post.getUserUrl();
        holder.videoFlag = post.getMedia();
    }

    /*
        RecyclerView holder
     */
    public static interface PostAdapterOnClickHandler {
        void handleOnClick (String subname, String postId, PostViewHolder passedIn);
    }

}
