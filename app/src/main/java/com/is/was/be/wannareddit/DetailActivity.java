package com.is.was.be.wannareddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.is.was.be.wannareddit.data.DataUtility;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();
    public static final String SUBNAME = "SUBNAME";
    public static final String POSTID = "POSTID";

    TextView da_timelineView;
    TextView da_authorView;
    TextView da_numberOfCommentsView;
    MainPost da_mMainPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            MainPost mMainPost = null;

            // in order to de-couple in either UI use bundle arguments
            Intent intent = getIntent();
            if (null != intent) {
                if (intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT) != null) {
                    Bundle bundle = intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT);
                    if (bundle.getParcelable(DetailFragment.PARCEL_ON_ARG) != null) {
                        mMainPost = bundle.getParcelable(DetailFragment.PARCEL_ON_ARG);
                        da_mMainPost = mMainPost;
                    } else {
                        // Nothing to-do for now.
                    }
                } else {
                    // this is a test block for the Widgets on the Home screen - DetailFragment handles it
                    String testPostid = intent.getStringExtra(POSTID);
                    String subredditNm = intent.getStringExtra(SUBNAME);
                }
            }

            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.PARCEL_ON_ARG, mMainPost);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.detailcontainer, df)
                    .add(R.id.detailcontainer_fragment, df)
                    .commit();
        }

        da_timelineView = (TextView) findViewById(R.id.timeline);
        da_authorView = (TextView) findViewById(R.id.author_by);
        da_numberOfCommentsView = (TextView) findViewById(R.id.comments_num);

        if (da_mMainPost!=null){
            if (da_mMainPost.createdUtcTime != 0L) {
                da_timelineView.setText(DataUtility.getDate(da_mMainPost.createdUtcTime));
            } else {
                da_timelineView.setText(Long.toString(da_mMainPost.createdUtcTime));
            }

            da_authorView.setText("by " + da_mMainPost.author);
            da_numberOfCommentsView.setText(String.valueOf(da_mMainPost.numComments) + " Comments");
        }
    }

}
