package com.is.was.be.wannareddit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.is.was.be.wannareddit.data.ForRedditProvider;
import com.is.was.be.wannareddit.data.ListColumns;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        LoaderManager.LoaderCallbacks<Cursor>,
        MainPagerFragment.OnPostItemSelectedListener
{
    private final static String TAG = MainActivity.class.getSimpleName();

    // Play service variables
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected GoogleApiClient mGoogleApiClient;

    @BindView(R2.id.toolbar) Toolbar toolbar;
    @BindView(R2.id.interpolator_spinner) Spinner spinner;
    @BindView(R2.id.container) ViewPager mViewPager;   //The {@link ViewPager} that will host the section contents.

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // Subreddit table handlers
    private static final int LOADER_ID = 22;
    private SimpleCursorAdapter mAdapter;
    private Cursor mCursor;
    public String mCurrentSubredditChoice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        /*
            Popping the spinner from cursor - Credit to this site: Thanks to:
            http://codetheory.in/understanding-and-populating-android-spinners/
            (A note about the site though, there is a ziggling AD to the right on this site)
         */
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.spinner_text_control,
                mCursor,
                new String[]{ListColumns.SUBREDDITNAME},
                new int[]{R.id.itemInSpinner},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // initialize to set the mAdapter to our spinner
        loadSpinner();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    buildMyAwarenessGclient();

    checkPlayServices();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.refresh) {
//            Intent intent = new Intent(this, TabsActivity.class);
//            startActivity(intent);
            return true;
        }

        if (id == R.id.add) {
            // Subreddit managing activity - Use Dialog
            Intent intent = new Intent(this, SubredditActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
//        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
//        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient!=null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    protected synchronized void buildMyAwarenessGclient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApi connected!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed");
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()){
            Log.i(TAG, "Activity Succeeded");
        } else {
            Log.e(TAG, "Activity detection failed: "+status.toString());
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Load data into the Spinner
        return new CursorLoader(this, ForRedditProvider.MainContract.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Load data into the Spinner
        mAdapter.swapCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
    private void loadSpinner() {

        // Specify the layout to use when the list of choices appears
        if (mAdapter!=null) {
            mAdapter.setDropDownViewResource(R.layout.spinner_textview);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    mCurrentSubredditChoice = ((TextView) view).getText().toString();
//                Toast.makeText(getApplicationContext(), mCurrentSubredditChoice, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner.setAdapter(mAdapter);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        // For now, afford 5 categories that are similar to each other and more unique to the 'reddit'
        String[] fiveCategories = {"HOT", "NEW", "RISING", "CONTROVERSIAL", "TOP"};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a MainPagerFragment instance for each category - important: category names
            // are part of main recycler view's parameter when invoking reddit public api.
            String inLowerCase = fiveCategories[position].toLowerCase();

            return MainPagerFragment.newInstance(position + 1, inLowerCase, mCurrentSubredditChoice);
        }

        @Override
        public int getCount() {
            // This matches the available tabs
            return fiveCategories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return fiveCategories[0];
                case 1:
                    return fiveCategories[1];
                case 2:
                    return fiveCategories[2];
                case 3:
                    return fiveCategories[3];
                case 4:
                    return fiveCategories[4];

                default:
                    return fiveCategories[0];
            }
        }
    }

    @Override
    public void OnPostItemClick(String subname, String postId) {
//        Intent intent = new Intent(this, SubredditActivity.class);
//        Bundle args = new Bundle();
//        args.putString("SUB", subname);
//        args.putString("POID", postId);
//        intent.putExtra("BUNDLE", args);
//        startActivity(intent);
    }
}
