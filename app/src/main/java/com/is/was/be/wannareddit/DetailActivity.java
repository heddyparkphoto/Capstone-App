package com.is.was.be.wannareddit;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.is.was.be.wannareddit.data.DataUtility;
import com.is.was.be.wannareddit.service.FetchPostAsyncTask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();
    public static final String SUBNAME = "SUBNAME";
    public static final String POSTID = "POSTID";
    private static final String FRAG_TAG="FRAG_TAG";

    @BindView (R2.id.timeline) TextView da_timelineView;
    @BindView (R2.id.author_by) TextView da_authorView;
    @BindView (R2.id.comments_num) TextView da_numberOfCommentsView;
    @BindString(R2.string.a11y_num_comments) String srNumComments;
    @BindString(R2.string.a11y_by_who) String srBy;

    MainPost mMainPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            mMainPost = null;

            // in order to de-couple in either UI use bundle arguments
            Intent intent = getIntent();
            if (null != intent) {
                if (intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT) != null) {
                    Bundle bundle = intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT);
                    if (bundle.getParcelable(DetailFragment.PARCEL_ON_ARG) != null) {
                        mMainPost = bundle.getParcelable(DetailFragment.PARCEL_ON_ARG);

                    } else {
                        Log.w(TAG, "MainPost was null on the Intent parcel");
                    }
                } else if (intent.getStringExtra(POSTID)!=null){
                    // Case of: Widget FillInIntent - Request AsyncTask to populate the rest of fields and construct a MainPost
                    runExtra(intent.getStringExtra(SUBNAME), intent.getStringExtra(POSTID));
                }
            }

            // Pass the mMainPost either gotten from the Parcelable or from running the runExtra
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.PARCEL_ON_ARG, mMainPost);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.detailcontainer, df)
                    .add(R.id.detailcontainer_fragment, df, FRAG_TAG)
                    .commit();
        }

        if (mMainPost!=null){
            if (mMainPost.createdUtcTime != 0L) {
                da_timelineView.setText(DataUtility.getDate(mMainPost.createdUtcTime));
            } else {
                da_timelineView.setText(Long.toString(mMainPost.createdUtcTime));
            }
Log.d(TAG, " i am a new code in detail");
            da_authorView.setText(String.format(srBy, mMainPost.author));
            da_numberOfCommentsView.setText(String.format(srNumComments, mMainPost.numComments));
        }
    }
    private void runExtra(String mysubname, String mypostId) {
        // Fetch data using AsyncTask - we'll only get one MainPost in the form of a list
        ArrayList<MainPost> posts = null;
        try {

            posts = ((FetchPostAsyncTask) new FetchPostAsyncTask().execute(mysubname, "", mypostId)).get();

            if (posts!=null && !posts.isEmpty()){
                mMainPost = posts.get(0);
            } else {
                Log.e(TAG, "post for Widget didn't return.");
            }

        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "" + e);
        }



    }

    /*
        Instructive Motion applied to the small Image area to help user the image can be scrolled
     */
    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        DetailFragment myfrag = (DetailFragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG);
        if (myfrag!=null){
            ScrollView myscroll = myfrag.scrollImageContainer;
            if (myscroll!=null) {
                final int startScrollPos = getResources().getDimensionPixelSize(
                        R.dimen.init_scroll_up_distance);

                Animator animator = ObjectAnimator.ofInt(
                        myscroll,
                        "scrollY",
                        startScrollPos)
                        .setDuration(300);
                animator.start();
            }
        }
    }
}
