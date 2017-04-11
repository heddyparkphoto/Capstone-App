package com.is.was.be.wannareddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String SUBNAME = "SUBNAME";
    public static final String POSTID = "POSTID";

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
                    } else {
                        // Nothing to-do for now.
                    }
                }
            }

            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.PARCEL_ON_ARG, mMainPost);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detailcontainer, df)
                    .commit();
        }
    }
}
