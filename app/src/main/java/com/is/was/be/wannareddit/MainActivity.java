package com.is.was.be.wannareddit;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.is.was.be.wannareddit.data.DataUtility;
import com.is.was.be.wannareddit.data.ForRedditProvider;
import com.is.was.be.wannareddit.data.ListColumns;
import com.is.was.be.wannareddit.service.WannaTaskService;

import java.util.ArrayList;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        LoaderManager.LoaderCallbacks<Cursor>,
        MainPagerFragment.OnPostItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener
{
    private final static String TAG = MainActivity.class.getSimpleName();

    // Play service variables
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected GoogleApiClient mGoogleApiClient;
    // Declare variables for pending intent and fence receiver.
    private final static String FENCE_RECIEVER_ACTION = "FENCE_RECIEVER_ACTION";
    private final static String FENCE_KEY = "timeFenceKey";
    private PendingIntent myPendingIntent;
    private MyFenceReceiver myFenceReceiver;
    private AwarenessFence mTimeFence;
    private long mPrefTimeFenceMinutes;
    private long FENCE_NEVER = 999L;

    @BindView(R2.id.main_content) CoordinatorLayout mCoorLayout;
    @BindView(R2.id.toolbar) Toolbar toolbar;
    @BindView(R2.id.subrddt_spinner) Spinner spinner;
    @BindView(R2.id.container) ViewPager mViewPager;   //The {@link ViewPager} that will host the section contents.
    @BindString(R2.string.pref_subrdd_key) String mPrefSubrddKey;
    @BindString(R2.string.a11y_num_comments) String srNumComments;
    @BindString(R2.string.a11y_by_who) String srBy;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // Subreddit table handlers in the spinner
    private static final int LOADER_ID = 22;
    private SimpleCursorAdapter mAdapter;
    private Cursor mCursor;
    public String mCurrentSubredditChoice;
    private ArrayList<String> mSpinnerList;
    private int mSpinnerIdx = Spinner.INVALID_POSITION;  // if there isn't a position
    private static final String SPIN_TO_POSITION = "SPIN_TO_POSITION";
    private static final String BROWSING_SUBNAME = "BROWSING_SUBNAME";

    // Following codes added during Tablet Lesson
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    // TwoPane-mode-only views - not present in other modes
    public TextView ma_timelineView;
    public TextView ma_authorView;
    public TextView ma_numberOfCommentsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        /*
            Populating the spinner from cursor - Credit: Thanks to:
            http://codetheory.in/understanding-and-populating-android-spinners/
         */
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.spinner_text_control,
                mCursor,
                new String[]{ListColumns.SUBREDDITNAME},
                new int[]{R.id.itemInSpinner},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // initialize to set the mAdapter to our spinner
        loadSpinner();

        if (savedInstanceState != null) {
            mSpinnerIdx = savedInstanceState.getInt(SPIN_TO_POSITION);
            mCurrentSubredditChoice = savedInstanceState.getString(BROWSING_SUBNAME);
        } else {
            SharedPreferences shared = getDefaultSharedPreferences(this);
            mCurrentSubredditChoice = shared.getString(mPrefSubrddKey, "");
        }

        MainPost post=null;

        if (findViewById(R.id.detailcontainer_fragment) != null){
            mTwoPane = true;

            ma_timelineView = (TextView) findViewById(R.id.timeline);
            ma_authorView = (TextView) findViewById(R.id.author_by);
            ma_numberOfCommentsView = (TextView) findViewById(R.id.comments_num);

            if (savedInstanceState == null) {
                DetailFragment df = new DetailFragment();
                if (post != null){
                    Bundle args = new Bundle();
                    args.putParcelable((DetailFragment.PARCEL_ON_ARG), post);
                    df.setArguments(args);
                } else {
                    Bundle args = new Bundle();
                    args.putStringArray(DetailFragment.GET_POST_ARG, new String[]{mCurrentSubredditChoice, "hot"});
                    df.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailcontainer_fragment, df, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        // Create the adapter that will return a fragment for first two
        // category sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        buildMyAwarenessGclient();

        // Schedule Periodic Task for Widgets (our widget setting is everything in
        // category "hot" as in reddit's default front screen)
        Bundle bundle = new Bundle();
        bundle.putString(DataUtility.CATEG_PARAM, "hot");

        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setService(WannaTaskService.class)
                .setExtras(bundle)
                .setUpdateCurrent(true)     // Periodic task is built if there's none ensuring only one is built
                .setFlex(10L)
                .setPeriod(3600L)           // once per hour 60min*60sec = 3600 long type - Widget has option of refresh immediately
                .setTag(DataUtility.PERIODIC_TAG)
                .build();
        if (checkPlayServices()) {
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }

        // instantiate variables for AwarenessFence api
        Intent intent = new Intent(FENCE_RECIEVER_ACTION);
        myPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        myFenceReceiver = new MyFenceReceiver();
        registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECIEVER_ACTION));
        // Set configuration on the timefence
        buildMyAwarenessFence();
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

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);

            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);

            startActivity(intent);

            return true;
        }

        if (id == R.id.refresh) {
//            Intent intent = new Intent(this, TabsActivity.class);
//            startActivity(intent);
            return true;
        }

        if (id == R.id.add) {
            // Subreddit managing activity
            Intent intent = new Intent(this, SubredditActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        SharedPreferences shared = getDefaultSharedPreferences(this);
        shared.registerOnSharedPreferenceChangeListener(this);

        if (mPrefTimeFenceMinutes < FENCE_NEVER) {
            registerFence(FENCE_KEY);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        unregisterFence(FENCE_KEY);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        mCurrentSubredditChoice = sharedPreferences.getString(mPrefSubrddKey, "");
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

        try {
            unregisterReceiver(myFenceReceiver);    // Exception "not registered" in some cases
        } catch (Exception allEx){
            Log.e(TAG, ""+ allEx);  // Catch the exception for this version - Haven't found a way to peek 'registered' state.
        }

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
                null, null, null,  DataUtility.sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Load data into the Spinner
        mAdapter.swapCursor(data);
        mCursor = data;

        if (mSpinnerIdx == Spinner.INVALID_POSITION) {
            SharedPreferences shared = getDefaultSharedPreferences(this);
            String prefSub = shared.getString(mPrefSubrddKey, "");

            String s;
            int idx = 0;
            while (mCursor.moveToNext()) {
                s = mCursor.getString(1);
                if (prefSub.equalsIgnoreCase(s)) {
                    mSpinnerIdx = idx;
                    break;
                }
                idx++;
            }
        }

        placeSubredditCurrent();
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
                mSpinnerIdx = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinner.setAdapter(mAdapter);
    }

    private void placeSubredditCurrent(){

        if (mSpinnerIdx > Spinner.INVALID_POSITION){
            spinner.setSelection(mSpinnerIdx);
        } else {
            spinner.setSelection(1);    // This should not happen, but set to the first item just in case
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (mSpinnerIdx != Spinner.INVALID_POSITION){
            outState.putInt(SPIN_TO_POSITION, mSpinnerIdx);
            outState.putString(BROWSING_SUBNAME, mCurrentSubredditChoice);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        // Afford 5 categories that are default choices on 'reddit' website
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
            // This matches the available number tabs
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
    public void OnPostItemClick(MainPost post) {

        if (mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.PARCEL_ON_ARG, post);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailcontainer_fragment, df, DETAILFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();

            if (post!=null){
                if (post.createdUtcTime != 0L) {
                    ma_timelineView.setText(DataUtility.getDate(post.createdUtcTime));
                } else {
                    ma_timelineView.setText(Long.toString(post.createdUtcTime));
                }
                ma_authorView.setText(String.format(srBy, post.author));
                ma_numberOfCommentsView.setText(String.format(srNumComments, post.numComments));
            }

        } else {
            Intent intent = new Intent(this, DetailActivity.class);

            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.PARCEL_ON_ARG, post);

            intent.putExtra(DetailFragment.EXTRA_ON_INTENT, args);

            startActivity(intent);
        }
    }

    /*
        Google Play Service - If user opts in a time aware reminder
        this receiver does the job
     */
    public class MyFenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        Snackbar mSnackbar = Snackbar.make(mCoorLayout,
                                "TimeFence past " + mPrefTimeFenceMinutes  + " minutes.", Snackbar.LENGTH_LONG);
                        mSnackbar.setAction("OK",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        unregisterFence(FENCE_KEY);
                                    }
                                });
                        mSnackbar.show();
                        buildMyAwarenessFence();    // repeat
                        break;
                    case FenceState.FALSE:
                        Log.i(TAG, "TimeFence not yet " + mPrefTimeFenceMinutes  + " munite.");
                        break;
                    case FenceState.UNKNOWN:
                        Log.i(TAG, "The TimeFence fence is in an unknown state.");
                        break;
                }
            }
        }
    }

    protected void buildMyAwarenessFence() {
        long nowMillis = System.currentTimeMillis();
        long oneHourMillis = 1L * 60L * 60L * 1000L;
        long oneMinuteMillis = 4L * 60L * 1000L;
        final long multiplyForMillis = 60L * 1000L;

        mPrefTimeFenceMinutes = DataUtility.getTimeFencingTimePreference(this);
        if (mPrefTimeFenceMinutes < FENCE_NEVER) {
            long fenceMillis = mPrefTimeFenceMinutes * multiplyForMillis;
            mTimeFence = TimeFence.inInterval(nowMillis + fenceMillis, Long.MAX_VALUE);
        } else {
            try {
                unregisterReceiver(myFenceReceiver);      // Error "not registered"
            } catch (Exception allEx){
                Log.e(TAG, ""+ allEx);  // Catch the exception until I find a way to peek 'registered' state
            }
            unregisterFence(FENCE_KEY);
        }
    }


    private void registerFence(final String keyname) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(keyname, mTimeFence, myPendingIntent)
                        .build()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()){
                    Log.i(TAG, "Fence successfully registered.");
                } else {
                    Log.i(TAG, "Fence could not be registered. " + status);
                }
            }
        });
    }

    protected void unregisterFence(final String fenceKey) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " could NOT be removed.");
            }
        });
    }
}
