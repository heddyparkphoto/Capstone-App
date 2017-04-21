package com.is.was.be.wannareddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hyeryungpark on 4/21/17.
 */

public class CardFullImageActivity extends AppCompatActivity {

    public static final String FULL_IMAGE_URL = "FULL_IMAGE_URL";
    @BindView(R2.id.post_image_full) ImageView fullImage;
    String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_full_image);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {

            Intent intent = getIntent();
            if (null != intent && intent.getStringExtra(FULL_IMAGE_URL) != null) {
                mUrl = intent.getStringExtra(FULL_IMAGE_URL);
            }
        }

    }

    @Override
    protected void onResume() {
        if (mUrl!=null && !mUrl.isEmpty()){
            Picasso.with(this).load(mUrl).into(fullImage);
        }
        super.onResume();
    }
}
