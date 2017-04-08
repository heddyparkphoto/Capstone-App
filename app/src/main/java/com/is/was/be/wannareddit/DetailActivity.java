package com.is.was.be.wannareddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            String subName = "";
            String postId = "";

            // in order to de-couple in either UI use bundle arguments
            Intent intent = getIntent();
            if (null != intent) {
                if (intent.getBundleExtra("BUNDLE")!= null) {
                    Bundle bundle = intent.getBundleExtra("BUNDLE");

                    if (bundle.getString("SUB") != null) {
                        subName = bundle.getString("SUB");
                    }
                    if (bundle.getString("POID") != null) {
                        postId = bundle.getString("POID");
                    }
                }
            }

            Bundle args = new Bundle();
            args.putString(DetailFragment.KEY_SUBRDDT, subName);
            args.putString(DetailFragment.KEY_POSTID, postId);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detailcontainer, df)
                    .commit();
        }
    }
}
